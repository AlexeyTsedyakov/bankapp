package org.example.bankapp.front.accounts.service;

import org.example.bankapp.front.integration.accounts.client.dto.request.UserRegisterRequest;
import org.example.bankapp.front.integration.accounts.exceptions.AccountsServiceException;
import org.example.bankapp.front.integration.accounts.service.AccountsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class AccountsServiceIntegrationTest {
    @Autowired
    private AccountsService accountsService;

    @Test
    void registerUser_registerRequestWithBadEmail_throwsException() {
        var registerDto = UserRegisterRequest.builder()
                .username("username")
                .password("password")
                .firstName("Alice")
                .lastName("Chain")
                .birthDate(LocalDate.of(2000, 1, 1))
                .email("email")
                .build();

        assertThatThrownBy(() -> accountsService.registerUser(registerDto))
                .isInstanceOf(AccountsServiceException.class);
    }

    @Test
    void getUserWithBankAccount_nonExistingId_throwException() {
        assertThatThrownBy(() -> accountsService.getUserWithBankAccounts(9999L))
                .isInstanceOf(AccountsServiceException.class);
    }
}