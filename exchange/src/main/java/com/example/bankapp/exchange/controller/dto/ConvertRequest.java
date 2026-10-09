package com.example.bankapp.exchange.controller.dto;

import com.example.bankapp.exchange.model.Currency;
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
