package org.example.bankapp.front.integration.transfer.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record TransferRequest(
    @NotNull Long fromAccountId,
    @NotNull Long toAccountId,
    @NotNull @Positive @Digits(integer = 12, fraction = 2) BigDecimal amount
) {
}
