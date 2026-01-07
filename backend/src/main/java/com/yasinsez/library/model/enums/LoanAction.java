package com.yasinsez.library.model.enums;

/**
 * Enum representing the action taken on a loan
 * Maps to the loan_action enum in the database
 */
public enum LoanAction {
    CHECKOUT,
    RETURN,
    RENEWAL,
    OVERDUE_NOTICE
} 