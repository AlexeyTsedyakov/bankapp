package com.example.bankapp.transfer.exceptions;

public class ResourceNotFoundException extends IntegrationException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
