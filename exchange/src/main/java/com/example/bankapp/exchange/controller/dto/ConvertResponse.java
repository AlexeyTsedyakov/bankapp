package com.example.bankapp.exchange.controller.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ConvertResponse(
        BigDecimal baseCurrencyRate,
        BigDecimal toCurrencyRate,
        BigDecimal converted
) {
}
