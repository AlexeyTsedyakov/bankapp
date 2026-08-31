package com.example.bankapp.accounts.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

@Configuration
public class OAuth2ClientConfig {

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {

        var provider = OAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials()
                        .build();
        var manager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, authorizedClientService);
        manager.setAuthorizedClientProvider(provider);

        return manager;
    }

    @Bean
    public RestClient oauth2RestClient(OAuth2AuthorizedClientManager authorizedClientManager) {
        var interceptor = new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);

        // Указываем ID регистрации из application.yaml
        interceptor.setClientRegistrationIdResolver(request -> "oauth2-service-client");

        return RestClient.builder()
                .requestInterceptor(interceptor)
                .build();
    }
}
