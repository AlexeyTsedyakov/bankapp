package com.example.bankapp.accounts.exception;

public class ServiceUnavailableException extends IntegrationException {
    public ServiceUnavailableException(String message) {
        super(message);
    }
}
