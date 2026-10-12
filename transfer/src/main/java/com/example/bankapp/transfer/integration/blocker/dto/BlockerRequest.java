package com.example.bankapp.transfer.integration.blocker.dto;

import jakarta.validation.constraints.NotNull;

public record BlockerRequest(
        @NotNull Long accountId
) {
}
