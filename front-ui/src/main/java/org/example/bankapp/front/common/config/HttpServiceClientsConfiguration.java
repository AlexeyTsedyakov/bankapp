package org.example.bankapp.front.common.config;

import org.example.bankapp.front.integration.accounts.AccountsServiceClient;
import org.example.bankapp.front.integration.cash.CashServiceClient;
import org.example.bankapp.front.integration.exchange.ExchangeServiceClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration
@ImportHttpServices(group = "accounts", types = AccountsServiceClient.class)
@ImportHttpServices(group = "cash", types = CashServiceClient.class)
@ImportHttpServices(group = "exchange", types = ExchangeServiceClient.class)
public class HttpServiceClientsConfiguration {
}
