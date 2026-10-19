package com.example.bankapp.cash.config;

import com.example.bankapp.cash.integration.accounts.AccountsServiceClient;
import com.example.bankapp.cash.integration.blocker.BlockerServiceClient;
import com.example.bankapp.cash.integration.notifications.NotificationsServiceClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration
@ImportHttpServices(group = "accounts", types = AccountsServiceClient.class)
@ImportHttpServices(group = "blocker", types = BlockerServiceClient.class)
@ImportHttpServices(group = "notifications", types = NotificationsServiceClient.class)
public class HttpServiceClientsConfiguration {
}
