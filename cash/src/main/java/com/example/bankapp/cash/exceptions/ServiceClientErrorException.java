package com.example.bankapp.cash.exceptions;

public class ServiceClientErrorException extends IntegrationException {
    public ServiceClientErrorException(String message) {
        super(message);
    }
}
