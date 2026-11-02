package com.cinebook.model;

/**
 * Seat categories and their ticket prices (FR-3).
 * Each constant stores its price and the rows it covers, so pricing rules live in one place
 * and the booking logic reads prices through {@link #getPrice()} instead of hard-coding them.
 */
public enum SeatType {

    REGULAR(150.0, "AB"),
    PREMIUM(250.0, "CD"),
    VIP(400.0, "E");

    private final double price;
    private final String rowLetters;

    SeatType(double price, String rowLetters) {
        this.price = price;
        this.rowLetters = rowLetters;
    }

    /** @return the ticket price in rupees for one seat of this category */
    public double getPrice() {
        return price;
    }

    /** @return the row letters that belong to this category, e.g. "AB" for REGULAR */
    public String getRowLetters() {
        return rowLetters;
    }

    /** @return a readable name such as "Regular" or "VIP" */
    public String getDisplayName() {
        if (this == VIP) {
            return "VIP";
        }
        return name().charAt(0) + name().substring(1).toLowerCase();
    }

    /**
     * Finds the category that a row letter belongs to.
     *
     * @param rowLetter a row letter from A to E
     * @return the matching seat category
     * @throws IllegalArgumentException if no category covers that row
     */
    public static SeatType forRow(char rowLetter) {
        for (SeatType type : values()) {
            if (type.rowLetters.indexOf(rowLetter) >= 0) {
                return type;
            }
        }
        throw new IllegalArgumentException("No seat category for row " + rowLetter);
    }
}
