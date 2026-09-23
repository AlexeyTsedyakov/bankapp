package com.example.bankapp.cash.service;

import com.example.bankapp.cash.exceptions.InvalidAmountException;
import com.example.bankapp.cash.integration.accounts.AccountsGateway;
import com.example.bankapp.cash.integration.accounts.dto.response.BankAccountResponse;
import com.example.bankapp.cash.model.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = CashService.class)
@ActiveProfiles("test")
public class CashServiceTest {
    @MockitoBean
    private AccountsGateway gateway;
    @Autowired
    private CashService service;

    private BankAccountResponse bankAccount;

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
        when(gateway.deposit(accountId, amount))
                .thenReturn(bankAccount);

        BankAccountResponse response = service.deposit(accountId, amount);

        assertThat(response).isEqualTo(bankAccount);
        verify(gateway, times(1))
                .deposit(accountId, amount);
    }

    @Test
    void deposit_NegativeAmount_throwsException() {
        long accountId = bankAccount.id();
        BigDecimal amount = new BigDecimal("-33.33");

        assertThatThrownBy(() -> service.deposit(accountId, amount))
                .isInstanceOf(InvalidAmountException.class);
        verify(gateway, never())
                .deposit(anyLong(), any(BigDecimal.class));
    }

    @Test
    void withdraw_PositiveAmount_callsGateway() {
        long accountId = bankAccount.id();
        BigDecimal amount = new BigDecimal("10.33");
        when(gateway.withdraw(accountId, amount))
                .thenReturn(bankAccount);

        BankAccountResponse response = service.withdraw(accountId, amount);

        assertThat(response).isEqualTo(bankAccount);
        verify(gateway, times(1))
                .withdraw(accountId, amount);
    }

    @Test
    void withdraw_NegativeAmount_throwsException() {
        long accountId = bankAccount.id();
        BigDecimal amount = new BigDecimal("-33.33");

        assertThatThrownBy(() -> service.withdraw(accountId, amount))
                .isInstanceOf(InvalidAmountException.class);
        verify(gateway, never())
                .withdraw(anyLong(), any(BigDecimal.class));
    }
}
