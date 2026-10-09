package com.example.bankapp.transfer.service;

import com.example.bankapp.transfer.exceptions.SelfTransferNotAllowedException;
import com.example.bankapp.transfer.exceptions.ServiceUnavailableException;
import com.example.bankapp.transfer.model.Transfer;
import com.example.bankapp.transfer.model.TransferStatus;
import com.example.bankapp.transfer.model.TransferStep;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferSagaOrchestratorTest {
    @Mock
    private TransferService transferService;
    @InjectMocks
    private TransferSagaOrchestrator orchestrator;

    @Test
    void startTransfer_WhenSelfTransfer_ThrowsException() {
        Transfer transfer = mock(Transfer.class);
        given(transfer.getFromAccountId()).willReturn(100L);
        given(transfer.getToAccountId()).willReturn(100L);

        assertThatThrownBy(() -> orchestrator.startTransfer(transfer))
                .isInstanceOf(SelfTransferNotAllowedException.class)
                .hasMessage("Счета отправителя и получателя должны быть разные");

        verify(transfer, never()).start();
        verify(transferService, never()).save(any());
    }

    @Test
    void startTransfer_WhenAccountsAreDifferent_StartsAndSavesTransfer() {
        Transfer transfer = mock(Transfer.class);
        given(transfer.getFromAccountId()).willReturn(100L);
        given(transfer.getToAccountId()).willReturn(200L);
        given(transferService.save(transfer)).willReturn(transfer);

        orchestrator.startTransfer(transfer);

        verify(transfer).start();
        verify(transferService).save(transfer);
    }

    @Test
    void recover_WhenTasksFound_ProcessesAllTasks() {
        Transfer task = mock(Transfer.class);
        given(task.getStatus()).willReturn(TransferStatus.RETRY_REQUIRED);
        // Симулируем один шаг цикла: сначала активен, затем завершен
        given(task.isActive()).willReturn(true, false);
        given(task.getStep()).willReturn(TransferStep.CHECK_BLOCKER);
        given(transferService.getRetryRequired()).willReturn(List.of(task));

        orchestrator.recover();

        verify(transferService).getRetryRequired();
        verify(task).resume();
        verify(task).blockerChecked();
        verify(transferService, atLeastOnce()).save(task);
    }

    @Test
    void recover_WhenServiceUnavailable_SetsRetryRequired() {
        Transfer task = mock(Transfer.class);
        given(task.getStatus()).willReturn(TransferStatus.RETRY_REQUIRED);
        given(task.isActive()).willReturn(true, false);
        given(task.getStep()).willReturn(TransferStep.WITHDRAW_SENDER);

        doThrow(new ServiceUnavailableException("Временная ошибка сервиса"))
                .when(transferService).withdrawSender(task);

        given(transferService.getRetryRequired()).willReturn(List.of(task));

        orchestrator.recover();

        verify(task).retryRequired();
        verify(task, never()).senderWithdrawn();
        verify(transferService, atLeastOnce()).save(task);
    }

    @Test
    void recover_WhenStepDepositReceiver_CompletesAndSavesNotify() {
        Transfer task = mock(Transfer.class);
        given(task.getStatus())
                .willReturn(TransferStatus.COMPLETED);
        given(task.isActive()).willReturn(true, false);
        given(task.getStep()).willReturn(TransferStep.DEPOSIT_RECEIVER);
        given(transferService.getRetryRequired()).willReturn(List.of(task));

        orchestrator.recover();

        verify(transferService).depositReceiver(task);
        verify(task).complete();
        verify(transferService).saveCompletedAndNotify(task);
    }
}