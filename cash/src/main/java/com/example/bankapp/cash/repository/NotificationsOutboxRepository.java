package com.example.bankapp.cash.repository;

import com.example.bankapp.cash.model.NotificationsOutbox;
import com.example.bankapp.cash.model.OutboxStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationsOutboxRepository extends CrudRepository<NotificationsOutbox, Long> {
    List<NotificationsOutbox> findAllByStatus(OutboxStatus status);
}
