package com.cinebook.model;

import com.cinebook.exception.BookingException;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * One screening of a movie at a fixed time.
 * Each show owns a private 5 x 8 seat grid stored in a 2D char array, so bookings in one
 * show never affect another show (FR-8). 'O' means available and 'X' means booked.
 */
public class Show {

    public static final char AVAILABLE = 'O';
    public static final char BOOKED = 'X';

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);

    private final int showId;
    private final String movieName;
    private final LocalTime showTime;
    private final char[][] seatGrid;

    /**
     * Overloaded constructor with auto-assigned default ID.
     */
    public Show(String movieName, LocalTime showTime) {
        this(1, movieName, showTime);
    }

    /**
     * @param showId    database / show identification number
     * @param movieName the movie title
     * @param showTime  the start time of this show
     */
    public Show(int showId, String movieName, LocalTime showTime) {
        if (movieName == null || movieName.trim().isEmpty()) {
            throw new IllegalArgumentException("Movie name cannot be empty.");
        }
        if (showTime == null) {
            throw new IllegalArgumentException("Show time cannot be null.");
        }
        this.showId = showId;
        this.movieName = movieName.trim();
        this.showTime = showTime;
        this.seatGrid = new char[Seat.ROW_COUNT][Seat.COLUMN_COUNT];
        for (char[] row : seatGrid) {
            Arrays.fill(row, AVAILABLE);
        }
    }

    public int getShowId() {
        return showId;
    }

    public String getMovieName() {
        return movieName;
    }

    public LocalTime getShowTime() {
        return showTime;
    }

    /** @return the show time as text, e.g. "7:30 PM" */
    public String getTimeText() {
        return TIME_FORMAT.format(showTime);
    }

    /** @return the movie and time, e.g. "Interstellar (7:30 PM)" */
    public String getTitle() {
        return movieName + " (" + getTimeText() + ")";
    }

    /** @return true if the seat is still free in this show */
    public boolean isAvailable(Seat seat) {
        return seatGrid[seat.getRowIndex()][seat.getColumnIndex()] == AVAILABLE;
    }

    /** @return the number of free seats in this show */
    public int countAvailableSeats() {
        int count = 0;
        for (char[] row : seatGrid) {
            for (char cell : row) {
                if (cell == AVAILABLE) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Marks every seat in the list as booked. The method checks all seats first and changes
     * the grid only when every seat is free, so the reservation is all-or-nothing.
     *
     * @throws BookingException if any seat is already booked; the grid is left unchanged
     */
    public void reserveSeats(List<Seat> seats) throws BookingException {
        for (Seat seat : seats) {
            if (!isAvailable(seat)) {
                throw new BookingException("Seat " + seat.getCode() + " is already booked.");
            }
        }
        for (Seat seat : seats) {
            seatGrid[seat.getRowIndex()][seat.getColumnIndex()] = BOOKED;
        }
    }

    /** Frees the given seats so they can be booked again (used on cancellation). */
    public void releaseSeats(List<Seat> seats) {
        for (Seat seat : seats) {
            seatGrid[seat.getRowIndex()][seat.getColumnIndex()] = AVAILABLE;
        }
    }

    /**
     * Syncs the seat grid with the database state.
     */
    public void applySeatGrid(char[][] grid) {
        if (grid != null && grid.length == Seat.ROW_COUNT && grid[0].length == Seat.COLUMN_COUNT) {
            for (int r = 0; r < Seat.ROW_COUNT; r++) {
                System.arraycopy(grid[r], 0, seatGrid[r], 0, Seat.COLUMN_COUNT);
            }
        }
    }

    /**
     * Draws the seat map as text, with column numbers across the top, row letters down the
     * side, and the price of each row category on the right.
     */
    public String renderSeatMap() {
        StringBuilder map = new StringBuilder();
        map.append("                [ S C R E E N ]").append(System.lineSeparator());
        map.append("    ");
        for (int column = 1; column <= Seat.COLUMN_COUNT; column++) {
            map.append(column);
            if (column < Seat.COLUMN_COUNT) {
                map.append(' ');
            }
        }
        map.append(System.lineSeparator());

        for (int row = 0; row < Seat.ROW_COUNT; row++) {
            char letter = Seat.rowLetter(row);
            SeatType type = SeatType.forRow(letter);
            map.append(String.format(" %c  ", letter));
            for (int column = 0; column < Seat.COLUMN_COUNT; column++) {
                if (column > 0) {
                    map.append(' ');
                }
                map.append(seatGrid[row][column]);
            }
            map.append("   ").append(type.getDisplayName())
               .append(" (").append(Money.format(type.getPrice())).append(')')
               .append(System.lineSeparator());
        }
        map.append("Legend: ").append(AVAILABLE).append(" = Available   ")
           .append(BOOKED).append(" = Booked").append(System.lineSeparator());
        return map.toString();
    }

    @Override
    public String toString() {
        return getTitle();
    }
}
