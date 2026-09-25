package com.example.bankapp.exchange.repository;

import com.example.bankapp.exchange.model.Currency;
import com.example.bankapp.exchange.model.ExchangeRate;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class RateRepository {
    private final Map<Currency, ExchangeRate> rates = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        var rate = new ExchangeRate(Currency.RUB, new BigDecimal("1.00"), new BigDecimal("1.00"));
        save(rate);
    }

    public List<ExchangeRate> findAll() {
        return List.copyOf(rates.values());
    }

    public Optional<ExchangeRate> findByCurrency(Currency currency) {
        return Optional.ofNullable(rates.get(currency));
    }

    public void save(ExchangeRate rate) {
        rate.setBuyRate(setScale(rate.getBuyRate()));
        rate.setSellRate(setScale(rate.getSellRate()));
        rates.put(rate.getCurrency(), rate);
    }

    private BigDecimal setScale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
