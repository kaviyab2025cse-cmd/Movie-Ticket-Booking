package com.cinebook.exception;

/**
 * Checked exception raised for every business-rule failure while booking or cancelling.
 * Examples: an invalid seat code, a seat that is already booked, an unknown booking ID,
 * or cancelling a booking that has already been cancelled.
 */
public class BookingException extends Exception {

    private static final long serialVersionUID = 1L;

    public BookingException(String message) {
        super(message);
    }
}
