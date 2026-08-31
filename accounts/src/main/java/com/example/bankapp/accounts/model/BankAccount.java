package com.example.bankapp.accounts.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@Table("bank_accounts")
public class BankAccount {
    @Id
    private Long id;

    @Column("user_id")
    private AggregateReference<User, Long> user;

    @Column
    private String accountNumber;

    @Column
    private Currency currency;

    @Column
    private BigDecimal balance;
}
