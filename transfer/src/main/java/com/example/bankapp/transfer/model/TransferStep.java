package com.example.bankapp.transfer.model;

public enum TransferStep {
    CHECK_BLOCKER,
    RESOLVE_ACCOUNTS,
    CALCULATE_AMOUNT,
    WITHDRAW_SENDER,
    DEPOSIT_RECEIVER,
    REFUND_WITHDRAW
}
