package com.example.bankapp.exchange.generator.config;

import com.example.bankapp.exchange.generator.integration.ExchangeServiceClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration
@ImportHttpServices(group = "api-gateway", types = ExchangeServiceClient.class)
public class HttpServiceClientsConfiguration {
}
