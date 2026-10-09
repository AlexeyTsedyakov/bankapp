package com.example.bankapp.transfer.exceptions;

public class ServiceUnavailableException extends IntegrationException {
    public ServiceUnavailableException(String message) {
        super(message);
    }
}
