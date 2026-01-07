package com.yasinsez.library.model.enums;

/**
 * Enum representing the status of a loan
 * Maps to the loan_status enum in the database
 */
public enum LoanStatus {
    ACTIVE,
    OVERDUE,
    RETURNED,
    LOST
} 