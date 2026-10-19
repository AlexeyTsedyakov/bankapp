package com.example.bankapp.transfer.service;

import com.example.bankapp.transfer.integration.accounts.AccountsGateway;
import com.example.bankapp.transfer.integration.notifications.NotificationsGateway;
import com.example.bankapp.transfer.model.NotificationsOutbox;
import com.example.bankapp.transfer.model.OutboxStatus;
import com.example.bankapp.transfer.model.Transfer;
import com.example.bankapp.transfer.repository.NotificationsOutboxRepository;
import com.example.bankapp.transfer.service.event.TransferCompleted;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class NotificationsOutboxService {
    private final NotificationsOutboxRepository repository;
    private final AccountsGateway accountsGateway;
    private final NotificationsGateway notificationsGateway;

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.SECONDS)
    @SchedulerLock(name = "notifications-outbox", lockAtMostFor = "5m", lockAtLeastFor = "2s")
    public void processOutbox() {
        List<NotificationsOutbox> items = repository.findAllByStatus(OutboxStatus.PENDING);
        items.forEach(this::processItem);
    }

    private void processItem(NotificationsOutbox item) {
        try {
            Optional<String> email = resolveEmail(item);
            if (email.isEmpty() || email.get().isBlank()) {
                repository.delete(item);
                return;
            }

            item.setEmail(email.get());
            notificationsGateway.notify(item.getEmail(), item.getText(), item.getIdempotencyKey());
            item.sent();
        } catch (Exception e) {
            item.retryRequired();
        }

        repository.save(item);
    }

    private Optional<String> resolveEmail(NotificationsOutbox item) {
        if (item.getEmail() != null) {
            return Optional.of(item.getEmail());
        }

        String email = accountsGateway.getUser(item.getUserId()).email();
        return email == null || email.isBlank() ? Optional.empty() : Optional.of(email);
    }

    @TransactionalEventListener(
            phase = TransactionPhase.BEFORE_COMMIT,
            classes = TransferCompleted.class
    )
    public void onTransferCompleted(TransferCompleted event) {
        Transfer transfer = event.transfer();
        Transfer.AccountSnapshot from = transfer.getFrom();
        Transfer.AccountSnapshot to = transfer.getTo();

        String text = "Перевод на номер счета " + to.accountNumber() + " на сумму " +
                transfer.getSourceAmount() + " " + from.currency() + " успешно осуществлен";
        var outbox = NotificationsOutbox.builder()
                .userId(from.userId())
                .idempotencyKey(UUID.randomUUID())
                .text(text)
                .status(OutboxStatus.PENDING)
                .build();
        repository.save(outbox);

        text = "Получен перевод с счета " + from.accountNumber() + " на сумму " +
                transfer.getTargetAmount() + " " + to.currency();
        outbox = NotificationsOutbox.builder()
                .userId(to.userId())
                .idempotencyKey(UUID.randomUUID())
                .text(text)
                .status(OutboxStatus.PENDING)
                .build();
        repository.save(outbox);
    }
}
