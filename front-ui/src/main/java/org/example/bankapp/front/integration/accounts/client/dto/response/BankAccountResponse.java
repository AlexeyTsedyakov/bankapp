package org.example.bankapp.front.integration.accounts.client.dto.response;

import org.example.bankapp.front.common.model.Currency;

import java.math.BigDecimal;

public record BankAccountResponse(
        Long id,
        Long userId,
        String accountNumber,
        Currency currency,
        BigDecimal balance
) {
}
