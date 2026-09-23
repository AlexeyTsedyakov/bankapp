package org.example.bankapp.front.integration.cash;

import org.example.bankapp.front.integration.accounts.dto.response.BankAccountResponse;
import org.example.bankapp.front.integration.cash.dto.CashDepositRequest;
import org.example.bankapp.front.integration.cash.dto.CashWithdrawRequest;
import org.springframework.security.oauth2.client.annotation.ClientRegistrationId;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@SuppressWarnings("UnusedReturnValue")
@HttpExchange("/cash")
@ClientRegistrationId("oauth2-service-client")
public interface CashServiceClient {

    @PostExchange("/deposit")
    BankAccountResponse deposit(@RequestBody CashDepositRequest request);

    @PostExchange("/withdraw")
    BankAccountResponse withdraw(@RequestBody CashWithdrawRequest request);
}
