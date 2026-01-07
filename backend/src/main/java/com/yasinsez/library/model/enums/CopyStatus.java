package com.yasinsez.library.model.enums;

/**
 * Enum representing the status of a book copy
 * Maps to the copy_status enum in the database
 */
public enum CopyStatus {
    AVAILABLE,
    CHECKED_OUT,
    IN_REPAIR,
    LOST,
    RESERVED
} 