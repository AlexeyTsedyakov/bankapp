package com.example.bankapp.accounts;

import com.example.bankapp.accounts.config.JdbcDialectTestConfiguration;
import com.example.bankapp.accounts.config.OAuth2TestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import({JdbcDialectTestConfiguration.class, OAuth2TestConfiguration.class})
@ActiveProfiles("test")
class AccountsApplicationTests {

    @Test
    void contextLoads() {
    }

}
