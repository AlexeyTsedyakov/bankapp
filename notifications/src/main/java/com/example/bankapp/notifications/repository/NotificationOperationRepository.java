package com.example.bankapp.notifications.repository;

import com.example.bankapp.notifications.model.NotificationOperation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotificationOperationRepository extends CrudRepository<NotificationOperation, Long> {
    boolean existsByIdempotencyKey(UUID idempotencyKey);
}
