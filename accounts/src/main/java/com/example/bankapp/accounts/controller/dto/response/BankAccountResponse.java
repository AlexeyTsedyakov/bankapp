package com.example.bankapp.accounts.controller.dto.response;

import com.example.bankapp.accounts.model.Currency;

import java.math.BigDecimal;

public record BankAccountResponse(
        Long id,
        Long userId,
        String accountNumber,
        Currency currency,
        BigDecimal balance
) {
}
