package com.example.bankapp.transfer.service;

import com.example.bankapp.transfer.config.JdbcDialectTestConfiguration;
import com.example.bankapp.transfer.config.OAuth2TestConfiguration;
import com.example.bankapp.transfer.exceptions.ServiceUnavailableException;
import com.example.bankapp.transfer.integration.accounts.AccountsGateway;
import com.example.bankapp.transfer.integration.accounts.dto.response.BankAccountResponse;
import com.example.bankapp.transfer.integration.blocker.BlockerGateway;
import com.example.bankapp.transfer.integration.blocker.dto.BlockerResponse;
import com.example.bankapp.transfer.integration.exchange.ExchangeGateway;
import com.example.bankapp.transfer.integration.exchange.dto.ConvertResponse;
import com.example.bankapp.transfer.model.CalculationResult;
import com.example.bankapp.transfer.model.Currency;
import com.example.bankapp.transfer.model.Transfer;
import com.example.bankapp.transfer.model.TransferStatus;
import com.example.bankapp.transfer.model.TransferStep;
import com.example.bankapp.transfer.repository.TransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest
@Import({JdbcDialectTestConfiguration.class, OAuth2TestConfiguration.class})
@Transactional
@ActiveProfiles("test")
public class TransferServiceTest {
    @Autowired
    private TransferService transferService;
    @Autowired
    private TransferRepository transferRepository;
    @MockitoBean
    private AccountsGateway accountsGateway;
    @MockitoBean
    private ExchangeGateway exchangeGateway;
    @MockitoBean
    private BlockerGateway blockerGateway;

    @BeforeEach
    void setUp() {
        transferRepository.deleteAll();
    }

