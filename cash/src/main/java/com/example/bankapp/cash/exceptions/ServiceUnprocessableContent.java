package com.example.bankapp.cash.exceptions;

public class ServiceUnprocessableContent extends IntegrationException {
    public ServiceUnprocessableContent(String message) {
        super(message);
    }
}
