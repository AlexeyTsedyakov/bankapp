package com.example.bankapp.cash.exceptions;

public class ResourceNotFoundException extends IntegrationException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
