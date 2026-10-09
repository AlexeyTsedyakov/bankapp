package com.example.bankapp.transfer.exceptions;

public class SelfTransferNotAllowedException extends RuntimeException {
    public SelfTransferNotAllowedException(String message) {
        super(message);
    }
}
