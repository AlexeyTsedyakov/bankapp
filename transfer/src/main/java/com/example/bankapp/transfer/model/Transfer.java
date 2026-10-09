package com.example.bankapp.transfer.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Table("transfers")
public class Transfer {
    @Id private Long id;
    private UUID idempotencyKey;
    private Long fromAccountId;
    private Long toAccountId;
    private Currency fromCurrency;
    private Currency toCurrency;
    private BigDecimal sourceAmount;
    private BigDecimal targetAmount;
    private BigDecimal baseCurrencyRate;
    private BigDecimal toCurrencyRate;
    private TransferStep step;
    private TransferStatus status;
    private Integer failCount;
    @Version private Long version;
    @CreatedDate private Instant createdAt;
    @LastModifiedDate private Instant updatedAt;

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

    public void resume() {
        this.status = TransferStatus.PROCESSING;
    }

    public void blockerChecked() {
        this.step = TransferStep.CALCULATE_AMOUNT;
        this.status = TransferStatus.PROCESSING;
    }

    public void amountCalculated() {
        this.step = TransferStep.WITHDRAW_SENDER;
    }

    public void senderWithdrawn() {
        this.step = TransferStep.DEPOSIT_RECEIVER;
    }

    public void complete() {
        this.status = TransferStatus.COMPLETED;
    }

    public void refunded() {
        this.status = TransferStatus.REFUNDED;
    }

    public void retryRequired() {
        this.failCount++;
        this.status = failCount >= 5 ? TransferStatus.FAILED : TransferStatus.RETRY_REQUIRED;
    }

    public void startCompensation() {
        this.failCount = 0;
        this.status = TransferStatus.PROCESSING;
        this.step = TransferStep.REFUND_WITHDRAW;
    }

    public void failed() {
        this.status = TransferStatus.FAILED;
    }

    public void applyTargetAmountCalculation(CalculationResult calculationResult) {
        this.fromCurrency = calculationResult.fromCurrency();
        this.toCurrency = calculationResult.toCurrency();
        this.baseCurrencyRate = calculationResult.baseCurrencyRate();
        this.toCurrencyRate = calculationResult.toCurrencyRate();
        this.targetAmount = calculationResult.amount();
    }

    public void validateForCalculateTargetAmount() {
        requireNonNull(fromAccountId, "fromAccountId");
        requireNonNull(toAccountId, "toAccountId");
        requireNonNull(sourceAmount, "sourceAmount");
    }

    public void validateForWithdraw() {
        requireNonNull(fromAccountId, "fromAccountId");
        requireNonNull(sourceAmount, "sourceAmount");
        requireNonNull(idempotencyKey, "idempotencyKey");
    }

    public void validateForDeposit() {
        requireNonNull(toAccountId, "toAccountId");
        requireNonNull(targetAmount, "targetAmount");
        requireNonNull(idempotencyKey, "idempotencyKey");
    }

    public void validateForRefund() {
        requireNonNull(fromAccountId, "fromAccountId");
        requireNonNull(sourceAmount, "sourceAmount");
        requireNonNull(idempotencyKey, "idempotencyKey");
    }

    private void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalStateException("Transfer field '" + fieldName + "' cannot be null");
        }
    }
}
