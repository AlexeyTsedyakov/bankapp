package org.example.bankapp.front.integration.accounts.client;

import org.example.bankapp.front.integration.accounts.client.dto.request.BankAccountCreateRequest;
import org.example.bankapp.front.integration.accounts.client.dto.request.PasswordUpdateRequest;
import org.example.bankapp.front.integration.accounts.client.dto.request.UserRegisterRequest;
import org.example.bankapp.front.integration.accounts.client.dto.request.UserUpdateRequest;
import org.example.bankapp.front.integration.accounts.client.dto.response.BankAccountResponse;
import org.example.bankapp.front.integration.accounts.client.dto.response.UserCredentialsResponse;
import org.example.bankapp.front.integration.accounts.client.dto.response.UserResponse;
import org.example.bankapp.front.integration.accounts.client.dto.response.UserWithBankAccountsResponse;
import org.springframework.security.oauth2.client.annotation.ClientRegistrationId;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.*;

@HttpExchange("/accounts")
@ClientRegistrationId("oauth2-service-client")
public interface AccountsServiceClient {

    @PostExchange("/users")
    UserResponse registerUser(@RequestBody UserRegisterRequest request);

    @PutExchange("/users/{id}")
    UserResponse updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest request);

    @GetExchange("/users/{id}")
    UserWithBankAccountsResponse getUserWithBankAccounts(@PathVariable Long id);

    @GetExchange("/users/{username}/credentials")
    UserCredentialsResponse getUserCredentials(@PathVariable String username);

    @PatchExchange("/users/{id}/password")
    void updateUserPassword(@PathVariable Long id, @RequestBody PasswordUpdateRequest request);

    @DeleteExchange("/users/{id}")
    void deleteUser(@PathVariable Long id);

    @PostExchange("/bank-accounts")
    BankAccountResponse createBankAccount(@RequestBody BankAccountCreateRequest request);

    @DeleteExchange("/bank-accounts/{id}")
    void deleteBankAccount(@PathVariable Long id);
}
