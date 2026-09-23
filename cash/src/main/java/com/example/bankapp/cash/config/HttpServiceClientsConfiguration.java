package com.example.bankapp.cash.config;

import com.example.bankapp.cash.integration.accounts.AccountsServiceClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration
@ImportHttpServices(group = "accounts", types = AccountsServiceClient.class)
public class HttpServiceClientsConfiguration {
}
