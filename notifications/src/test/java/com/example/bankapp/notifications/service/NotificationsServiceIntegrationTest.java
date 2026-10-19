package com.example.bankapp.notifications.service;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatNoException;

@Tag("integration")
@SpringBootTest
@ActiveProfiles("test")
public class NotificationsServiceIntegrationTest {
    @Autowired NotificationsService service;

    @Test
    void notify_sendsEmail() {
        assertThatNoException()
                .isThrownBy(() -> service.notify("test@example.com", "Hello world!", UUID.randomUUID()));
    }
}
