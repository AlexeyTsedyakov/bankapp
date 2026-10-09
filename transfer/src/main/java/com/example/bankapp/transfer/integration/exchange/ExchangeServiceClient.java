package com.example.bankapp.transfer.integration.exchange;

import com.example.bankapp.transfer.integration.exchange.dto.ConvertRequest;
import com.example.bankapp.transfer.integration.exchange.dto.ConvertResponse;
import com.example.bankapp.transfer.integration.exchange.dto.ExchangeRate;
import com.example.bankapp.transfer.model.Currency;
import org.springframework.security.oauth2.client.annotation.ClientRegistrationId;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
@ClientRegistrationId("oauth2-service-client")
public interface ExchangeServiceClient {

    @GetExchange("/{currency}")
    ExchangeRate getExchangeRate(@PathVariable Currency currency);

    @PostExchange("/convert")
    ConvertResponse convert(@RequestBody ConvertRequest request);
}
