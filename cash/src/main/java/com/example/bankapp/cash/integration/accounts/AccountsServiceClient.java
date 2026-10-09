package com.example.bankapp.cash.integration.accounts;

import com.example.bankapp.cash.integration.accounts.dto.request.BankAccountDepositRequest;
import com.example.bankapp.cash.integration.accounts.dto.request.BankAccountWithdrawRequest;
import com.example.bankapp.cash.integration.accounts.dto.response.BankAccountResponse;
import org.springframework.security.oauth2.client.annotation.ClientRegistrationId;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.UUID;

@SuppressWarnings("UnusedReturnValue")
@HttpExchange
@ClientRegistrationId("oauth2-service-client")
public interface AccountsServiceClient {

    @PostExchange("/bank-accounts/{id}/deposit")
    BankAccountResponse deposit(@PathVariable Long id,
                                @RequestBody BankAccountDepositRequest request,
                                @RequestHeader(name = "Idempotency-Key") UUID idempotencyKey);

    @PostExchange("/bank-accounts/{id}/withdraw")
    BankAccountResponse withdraw(@PathVariable Long id,
                                 @RequestBody BankAccountWithdrawRequest request,
                                 @RequestHeader(name = "Idempotency-Key") UUID idempotencyKey);
}
