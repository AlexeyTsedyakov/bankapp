package com.example.bankapp.transfer.model;

public enum TransferStep {
    CHECK_BLOCKER,
    CALCULATE_AMOUNT,
    WITHDRAW_SENDER,
    DEPOSIT_RECEIVER,
    REFUND_WITHDRAW
}
