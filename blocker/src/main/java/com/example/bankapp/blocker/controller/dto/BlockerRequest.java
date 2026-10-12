package com.example.bankapp.blocker.controller.dto;

import jakarta.validation.constraints.NotNull;

public record BlockerRequest(
        @NotNull Long accountId
) {
}
