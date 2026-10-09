package com.example.bankapp.cash.exceptions;

public class IdempotencyKeyException extends RuntimeException {
    public IdempotencyKeyException(String message) {
        super(message);
    }
}
