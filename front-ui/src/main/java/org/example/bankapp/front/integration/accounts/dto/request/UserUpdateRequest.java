package org.example.bankapp.front.integration.accounts.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.example.bankapp.front.integration.accounts.validation.MinAge;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Builder
public record UserUpdateRequest(
        @NotBlank @Size(min = 2, max = 255) String firstName,
        @NotBlank @Size(min = 2, max = 255) String lastName,
        @NotNull @MinAge @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthDate,
        @Email String email
) {
    public UserUpdateRequest() {
        this(null, null, null, null);
    }
}
