package com.example.bankapp.cash.exceptions;

public class ServiceServerErrorException extends IntegrationException {
    public ServiceServerErrorException(String message) {
        super(message);
    }
}
