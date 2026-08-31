package com.example.bankapp.accounts.controller.dto.response;

import java.util.List;

public record UserWithBankAccountsResponse(
        UserResponse user,
        List<BankAccountResponse> bankAccounts
) {
}
