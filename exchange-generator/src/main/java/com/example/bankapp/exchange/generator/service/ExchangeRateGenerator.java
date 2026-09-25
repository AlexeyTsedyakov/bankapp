package com.example.bankapp.exchange.generator.service;

import com.example.bankapp.exchange.generator.integration.dto.ExchangeRate;
import com.example.bankapp.exchange.generator.model.Currency;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.random.RandomGenerator;

@Service
public class ExchangeRateGenerator {
    private final List<ExchangeRate> baseRates = List.of(
            new ExchangeRate(Currency.USD, new BigDecimal("80.00"), new BigDecimal("84.00")),
            new ExchangeRate(Currency.EUR, new BigDecimal("90.00"), new BigDecimal("94.00")),
            new ExchangeRate(Currency.CNY, new BigDecimal("12.00"), new BigDecimal("13.00"))
    );

    public List<ExchangeRate> generateRates() {
        return baseRates.stream()
                .map(this::generateRate)
                .toList();
    }

    private ExchangeRate generateRate(ExchangeRate baseRate) {
        double buyRateDelta = RandomGenerator.getDefault().nextDouble(-1.50, 1.50);
        BigDecimal buyRate = baseRate.getBuyRate()
                .add(BigDecimal.valueOf(buyRateDelta))
                .setScale(2, RoundingMode.HALF_UP);

        double sellRateDelta = RandomGenerator.getDefault().nextDouble(-1.50, 1.50);
        BigDecimal sellRate = baseRate.getSellRate()
                .add(BigDecimal.valueOf(sellRateDelta))
                .setScale(2, RoundingMode.HALF_UP);

        return new ExchangeRate(baseRate.getCurrency(), buyRate, sellRate);
    }
}
