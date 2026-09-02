package org.example.bankapp.front.integration.accounts.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordUpdateRequest(
        @NotBlank @Size(min = 8, max = 255) String newPassword
) {
    public PasswordUpdateRequest() {
        this(null);
    }
}
