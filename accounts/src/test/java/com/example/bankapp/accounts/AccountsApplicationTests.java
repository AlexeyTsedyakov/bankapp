package com.example.bankapp.accounts;

import com.example.bankapp.accounts.config.JdbcDialectTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import(JdbcDialectTestConfiguration.class)
@ActiveProfiles("test")
class AccountsApplicationTests {

    @Test
    void contextLoads() {
    }

}
