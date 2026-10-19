package com.example.bankapp.transfer.repository;

import com.example.bankapp.transfer.model.NotificationsOutbox;
import com.example.bankapp.transfer.model.OutboxStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationsOutboxRepository extends CrudRepository<NotificationsOutbox, Long> {
    List<NotificationsOutbox> findAllByStatus(OutboxStatus status);
}
