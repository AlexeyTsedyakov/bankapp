package com.example.bankapp.transfer.saga;

import com.example.bankapp.transfer.exceptions.ServiceUnavailableException;
import com.example.bankapp.transfer.exceptions.TransferException;
import com.example.bankapp.transfer.model.CalculationResult;
import com.example.bankapp.transfer.model.Currency;
import com.example.bankapp.transfer.model.Transfer;
import com.example.bankapp.transfer.service.TransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = SagaStepExecutor.class)
public class SagaStepExecutorTest {
    @Autowired
    SagaStepExecutor stepExecutor;
    @MockitoBean
    TransferService transferService;

    private Transfer transfer;

    @BeforeEach
    void setUp() {
        Transfer realTransfer = Transfer.builder()
                .id(1L)
                .idempotencyKey(UUID.randomUUID())
                .fromAccountId(1L)
                .toAccountId(2L)
                .sourceAmount(new BigDecimal("100.00"))
                .failCount(0)
                .build();
        transfer = spy(realTransfer);
    }

    @Test
    void executeStep_checkBlocker_blocksTransferWhenShouldBlock() {
        transfer.start();
        when(transferService.shouldBlock(any(Transfer.class))).thenReturn(true);

        stepExecutor.executeStep(transfer);

        verify(transferService).shouldBlock(transfer);
        verify(transfer).block();
    }

    @Test
    void executeStep_checkBlocker_movesToCalculateAmountWhenShouldNotBlock() {
        transfer.start();
        when(transferService.shouldBlock(any(Transfer.class))).thenReturn(false);

        stepExecutor.executeStep(transfer);

        verify(transferService).shouldBlock(transfer);
        verify(transfer).moveToCalculateAmount();
    }

    @Test
    void executeStep_calculateAmount_appliesCalculationAndMovesToWithdrawSender() {
        transfer.moveToCalculateAmount();
        CalculationResult calculationResult = new CalculationResult(
                Currency.RUB,
                Currency.USD,
                BigDecimal.ONE,
                new BigDecimal("0.01"),
                new BigDecimal("1.00")
        );
        when(transferService.calculateTargetAmount(any(Transfer.class))).thenReturn(calculationResult);

        stepExecutor.executeStep(transfer);

        verify(transferService).calculateTargetAmount(transfer);
        verify(transfer).applyTargetAmountCalculation(calculationResult);
        verify(transfer).moveToWithdrawSender();
    }

    @Test
    void executeStep_withdrawSender_withdrawsAndMovesToDepositReceiver() {
        transfer.moveToWithdrawSender();

        stepExecutor.executeStep(transfer);

        verify(transferService).withdrawSender(transfer);
        verify(transfer).moveToDepositReceiver();
    }

    @Test
    void executeStep_depositReceiver_depositsAndCompletesTransfer() {
        transfer.moveToDepositReceiver();

        stepExecutor.executeStep(transfer);

        verify(transferService).depositReceiver(transfer);
        verify(transfer).complete();
    }

    @Test
    void executeStep_refundWithdraw_refundsAndMarksRefunded() {
        transfer.startCompensation();

        stepExecutor.executeStep(transfer);

        verify(transferService).refundWithdraw(transfer);
        verify(transfer).refunded();
    }

    @Test
    void executeStep_nullTransfer_throwsTransferException() {
        assertThatThrownBy(() -> stepExecutor.executeStep(null))
                .isInstanceOf(TransferException.class)
                .hasMessage("Отсутствуют данные для перевода");
    }

    @Test
    void executeStep_nullStep_marksTransferAsFailed() {
        stepExecutor.executeStep(transfer);

        verify(transfer).failed();
    }

    @Test
    void executeStep_serviceUnavailableException_requiresRetry() {
        transfer.moveToWithdrawSender();
        doThrow(new ServiceUnavailableException("Service unavailable"))
                .when(transferService).withdrawSender(any(Transfer.class));

        stepExecutor.executeStep(transfer);

        verify(transferService).withdrawSender(transfer);
        verify(transfer).retryRequired();
        verify(transfer, never()).moveToDepositReceiver();
    }

    @Test
    void executeStep_generalException_marksTransferAsFailed() {
        transfer.moveToWithdrawSender();
        doThrow(new RuntimeException("Unexpected error"))
                .when(transferService).withdrawSender(any(Transfer.class));

        stepExecutor.executeStep(transfer);

        verify(transferService).withdrawSender(transfer);
        verify(transfer).failed();
        verify(transfer, never()).moveToDepositReceiver();
        verify(transfer, never()).startCompensation();
    }

    @Test
    void executeStep_depositReceiverFails_startsCompensation() {
        transfer.moveToDepositReceiver();
        doThrow(new RuntimeException("Deposit error"))
                .when(transferService).depositReceiver(any(Transfer.class));

        stepExecutor.executeStep(transfer);

        verify(transferService).depositReceiver(transfer);
        verify(transfer).failed();
        verify(transfer).needCompensation();
        verify(transfer).startCompensation();
    }
}
