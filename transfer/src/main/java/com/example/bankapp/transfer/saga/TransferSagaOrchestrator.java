package com.example.bankapp.transfer.saga;

import com.example.bankapp.transfer.exceptions.SelfTransferNotAllowedException;
import com.example.bankapp.transfer.model.Transfer;
import com.example.bankapp.transfer.model.TransferStatus;
import com.example.bankapp.transfer.service.TransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Component
public class TransferSagaOrchestrator {
    private final TransferService transferService;
    private final SagaStepExecutor stepExecutor;
    private final Executor transferExecutor;

    @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.SECONDS)
    @SchedulerLock(name = "transfer-recovery", lockAtMostFor = "5m", lockAtLeastFor = "10s")
    public void recover() {
        List<Transfer> tasks = transferService.getRetryRequired();
        tasks.forEach(this::process);
    }

    public void startTransfer(Transfer transfer) {
        if (Objects.equals(transfer.getFrom().accountId(), transfer.getTo().accountId())) {
            throw new SelfTransferNotAllowedException("Счета отправителя и получателя должны быть разные");
        }

        transfer.start();
        Transfer saved = transferService.save(transfer);
        transferExecutor.execute(() -> process(saved));
    }

    private void process(Transfer transfer) {
        if (transfer.getStatus() == TransferStatus.RETRY_REQUIRED) {
            transfer.resume();
        }

        while (transfer.isActive()) {
            stepExecutor.executeStep(transfer);
            saveState(transfer);
        }
    }

    private void saveState(Transfer transfer) {
        try {
            if (transfer.getStatus() == TransferStatus.COMPLETED) {
                transferService.saveCompletedAndNotify(transfer);
            } else {
                transferService.save(transfer);
            }
        } catch (Exception e) {
            log.error("Не удалось сохранить перевод, accountId={}", transfer.getId(), e);
        }
    }
}
