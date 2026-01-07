package com.yasinsez.library.model.enums;

/**
 * Enum representing the type of a transaction
 * Maps to the transaction_type enum in the database
 */
public enum TransactionType {
    FINE_PAYMENT,
    MEMBERSHIP_FEE,
    LOST_ITEM_FEE,
    RESERVATION_FEE,
    REFUND
} 