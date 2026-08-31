package com.example.bankapp.accounts.controller.dto.response;

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
