package com.example.bankapp.transfer.service;

import com.example.bankapp.transfer.exceptions.SelfTransferNotAllowedException;
import com.example.bankapp.transfer.exceptions.ServiceUnavailableException;
import com.example.bankapp.transfer.exceptions.TransferException;
import com.example.bankapp.transfer.model.Transfer;
import com.example.bankapp.transfer.model.TransferStatus;
import com.example.bankapp.transfer.model.TransferStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class TransferSagaOrchestrator {
    private final TransferService transferService;

    @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.SECONDS)
    @SchedulerLock(name = "transfer-recovery", lockAtMostFor = "5m", lockAtLeastFor = "10s")
    public void recover() {
        List<Transfer> tasks = transferService.getRetryRequired();
        tasks.forEach(this::process);
    }

    public void startTransfer(Transfer transfer) {
        if (Objects.equals(transfer.getFromAccountId(), transfer.getToAccountId())) {
            throw new SelfTransferNotAllowedException("Счета отправителя и получателя должны быть разные");
        }

        transfer.start();
        Transfer saved = transferService.save(transfer);
        Thread.startVirtualThread(() -> process(saved));
    }

    private void process(Transfer transfer) {
        if (transfer.getStatus() == TransferStatus.RETRY_REQUIRED) {
            transfer.resume();
        }

        while (transfer.isActive()) {
            executeStep(transfer);
            saveState(transfer);
        }
    }

    private void executeStep(Transfer transfer) {
        TransferStep step = transfer.getStep();
        try {
            switch (step) {
                case CHECK_BLOCKER -> {
                    if (transferService.shouldBlock(transfer)) {
                        transfer.block();
                    } else {
                        transfer.blockerChecked();
                    }
                }
                case CALCULATE_AMOUNT -> {
                    transfer.applyTargetAmountCalculation(transferService.calculateTargetAmount(transfer));
                    transfer.amountCalculated();
                }
                case WITHDRAW_SENDER -> {
                    transferService.withdrawSender(transfer);
                    transfer.senderWithdrawn();
                }
                case DEPOSIT_RECEIVER -> {
                    transferService.depositReceiver(transfer);
                    transfer.complete();
                }
                case REFUND_WITHDRAW -> {
                    transferService.refundWithdraw(transfer);
                    transfer.refunded();
                }
                case null -> throw new TransferException("Отсутствует этап перевода");
                default -> throw new TransferException("Этап не поддерживается: " + step.name());
            }
        } catch (ServiceUnavailableException e) {
            transfer.retryRequired();
        } catch (Exception e) {
            log.error("Перевод не удался: {}", e.getMessage(), e);
            transfer.failed();
        }

        if (transfer.needCompensation()) {
            transfer.startCompensation();
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
            log.error("Не удалось сохранить перевод, id={}", transfer.getId(), e);
        }
    }
}
