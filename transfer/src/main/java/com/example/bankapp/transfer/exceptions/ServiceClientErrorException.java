package com.example.bankapp.transfer.exceptions;

public class ServiceClientErrorException extends IntegrationException {
    public ServiceClientErrorException(String message) {
        super(message);
    }
}
