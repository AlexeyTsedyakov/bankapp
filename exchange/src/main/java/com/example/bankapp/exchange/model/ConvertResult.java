package com.example.bankapp.exchange.model;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ConvertResult(
        Currency fromCurrency,
        Currency toCurrency,
        BigDecimal baseCurrencyRate,
        BigDecimal toCurrencyRate,
        BigDecimal converted
) {
}
