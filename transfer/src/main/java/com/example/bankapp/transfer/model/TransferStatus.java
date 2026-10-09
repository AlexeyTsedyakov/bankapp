package com.example.bankapp.transfer.model;

public enum TransferStatus {
    NEW,
    PROCESSING,
    COMPLETED,
    REFUNDED,
    REJECTED,
    FAILED,
    RETRY_REQUIRED
}
