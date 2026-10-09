package com.example.bankapp.transfer;

import com.example.bankapp.transfer.config.JdbcDialectTestConfiguration;
import com.example.bankapp.transfer.config.OAuth2TestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import({JdbcDialectTestConfiguration.class, OAuth2TestConfiguration.class})
@ActiveProfiles("test")
class TransferApplicationTests {

    @Test
    void contextLoads() {
    }

}
