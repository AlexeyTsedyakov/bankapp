package com.example.bankapp.transfer.exceptions;

public class ServiceUnprocessableContent extends IntegrationException {
    public ServiceUnprocessableContent(String message) {
        super(message);
    }
}
