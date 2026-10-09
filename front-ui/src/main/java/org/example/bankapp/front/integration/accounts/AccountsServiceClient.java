package org.example.bankapp.front.integration.accounts;

import org.example.bankapp.front.integration.accounts.dto.request.BankAccountCreateRequest;
import org.example.bankapp.front.integration.accounts.dto.request.PasswordUpdateRequest;
import org.example.bankapp.front.integration.accounts.dto.request.UserRegisterRequest;
import org.example.bankapp.front.integration.accounts.dto.request.UserUpdateRequest;
import org.example.bankapp.front.integration.accounts.dto.response.BankAccountResponse;
import org.example.bankapp.front.integration.accounts.dto.response.UserCredentialsResponse;
import org.example.bankapp.front.integration.accounts.dto.response.UserResponse;
import org.springframework.security.oauth2.client.annotation.ClientRegistrationId;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.*;

import java.util.List;

@SuppressWarnings("UnusedReturnValue")
@HttpExchange
@ClientRegistrationId("oauth2-service-client")
public interface AccountsServiceClient {

    @PostExchange("/users")
    UserResponse registerUser(@RequestBody UserRegisterRequest request);

    @GetExchange("/users/{id}")
    UserResponse getUser(@PathVariable Long id);

    @PutExchange("/users/{id}")
    UserResponse updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest request);

    @GetExchange("/users/{username}/credentials")
    UserCredentialsResponse getUserCredentials(@PathVariable String username);

    @PatchExchange("/users/{id}/password")
    void updateUserPassword(@PathVariable Long id, @RequestBody PasswordUpdateRequest request);

    @DeleteExchange("/users/{id}")
    void deleteUser(@PathVariable Long id);

    @PostExchange("/bank-accounts")
    BankAccountResponse createBankAccount(@RequestBody BankAccountCreateRequest request);

    @GetExchange("/bank-accounts")
    BankAccountResponse getBankAccountByNumber(@RequestParam String number);

    @GetExchange("/bank-accounts")
    List<BankAccountResponse> getUserBankAccounts(@RequestParam Long userId);

    @DeleteExchange("/bank-accounts/{id}")
    void deleteBankAccount(@PathVariable Long id);
}
