package com.cinebook.exception;

/**
 * Raised when a discount coupon code is empty or does not match any valid coupon.
 * It extends {@link BookingException} because an invalid coupon stops the booking.
 */
public class InvalidCouponException extends BookingException {

    private static final long serialVersionUID = 1L;

    public InvalidCouponException(String message) {
        super(message);
    }
}
