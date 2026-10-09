package com.example.bankapp.transfer.model;

import java.math.BigDecimal;

public record CalculationResult(
        Currency fromCurrency,
        Currency toCurrency,
        BigDecimal baseCurrencyRate,
        BigDecimal toCurrencyRate,
        BigDecimal amount
) {
}
