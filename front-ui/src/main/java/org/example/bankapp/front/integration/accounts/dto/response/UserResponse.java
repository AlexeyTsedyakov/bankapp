package org.example.bankapp.front.integration.accounts.dto.response;

import java.time.LocalDate;

public record UserResponse(
        Long id,
        String username,
        String firstName,
        String lastName,
        LocalDate birthDate,
        String email
) {
}
