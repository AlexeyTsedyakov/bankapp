package org.example.bankapp.front.integration.exchange.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.example.bankapp.front.common.model.Currency;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class ExchangeRate {
    @NotNull
    private Currency currency;
    @NotNull @Positive @Digits(integer = 12, fraction = 2)
    private BigDecimal buyRate;
    @NotNull @Positive @Digits(integer = 12, fraction = 2)
    private BigDecimal sellRate;
}
