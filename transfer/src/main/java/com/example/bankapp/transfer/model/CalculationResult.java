package com.example.bankapp.transfer.model;

import java.math.BigDecimal;

public record CalculationResult(
        BigDecimal baseCurrencyRate,
        BigDecimal toCurrencyRate,
        BigDecimal result
) {
}
