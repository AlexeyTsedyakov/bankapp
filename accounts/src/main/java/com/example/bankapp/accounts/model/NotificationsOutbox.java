package com.example.bankapp.accounts.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Builder
@Getter
@Table("notifications_outbox")
public class NotificationsOutbox {
    @Id private Long id;
    private UUID idempotencyKey;
    private Long userId;
    @Setter private String email;
    private String text;
    private OutboxStatus status;
    private int retryCount;
    @CreatedDate private Instant createdAt;

    public void retryRequired() {
        this.retryCount++;
        if (retryCount > 5) {
            this.status = OutboxStatus.FAILED;
        }
    }

    public void sent() {
        this.status = OutboxStatus.SENT;
    }
}
