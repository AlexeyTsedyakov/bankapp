package com.example.bankapp.cash.service.event;

import com.example.bankapp.cash.integration.accounts.dto.response.BankAccountResponse;

import java.math.BigDecimal;

public record WithdrawCompleted(BankAccountResponse bankAccount, BigDecimal amount) {
}
