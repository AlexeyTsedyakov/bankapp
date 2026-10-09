package com.example.bankapp.accounts.repository;

import com.example.bankapp.accounts.model.BankAccountOperation;
import com.example.bankapp.accounts.model.OperationType;
import org.springframework.data.relational.core.sql.LockMode;
import org.springframework.data.relational.repository.Lock;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BankAccountOperationRepository extends CrudRepository<BankAccountOperation, Long> {

    @Lock(LockMode.PESSIMISTIC_WRITE)
    boolean existsByIdempotencyKeyAndOperationType(UUID idempotencyKey, OperationType operationType);
}