    @Test
    void save_validTransfer_persistsTransferInDatabase() {
        UUID idempotencyKey = UUID.randomUUID();
        Transfer transfer = Transfer.builder()
                .idempotencyKey(idempotencyKey)
                .fromAccountId(1L)
                .toAccountId(2L)
                .sourceAmount(new BigDecimal("100.00"))
                .status(TransferStatus.NEW)
                .step(TransferStep.CHECK_BLOCKER)
                .build();

        Transfer saved = transferService.save(transfer);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();

        Optional<Transfer> found = transferRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get()).usingRecursiveComparison()
                .ignoringFields("id", "sourceAmount", "version", "createdAt", "updatedAt")
                .isEqualTo(transfer);
        assertThat(saved.getSourceAmount()).isEqualByComparingTo(new BigDecimal("100.00"));

    }

    @Test
    void saveCompletedAndNotify_persistsTransferInDatabase() {
        UUID idempotencyKey = UUID.randomUUID();
        Transfer transfer = Transfer.builder()
                .idempotencyKey(idempotencyKey)
                .fromAccountId(10L)
                .toAccountId(20L)
                .sourceAmount(new BigDecimal("500.00"))
                .targetAmount(new BigDecimal("500.00"))
                .status(TransferStatus.COMPLETED)
                .step(TransferStep.DEPOSIT_RECEIVER)
                .build();

        transferService.saveCompletedAndNotify(transfer);

        Transfer saved = transferService.save(transfer);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();

        Optional<Transfer> found = transferRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get()).usingRecursiveComparison()
                .ignoringFields("id", "sourceAmount", "targetAmount", "version", "createdAt", "updatedAt")
                .isEqualTo(transfer);
        assertThat(found.get().getSourceAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(found.get().getTargetAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
    }

    @Test
    void getRetryRequired_returnsOnlyTransfersWithRetryRequiredStatus() {
        Transfer retryTransfer1 = Transfer.builder()
                .idempotencyKey(UUID.randomUUID())
                .fromAccountId(1L)
                .toAccountId(2L)
                .sourceAmount(new BigDecimal("10.00"))
                .status(TransferStatus.RETRY_REQUIRED)
                .step(TransferStep.WITHDRAW_SENDER)
                .build();
        Transfer retryTransfer2 = Transfer.builder()
                .idempotencyKey(UUID.randomUUID())
                .fromAccountId(3L)
                .toAccountId(4L)
                .sourceAmount(new BigDecimal("20.00"))
                .status(TransferStatus.RETRY_REQUIRED)
                .step(TransferStep.DEPOSIT_RECEIVER)
                .build();
        Transfer otherTransfer = Transfer.builder()
                .idempotencyKey(UUID.randomUUID())
                .fromAccountId(5L)
                .toAccountId(6L)
                .sourceAmount(new BigDecimal("30.00"))
                .status(TransferStatus.PROCESSING)
                .step(TransferStep.CHECK_BLOCKER)
                .build();

        transferRepository.save(retryTransfer1);
        transferRepository.save(retryTransfer2);
        transferRepository.save(otherTransfer);

        List<Transfer> result = transferService.getRetryRequired();

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Transfer::getStatus)
                .containsOnly(TransferStatus.RETRY_REQUIRED);
    }

    @Test
    void calculateTargetAmount_sameCurrency_returnsSourceAmountWithoutExchange() {
        BankAccountResponse fromAccount = BankAccountResponse.builder()
                .id(1L)
                .currency(Currency.RUB)
                .build();
        BankAccountResponse toAccount = BankAccountResponse.builder()
                .id(2L)
                .currency(Currency.RUB)
                .build();

        when(accountsGateway.getBankAccount(1L)).thenReturn(fromAccount);
        when(accountsGateway.getBankAccount(2L)).thenReturn(toAccount);

        Transfer transfer = Transfer.builder()
                .fromAccountId(1L)
                .toAccountId(2L)
                .sourceAmount(new BigDecimal("150.00"))
                .build();

        CalculationResult result = transferService.calculateTargetAmount(transfer);

        assertThat(result).isNotNull();
        assertThat(result.fromCurrency()).isEqualTo(Currency.RUB);
        assertThat(result.toCurrency()).isEqualTo(Currency.RUB);
        assertThat(result.baseCurrencyRate()).isNull();
        assertThat(result.toCurrencyRate()).isNull();
        assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("150.00"));

        verify(accountsGateway).getBankAccount(1L);
        verify(accountsGateway).getBankAccount(2L);
        verifyNoInteractions(exchangeGateway);
    }

    @Test
    void calculateTargetAmount_differentCurrencies_callsExchangeGatewayAndReturnsCalculatedResult() {
        BankAccountResponse fromAccount = BankAccountResponse.builder()
                .id(1L)
                .currency(Currency.RUB)
                .build();
        BankAccountResponse toAccount = BankAccountResponse.builder()
                .id(2L)
                .currency(Currency.USD)
                .build();

        when(accountsGateway.getBankAccount(1L)).thenReturn(fromAccount);
        when(accountsGateway.getBankAccount(2L)).thenReturn(toAccount);

        ConvertResponse convertResponse = ConvertResponse.builder()
                .baseCurrencyRate(new BigDecimal("1.00"))
                .toCurrencyRate(new BigDecimal("0.01"))
                .converted(new BigDecimal("1.50"))
                .build();
        when(exchangeGateway.convert(Currency.RUB, Currency.USD, new BigDecimal("150.00")))
                .thenReturn(convertResponse);

        Transfer transfer = Transfer.builder()
                .fromAccountId(1L)
                .toAccountId(2L)
                .sourceAmount(new BigDecimal("150.00"))
                .build();

        CalculationResult result = transferService.calculateTargetAmount(transfer);

        assertThat(result).isNotNull();
        assertThat(result.fromCurrency()).isEqualTo(Currency.RUB);
        assertThat(result.toCurrency()).isEqualTo(Currency.USD);
        assertThat(result.baseCurrencyRate()).isEqualByComparingTo(new BigDecimal("1.00"));
        assertThat(result.toCurrencyRate()).isEqualByComparingTo(new BigDecimal("0.01"));
        assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("1.50"));

        verify(accountsGateway).getBankAccount(1L);
        verify(accountsGateway).getBankAccount(2L);
        verify(exchangeGateway).convert(Currency.RUB, Currency.USD, new BigDecimal("150.00"));
    }

    @Test
    void calculateTargetAmount_missingRequiredFields_throwsIllegalStateException() {
        Transfer transfer = Transfer.builder()
                .fromAccountId(1L)
                .sourceAmount(new BigDecimal("150.00"))
                .build();

        assertThatThrownBy(() -> transferService.calculateTargetAmount(transfer))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("toAccountId");

        verifyNoInteractions(accountsGateway, exchangeGateway);
    }

    @Test
    void withdrawSender_validTransfer_callsAccountsGatewayWithdraw() {
        UUID idempotencyKey = UUID.randomUUID();
        Transfer transfer = Transfer.builder()
                .fromAccountId(1L)
                .sourceAmount(new BigDecimal("100.00"))
                .idempotencyKey(idempotencyKey)
                .build();

        transferService.withdrawSender(transfer);

        verify(accountsGateway).withdraw(1L, new BigDecimal("100.00"), idempotencyKey);
    }

    @Test
    void withdrawSender_missingRequiredFields_throwsIllegalStateException() {
        Transfer transfer = Transfer.builder()
                .fromAccountId(1L)
                .sourceAmount(new BigDecimal("100.00"))
                .build();

        assertThatThrownBy(() -> transferService.withdrawSender(transfer))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("idempotencyKey");

        verifyNoInteractions(accountsGateway);
    }

    @Test
    void depositReceiver_validTransfer_callsAccountsGatewayDeposit() {
        UUID idempotencyKey = UUID.randomUUID();
        Transfer transfer = Transfer.builder()
                .toAccountId(2L)
                .targetAmount(new BigDecimal("200.00"))
                .idempotencyKey(idempotencyKey)
                .build();

        transferService.depositReceiver(transfer);

        verify(accountsGateway).deposit(2L, new BigDecimal("200.00"), idempotencyKey);
    }

    @Test
    void depositReceiver_missingRequiredFields_throwsIllegalStateException() {
        Transfer transfer = Transfer.builder()
                .toAccountId(2L)
                .idempotencyKey(UUID.randomUUID())
                .build();

        assertThatThrownBy(() -> transferService.depositReceiver(transfer))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("targetAmount");

        verifyNoInteractions(accountsGateway);
    }

    @Test
    void refundWithdraw_validTransfer_callsAccountsGatewayDepositForSender() {
        UUID idempotencyKey = UUID.randomUUID();
        Transfer transfer = Transfer.builder()
                .fromAccountId(1L)
                .sourceAmount(new BigDecimal("100.00"))
                .idempotencyKey(idempotencyKey)
                .build();

        transferService.refundWithdraw(transfer);

        verify(accountsGateway).deposit(1L, new BigDecimal("100.00"), idempotencyKey);
    }

    @Test
    void refundWithdraw_missingRequiredFields_throwsIllegalStateException() {
        Transfer transfer = Transfer.builder()
                .fromAccountId(1L)
                .idempotencyKey(UUID.randomUUID())
                .build();

        assertThatThrownBy(() -> transferService.refundWithdraw(transfer))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sourceAmount");

        verifyNoInteractions(accountsGateway);
    }

    @Test
    void shouldBlock_whenBlockerReturnsTrue_returnsTrue() {
        when(blockerGateway.checkAccountOperation(1L)).thenReturn(new BlockerResponse(true));
        Transfer transfer = Transfer.builder()
                .fromAccountId(1L)
                .build();

        boolean shouldBlock = transferService.shouldBlock(transfer);

        assertThat(shouldBlock).isTrue();
        verify(blockerGateway).checkAccountOperation(1L);
    }

    @Test
    void shouldBlock_whenBlockerReturnsFalse_returnsFalse() {
        when(blockerGateway.checkAccountOperation(1L)).thenReturn(new BlockerResponse(false));
        Transfer transfer = Transfer.builder()
                .fromAccountId(1L)
                .build();

        boolean shouldBlock = transferService.shouldBlock(transfer);

        assertThat(shouldBlock).isFalse();
        verify(blockerGateway).checkAccountOperation(1L);
    }

    @Test
    void withdrawSender_whenServiceUnavailableException_retriesOperation() {
        UUID idempotencyKey = UUID.randomUUID();
        Transfer transfer = Transfer.builder()
                .fromAccountId(1L)
                .sourceAmount(new BigDecimal("100.00"))
                .idempotencyKey(idempotencyKey)
                .build();

        doThrow(new ServiceUnavailableException("Service temporarily down"))
                .doNothing()
                .when(accountsGateway).withdraw(1L, new BigDecimal("100.00"), idempotencyKey);

        transferService.withdrawSender(transfer);

        verify(accountsGateway, times(2)).withdraw(1L, new BigDecimal("100.00"), idempotencyKey);
    }
}
