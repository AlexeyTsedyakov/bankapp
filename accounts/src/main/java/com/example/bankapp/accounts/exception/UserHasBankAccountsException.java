package com.example.bankapp.accounts.exception;

public class UserHasBankAccountsException extends RuntimeException {
    public UserHasBankAccountsException(String message) {
        super(message);
    }
}
