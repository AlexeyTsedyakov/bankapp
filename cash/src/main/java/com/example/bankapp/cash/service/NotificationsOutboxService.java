package com.example.bankapp.cash.service;

import com.example.bankapp.cash.integration.accounts.AccountsGateway;
import com.example.bankapp.cash.integration.accounts.dto.response.BankAccountResponse;
import com.example.bankapp.cash.integration.notifications.NotificationsGateway;
import com.example.bankapp.cash.model.NotificationsOutbox;
import com.example.bankapp.cash.model.OutboxStatus;
import com.example.bankapp.cash.repository.NotificationsOutboxRepository;
import com.example.bankapp.cash.service.event.DepositCompleted;
import com.example.bankapp.cash.service.event.WithdrawCompleted;
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

        return Optional.ofNullable(accountsGateway.getUser(item.getUserId()).email());
    }

    @TransactionalEventListener(
            phase = TransactionPhase.BEFORE_COMMIT,
            classes = DepositCompleted.class,
            fallbackExecution = true
    )
    public void onDepositCompleted(DepositCompleted event) {
        BankAccountResponse account = event.bankAccount();
        String text = "Успешное пополнение счета " + account.accountNumber() + " на сумму " +
                event.amount() + " " + account.currency().name();
        var outbox = NotificationsOutbox.builder()
                .userId(account.userId())
                .idempotencyKey(UUID.randomUUID())
                .text(text)
                .status(OutboxStatus.PENDING)
                .build();

        repository.save(outbox);
    }

    @TransactionalEventListener(
            phase = TransactionPhase.BEFORE_COMMIT,
            classes = WithdrawCompleted.class,
            fallbackExecution = true
    )
    public void onWithdrawCompleted(WithdrawCompleted event) {
        BankAccountResponse account = event.bankAccount();
        String text = "Успешное снятие со счета " + account.accountNumber() + " на сумму " +
                event.amount() + " " + account.currency().name();
        var outbox = NotificationsOutbox.builder()
                .userId(account.userId())
                .idempotencyKey(UUID.randomUUID())
                .text(text)
                .status(OutboxStatus.PENDING)
                .build();

        repository.save(outbox);
    }
}
