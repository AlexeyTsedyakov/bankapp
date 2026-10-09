package com.example.bankapp.exchange.service;

import com.example.bankapp.exchange.exceptions.CurrencyConversionException;
import com.example.bankapp.exchange.exceptions.InvalidRateException;
import com.example.bankapp.exchange.exceptions.RateNotFoundException;
import com.example.bankapp.exchange.model.ConvertResult;
import com.example.bankapp.exchange.model.Currency;
import com.example.bankapp.exchange.model.ExchangeRate;
import com.example.bankapp.exchange.repository.RateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ExchangeService {
    private final RateRepository repository;

    public List<ExchangeRate> getRates() {
        return repository.findAll().stream()
                .filter(rate -> rate.getCurrency() != Currency.RUB)
                .toList();
    }

    public ExchangeRate getRate(Currency currency) {
        return repository.findByCurrency(currency)
                .orElseThrow(() -> new RateNotFoundException("Не найден курс валюты " + currency));
    }

    public void setRates(List<ExchangeRate> rates) {
        rates.forEach(this::setRate);
    }

    public void setRate(ExchangeRate rate) {
        validateExchangeRate(rate);
        repository.save(rate);
    }

    private void validateExchangeRate(ExchangeRate rate) {
        if (rate == null) {
            throw new InvalidRateException("Нет данных для обновления курса");
        }
        if (rate.getSellRate() == null || rate.getSellRate().signum() <= 0) {
            throw new InvalidRateException("Значение курса продажи должно быть больше 0");
        }
        if (rate.getBuyRate() == null || rate.getBuyRate().signum() <= 0) {
            throw new InvalidRateException("Значение курса покупки должно быть больше 0");
        }
        if (isBaseCurrency(rate.getCurrency())) {
            throw new InvalidRateException("Нельзя изменить курс: рубль - базовая валюта");
        }
    }

    public ConvertResult convert(Currency from, Currency to, BigDecimal amount) {
        validateConversionCurrencies(from, to);
        validateConversionAmount(amount);

        BigDecimal baseCurrencyRate = isBaseCurrency(from) ? null : getRate(from).getBuyRate();
        BigDecimal baseCurrencyAmount = isBaseCurrency(from) ?
                amount :
                amount.multiply(baseCurrencyRate);

        BigDecimal toCurrencyRate = isBaseCurrency(to) ? null : getRate(to).getSellRate();
        BigDecimal convertedAmount = isBaseCurrency(to) ?
                baseCurrencyAmount.setScale(2, RoundingMode.HALF_UP) :
                baseCurrencyAmount.divide(toCurrencyRate, 2, RoundingMode.HALF_UP);

        return ConvertResult.builder()
                .fromCurrency(from)
                .toCurrency(to)
                .baseCurrencyRate(baseCurrencyRate)
                .toCurrencyRate(toCurrencyRate)
                .converted(convertedAmount)
                .build();
    }

    private void validateConversionCurrencies(Currency from, Currency to) {
        if (from == null) {
            throw new CurrencyConversionException("Не указана исходная валюта");
        }
        if (to == null) {
            throw new CurrencyConversionException("Не указана целевая валюта");
        }
        if (from == to) {
            throw new CurrencyConversionException("Исходная и целевая валюта не могут быть одинаковыми");
        }
    }

    private void validateConversionAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new CurrencyConversionException("Сумма должна быть больше 0");
        }
    }

    private boolean isBaseCurrency(Currency currency) {
        return currency == Currency.RUB;
    }
}
