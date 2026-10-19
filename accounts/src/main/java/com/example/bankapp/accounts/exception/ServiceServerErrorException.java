package com.example.bankapp.accounts.exception;

public class ServiceServerErrorException extends IntegrationException {
    public ServiceServerErrorException(String message) {
        super(message);
    }
}
