package com.example.bankapp.cash.service;

import com.example.bankapp.cash.exceptions.BlockerException;
import com.example.bankapp.cash.exceptions.IdempotencyKeyException;
import com.example.bankapp.cash.exceptions.InvalidAmountException;
import com.example.bankapp.cash.integration.accounts.AccountsGateway;
import com.example.bankapp.cash.integration.accounts.dto.response.BankAccountResponse;
import com.example.bankapp.cash.integration.blocker.BlockerGateway;
import com.example.bankapp.cash.service.event.DepositCompleted;
import com.example.bankapp.cash.service.event.WithdrawCompleted;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CashService {
    private final AccountsGateway accountsGateway;
    private final BlockerGateway blockerGateway;
    private final ApplicationEventPublisher eventPublisher;

    public BankAccountResponse deposit(Long accountId, BigDecimal amount, UUID idempotencyKey) {
        validateNonNegative(amount, "Сумма пополнения должна быть больше 0");
        validateIdempotencyKey(idempotencyKey);
        validateSuspiciousOperation(accountId);

        BankAccountResponse response = accountsGateway.deposit(accountId, amount, idempotencyKey);
        eventPublisher.publishEvent(new DepositCompleted(response, amount));

        return response;
    }

    public BankAccountResponse withdraw(Long accountId, BigDecimal amount, UUID idempotencyKey) {
        validateNonNegative(amount, "Сумма снятия должна быть больше 0");
        validateIdempotencyKey(idempotencyKey);
        validateSuspiciousOperation(accountId);

        BankAccountResponse response = accountsGateway.withdraw(accountId, amount, idempotencyKey);
        eventPublisher.publishEvent(new WithdrawCompleted(response, amount));

        return response;
    }

    private void validateNonNegative(BigDecimal value, String errMessage) {
        if (value.signum() < 0) {
            throw new InvalidAmountException(errMessage);
        }
    }

    private void validateIdempotencyKey(UUID idempotencyKey) {
        if (idempotencyKey == null) {
            throw new IdempotencyKeyException("Ключ идемпотентности не может быть null");
        }
    }

    private void validateSuspiciousOperation(Long accountId) {
        if (blockerGateway.checkAccountOperation(accountId).block()) {
            throw new BlockerException("Подозрительная операция: заблокировано");
        }
    }
}
