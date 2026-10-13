package com.example.bankapp.transfer.saga;

import com.example.bankapp.transfer.exceptions.ServiceUnavailableException;
import com.example.bankapp.transfer.exceptions.TransferException;
import com.example.bankapp.transfer.model.Transfer;
import com.example.bankapp.transfer.model.TransferStep;
import com.example.bankapp.transfer.service.TransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
class SagaStepExecutor {
    private final TransferService transferService;

    public void executeStep(Transfer transfer) {
        if (transfer == null) {
            throw new TransferException("Отсутствуют данные для перевода");
        }

        TransferStep step = transfer.getStep();
        try {
            switch (step) {
                case CHECK_BLOCKER -> {
                    if (transferService.shouldBlock(transfer)) {
                        transfer.block();
                    } else {
                        transfer.moveToCalculateAmount();
                    }
                }
                case CALCULATE_AMOUNT -> {
                    transfer.applyTargetAmountCalculation(transferService.calculateTargetAmount(transfer));
                    transfer.moveToWithdrawSender();
                }
                case WITHDRAW_SENDER -> {
                    transferService.withdrawSender(transfer);
                    transfer.moveToDepositReceiver();
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
}

