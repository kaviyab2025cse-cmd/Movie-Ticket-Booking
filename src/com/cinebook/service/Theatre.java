package com.cinebook.service;

import com.cinebook.dao.CineBookDAO;
import com.cinebook.dao.DBConnection;
import com.cinebook.exception.BookingException;
import com.cinebook.exception.InvalidCouponException;
import com.cinebook.model.Booking;
import com.cinebook.model.Seat;
import com.cinebook.model.Show;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * The cinema service layer. A Theatre is composed of Shows and manages bookings.
 * Features 5 Tamil Blockbuster Movies:
 *  1. Baattha (Tamil)
 *  2. Dorothy (Tamil)
 *  3. Meesaya Murukku 2 (Tamil)
 *  4. Jailer 2 (Tamil)
 *  5. Mandaadi (Tamil)
 */
public class Theatre {

    private static final Pattern BOOKING_ID_PATTERN = Pattern.compile("BK\\d+");

    private final String name;
    private final List<Show> shows = new ArrayList<>();
    private final List<Booking> bookings = new ArrayList<>();
    private boolean dbConnected = false;

    public Theatre(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Theatre name cannot be empty.");
        }
        this.name = name.trim();
        checkDatabaseConnectivity();
    }

    public void checkDatabaseConnectivity() {
        try {
            if (DBConnection.isAvailable()) {
                CineBookDAO.initializeDatabase();
                this.dbConnected = true;
            } else {
                this.dbConnected = false;
            }
        } catch (Exception e) {
            this.dbConnected = false;
        }
    }

    public boolean isDbConnected() {
        return dbConnected;
    }

    /**
     * Creates the theatre with sample shows for the 5 Tamil movies.
     * Shows 1 and 2 feature Baattha at different times to demonstrate independent seat grids.
     */
    public static Theatre withSampleShows() {
        Theatre theatre = new Theatre("CineBook Cinema, Chennai");
        theatre.addShow(new Show(1, "Baattha (Tamil)", LocalTime.of(10, 30)));
        theatre.addShow(new Show(2, "Baattha (Tamil)", LocalTime.of(18, 45)));
        theatre.addShow(new Show(3, "Dorothy (Tamil)", LocalTime.of(13, 45)));
        theatre.addShow(new Show(4, "Dorothy (Tamil)", LocalTime.of(19, 15)));
        theatre.addShow(new Show(5, "Meesaya Murukku 2 (Tamil)", LocalTime.of(11, 15)));
        theatre.addShow(new Show(6, "Jailer 2 (Tamil)", LocalTime.of(19, 0)));
        theatre.addShow(new Show(7, "Mandaadi (Tamil)", LocalTime.of(20, 30)));

        if (theatre.isDbConnected()) {
            theatre.refreshSeatGridsFromDB();
        }

        return theatre;
    }

    public void refreshSeatGridsFromDB() {
        if (!dbConnected) return;
        try {
            for (Show show : shows) {
                char[][] grid = CineBookDAO.loadSeatGrid(show.getShowId());
                show.applySeatGrid(grid);
            }
        } catch (Exception ignored) {
        }
    }

    public String getName() {
        return name;
    }

    public void addShow(Show show) {
        if (show == null) {
            throw new IllegalArgumentException("Show cannot be null.");
        }
        shows.add(show);
    }

    /** @return all shows in the order they were added */
    public List<Show> getShows() {
        return Collections.unmodifiableList(shows);
    }

    /** @return the distinct movie names, in the order they first appear */
    public List<String> getMovieNames() {
        Set<String> names = new LinkedHashSet<>();
        for (Show show : shows) {
            names.add(show.getMovieName());
        }
        return new ArrayList<>(names);
    }

    /** @return every show of the given movie, in the order they were added */
    public List<Show> getShowsForMovie(String movieName) {
        List<Show> matches = new ArrayList<>();
        for (Show show : shows) {
            if (show.getMovieName().equalsIgnoreCase(movieName)) {
                matches.add(show);
            }
        }
        return matches;
    }

    /**
     * Books one or more seats in one transaction (FR-4, FR-6).
     * Saves record to MySQL if database is connected.
     */
    public Booking bookTickets(Show show, String seatInput, String couponCode)
            throws BookingException, InvalidCouponException {
        if (show == null || !shows.contains(show)) {
            throw new BookingException("The selected show is not available.");
        }
        Booking booking = new Booking(show, Seat.parseList(seatInput), couponCode);
        booking.bookSeats();
        bookings.add(booking);

        if (dbConnected) {
            try {
                CineBookDAO.saveBooking(booking, show.getShowId());
            } catch (Exception e) {
                System.err.println("[WARNING] Could not save to MySQL: " + e.getMessage());
            }
        }

        return booking;
    }

    /**
     * Finds a booking by its ID.
     */
    public Booking findBooking(String rawId) throws BookingException {
        if (rawId == null || rawId.trim().isEmpty()) {
            throw new BookingException("Booking ID cannot be empty.");
        }
        String id = rawId.trim().toUpperCase(Locale.ENGLISH);
        if (!BOOKING_ID_PATTERN.matcher(id).matches()) {
            throw new BookingException("Invalid booking ID '" + rawId.trim() + "'. IDs look like BK1001.");
        }
        for (Booking booking : bookings) {
            if (id.equals(booking.getBookingId())) {
                return booking;
            }
        }
        throw new BookingException("No booking found with ID " + id + ".");
    }

    /**
     * Cancels a booking by ID, frees its seats and returns the 80% refund.
     */
    public double cancelBooking(String bookingId) throws BookingException {
        Booking booking = findBooking(bookingId);
        double refund = booking.cancelBooking();

        if (dbConnected) {
            try {
                CineBookDAO.updateCancelBooking(booking.getBookingId(), refund,
                        booking.getShow().getShowId(), booking.getSeats());
            } catch (Exception e) {
                System.err.println("[WARNING] Could not update MySQL: " + e.getMessage());
            }
        }

        return refund;
    }

    /** @return every booking made, including cancelled ones */
    public List<Booking> getBookings() {
        return Collections.unmodifiableList(bookings);
    }
}
