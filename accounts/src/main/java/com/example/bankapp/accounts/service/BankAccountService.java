package com.example.bankapp.accounts.service;

import com.example.bankapp.accounts.exception.EntityAlreadyExistsException;
import com.example.bankapp.accounts.exception.EntityNotFoundException;
import com.example.bankapp.accounts.exception.IdempotencyKeyException;
import com.example.bankapp.accounts.model.BankAccount;
import com.example.bankapp.accounts.model.BankAccountOperation;
import com.example.bankapp.accounts.model.OperationType;
import com.example.bankapp.accounts.model.User;
import com.example.bankapp.accounts.repository.BankAccountOperationRepository;
import com.example.bankapp.accounts.repository.BankAccountRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Sort;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class BankAccountService {
    private final BankAccountRepository accountRepository;
    private final BankAccountOperationRepository operationRepository;

    @Transactional
    public BankAccount create(BankAccount entity) {
        if (accountRepository.existsByUserAndCurrency(entity.getUser(), entity.getCurrency())) {
            throw new EntityAlreadyExistsException("У пользователя уже есть счет в данной валюте");
        }

        entity.setAccountNumber(generateAccountNumber());
        entity.setBalance(new BigDecimal("0.00"));

        return accountRepository.save(entity);
    }

    private String generateAccountNumber() {
        Long nextNumber = accountRepository.getNextAccountNumber();
        return "%010d".formatted(nextNumber);
    }

    @Transactional(readOnly = true)
    public BankAccount findById(Long id) {
        return findByIdOrThrow(id);
    }

    @Transactional(readOnly = true)
    public BankAccount findByNumber(String number) {
        return accountRepository.findByAccountNumber(number)
                .orElseThrow(() -> new EntityNotFoundException("Банковский аккаунт не найден, номер: " + number));
    }

    @Transactional(readOnly = true)
    public List<BankAccount> findAllByUserId(Long userId) {
        AggregateReference<User, Long> user = AggregateReference.to(userId);
        Sort sort = Sort.by(Sort.Direction.ASC, "accountNumber");

        return accountRepository.findAllByUser(user, sort);
    }

    @Transactional
    public void delete(Long id) {
        accountRepository.deleteById(id);
    }

    @Transactional
    public BankAccount deposit(Long id, BigDecimal amount, UUID idempotencyKey) {
        validateIdempotencyKey(idempotencyKey);
        if (operationRepository.existsByIdempotencyKeyAndOperationType(
                idempotencyKey, OperationType.DEPOSIT)) {
            return findByIdOrThrow(id);
        }

        BankAccount account = findByIdOrThrow(id);
        account.deposit(amount);
        account = accountRepository.save(account);
        recordOperation(account, idempotencyKey, OperationType.DEPOSIT, amount);

        return account;
    }

    @Transactional
    public BankAccount withdraw(Long id, BigDecimal amount, UUID idempotencyKey) {
        validateIdempotencyKey(idempotencyKey);
        if (operationRepository.existsByIdempotencyKeyAndOperationType(
                idempotencyKey, OperationType.WITHDRAW)) {
            return findByIdOrThrow(id);
        }

        BankAccount account = findByIdOrThrow(id);
        account.withdraw(amount);
        account = accountRepository.save(account);
        recordOperation(account, idempotencyKey, OperationType.WITHDRAW, amount);

        return account;
    }

    private @NonNull BankAccount findByIdOrThrow(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Банковский счет на найден, id: " + id));
    }

    private void validateIdempotencyKey(UUID idempotencyKey) {
        if (idempotencyKey == null) {
            throw new IdempotencyKeyException("Ключ идемпотентности не может быть null");
        }
    }

    private void recordOperation(BankAccount account, UUID idempotencyKey, OperationType deposit, BigDecimal amount) {
        BankAccountOperation operation = BankAccountOperation.builder()
                .accountId(AggregateReference.to(account.getId()))
                .idempotencyKey(idempotencyKey)
                .operationType(deposit)
                .amount(amount)
                .build();
        operationRepository.save(operation);
    }
}
