package com.example.bankapp.cash.exceptions;

public class ServiceUnavailableException extends IntegrationException {
    public ServiceUnavailableException(String message) {
        super(message);
    }
}
