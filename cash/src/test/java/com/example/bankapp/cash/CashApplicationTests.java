package com.example.bankapp.cash;

import com.example.bankapp.cash.config.OAuth2TestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(OAuth2TestConfiguration.class)
class CashApplicationTests {

    @Test
    void contextLoads() {
    }

}
