package com.example.bankapp.accounts.service;

import com.example.bankapp.accounts.exception.EntityAlreadyExistsException;
import com.example.bankapp.accounts.exception.EntityNotFoundException;
import com.example.bankapp.accounts.exception.InvalidAmountException;
import com.example.bankapp.accounts.exception.NotEnoughBalanceException;
import com.example.bankapp.accounts.mapper.BankAccountMapper;
import com.example.bankapp.accounts.model.BankAccount;
import com.example.bankapp.accounts.model.User;
import com.example.bankapp.accounts.repository.BankAccountRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Sort;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@RequiredArgsConstructor
@Service
public class BankAccountService {
    private final BankAccountRepository repository;
    private final BankAccountMapper mapper;

    @Transactional
    public BankAccount create(BankAccount entity) {
        if (repository.existsByUserAndCurrency(entity.getUser(), entity.getCurrency())) {
            throw new EntityAlreadyExistsException("У пользователя уже есть счет в данной валюте");
        }

        entity.setAccountNumber(generateAccountNumber());
        entity.setBalance(new BigDecimal("0.00"));

        return repository.save(entity);
    }

    private String generateAccountNumber() {
        Long nextNumber = repository.getNextAccountNumber();
        return "%010d".formatted(nextNumber);
    }

    @Transactional(readOnly = true)
    public BankAccount findById(Long id) {
        return findByIdOrThrow(id);
    }

    @Transactional(readOnly = true)
    public List<BankAccount> findAllByUserId(Long userId) {
        AggregateReference<User, Long> user = AggregateReference.to(userId);
        Sort sort = Sort.by(Sort.Direction.ASC, "accountNumber");

        return repository.findAllByUser(user, sort);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Transactional
    public BankAccount deposit(Long id, BigDecimal amount) {
        if (amount == null || amount.signum() < 0) {
            throw new InvalidAmountException("Сумма пополнения должна быть больше 0");
        }

        BankAccount account = findByIdOrThrow(id);
        BigDecimal newBalance = account.getBalance()
                .add(amount)
                .setScale(2, RoundingMode.HALF_UP);
        account.setBalance(newBalance);

        return repository.save(account);
    }

    @Transactional
    public BankAccount withdraw(Long id, BigDecimal amount) {
        if (amount == null || amount.signum() < 0) {
            throw new InvalidAmountException("Сумма снятия должна быть больше 0");
        }

        BankAccount account = findByIdOrThrow(id);
        if (account.getBalance().compareTo(amount) < 0) {
            throw new NotEnoughBalanceException("Не достаточно средств на балансе");
        }

        BigDecimal newBalance = account.getBalance()
                .subtract(amount)
                .setScale(2, RoundingMode.HALF_UP);
        account.setBalance(newBalance);

        return repository.save(account);
    }

    private @NonNull BankAccount findByIdOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Банковский счет на найден, id: " + id));
    }
}
