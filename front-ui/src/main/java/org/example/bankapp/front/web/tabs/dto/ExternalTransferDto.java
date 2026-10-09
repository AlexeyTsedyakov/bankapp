package org.example.bankapp.front.web.tabs.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ExternalTransferDto(
        @NotNull Long fromAccountId,
        @NotNull String toAccountNumber,
        @NotNull @Positive @Digits(integer = 12, fraction = 2) BigDecimal amount
) {
    public ExternalTransferDto() {
        this(null, null, null);
    }
}
