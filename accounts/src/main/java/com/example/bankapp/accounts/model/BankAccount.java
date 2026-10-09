package com.example.bankapp.accounts.model;

import com.example.bankapp.accounts.exception.InvalidAmountException;
import com.example.bankapp.accounts.exception.NotEnoughBalanceException;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

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

    @Column
    @CreatedDate
    private Instant createdAt;

    @Column
    @LastModifiedDate
    private Instant updatedAt;

    public void deposit(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidAmountException("Сумма пополнения должна быть больше 0");
        }

        this.balance = balance
                .add(amount)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public void withdraw(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidAmountException("Сумма снятия должна быть больше 0");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new NotEnoughBalanceException("Не достаточно средств на балансе");
        }

        this.balance = balance
                .subtract(amount)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
