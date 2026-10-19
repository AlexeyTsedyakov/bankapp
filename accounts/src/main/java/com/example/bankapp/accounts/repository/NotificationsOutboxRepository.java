package com.example.bankapp.accounts.repository;

import com.example.bankapp.accounts.model.NotificationsOutbox;
import com.example.bankapp.accounts.model.OutboxStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationsOutboxRepository extends CrudRepository<NotificationsOutbox, Long> {
    List<NotificationsOutbox> findAllByStatus(OutboxStatus status);
}
