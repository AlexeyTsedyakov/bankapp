package com.example.bankapp.transfer.config;

import com.example.bankapp.transfer.integration.accounts.AccountsServiceClient;
import com.example.bankapp.transfer.integration.blocker.BlockerServiceClient;
import com.example.bankapp.transfer.integration.exchange.ExchangeServiceClient;
import com.example.bankapp.transfer.integration.notifications.NotificationsServiceClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration
@ImportHttpServices(group = "accounts", types = AccountsServiceClient.class)
@ImportHttpServices(group = "exchange", types = ExchangeServiceClient.class)
@ImportHttpServices(group = "blocker", types = BlockerServiceClient.class)
@ImportHttpServices(group = "notifications", types = NotificationsServiceClient.class)
public class HttpServiceClientsConfiguration {
}
