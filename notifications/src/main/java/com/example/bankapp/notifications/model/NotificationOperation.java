package com.example.bankapp.notifications.model;

import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Builder
@Table("notification_operation")
public class NotificationOperation {
    @Id private Long id;
    private UUID idempotencyKey;
    private String email;
    @CreatedDate private Instant createdAt;
}
