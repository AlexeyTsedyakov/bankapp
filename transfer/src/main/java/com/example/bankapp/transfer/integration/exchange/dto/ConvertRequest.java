package com.example.bankapp.transfer.integration.exchange.dto;

import com.example.bankapp.transfer.model.Currency;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ConvertRequest(
        @NotNull Currency from,
        @NotNull Currency to,
        @NotNull @Positive @Digits(integer = 12, fraction = 2) BigDecimal amount
) {
}
