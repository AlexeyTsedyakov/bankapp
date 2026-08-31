package com.example.bankapp.accounts.controller.dto.request;

import com.example.bankapp.accounts.controller.validation.MinAge;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record UserUpdateRequest(
        @NotBlank @Size(min = 2, max = 255) String firstName,
        @NotBlank @Size(min = 2, max = 255) String lastName,
        @NotNull @MinAge LocalDate birthDate,
        @Email String email
) {
}
