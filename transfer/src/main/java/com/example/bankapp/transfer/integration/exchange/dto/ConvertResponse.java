package com.example.bankapp.transfer.integration.exchange.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ConvertResponse(
        BigDecimal baseCurrencyRate,
        BigDecimal toCurrencyRate,
        BigDecimal converted
) {
}
