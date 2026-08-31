package org.example.bankapp.front.integration.accounts.client.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.example.bankapp.front.integration.accounts.client.validation.MinAge;

import java.time.LocalDate;

@Builder
public record UserRegisterRequest(
        @NotBlank @Size(min = 4, max = 255) String username,
        @NotBlank @Size(min = 8, max = 255) String password,
        @NotBlank @Size(min = 2, max = 255) String firstName,
        @NotBlank @Size(min = 2, max = 255) String lastName,
        @NotNull @MinAge LocalDate birthDate,
        @Email String email
) {
    public UserRegisterRequest() {
        this(null, null, null, null, null, null);
    }
}
