package com.cinebook.model;

import com.cinebook.exception.BookingException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * An immutable position in the 5 x 8 seat grid, such as C3 (row C, column 3).
 * Rows run from A to E and columns from 1 to 8.
 */
public final class Seat {

    public static final int ROW_COUNT = 5;
    public static final int COLUMN_COUNT = 8;
    private static final String ROW_LETTERS = "ABCDE";

    private final int rowIndex;     // 0 = row A ... 4 = row E
    private final int columnIndex;  // 0 = column 1 ... 7 = column 8

    /**
     * @param rowIndex    zero-based row (0-4)
     * @param columnIndex zero-based column (0-7)
     * @throws IllegalArgumentException if the position is outside the grid
     */
    public Seat(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= ROW_COUNT || columnIndex < 0 || columnIndex >= COLUMN_COUNT) {
            throw new IllegalArgumentException(
                    "Seat position out of range: row " + rowIndex + ", column " + columnIndex);
        }
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
    }

    /**
     * Parses one seat code such as "C3". Case and surrounding spaces are ignored.
     *
     * @throws BookingException if the code is empty or outside A1-E8
     */
    public static Seat parse(String rawCode) throws BookingException {
        if (rawCode == null || rawCode.trim().isEmpty()) {
            throw new BookingException("Seat code cannot be empty.");
        }
        String code = rawCode.trim().toUpperCase(Locale.ENGLISH);
        if (code.length() != 2) {
            throw invalidCode(rawCode);
        }
        int row = ROW_LETTERS.indexOf(code.charAt(0));
        char column = code.charAt(1);
        if (row < 0 || column < '1' || column > '8') {
            throw invalidCode(rawCode);
        }
        return new Seat(row, column - '1');
    }

    /**
     * Parses a comma-separated list such as "C3,C4". An empty item is rejected,
     * so "C3,,C4" is invalid.
     *
     * @throws BookingException if the list is empty or any code is invalid
     */
    public static List<Seat> parseList(String input) throws BookingException {
        if (input == null || input.trim().isEmpty()) {
            throw new BookingException("No seats entered. Please enter at least one seat code, e.g. C3,C4.");
        }
        List<Seat> seats = new ArrayList<>();
        for (String token : input.split(",", -1)) {
            seats.add(parse(token));
        }
        return seats;
    }

    /** @return the row letter (A-E) for a zero-based row index */
    public static char rowLetter(int rowIndex) {
        return ROW_LETTERS.charAt(rowIndex);
    }

    private static BookingException invalidCode(String rawCode) {
        return new BookingException("Invalid seat code '" + rawCode.trim()
                + "'. Use a row from A to E and a column from 1 to 8, e.g. C3.");
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    /** @return the seat code, e.g. "C3" */
    public String getCode() {
        return "" + rowLetter(rowIndex) + (columnIndex + 1);
    }

    /** @return the category (REGULAR, PREMIUM or VIP) this seat belongs to */
    public SeatType getSeatType() {
        return SeatType.forRow(rowLetter(rowIndex));
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Seat)) {
            return false;
        }
        Seat seat = (Seat) other;
        return rowIndex == seat.rowIndex && columnIndex == seat.columnIndex;
    }

    @Override
    public int hashCode() {
        return 31 * rowIndex + columnIndex;
    }

    @Override
    public String toString() {
        return getCode();
    }
}
