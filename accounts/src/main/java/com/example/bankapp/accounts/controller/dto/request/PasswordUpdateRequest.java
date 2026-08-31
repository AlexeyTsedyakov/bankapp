package com.example.bankapp.accounts.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordUpdateRequest(
        @NotBlank @Size(min = 8, max = 255) String newPassword
) {
}
