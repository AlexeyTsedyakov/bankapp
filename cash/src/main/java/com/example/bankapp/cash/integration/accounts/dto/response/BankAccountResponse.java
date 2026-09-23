package com.example.bankapp.cash.integration.accounts.dto.response;

import com.example.bankapp.cash.model.Currency;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record BankAccountResponse(
        Long id,
        Long userId,
        String accountNumber,
        Currency currency,
        BigDecimal balance
) {
    public BankAccountResponse() {
        this(null, null, null, null, null);
    }
}
