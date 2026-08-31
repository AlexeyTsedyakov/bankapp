package org.example.bankapp.front.integration.accounts.client.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.example.bankapp.front.common.model.Currency;

@Builder
public record BankAccountCreateRequest(
        @NotNull Long userId,
        @NotNull Currency currency
) {
}
