package com.example.bankapp.accounts.controller.dto.response;

public record UserCredentialsResponse(
        Long id,
        String username,
        String password
) {
}
