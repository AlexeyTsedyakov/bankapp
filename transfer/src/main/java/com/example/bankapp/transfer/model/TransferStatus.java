package com.example.bankapp.transfer.model;

public enum TransferStatus {
    NEW,
    PROCESSING,
    COMPLETED,
    REFUNDED,
    BLOCKED,
    FAILED,
    RETRY_REQUIRED
}
