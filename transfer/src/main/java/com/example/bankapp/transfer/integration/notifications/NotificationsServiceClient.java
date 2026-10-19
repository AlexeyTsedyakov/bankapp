package com.example.bankapp.transfer.integration.notifications;

import com.example.bankapp.transfer.integration.notifications.dto.NotifyRequest;
import org.springframework.security.oauth2.client.annotation.ClientRegistrationId;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
@ClientRegistrationId("oauth2-service-client")
public interface NotificationsServiceClient {

    @PostExchange("/notify")
    void notify(@RequestBody NotifyRequest request);
}
