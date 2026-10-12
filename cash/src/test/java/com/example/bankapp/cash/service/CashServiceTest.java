package com.example.bankapp.cash.service;

import com.example.bankapp.cash.exceptions.BlockerException;
import com.example.bankapp.cash.exceptions.IdempotencyKeyException;
import com.example.bankapp.cash.exceptions.InvalidAmountException;
import com.example.bankapp.cash.integration.accounts.AccountsGateway;
import com.example.bankapp.cash.integration.accounts.dto.response.BankAccountResponse;
import com.example.bankapp.cash.integration.blocker.BlockerGateway;
import com.example.bankapp.cash.integration.blocker.dto.BlockerResponse;
import com.example.bankapp.cash.model.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = CashService.class)
@ActiveProfiles("test")
public class CashServiceTest {
    @MockitoBean
    private AccountsGateway accountsGateway;
    @MockitoBean
    private BlockerGateway blockerGateway;
    @Autowired
    private CashService service;

    private BankAccountResponse bankAccount;
    private BlockerResponse trueBlockerResponse = new BlockerResponse(true);
    private BlockerResponse falseBlockerResponse = new BlockerResponse(false);

    @BeforeEach
    void setUp() {
        bankAccount = BankAccountResponse.builder()
                .id(1L)
                .userId(1L)
                .accountNumber("00000001")
                .currency(Currency.RUB)
                .balance(new BigDecimal("100.00"))
                .build();
    }

    @Test
    void deposit_PositiveAmount_callsGateway() {
        long accountId = bankAccount.id();
        BigDecimal amount = new BigDecimal("33.33");
        UUID idempotencyKey = UUID.randomUUID();
        when(blockerGateway.checkAccountOperation(accountId))
                .thenReturn(falseBlockerResponse);
        when(accountsGateway.deposit(accountId, amount, idempotencyKey))
                .thenReturn(bankAccount);

        BankAccountResponse response = service.deposit(accountId, amount, idempotencyKey);

        assertThat(response).isEqualTo(bankAccount);
        verify(accountsGateway, times(1))
                .deposit(accountId, amount, idempotencyKey);
    }

    @Test
    void deposit_NegativeAmount_throwsException() {
        long accountId = bankAccount.id();
        BigDecimal amount = new BigDecimal("-33.33");
        UUID idempotencyKey = UUID.randomUUID();

        assertThatThrownBy(() -> service.deposit(accountId, amount, idempotencyKey))
                .isInstanceOf(InvalidAmountException.class);
        verify(accountsGateway, never())
                .deposit(anyLong(), any(BigDecimal.class), eq(idempotencyKey));
    }

    @Test
    void deposit_NullIdempotencyKey_throwsException() {
        long accountId = bankAccount.id();
        BigDecimal amount = new BigDecimal("33.33");

        assertThatThrownBy(() -> service.deposit(accountId, amount, null))
                .isInstanceOf(IdempotencyKeyException.class);
        verify(accountsGateway, never())
                .deposit(anyLong(), any(BigDecimal.class), any());
    }

    @Test
    void deposit_blockOperation_throwsException() {
        long accountId = bankAccount.id();
        BigDecimal amount = new BigDecimal("10.33");
        UUID idempotencyKey = UUID.randomUUID();

        when(blockerGateway.checkAccountOperation(accountId))
                .thenReturn(trueBlockerResponse);
        assertThatThrownBy(() -> service.deposit(accountId, amount, idempotencyKey))
                .isInstanceOf(BlockerException.class);
        verify(accountsGateway, never())
                .deposit(anyLong(), any(BigDecimal.class), any());
    }

    @Test
    void withdraw_PositiveAmount_callsGateway() {
        long accountId = bankAccount.id();
        BigDecimal amount = new BigDecimal("10.33");
        UUID idempotencyKey = UUID.randomUUID();

        when(blockerGateway.checkAccountOperation(accountId))
                .thenReturn(falseBlockerResponse);
        when(accountsGateway.withdraw(accountId, amount, idempotencyKey))
                .thenReturn(bankAccount);

        BankAccountResponse response = service.withdraw(accountId, amount, idempotencyKey);

        assertThat(response).isEqualTo(bankAccount);
        verify(accountsGateway, times(1))
                .withdraw(accountId, amount, idempotencyKey);
    }

    @Test
    void withdraw_NegativeAmount_throwsException() {
        long accountId = bankAccount.id();
        BigDecimal amount = new BigDecimal("-33.33");
        UUID idempotencyKey = UUID.randomUUID();

        assertThatThrownBy(() -> service.withdraw(accountId, amount, idempotencyKey))
                .isInstanceOf(InvalidAmountException.class);
        verify(accountsGateway, never())
                .withdraw(anyLong(), any(BigDecimal.class), eq(idempotencyKey));
    }

    @Test
    void withdraw_NullIdempotencyKey_throwsException() {
        long accountId = bankAccount.id();
        BigDecimal amount = new BigDecimal("10.33");

        assertThatThrownBy(() -> service.withdraw(accountId, amount, null))
                .isInstanceOf(IdempotencyKeyException.class);
        verify(accountsGateway, never())
                .withdraw(anyLong(), any(BigDecimal.class), any());
    }

    @Test
    void withdraw_blockOperation_throwsException() {
        long accountId = bankAccount.id();
        BigDecimal amount = new BigDecimal("10.33");
        UUID idempotencyKey = UUID.randomUUID();

        when(blockerGateway.checkAccountOperation(accountId))
                .thenReturn(trueBlockerResponse);
        assertThatThrownBy(() -> service.withdraw(accountId, amount, idempotencyKey))
                .isInstanceOf(BlockerException.class);
        verify(accountsGateway, never())
                .withdraw(anyLong(), any(BigDecimal.class), any());
    }
}
