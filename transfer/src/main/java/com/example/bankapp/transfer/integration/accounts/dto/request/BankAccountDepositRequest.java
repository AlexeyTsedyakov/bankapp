package com.example.bankapp.transfer.integration.accounts.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record BankAccountDepositRequest(
        @NotNull @Positive @Digits(integer = 12, fraction = 2) BigDecimal amount
) {
}
