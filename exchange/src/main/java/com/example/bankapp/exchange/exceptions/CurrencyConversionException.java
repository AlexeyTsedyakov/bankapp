package com.example.bankapp.exchange.exceptions;

public class CurrencyConversionException extends RuntimeException {
    public CurrencyConversionException(String message) {
        super(message);
    }
}
