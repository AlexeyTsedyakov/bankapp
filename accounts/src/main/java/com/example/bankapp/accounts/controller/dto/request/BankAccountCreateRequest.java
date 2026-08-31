package com.example.bankapp.accounts.controller.dto.request;

import com.example.bankapp.accounts.model.Currency;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record BankAccountCreateRequest(
        @NotNull Long userId,
        @NotNull Currency currency
) {
}
