package com.cinebook.model;

import java.util.Locale;

/**
 * Small helper for rupee amounts: rounds to paise and formats for display.
 */
public final class Money {

    private Money() {
        // utility class, not meant to be instantiated
    }

    /** Rounds an amount to two decimal places. */
    public static double round(double amount) {
        return Math.round(amount * 100.0) / 100.0;
    }

    /** Formats an amount for display, e.g. "Rs. 450.00". */
    public static String format(double amount) {
        return String.format(Locale.ENGLISH, "Rs. %.2f", amount);
    }
}
