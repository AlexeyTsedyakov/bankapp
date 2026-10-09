package com.example.bankapp.transfer.service;

import com.example.bankapp.transfer.config.JdbcDialectTestConfiguration;
import com.example.bankapp.transfer.config.OAuth2TestConfiguration;
import com.example.bankapp.transfer.integration.accounts.AccountsGateway;
import com.example.bankapp.transfer.integration.accounts.dto.response.BankAccountResponse;
import com.example.bankapp.transfer.integration.exchange.ExchangeGateway;
import com.example.bankapp.transfer.integration.exchange.dto.ConvertResponse;
import com.example.bankapp.transfer.model.*;
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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

    private Transfer defaultTransfer;

    @BeforeEach
    void setUp() {
        defaultTransfer = createTestTransfer(
                1L,
                2L,
                Currency.RUB,
                Currency.USD,
                new BigDecimal("1000.00"),
                new BigDecimal("10.00"),
                TransferStep.CHECK_BLOCKER,
                TransferStatus.NEW,
                0,
                UUID.randomUUID()
        );
    }

    private Transfer createTestTransfer(
            Long fromAccountId,
            Long toAccountId,
            Currency fromCurrency,
            Currency toCurrency,
            BigDecimal sourceAmount,
            BigDecimal targetAmount,
            TransferStep step,
            TransferStatus status,
            int failCount,
            UUID idempotencyKey
    ) {
        Transfer transfer = new Transfer();
        transfer.setFromAccountId(fromAccountId);
        transfer.setToAccountId(toAccountId);
        transfer.setFromCurrency(fromCurrency);
        transfer.setToCurrency(toCurrency);
        transfer.setSourceAmount(sourceAmount);
        transfer.setTargetAmount(targetAmount);
        transfer.setStep(step);
        transfer.setStatus(status);
        transfer.setFailCount(failCount);
        transfer.setIdempotencyKey(idempotencyKey);
        return transfer;
    }

    @Test
    void save_validTransfer_savesAndReturnsTransfer() {
        Transfer saved = transferService.save(defaultTransfer);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getFromAccountId()).isEqualTo(defaultTransfer.getFromAccountId());
        assertThat(saved.getToAccountId()).isEqualTo(defaultTransfer.getToAccountId());
        assertThat(saved.getSourceAmount()).isEqualByComparingTo(defaultTransfer.getSourceAmount());
        assertThat(saved.getTargetAmount()).isEqualByComparingTo(defaultTransfer.getTargetAmount());
        assertThat(saved.getStatus()).isEqualTo(TransferStatus.NEW);
        assertThat(saved.getStep()).isEqualTo(TransferStep.CHECK_BLOCKER);

        Transfer fromDb = transferRepository.findById(saved.getId()).orElse(null);
        assertThat(fromDb).isNotNull();
        assertThat(fromDb.getCreatedAt()).isCloseTo(saved.getCreatedAt(), within(1, ChronoUnit.MICROS));
        assertThat(fromDb.getUpdatedAt()).isCloseTo(saved.getUpdatedAt(), within(1, ChronoUnit.MICROS));
        assertThat(fromDb)
                .usingRecursiveComparison()
                .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .ignoringFields("createdAt", "updatedAt")
                .isEqualTo(saved);
    }

    @Test
    void save_existingTransfer_updatesTransfer() {
        Transfer saved = transferService.save(defaultTransfer);
        saved.setStatus(TransferStatus.PROCESSING);
        saved.setStep(TransferStep.WITHDRAW_SENDER);
        saved.setFailCount(1);

        Transfer updated = transferService.save(saved);

        assertThat(updated.getId()).isEqualTo(saved.getId());
        assertThat(updated.getStatus()).isEqualTo(TransferStatus.PROCESSING);
        assertThat(updated.getStep()).isEqualTo(TransferStep.WITHDRAW_SENDER);
        assertThat(updated.getFailCount()).isEqualTo(1);

        Transfer fromDb = transferRepository.findById(saved.getId()).orElse(null);
        assertThat(fromDb).isNotNull();
        assertThat(fromDb.getStatus()).isEqualTo(TransferStatus.PROCESSING);
        assertThat(fromDb.getStep()).isEqualTo(TransferStep.WITHDRAW_SENDER);
        assertThat(fromDb.getFailCount()).isEqualTo(1);
    }

    @Test
    void saveCompletedAndNotify_validTransfer_savesTransfer() {
        Transfer saved = transferService.save(defaultTransfer);
        saved.setStatus(TransferStatus.COMPLETED);

        transferService.saveCompletedAndNotify(saved);

        Transfer fromDb = transferRepository.findById(saved.getId()).orElse(null);
        assertThat(fromDb).isNotNull();
        assertThat(fromDb.getStatus()).isEqualTo(TransferStatus.COMPLETED);
    }

    @Test
    void getRetryRequired_hasMixedTransfers_returnsOnlyRetryRequiredTransfers() {
        Transfer retry1 = createTestTransfer(1L, 2L, Currency.RUB, Currency.RUB, new BigDecimal("100.00"), new BigDecimal("100.00"), TransferStep.WITHDRAW_SENDER, TransferStatus.RETRY_REQUIRED, 1, UUID.randomUUID());
        Transfer retry2 = createTestTransfer(3L, 4L, Currency.USD, Currency.USD, new BigDecimal("200.00"), new BigDecimal("200.00"), TransferStep.DEPOSIT_RECEIVER, TransferStatus.RETRY_REQUIRED, 2, UUID.randomUUID());
        Transfer completed = createTestTransfer(5L, 6L, Currency.RUB, Currency.RUB, new BigDecimal("300.00"), new BigDecimal("300.00"), TransferStep.DEPOSIT_RECEIVER, TransferStatus.COMPLETED, 0, UUID.randomUUID());
        Transfer failed = createTestTransfer(7L, 8L, Currency.RUB, Currency.RUB, new BigDecimal("400.00"), new BigDecimal("400.00"), TransferStep.REFUND_WITHDRAW, TransferStatus.FAILED, 5, UUID.randomUUID());
        Transfer newTransfer = createTestTransfer(9L, 10L, Currency.RUB, Currency.RUB, new BigDecimal("500.00"), new BigDecimal("500.00"), TransferStep.CHECK_BLOCKER, TransferStatus.NEW, 0, UUID.randomUUID());

        transferRepository.saveAll(List.of(retry1, retry2, completed, failed, newTransfer));

        List<Transfer> result = transferService.getRetryRequired();

        assertThat(result)
                .isNotNull()
                .hasSize(2)
                .allMatch(t -> t.getStatus() == TransferStatus.RETRY_REQUIRED)
                .extracting(Transfer::getId)
                .containsExactlyInAnyOrder(retry1.getId(), retry2.getId());
    }

    @Test
    void getRetryRequired_noRetryRequiredTransfers_returnsEmptyList() {
        Transfer completed = createTestTransfer(1L, 2L, Currency.RUB, Currency.RUB, new BigDecimal("100.00"), new BigDecimal("100.00"), TransferStep.DEPOSIT_RECEIVER, TransferStatus.COMPLETED, 0, UUID.randomUUID());
        Transfer newTransfer = createTestTransfer(3L, 4L, Currency.USD, Currency.USD, new BigDecimal("200.00"), new BigDecimal("200.00"), TransferStep.CHECK_BLOCKER, TransferStatus.NEW, 0, UUID.randomUUID());

        transferRepository.saveAll(List.of(completed, newTransfer));

        List<Transfer> result = transferService.getRetryRequired();

        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    void calculateTargetAmount_sameCurrency_returnsSourceAmountWithoutExchange() {
        Transfer transfer = new Transfer();
        transfer.setFromAccountId(1L);
        transfer.setToAccountId(2L);
        transfer.setSourceAmount(new BigDecimal("150.00"));

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

        CalculationResult result = transferService.calculateTargetAmount(transfer);

        assertThat(result).isNotNull();
        assertThat(result.fromCurrency()).isEqualTo(Currency.RUB);
        assertThat(result.toCurrency()).isEqualTo(Currency.RUB);
        assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("150.00"));

        verify(accountsGateway, times(1)).getBankAccount(1L);
        verify(accountsGateway, times(1)).getBankAccount(2L);
        verify(exchangeGateway, never()).convert(any(), any(), any());
    }

    @Test
    void calculateTargetAmount_differentCurrencies_callsExchangeGatewayAndReturnsConverted() {
        Transfer transfer = new Transfer();
        transfer.setFromAccountId(1L);
        transfer.setToAccountId(2L);
        transfer.setSourceAmount(new BigDecimal("100.00"));

        BankAccountResponse fromAccount = BankAccountResponse.builder()
                .id(1L)
                .currency(Currency.USD)
                .build();
        BankAccountResponse toAccount = BankAccountResponse.builder()
                .id(2L)
                .currency(Currency.RUB)
                .build();
        ConvertResponse convertResponse = ConvertResponse.builder()
                .baseCurrencyRate(new BigDecimal("95.00"))
                .converted(new BigDecimal("9500.00"))
                .build();

        when(accountsGateway.getBankAccount(1L)).thenReturn(fromAccount);
        when(accountsGateway.getBankAccount(2L)).thenReturn(toAccount);
        when(exchangeGateway.convert(Currency.USD, Currency.RUB, new BigDecimal("100.00")))
                .thenReturn(convertResponse);

        CalculationResult result = transferService.calculateTargetAmount(transfer);

        assertThat(result).isNotNull();
        assertThat(result.fromCurrency()).isEqualTo(Currency.USD);
        assertThat(result.toCurrency()).isEqualTo(Currency.RUB);
        assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("9500.00"));

        verify(accountsGateway, times(1)).getBankAccount(1L);
        verify(accountsGateway, times(1)).getBankAccount(2L);
        verify(exchangeGateway, times(1)).convert(Currency.USD, Currency.RUB, new BigDecimal("100.00"));
    }

    @Test
    void calculateTargetAmount_nullFromAccountId_throwsException() {
        Transfer transfer = new Transfer();
        transfer.setFromAccountId(null);
        transfer.setToAccountId(2L);
        transfer.setSourceAmount(new BigDecimal("100.00"));

        assertThatThrownBy(() -> transferService.calculateTargetAmount(transfer))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fromAccountId");

        verify(accountsGateway, never()).getBankAccount(any());
        verify(exchangeGateway, never()).convert(any(), any(), any());
    }

    @Test
    void calculateTargetAmount_nullToAccountId_throwsException() {
        Transfer transfer = new Transfer();
        transfer.setFromAccountId(1L);
        transfer.setToAccountId(null);
        transfer.setSourceAmount(new BigDecimal("100.00"));

        assertThatThrownBy(() -> transferService.calculateTargetAmount(transfer))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("toAccountId");

        verify(accountsGateway, never()).getBankAccount(any());
        verify(exchangeGateway, never()).convert(any(), any(), any());
    }

    @Test
    void calculateTargetAmount_nullSourceAmount_throwsException() {
        Transfer transfer = new Transfer();
        transfer.setFromAccountId(1L);
        transfer.setToAccountId(2L);
        transfer.setSourceAmount(null);

        assertThatThrownBy(() -> transferService.calculateTargetAmount(transfer))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sourceAmount");

        verify(accountsGateway, never()).getBankAccount(any());
        verify(exchangeGateway, never()).convert(any(), any(), any());
    }

    @Test
    void withdrawSender_validTransfer_callsAccountsGatewayWithdraw() {
        Transfer transfer = new Transfer();
        transfer.setFromAccountId(1L);
        transfer.setSourceAmount(new BigDecimal("250.00"));
        UUID idempotencyKey = UUID.randomUUID();
        transfer.setIdempotencyKey(idempotencyKey);

        transferService.withdrawSender(transfer);

        verify(accountsGateway, times(1)).withdraw(1L, new BigDecimal("250.00"), idempotencyKey);
    }

    @Test
    void withdrawSender_nullFromAccountId_throwsException() {
        Transfer transfer = new Transfer();
        transfer.setFromAccountId(null);
        transfer.setSourceAmount(new BigDecimal("250.00"));
        transfer.setIdempotencyKey(UUID.randomUUID());

        assertThatThrownBy(() -> transferService.withdrawSender(transfer))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fromAccountId");

        verify(accountsGateway, never()).withdraw(any(), any(), any());
    }

    @Test
    void withdrawSender_nullSourceAmount_throwsException() {
        Transfer transfer = new Transfer();
        transfer.setFromAccountId(1L);
        transfer.setSourceAmount(null);
        transfer.setIdempotencyKey(UUID.randomUUID());

        assertThatThrownBy(() -> transferService.withdrawSender(transfer))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sourceAmount");

        verify(accountsGateway, never()).withdraw(any(), any(), any());
    }

    @Test
    void withdrawSender_nullIdempotencyKey_throwsException() {
        Transfer transfer = new Transfer();
        transfer.setFromAccountId(1L);
        transfer.setSourceAmount(new BigDecimal("250.00"));
        transfer.setIdempotencyKey(null);

        assertThatThrownBy(() -> transferService.withdrawSender(transfer))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("idempotencyKey");

        verify(accountsGateway, never()).withdraw(any(), any(), any());
    }

    @Test
    void depositReceiver_validTransfer_callsAccountsGatewayDeposit() {
        Transfer transfer = new Transfer();
        transfer.setToAccountId(2L);
        transfer.setTargetAmount(new BigDecimal("500.00"));
        UUID idempotencyKey = UUID.randomUUID();
        transfer.setIdempotencyKey(idempotencyKey);

        transferService.depositReceiver(transfer);

        verify(accountsGateway, times(1)).deposit(2L, new BigDecimal("500.00"), idempotencyKey);
    }

    @Test
    void depositReceiver_nullToAccountId_throwsException() {
        Transfer transfer = new Transfer();
        transfer.setToAccountId(null);
        transfer.setTargetAmount(new BigDecimal("500.00"));
        transfer.setIdempotencyKey(UUID.randomUUID());

        assertThatThrownBy(() -> transferService.depositReceiver(transfer))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("toAccountId");

        verify(accountsGateway, never()).deposit(any(), any(), any());
    }

    @Test
    void depositReceiver_nullTargetAmount_throwsException() {
        Transfer transfer = new Transfer();
        transfer.setToAccountId(2L);
        transfer.setTargetAmount(null);
        transfer.setIdempotencyKey(UUID.randomUUID());

        assertThatThrownBy(() -> transferService.depositReceiver(transfer))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("targetAmount");

        verify(accountsGateway, never()).deposit(any(), any(), any());
    }

    @Test
    void depositReceiver_nullIdempotencyKey_throwsException() {
        Transfer transfer = new Transfer();
        transfer.setToAccountId(2L);
        transfer.setTargetAmount(new BigDecimal("500.00"));
        transfer.setIdempotencyKey(null);

        assertThatThrownBy(() -> transferService.depositReceiver(transfer))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("idempotencyKey");

        verify(accountsGateway, never()).deposit(any(), any(), any());
    }

    @Test
    void refundWithdraw_validTransfer_callsAccountsGatewayDeposit() {
        Transfer transfer = new Transfer();
        transfer.setFromAccountId(1L);
        transfer.setSourceAmount(new BigDecimal("250.00"));
        UUID idempotencyKey = UUID.randomUUID();
        transfer.setIdempotencyKey(idempotencyKey);

        transferService.refundWithdraw(transfer);

        verify(accountsGateway, times(1)).deposit(1L, new BigDecimal("250.00"), idempotencyKey);
    }

    @Test
    void refundWithdraw_nullFromAccountId_throwsException() {
        Transfer transfer = new Transfer();
        transfer.setFromAccountId(null);
        transfer.setSourceAmount(new BigDecimal("250.00"));
        transfer.setIdempotencyKey(UUID.randomUUID());

        assertThatThrownBy(() -> transferService.refundWithdraw(transfer))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fromAccountId");

        verify(accountsGateway, never()).deposit(any(), any(), any());
    }

    @Test
    void refundWithdraw_nullSourceAmount_throwsException() {
        Transfer transfer = new Transfer();
        transfer.setFromAccountId(1L);
        transfer.setSourceAmount(null);
        transfer.setIdempotencyKey(UUID.randomUUID());

        assertThatThrownBy(() -> transferService.refundWithdraw(transfer))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sourceAmount");

        verify(accountsGateway, never()).deposit(any(), any(), any());
    }

    @Test
    void refundWithdraw_nullIdempotencyKey_throwsException() {
        Transfer transfer = new Transfer();
        transfer.setFromAccountId(1L);
        transfer.setSourceAmount(new BigDecimal("250.00"));
        transfer.setIdempotencyKey(null);

        assertThatThrownBy(() -> transferService.refundWithdraw(transfer))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("idempotencyKey");

        verify(accountsGateway, never()).deposit(any(), any(), any());
    }
}
