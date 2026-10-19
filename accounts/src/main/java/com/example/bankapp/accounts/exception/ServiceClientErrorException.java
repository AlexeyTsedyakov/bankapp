package com.example.bankapp.accounts.exception;

public class ServiceClientErrorException extends IntegrationException {
    public ServiceClientErrorException(String message) {
        super(message);
    }
}
