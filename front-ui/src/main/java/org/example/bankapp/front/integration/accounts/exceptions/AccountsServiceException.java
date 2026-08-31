package org.example.bankapp.front.integration.accounts.exceptions;

public class AccountsServiceException extends RuntimeException {
    public AccountsServiceException(String message) {
        super(message);
    }

    public AccountsServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
