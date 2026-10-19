package com.example.bankapp.accounts.integration.notifications.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record NotifyRequest(
        @NotNull UUID idempotencyKey,
        @NotNull @Email String email,
        @NotNull @NotBlank String text
) {
}
