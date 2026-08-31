package com.example.bankapp.accounts.service;

import com.example.bankapp.accounts.exception.EntityAlreadyExistsException;
import com.example.bankapp.accounts.exception.EntityNotFoundException;
import com.example.bankapp.accounts.model.BankAccount;
import com.example.bankapp.accounts.repository.BankAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
@Service
public class BankAccountService {
    private final BankAccountRepository repository;

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

    @Transactional
    public BankAccount findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Банковский счет на найден, id: " + id));
    }

    @Transactional(readOnly = true)
    public List<BankAccount> findAllByUserId(Long userId) {
        return repository.findAllByUser(AggregateReference.to(userId));
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
