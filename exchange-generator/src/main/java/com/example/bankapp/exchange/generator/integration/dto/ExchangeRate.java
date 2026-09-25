package com.example.bankapp.exchange.generator.integration.dto;

import com.example.bankapp.exchange.generator.model.Currency;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class ExchangeRate {
    private Currency currency;
    private BigDecimal buyRate;
    private BigDecimal sellRate;
}
