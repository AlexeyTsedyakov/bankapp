package com.example.bankapp.transfer.exceptions;

public class ServiceServerErrorException extends IntegrationException {
    public ServiceServerErrorException(String message) {
        super(message);
    }
}
