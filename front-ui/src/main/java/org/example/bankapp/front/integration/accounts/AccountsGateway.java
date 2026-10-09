package org.example.bankapp.front.integration.accounts;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bankapp.front.common.model.Currency;
import org.example.bankapp.front.integration.accounts.dto.request.BankAccountCreateRequest;
import org.example.bankapp.front.integration.accounts.dto.request.PasswordUpdateRequest;
import org.example.bankapp.front.integration.accounts.dto.request.UserRegisterRequest;
import org.example.bankapp.front.integration.accounts.dto.request.UserUpdateRequest;
import org.example.bankapp.front.integration.accounts.dto.response.BankAccountResponse;
import org.example.bankapp.front.integration.accounts.dto.response.UserResponse;
import org.example.bankapp.front.common.exceptions.IntegrationException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Component
public class AccountsGateway {
    private final AccountsServiceClient client;

    public void registerUser(UserRegisterRequest registerRequest) {
        try {
            client.registerUser(registerRequest);
        } catch (HttpClientErrorException.Conflict e) {
            throw new IntegrationException("Пользователь с таким логином уже существует");
        } catch (RuntimeException e) {
            handleHttpError(e);
            throw e;
        }
    }

    public UserResponse getUser(Long userId) {
        try {
            return client.getUser(userId);
        } catch (RuntimeException e) {
            handleHttpError(e);
            throw e;
        }
    }

    public void updateUser(Long id, UserUpdateRequest updateRequest) {
        try {
            client.updateUser(id, updateRequest);
        } catch (RuntimeException e) {
            handleHttpError(e);
            throw e;
        }
    }

    public void updateUserPassword(Long id, PasswordUpdateRequest request) {
        try {
            client.updateUserPassword(id, request);
        } catch (RuntimeException e) {
            handleHttpError(e);
            throw e;
        }
    }

    public void deleteUser(Long id) {
        try {
            client.deleteUser(id);
        } catch (HttpClientErrorException.Conflict e) {
            throw new IntegrationException("Нельзя удалить аккаунт, пока есть активные счета");
        } catch (RuntimeException e) {
            handleHttpError(e);
            throw e;
        }
    }

    public Set<Currency> getAvailableCurrencies(List<BankAccountResponse> userWithBankAccounts) {
        Set<Currency> availableCurrencies = EnumSet.allOf(Currency.class);
        for (BankAccountResponse bankAccount : userWithBankAccounts) {
            availableCurrencies.remove(bankAccount.currency());
        }

        return availableCurrencies;
    }

    public void createBankAccount(Long userId, Currency currency) {
        var request = new BankAccountCreateRequest(userId, currency);

        try {
            client.createBankAccount(request);
        } catch (HttpClientErrorException.Conflict e) {
            throw new IntegrationException("Счет в данной валюте уже существует");
        } catch (RuntimeException e) {
            handleHttpError(e);
            throw e;
        }
    }

    public BankAccountResponse getBankAccountByNumber(String number) {
        try {
            return client.getBankAccountByNumber(number);
        } catch (HttpClientErrorException.NotFound e) {
            throw new IntegrationException("Банковский счет не найден");
        } catch (RuntimeException e) {
            handleHttpError(e);
            throw e;
        }
    }

    public List<BankAccountResponse> getUserBankAccounts(Long userId) {
        try {
            return client.getUserBankAccounts(userId);
        } catch (RuntimeException e) {
            handleHttpError(e);
            throw e;
        }
    }

    public void deleteBankAccount(Long bankAccountId) {
        try {
            client.deleteBankAccount(bankAccountId);
        } catch (RuntimeException e) {
            handleHttpError(e);
            throw e;
        }
    }

    private void handleHttpError(Exception e) {
        log.error("Error executing request to the `Account service`: {}", e.getMessage());

        if (e instanceof HttpStatusCodeException) {
            throw new IntegrationException("Ошибка выполнения запроса к сервису аккаунтов");
        } else if (e instanceof ResourceAccessException) {
            throw new IntegrationException("Служба аккаунтов не доступна");
        }
    }
}
