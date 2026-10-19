package com.example.bankapp.transfer.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
@Getter
@Table("transfers")
public class Transfer {
    @Id private Long id;
    private UUID idempotencyKey;
    @Embedded.Nullable(prefix = "from_") private AccountSnapshot from;
    @Embedded.Nullable(prefix = "to_") private AccountSnapshot to;
    private BigDecimal sourceAmount;
    private BigDecimal targetAmount;
    private BigDecimal baseCurrencyRate;
    private BigDecimal toCurrencyRate;
    private TransferStep step;
    private TransferStatus status;
    private int failCount;
    @Setter private String errorMessage;
    @Version private Long version;
    @CreatedDate private Instant createdAt;
    @LastModifiedDate private Instant updatedAt;

    @Builder
    public record AccountSnapshot(
            Long accountId,
            Long userId,
            String accountNumber,
            Currency currency
    ) { }

    public boolean isActive() {
        return status == TransferStatus.NEW || status == TransferStatus.PROCESSING;
    }

    public boolean needCompensation() {
        return step == TransferStep.DEPOSIT_RECEIVER && status == TransferStatus.FAILED;
    }

    public void start() {
        this.step = TransferStep.CHECK_BLOCKER;
        this.status = TransferStatus.NEW;
        this.failCount = 0;
    }

    public void startCompensation() {
        this.failCount = 0;
        this.status = TransferStatus.PROCESSING;
        this.step = TransferStep.REFUND_WITHDRAW;
    }

    public void resume() {
        this.status = TransferStatus.PROCESSING;
    }

    public void block() {
        this.status = TransferStatus.BLOCKED;
    }

    public void complete() {
        this.status = TransferStatus.COMPLETED;
        this.errorMessage = null;
    }

    public void refunded() {
        this.status = TransferStatus.REFUNDED;
    }

    public void retryRequired(String message) {
        this.failCount++;
        this.errorMessage = message;
        this.status = failCount >= 5 ? TransferStatus.FAILED : TransferStatus.RETRY_REQUIRED;
    }

    public void failed(String message) {
        this.status = TransferStatus.FAILED;
        this.errorMessage = message;
    }

    public void moveToResolveAccounts() {
        this.step = TransferStep.RESOLVE_ACCOUNTS;
        this.status = TransferStatus.PROCESSING;
    }

    public void moveToCalculateAmount() {
        this.step = TransferStep.CALCULATE_AMOUNT;
        this.status = TransferStatus.PROCESSING;
    }

    public void moveToWithdrawSender() {
        this.step = TransferStep.WITHDRAW_SENDER;
        this.status = TransferStatus.PROCESSING;
    }

    public void moveToDepositReceiver() {
        this.step = TransferStep.DEPOSIT_RECEIVER;
        this.status = TransferStatus.PROCESSING;
    }

    public void applyAccounts(AccountSnapshot from, AccountSnapshot to) {
        this.from = from;
        this.to = to;
    }

    public void applyTargetAmountCalculation(CalculationResult calculationResult) {
        this.baseCurrencyRate = calculationResult.baseCurrencyRate();
        this.toCurrencyRate = calculationResult.toCurrencyRate();
        this.targetAmount = calculationResult.result();
    }

    public void validateForCalculateTargetAmount() {
        requireNonNull(from, "from");
        requireNonNull(from.accountId, "from.accountId");
        requireNonNull(to, "to");
        requireNonNull(to.accountId, "to.accountId");
        requireNonNull(sourceAmount, "sourceAmount");
    }

    public void validateForWithdraw() {
        requireNonNull(from, "from");
        requireNonNull(from.accountId, "from.accountId");
        requireNonNull(sourceAmount, "sourceAmount");
        requireNonNull(idempotencyKey, "idempotencyKey");
    }

    public void validateForDeposit() {
        requireNonNull(to, "to");
        requireNonNull(to.accountId, "to.accountId");
        requireNonNull(targetAmount, "targetAmount");
        requireNonNull(idempotencyKey, "idempotencyKey");
    }

    public void validateForRefund() {
        requireNonNull(from, "from");
        requireNonNull(from.accountId, "from.accountId");
        requireNonNull(sourceAmount, "sourceAmount");
        requireNonNull(idempotencyKey, "idempotencyKey");
    }

    private void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalStateException("Transfer field '" + fieldName + "' cannot be null");
        }
    }
}
