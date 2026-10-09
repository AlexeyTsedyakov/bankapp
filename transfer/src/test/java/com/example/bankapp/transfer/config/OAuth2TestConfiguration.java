package com.example.bankapp.transfer.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@TestConfiguration
public class OAuth2TestConfiguration {
    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return Mockito.mock(ClientRegistrationRepository.class);
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return Mockito.mock(JwtDecoder.class);
    }
}
