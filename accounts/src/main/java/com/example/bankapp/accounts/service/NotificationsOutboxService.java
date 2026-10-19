package com.example.bankapp.accounts.service;

import com.example.bankapp.accounts.integration.notifications.NotificationsGateway;
import com.example.bankapp.accounts.model.NotificationsOutbox;
import com.example.bankapp.accounts.model.OutboxStatus;
import com.example.bankapp.accounts.model.User;
import com.example.bankapp.accounts.repository.NotificationsOutboxRepository;
import com.example.bankapp.accounts.service.event.UserCreated;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class NotificationsOutboxService {
    private final NotificationsOutboxRepository repository;
    private final NotificationsGateway notificationsGateway;

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.SECONDS)
    @SchedulerLock(name = "notifications-outbox", lockAtMostFor = "5m", lockAtLeastFor = "2s")
    public void processOutbox() {
        List<NotificationsOutbox> items = repository.findAllByStatus(OutboxStatus.PENDING);
        items.forEach(this::processItem);
    }

    private void processItem(NotificationsOutbox item) {
        try {
            notificationsGateway.notify(item.getEmail(), item.getText(), item.getIdempotencyKey());
            item.sent();
        } catch (Exception e) {
            item.retryRequired();
        }

        repository.save(item);
    }

    @TransactionalEventListener(
            phase = TransactionPhase.BEFORE_COMMIT,
            classes = UserCreated.class
    )
    public void onUserCreated(UserCreated event) {
        User user = event.user();
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            return;
        }

        String text = """
                Успешное создание аккаунта!
                
                Логин: %s
                Имя: %s
                Фамилия: %s
                Дата рождения: %s
                """
                .formatted(user.getUsername(), user.getFirstName(), user.getLastName(), user.getBirthDate());

        var outbox = NotificationsOutbox.builder()
                .userId(user.getId())
                .idempotencyKey(UUID.randomUUID())
                .email(user.getEmail())
                .text(text)
                .status(OutboxStatus.PENDING)
                .build();

        repository.save(outbox);
    }
}
