package org.example.bankapp.front.integration.accounts.client.dto.response;

public record UserCredentialsResponse(
        Long id,
        String username,
        String password
) {
}
