package com.example.bankapp.exchange.exceptions;

public class InvalidRateException extends RuntimeException {
    public InvalidRateException(String message) {
        super(message);
    }
}
