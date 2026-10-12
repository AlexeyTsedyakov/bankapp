package com.example.bankapp.cash.integration.blocker.dto;

import jakarta.validation.constraints.NotNull;

public record BlockerRequest(
        @NotNull Long accountId
) {
}
