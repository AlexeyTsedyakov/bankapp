package com.example.bankapp.exchange.generator.integration;

import com.example.bankapp.exchange.generator.integration.dto.ExchangeRate;
import org.springframework.security.oauth2.client.annotation.ClientRegistrationId;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PutExchange;

import java.util.List;

@HttpExchange
@ClientRegistrationId("oauth2-service-client")
public interface ExchangeServiceClient {

    @PutExchange
    void setExchangeRates(@RequestBody List<ExchangeRate> rates);
}
