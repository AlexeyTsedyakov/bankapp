package org.example.bankapp.front.integration.exchange;

import org.example.bankapp.front.integration.exchange.dto.ExchangeRate;
import org.springframework.security.oauth2.client.annotation.ClientRegistrationId;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

@HttpExchange
@ClientRegistrationId("oauth2-service-client")
public interface ExchangeServiceClient {

    @GetExchange
    List<ExchangeRate> getExchangeRates();
}
