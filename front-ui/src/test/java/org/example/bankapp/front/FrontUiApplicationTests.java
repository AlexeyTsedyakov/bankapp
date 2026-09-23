package org.example.bankapp.front;

import org.example.bankapp.front.config.OAuth2TestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(OAuth2TestConfiguration.class)
class FrontUiApplicationTests {

    @Test
    void contextLoads() {
    }

}
