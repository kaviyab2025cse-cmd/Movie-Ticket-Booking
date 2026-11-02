package com.cinebook.model;

/**
 * Life cycle of a booking.
 * PENDING   - created but seats not yet reserved.
 * CONFIRMED - seats reserved and booking ID issued.
 * CANCELLED - seats released and refund issued.
 */
public enum BookingStatus {
    PENDING,
    CONFIRMED,
    CANCELLED
}
