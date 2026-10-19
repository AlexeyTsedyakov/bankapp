package com.example.bankapp.accounts.service;

import com.example.bankapp.accounts.exception.EntityAlreadyExistsException;
import com.example.bankapp.accounts.exception.EntityNotFoundException;
import com.example.bankapp.accounts.exception.UserHasBankAccountsException;
import com.example.bankapp.accounts.mapper.UserMapper;
import com.example.bankapp.accounts.model.BankAccount;
import com.example.bankapp.accounts.model.User;
import com.example.bankapp.accounts.repository.UserRepository;
import com.example.bankapp.accounts.service.event.UserCreated;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper mapper;
    private final BankAccountService bankAccountService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public User create(User user) {
        String username = user.getUsername();
        if (repository.existsByUsernameIgnoreCase(username)) {
            throw new EntityAlreadyExistsException("Пользователь с таким логином уже существует: " + username);
        }

        String passwordHash = passwordEncoder.encode(user.getPassword());
        user.setPassword(passwordHash);

        User saved = repository.save(user);
        eventPublisher.publishEvent(new UserCreated(saved));

        return user;
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return findByIdOrThrow(id);
    }

    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return repository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден, логин: " + username));
    }

    @Transactional
    public User update(User user) {
        User target = findByIdOrThrow(user.getId());
        mapper.patchEntity(user, target);

        return repository.save(target);
    }

    @Transactional
    public void updatePassword(Long id, String newPassword) {
        User user = findByIdOrThrow(id);
        user.setPassword(passwordEncoder.encode(newPassword));

        repository.save(user);
    }

    @Transactional
    public void delete(Long id) {
        List<BankAccount> bankAccounts = bankAccountService.findAllByUserId(id);
        if (!bankAccounts.isEmpty()) {
            throw new UserHasBankAccountsException("Невозможно удалить пользователя: есть активные банковские счета");
        }

        repository.deleteById(id);
    }

    private User findByIdOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден, id: " + id));
    }
}

