package com.yasinsez.library.model.enums;

/**
 * Enum representing the status of a book reservation
 * Maps to the reservation_status enum in the database
 */
public enum ReservationStatus {
    PENDING,
    READY_FOR_PICKUP,
    COMPLETED,
    CANCELLED
} 