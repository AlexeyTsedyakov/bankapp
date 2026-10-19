package com.example.bankapp.notifications.service;

import com.example.bankapp.notifications.model.NotificationOperation;
import com.example.bankapp.notifications.repository.NotificationOperationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class NotificationsService {
    private final JavaMailSender mailSender;
    private final NotificationOperationRepository operationRepository;

    public void notify(String email, String text, UUID idempotencyKey) {
        // Резервируем ключ
        try {
            recordOperation(idempotencyKey, email);
        } catch (DuplicateKeyException e) {
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@bankapp.org");
        message.setTo(email);
        message.setSubject("Уведомление");
        message.setText(text);
        mailSender.send(message);
    }

    private void recordOperation(UUID idempotencyKey, String email) {
        NotificationOperation operation = NotificationOperation.builder()
                .idempotencyKey(idempotencyKey)
                .email(email)
                .build();

        operationRepository.save(operation);
    }
}
