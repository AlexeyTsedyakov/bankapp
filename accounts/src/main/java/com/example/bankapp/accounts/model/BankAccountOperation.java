package com.example.bankapp.accounts.model;

import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
@Table("bank_account_operations")
public class BankAccountOperation {
    @Id
    private Long id;

    @Column
    private AggregateReference<BankAccount, Long> accountId;

    @Column
    private UUID idempotencyKey;

    @Column
    private OperationType operationType;

    @Column
    BigDecimal amount;

    @Column
    @CreatedDate
    Instant createdAt;
}
