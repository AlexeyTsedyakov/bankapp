package com.example.bankapp.transfer.saga;

import com.example.bankapp.transfer.config.ExecutorConfigTest;
import com.example.bankapp.transfer.exceptions.SelfTransferNotAllowedException;
import com.example.bankapp.transfer.model.Transfer;
import com.example.bankapp.transfer.model.TransferStatus;
import com.example.bankapp.transfer.model.TransferStep;
import com.example.bankapp.transfer.service.TransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {TransferSagaOrchestrator.class, ExecutorConfigTest.class})
@ActiveProfiles("test")
class TransferSagaOrchestratorTest {
    @Autowired
    private TransferSagaOrchestrator orchestrator;
    @MockitoBean
    private TransferService transferService;
    @MockitoBean
    private SagaStepExecutor stepExecutor;

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
                .status(TransferStatus.NEW)
                .step(TransferStep.CHECK_BLOCKER)
                .build();
        transfer = spy(realTransfer);
    }

    @Test
    void startTransfer_whenSelfTransfer_throwsSelfTransferNotAllowedException() {
        Transfer selfTransfer = spy(Transfer.builder()
                .fromAccountId(1L)
                .toAccountId(1L)
                .build());

        assertThatThrownBy(() -> orchestrator.startTransfer(selfTransfer))
                .isInstanceOf(SelfTransferNotAllowedException.class)
                .hasMessage("Счета отправителя и получателя должны быть разные");

        verify(selfTransfer, never()).start();
        verifyNoInteractions(transferService);
        verifyNoInteractions(stepExecutor);
    }

    @Test
    void startTransfer_validTransfer_startsSavesAndExecutesProcess() {
        when(transferService.save(transfer)).thenReturn(transfer);
        doAnswer(_ -> {
            transfer.complete();
            return null;
        }).when(stepExecutor).executeStep(transfer);


        orchestrator.startTransfer(transfer);

        verify(transfer).start();
        verify(transferService).save(transfer);
        verify(stepExecutor).executeStep(transfer);
        verify(transferService).saveCompletedAndNotify(transfer);
    }

    @Test
    void recover_whenNoRetryRequiredTasks_doesNothing() {
        when(transferService.getRetryRequired()).thenReturn(List.of());

        orchestrator.recover();

        verify(transferService).getRetryRequired();
        verifyNoInteractions(stepExecutor);
        verify(transferService, never()).save(any());
        verify(transferService, never()).saveCompletedAndNotify(any());
    }

    @Test
    void recover_whenTasksWithRetryRequiredStatus_resumesAndProcessesUntilComplete() {
        Transfer task = spy(Transfer.builder()
                .id(2L)
                .idempotencyKey(UUID.randomUUID())
                .fromAccountId(10L)
                .toAccountId(20L)
                .sourceAmount(new BigDecimal("500.00"))
                .status(TransferStatus.RETRY_REQUIRED)
                .step(TransferStep.WITHDRAW_SENDER)
                .build());

        when(transferService.getRetryRequired()).thenReturn(List.of(task));
        doAnswer(_ -> {
            task.complete();
            return null;
        }).when(stepExecutor).executeStep(task);

        orchestrator.recover();

        verify(task).resume();
        assertThat(task.getStatus()).isEqualTo(TransferStatus.COMPLETED);
        verify(stepExecutor).executeStep(task);
        verify(transferService).saveCompletedAndNotify(task);
    }

    @Test
    void recover_whenMultipleStepsNeeded_executesStepsAndSavesStateEachIteration() {
        Transfer task = spy(Transfer.builder()
                .id(3L)
                .idempotencyKey(UUID.randomUUID())
                .fromAccountId(10L)
                .toAccountId(20L)
                .sourceAmount(new BigDecimal("200.00"))
                .status(TransferStatus.PROCESSING)
                .step(TransferStep.CHECK_BLOCKER)
                .build());

        when(transferService.getRetryRequired()).thenReturn(List.of(task));

        doAnswer(_ -> {
            task.moveToCalculateAmount();
            return null;
        }).doAnswer(_ -> {
            task.complete();
            return null;
        }).when(stepExecutor).executeStep(task);

        orchestrator.recover();

        verify(stepExecutor, times(2)).executeStep(task);
        verify(transferService, times(1)).save(task);
        verify(transferService, times(1)).saveCompletedAndNotify(task);
    }
}