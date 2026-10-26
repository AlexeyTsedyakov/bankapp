package com.example.bankapp.accounts.config;

import com.example.bankapp.accounts.integration.notifications.NotificationsServiceClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration
@ImportHttpServices(group = "api-gateway", types = NotificationsServiceClient.class)
public class HttpServiceClientsConfiguration {
}
