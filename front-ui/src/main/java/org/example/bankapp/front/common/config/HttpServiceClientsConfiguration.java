package org.example.bankapp.front.common.config;

import org.example.bankapp.front.integration.accounts.client.AccountsServiceClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration
@ImportHttpServices(group = "accounts", types = AccountsServiceClient.class)
public class HttpServiceClientsConfiguration {
}
