package com.cinebook.model;

import com.cinebook.exception.BookingException;

/**
 * Contract for anything that can reserve seats and later give them back (FR-4, FR-7).
 * {@link Booking} implements it: bookSeats() confirms the reservation and
 * cancelBooking() releases the seats and calculates the refund.
 */
public interface Bookable {

    /**
     * Reserves every seat in the booking. This is all-or-nothing: if any seat is
     * unavailable, no seat is reserved and an exception is thrown.
     *
     * @throws BookingException if a seat is already taken or the booking is not pending
     */
    void bookSeats() throws BookingException;

    /**
     * Cancels a confirmed booking, frees its seats and returns the refund amount.
     *
     * @return the refund paid back to the customer (80% of the amount paid)
     * @throws BookingException if the booking is already cancelled or was never confirmed
     */
    double cancelBooking() throws BookingException;
}
