package com.example.bankapp.transfer.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransferTest {

    private Transfer transfer;

    @BeforeEach
    void setUp() {
        transfer = Transfer.builder()
                .id(1L)
                .idempotencyKey(UUID.randomUUID())
                .fromAccountId(100L)
                .toAccountId(200L)
                .fromCurrency(Currency.RUB)
                .toCurrency(Currency.USD)
                .sourceAmount(new BigDecimal("1000.00"))
                .targetAmount(new BigDecimal("10.00"))
                .baseCurrencyRate(new BigDecimal("1.0"))
                .toCurrencyRate(new BigDecimal("100.0"))
                .failCount(2)
                .version(1L)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Nested
    @DisplayName("State transitions and lifecycle methods")
    class LifecycleTests {

        @Test
        @DisplayName("start() should initialize step to CHECK_BLOCKER, status to NEW, and reset failCount to 0")
        void start_initializesTransferState() {
            transfer.start();

            assertThat(transfer.getStep()).isEqualTo(TransferStep.CHECK_BLOCKER);
            assertThat(transfer.getStatus()).isEqualTo(TransferStatus.NEW);
            assertThat(transfer.getFailCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("startCompensation() should set step to REFUND_WITHDRAW, status to PROCESSING, and reset failCount to 0")
        void startCompensation_setsStepAndStatusAndResetsFailCount() {
            transfer.startCompensation();

            assertThat(transfer.getStep()).isEqualTo(TransferStep.REFUND_WITHDRAW);
            assertThat(transfer.getStatus()).isEqualTo(TransferStatus.PROCESSING);
            assertThat(transfer.getFailCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("resume() should set status to PROCESSING")
        void resume_setsStatusToProcessing() {
            transfer.resume();

            assertThat(transfer.getStatus()).isEqualTo(TransferStatus.PROCESSING);
        }

        @Test
        @DisplayName("block() should set status to BLOCKED")
        void block_setsStatusToBlocked() {
            transfer.block();

            assertThat(transfer.getStatus()).isEqualTo(TransferStatus.BLOCKED);
        }

        @Test
        @DisplayName("complete() should set status to COMPLETED")
        void complete_setsStatusToCompleted() {
            transfer.complete();

            assertThat(transfer.getStatus()).isEqualTo(TransferStatus.COMPLETED);
        }

        @Test
        @DisplayName("refunded() should set status to REFUNDED")
        void refunded_setsStatusToRefunded() {
            transfer.refunded();

            assertThat(transfer.getStatus()).isEqualTo(TransferStatus.REFUNDED);
        }

        @Test
        @DisplayName("failed() should set status to FAILED")
        void failed_setsStatusToFailed() {
            transfer.failed();

            assertThat(transfer.getStatus()).isEqualTo(TransferStatus.FAILED);
        }

        @Test
        @DisplayName("moveToCalculateAmount() should update step to CALCULATE_AMOUNT and status to PROCESSING")
        void moveToCalculateAmount_updatesStepAndStatus() {
            transfer.moveToCalculateAmount();

            assertThat(transfer.getStep()).isEqualTo(TransferStep.CALCULATE_AMOUNT);
            assertThat(transfer.getStatus()).isEqualTo(TransferStatus.PROCESSING);
        }

        @Test
        @DisplayName("moveToWithdrawSender() should update step to WITHDRAW_SENDER and status to PROCESSING")
        void moveToWithdrawSender_updatesStepAndStatus() {
            transfer.moveToWithdrawSender();

            assertThat(transfer.getStep()).isEqualTo(TransferStep.WITHDRAW_SENDER);
            assertThat(transfer.getStatus()).isEqualTo(TransferStatus.PROCESSING);
        }

        @Test
        @DisplayName("moveToDepositReceiver() should update step to DEPOSIT_RECEIVER and status to PROCESSING")
        void moveToDepositReceiver_updatesStepAndStatus() {
            transfer.moveToDepositReceiver();

            assertThat(transfer.getStep()).isEqualTo(TransferStep.DEPOSIT_RECEIVER);
            assertThat(transfer.getStatus()).isEqualTo(TransferStatus.PROCESSING);
        }
    }

    @Nested
    @DisplayName("Retry logic")
    class RetryTests {

        @Test
        @DisplayName("retryRequired() increments failCount and sets RETRY_REQUIRED when failCount < 5")
        void retryRequired_whenFailCountLessThanFive_setsRetryRequired() {
            Transfer freshTransfer = Transfer.builder()
                    .failCount(0)
                    .build();

            for (int i = 1; i <= 4; i++) {
                freshTransfer.retryRequired();
                assertThat(freshTransfer.getFailCount()).isEqualTo(i);
                assertThat(freshTransfer.getStatus()).isEqualTo(TransferStatus.RETRY_REQUIRED);
            }
        }

        @Test
        @DisplayName("retryRequired() increments failCount and sets FAILED when failCount reaches 5 or more")
        void retryRequired_whenFailCountReachesFiveOrMore_setsFailed() {
            Transfer freshTransfer = Transfer.builder()
                    .failCount(4)
                    .build();

            freshTransfer.retryRequired();
            assertThat(freshTransfer.getFailCount()).isEqualTo(5);
            assertThat(freshTransfer.getStatus()).isEqualTo(TransferStatus.FAILED);

            freshTransfer.retryRequired();
            assertThat(freshTransfer.getFailCount()).isEqualTo(6);
            assertThat(freshTransfer.getStatus()).isEqualTo(TransferStatus.FAILED);
        }
    }

    @Nested
    @DisplayName("isActive() tests")
    class IsActiveTests {

        @ParameterizedTest
        @EnumSource(value = TransferStatus.class, names = {"NEW", "PROCESSING"})
        @DisplayName("isActive() returns true for NEW and PROCESSING statuses")
        void isActive_returnsTrueForActiveStatuses(TransferStatus status) {
            Transfer testTransfer = Transfer.builder().status(status).build();
            assertThat(testTransfer.isActive()).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = TransferStatus.class, names = {"NEW", "PROCESSING"}, mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("isActive() returns false for non-active statuses")
        void isActive_returnsFalseForNonActiveStatuses(TransferStatus status) {
            Transfer testTransfer = Transfer.builder().status(status).build();
            assertThat(testTransfer.isActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("needCompensation() tests")
    class NeedCompensationTests {

        @Test
        @DisplayName("needCompensation() returns true when step is DEPOSIT_RECEIVER and status is FAILED")
        void needCompensation_whenDepositReceiverAndFailed_returnsTrue() {
            Transfer testTransfer = Transfer.builder()
                    .step(TransferStep.DEPOSIT_RECEIVER)
                    .status(TransferStatus.FAILED)
                    .build();

            assertThat(testTransfer.needCompensation()).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = TransferStatus.class, names = {"FAILED"}, mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("needCompensation() returns false when step is DEPOSIT_RECEIVER but status is not FAILED")
        void needCompensation_whenDepositReceiverAndNotFailed_returnsFalse(TransferStatus status) {
            Transfer testTransfer = Transfer.builder()
                    .step(TransferStep.DEPOSIT_RECEIVER)
                    .status(status)
                    .build();

            assertThat(testTransfer.needCompensation()).isFalse();
        }

        @ParameterizedTest
        @EnumSource(value = TransferStep.class, names = {"DEPOSIT_RECEIVER"}, mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("needCompensation() returns false when status is FAILED but step is not DEPOSIT_RECEIVER")
        void needCompensation_whenNotDepositReceiverAndFailed_returnsFalse(TransferStep step) {
            Transfer testTransfer = Transfer.builder()
                    .step(step)
                    .status(TransferStatus.FAILED)
                    .build();

            assertThat(testTransfer.needCompensation()).isFalse();
        }
    }

    @Nested
    @DisplayName("applyTargetAmountCalculation() tests")
    class CalculationTests {

        @Test
        @DisplayName("applyTargetAmountCalculation() sets calculation result fields on Transfer")
        void applyTargetAmountCalculation_setsAllCalculatedFields() {
            CalculationResult result = new CalculationResult(
                    Currency.RUB,
                    Currency.USD,
                    new BigDecimal("1.0"),
                    new BigDecimal("95.50"),
                    new BigDecimal("10.47")
            );

            transfer.applyTargetAmountCalculation(result);

            assertThat(transfer.getFromCurrency()).isEqualTo(Currency.RUB);
            assertThat(transfer.getToCurrency()).isEqualTo(Currency.USD);
            assertThat(transfer.getBaseCurrencyRate()).isEqualByComparingTo("1.0");
            assertThat(transfer.getToCurrencyRate()).isEqualByComparingTo("95.50");
            assertThat(transfer.getTargetAmount()).isEqualByComparingTo("10.47");
        }
    }

    @Nested
    @DisplayName("Validation methods tests")
    class ValidationTests {

        @Test
        @DisplayName("validateForCalculateTargetAmount() succeeds when all required fields are present")
        void validateForCalculateTargetAmount_whenValid_doesNotThrow() {
            Transfer valid = Transfer.builder()
                    .fromAccountId(1L)
                    .toAccountId(2L)
                    .sourceAmount(new BigDecimal("100.00"))
                    .build();

            valid.validateForCalculateTargetAmount();
        }

        @Test
        @DisplayName("validateForCalculateTargetAmount() throws when fromAccountId is null")
        void validateForCalculateTargetAmount_whenFromAccountIdNull_throwsException() {
            Transfer invalid = Transfer.builder()
                    .toAccountId(2L)
                    .sourceAmount(new BigDecimal("100.00"))
                    .build();

            assertThatThrownBy(invalid::validateForCalculateTargetAmount)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Transfer field 'fromAccountId' cannot be null");
        }

        @Test
        @DisplayName("validateForCalculateTargetAmount() throws when toAccountId is null")
        void validateForCalculateTargetAmount_whenToAccountIdNull_throwsException() {
            Transfer invalid = Transfer.builder()
                    .fromAccountId(1L)
                    .sourceAmount(new BigDecimal("100.00"))
                    .build();

            assertThatThrownBy(invalid::validateForCalculateTargetAmount)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Transfer field 'toAccountId' cannot be null");
        }

        @Test
        @DisplayName("validateForCalculateTargetAmount() throws when sourceAmount is null")
        void validateForCalculateTargetAmount_whenSourceAmountNull_throwsException() {
            Transfer invalid = Transfer.builder()
                    .fromAccountId(1L)
                    .toAccountId(2L)
                    .build();

            assertThatThrownBy(invalid::validateForCalculateTargetAmount)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Transfer field 'sourceAmount' cannot be null");
        }

        @Test
        @DisplayName("validateForWithdraw() succeeds when all required fields are present")
        void validateForWithdraw_whenValid_doesNotThrow() {
            Transfer valid = Transfer.builder()
                    .fromAccountId(1L)
                    .sourceAmount(new BigDecimal("100.00"))
                    .idempotencyKey(UUID.randomUUID())
                    .build();

            valid.validateForWithdraw();
        }

        @Test
        @DisplayName("validateForWithdraw() throws when fromAccountId is null")
        void validateForWithdraw_whenFromAccountIdNull_throwsException() {
            Transfer invalid = Transfer.builder()
                    .sourceAmount(new BigDecimal("100.00"))
                    .idempotencyKey(UUID.randomUUID())
                    .build();

            assertThatThrownBy(invalid::validateForWithdraw)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Transfer field 'fromAccountId' cannot be null");
        }

        @Test
        @DisplayName("validateForWithdraw() throws when sourceAmount is null")
        void validateForWithdraw_whenSourceAmountNull_throwsException() {
            Transfer invalid = Transfer.builder()
                    .fromAccountId(1L)
                    .idempotencyKey(UUID.randomUUID())
                    .build();

            assertThatThrownBy(invalid::validateForWithdraw)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Transfer field 'sourceAmount' cannot be null");
        }

        @Test
        @DisplayName("validateForWithdraw() throws when idempotencyKey is null")
        void validateForWithdraw_whenIdempotencyKeyNull_throwsException() {
            Transfer invalid = Transfer.builder()
                    .fromAccountId(1L)
                    .sourceAmount(new BigDecimal("100.00"))
                    .build();

            assertThatThrownBy(invalid::validateForWithdraw)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Transfer field 'idempotencyKey' cannot be null");
        }

        @Test
        @DisplayName("validateForDeposit() succeeds when all required fields are present")
        void validateForDeposit_whenValid_doesNotThrow() {
            Transfer valid = Transfer.builder()
                    .toAccountId(2L)
                    .targetAmount(new BigDecimal("100.00"))
                    .idempotencyKey(UUID.randomUUID())
                    .build();

            valid.validateForDeposit();
        }

        @Test
        @DisplayName("validateForDeposit() throws when toAccountId is null")
        void validateForDeposit_whenToAccountIdNull_throwsException() {
            Transfer invalid = Transfer.builder()
                    .targetAmount(new BigDecimal("100.00"))
                    .idempotencyKey(UUID.randomUUID())
                    .build();

            assertThatThrownBy(invalid::validateForDeposit)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Transfer field 'toAccountId' cannot be null");
        }

        @Test
        @DisplayName("validateForDeposit() throws when targetAmount is null")
        void validateForDeposit_whenTargetAmountNull_throwsException() {
            Transfer invalid = Transfer.builder()
                    .toAccountId(2L)
                    .idempotencyKey(UUID.randomUUID())
                    .build();

            assertThatThrownBy(invalid::validateForDeposit)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Transfer field 'targetAmount' cannot be null");
        }

        @Test
        @DisplayName("validateForDeposit() throws when idempotencyKey is null")
        void validateForDeposit_whenIdempotencyKeyNull_throwsException() {
            Transfer invalid = Transfer.builder()
                    .toAccountId(2L)
                    .targetAmount(new BigDecimal("100.00"))
                    .build();

            assertThatThrownBy(invalid::validateForDeposit)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Transfer field 'idempotencyKey' cannot be null");
        }

        @Test
        @DisplayName("validateForRefund() succeeds when all required fields are present")
        void validateForRefund_whenValid_doesNotThrow() {
            Transfer valid = Transfer.builder()
                    .fromAccountId(1L)
                    .sourceAmount(new BigDecimal("100.00"))
                    .idempotencyKey(UUID.randomUUID())
                    .build();

            valid.validateForRefund();
        }

        @Test
        @DisplayName("validateForRefund() throws when fromAccountId is null")
        void validateForRefund_whenFromAccountIdNull_throwsException() {
            Transfer invalid = Transfer.builder()
                    .sourceAmount(new BigDecimal("100.00"))
                    .idempotencyKey(UUID.randomUUID())
                    .build();

            assertThatThrownBy(invalid::validateForRefund)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Transfer field 'fromAccountId' cannot be null");
        }

        @Test
        @DisplayName("validateForRefund() throws when sourceAmount is null")
        void validateForRefund_whenSourceAmountNull_throwsException() {
            Transfer invalid = Transfer.builder()
                    .fromAccountId(1L)
                    .idempotencyKey(UUID.randomUUID())
                    .build();

            assertThatThrownBy(invalid::validateForRefund)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Transfer field 'sourceAmount' cannot be null");
        }

        @Test
        @DisplayName("validateForRefund() throws when idempotencyKey is null")
        void validateForRefund_whenIdempotencyKeyNull_throwsException() {
            Transfer invalid = Transfer.builder()
                    .fromAccountId(1L)
                    .sourceAmount(new BigDecimal("100.00"))
                    .build();

            assertThatThrownBy(invalid::validateForRefund)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Transfer field 'idempotencyKey' cannot be null");
        }
    }
}
