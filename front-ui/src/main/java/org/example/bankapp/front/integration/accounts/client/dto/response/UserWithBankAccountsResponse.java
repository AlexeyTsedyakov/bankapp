package org.example.bankapp.front.integration.accounts.client.dto.response;

import java.util.List;

public record UserWithBankAccountsResponse(
        UserResponse user,
        List<BankAccountResponse> bankAccounts
) {
}
