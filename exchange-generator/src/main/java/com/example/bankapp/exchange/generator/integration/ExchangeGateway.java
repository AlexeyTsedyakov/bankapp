package com.example.bankapp.exchange.generator.integration;

import com.example.bankapp.exchange.generator.exception.IntegrationException;
import com.example.bankapp.exchange.generator.integration.dto.ExchangeRate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class ExchangeGateway {
    private final ExchangeServiceClient client;

    public void setExchangeRates(List<ExchangeRate> rates) {
        try {
            client.setExchangeRates(rates);
        } catch (RuntimeException e) {
            handleHttpError(e);
            throw e;
        }
    }

    private void handleHttpError(RuntimeException e) {
        log.error("Error executing request to the `Exchange service`: {}", e.getMessage());

        if (e instanceof HttpStatusCodeException) {
            throw new IntegrationException("Ошибка выполнения запроса к сервису обмена валюты");
        } else if (e instanceof ResourceAccessException) {
            throw new IntegrationException("Служба обмена валюты не доступна");
        }
    }
}
