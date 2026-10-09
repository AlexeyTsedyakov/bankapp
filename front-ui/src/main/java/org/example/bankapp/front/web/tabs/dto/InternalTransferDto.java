package org.example.bankapp.front.web.tabs.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record InternalTransferDto(
        @NotNull Long fromAccountId,
        @NotNull Long toAccountId,
        @NotNull @Positive @Digits(integer = 12, fraction = 2) BigDecimal amount
) {
    public InternalTransferDto() {
        this(null, null, null);
    }
}
