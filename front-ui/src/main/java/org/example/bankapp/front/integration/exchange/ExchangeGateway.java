package org.example.bankapp.front.integration.exchange;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bankapp.front.common.exceptions.IntegrationException;
import org.example.bankapp.front.integration.exchange.dto.ExchangeRate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class ExchangeGateway {
    private final ExchangeServiceClient client;

    public List<ExchangeRate> getExchangeRates() {
        try {
            return client.getExchangeRates();
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
