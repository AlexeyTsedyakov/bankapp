package com.example.bankapp.cash.integration.blocker;

import com.example.bankapp.cash.integration.blocker.dto.BlockerRequest;
import com.example.bankapp.cash.integration.blocker.dto.BlockerResponse;
import org.springframework.security.oauth2.client.annotation.ClientRegistrationId;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
@ClientRegistrationId("oauth2-service-client")
public interface BlockerServiceClient {

    @PostExchange
    BlockerResponse checkAccountOperation(@RequestBody BlockerRequest request);
}
