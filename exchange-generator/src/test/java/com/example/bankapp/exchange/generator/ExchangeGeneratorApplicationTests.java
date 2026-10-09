package com.example.bankapp.exchange.generator;

import com.example.bankapp.exchange.generator.config.OAuth2TestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import(OAuth2TestConfiguration.class)
@ActiveProfiles("test")
class ExchangeGeneratorApplicationTests {

    @Test
    void contextLoads() {
    }

}
