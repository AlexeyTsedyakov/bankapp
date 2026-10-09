package com.example.bankapp.cash.service;

import com.example.bankapp.cash.exceptions.IdempotencyKeyException;
import com.example.bankapp.cash.exceptions.InvalidAmountException;
import com.example.bankapp.cash.integration.accounts.AccountsGateway;
import com.example.bankapp.cash.integration.accounts.dto.response.BankAccountResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CashService {
    private final AccountsGateway gateway;

    public BankAccountResponse deposit(Long accountId, BigDecimal amount, UUID idempotencyKey) {
        validateNonNegative(amount, "Сумма пополнения должна быть больше 0");
        validateIdempotencyKey(idempotencyKey);
        // TODO: Добавить антифрод и отправку уведомлений

        return gateway.deposit(accountId, amount, idempotencyKey);
    }

    public BankAccountResponse withdraw(Long accountId, BigDecimal amount, UUID idempotencyKey) {
        validateNonNegative(amount, "Сумма снятия должна быть больше 0");
        validateIdempotencyKey(idempotencyKey);
        // TODO: Добавить антифрод и отправку уведомлений

        return gateway.withdraw(accountId, amount, idempotencyKey);
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
}
