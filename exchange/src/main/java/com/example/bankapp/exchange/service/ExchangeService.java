package com.example.bankapp.exchange.service;

import com.example.bankapp.exchange.exceptions.InvalidRateException;
import com.example.bankapp.exchange.exceptions.RateNotFoundException;
import com.example.bankapp.exchange.model.Currency;
import com.example.bankapp.exchange.model.ExchangeRate;
import com.example.bankapp.exchange.repository.RateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ExchangeService {
    private final RateRepository repository;

    public List<ExchangeRate> getRates() {
        return repository.findAll();
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
            throw new InvalidRateException("Значение курса покупки должно быть больше 0");
        }
        if (rate.getBuyRate() == null || rate.getBuyRate().signum() <= 0) {
            throw new InvalidRateException("Значение курса продажи должно быть больше 0");
        }
        if (rate.getCurrency() == Currency.RUB) {
            throw new InvalidRateException("Нельзя изменить курс: рубль - базовая валюта");
        }
    }
}
