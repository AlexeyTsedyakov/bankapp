package com.example.bankapp.notifications.service;

import com.example.bankapp.notifications.config.JdbcDialectTestConfiguration;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Import(JdbcDialectTestConfiguration.class)
@ActiveProfiles("test")
public class NotificationsServiceTest {
    @Autowired
    private NotificationsService service;
    @MockitoBean
    private JavaMailSender mailSender;

    @Test
    public void notify_callsMailSender() {
        String email = "client@example.com";
        String text = "test";

        service.notify(email, text, UUID.randomUUID());

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage sentMessage = captor.getValue();

        assertThat(sentMessage.getText()).isEqualTo(text);
        assertThat(sentMessage.getTo()).containsExactly(email);
        assertThat(sentMessage.getFrom()).isEqualTo("noreply@bankapp.org");
        assertThat(sentMessage.getSubject()).isEqualTo("Уведомление");
    }

    @Test
    public void notify_existsIdempotencyKey_doNothing() {
        String email = "client@example.com";
        String text = "test";
        UUID idempotencyKey = UUID.randomUUID();

        service.notify(email, text, idempotencyKey);
        service.notify(email, text, idempotencyKey);

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}
