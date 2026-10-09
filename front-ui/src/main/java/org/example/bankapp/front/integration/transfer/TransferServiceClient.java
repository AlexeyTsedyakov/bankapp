package org.example.bankapp.front.integration.transfer;

import org.example.bankapp.front.integration.transfer.dto.TransferRequest;
import org.springframework.security.oauth2.client.annotation.ClientRegistrationId;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.UUID;

@HttpExchange
@ClientRegistrationId("oauth2-service-client")
public interface TransferServiceClient {

    @PostExchange
    void transfer(@RequestBody TransferRequest request,
                  @RequestHeader(name = "Idempotency-Key") UUID idempotencyKey);
}
