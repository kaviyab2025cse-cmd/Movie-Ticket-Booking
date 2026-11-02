package com.cinebook.app;

import com.cinebook.dao.DBConnection;
import com.cinebook.exception.BookingException;
import com.cinebook.exception.InvalidCouponException;
import com.cinebook.model.Booking;
import com.cinebook.model.BookingStatus;
import com.cinebook.model.Coupon;
import com.cinebook.model.Money;
import com.cinebook.model.Seat;
import com.cinebook.model.Show;
import com.cinebook.service.Theatre;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.List;

/**
 * Console application entry point for CineBook (Movie Ticket Booking System).
 * Demonstrates:
 *  - Core Java OOP Concepts (Inheritance, Polymorphism, Encapsulation, Abstraction, Enums)
 *  - DBMS Integration (MySQL 8.0, JDBC Driver, CRUD Operations, ACID Transactions)
 */
public class CineBook {

    private static final String LINE = "------------------------------------------------------------";
    private static final int TOTAL_SEATS = Seat.ROW_COUNT * Seat.COLUMN_COUNT;

    private final Theatre theatre;
    private final BufferedReader in;
    private final PrintStream out;

    private Show selectedShow;
    private Coupon appliedCoupon;

    public CineBook(Theatre theatre, BufferedReader in, PrintStream out) {
        this.theatre = theatre;
        this.in = in;
        this.out = out;
    }

    public static void main(String[] args) {
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
        new CineBook(Theatre.withSampleShows(), console, System.out).run();
    }

    /** Runs the menu loop until the user chooses Exit or the input stream closes. */
    public void run() {
        out.println();
        out.println("============================================================");
        out.println("   Welcome to CineBook - Movie Ticket Booking System");
        out.println("   Venue: " + theatre.getName());
        if (theatre.isDbConnected()) {
            out.println("   [DBMS STATUS]: Connected to MySQL (cinebook_db) via JDBC");
        } else {
            out.println("   [DBMS STATUS]: In-Memory Mode (MySQL server not detected)");
        }
        out.println("============================================================");

        boolean running = true;
        try {
            while (running) {
                printMenu();
                String choice = ask("Enter your choice (1-9): ");
                switch (choice) {
                    case "1":
                        viewAvailableMovies();
                        break;
                    case "2":
                        selectMovieAndShow();
                        break;
                    case "3":
                        viewSeatMap();
                        break;
                    case "4":
                        bookTickets();
                        break;
                    case "5":
                        applyCoupon();
                        break;
                    case "6":
                        viewBookingDetails();
                        break;
                    case "7":
                        cancelBooking();
                        break;
                    case "8":
                        viewAllBookings();
                        break;
                    case "9":
                        running = false;
                        break;
                    case "":
                        error("Input cannot be empty. Please enter a menu number from 1 to 9.");
                        break;
                    default:
                        error("'" + choice + "' is not a valid option. Please enter a number from 1 to 9.");
                        break;
                }
            }
        } catch (InputClosedException e) {
            out.println();
            out.println("Input closed. Exiting CineBook.");
        }
        out.println("Thank you for using CineBook. Goodbye!");
    }

    private void printMenu() {
        out.println();
        out.println("========== CINEBOOK ==========");
        out.println("1. View Available Movies (READ)");
        out.println("2. Select Movie and Show");
        out.println("3. View Seat Map (READ)");
        out.println("4. Book Tickets (CREATE)");
        out.println("5. Apply Coupon");
        out.println("6. View Booking Details (READ)");
        out.println("7. Cancel Booking (UPDATE/DELETE)");
        out.println("8. View All Bookings (READ)");
        out.println("9. Exit");
        out.println("==============================");
        if (selectedShow != null) {
            out.println("Selected show : " + selectedShow.getTitle());
        }
        if (appliedCoupon != null) {
            out.println("Applied coupon: " + appliedCoupon.getDescription());
        }
    }

    /** Option 1: lists every show with its time and the number of free seats. */
    private void viewAvailableMovies() {
        printSection("AVAILABLE SHOWS");
        out.printf("%-4s %-18s %-10s %-12s%n", "No.", "Movie", "Show Time", "Seats Free");
        List<Show> shows = theatre.getShows();
        for (int i = 0; i < shows.size(); i++) {
            Show show = shows.get(i);
            out.printf("%-4d %-18s %-10s %-12s%n", i + 1, show.getMovieName(), show.getTimeText(),
                    show.countAvailableSeats() + " / " + TOTAL_SEATS);
        }
    }

    /** Option 2: choose a movie first, then one of its showtimes. */
    private void selectMovieAndShow() {
        printSection("SELECT A MOVIE");
        List<String> movies = theatre.getMovieNames();
        for (int i = 0; i < movies.size(); i++) {
            out.printf(" %d. %s%n", i + 1, movies.get(i));
        }
        int movieNumber = readNumber("Enter movie number (1-" + movies.size() + "): ", 1, movies.size());
        if (movieNumber == -1) {
            return;
        }
        String movieName = movies.get(movieNumber - 1);

        List<Show> showsForMovie = theatre.getShowsForMovie(movieName);
        printSection("SHOWS FOR " + movieName.toUpperCase());
        for (int i = 0; i < showsForMovie.size(); i++) {
            Show show = showsForMovie.get(i);
            out.printf(" %d. %-10s (%d seats free)%n", i + 1, show.getTimeText(), show.countAvailableSeats());
        }
        int showNumber = readNumber("Enter show number (1-" + showsForMovie.size() + "): ",
                1, showsForMovie.size());
        if (showNumber == -1) {
            return;
        }
        selectedShow = showsForMovie.get(showNumber - 1);
        success("Selected " + selectedShow.getTitle() + ". Use option 3 to view the seat map or option 4 to book.");
    }

    /** Option 3: draws the live 5 x 8 seat map of the selected show. */
    private void viewSeatMap() {
        Show show = requireSelectedShow();
        if (show == null) {
            return;
        }
        printSection("SEAT MAP - " + show.getTitle());
        out.print(show.renderSeatMap());
        out.println("Available seats: " + show.countAvailableSeats() + " of " + TOTAL_SEATS);
    }

    /** Option 4: books one or more seats in one transaction (CREATE). */
    private void bookTickets() {
        Show show = requireSelectedShow();
        if (show == null) {
            return;
        }
        printSection("BOOK TICKETS - " + show.getTitle());
        out.print(show.renderSeatMap());

        String seatInput = ask("Enter seats (e.g. C3,C4): ");
        if (seatInput.isEmpty()) {
            error("Seat selection cannot be empty. Please enter seat codes such as C3,C4.");
            return;
        }

        String couponPrompt = appliedCoupon == null
                ? "Coupon code (Enter to skip): "
                : "Coupon code (Enter to use " + appliedCoupon.name() + "): ";
        String couponInput = ask(couponPrompt);
        String couponToUse = couponInput.isEmpty() && appliedCoupon != null
                ? appliedCoupon.name()
                : couponInput;

        try {
            Booking booking = theatre.bookTickets(show, seatInput, couponToUse);
            appliedCoupon = null;
            success("Booking confirmed successfully!");
            out.print(booking.getReceipt());
        } catch (BookingException e) {
            error("Booking failed: " + e.getMessage());
            out.println("        No seats were booked. Please try again.");
        }
    }

    /** Option 5: validates a coupon and keeps it for the next booking. */
    private void applyCoupon() {
        printSection("APPLY COUPON");
        out.print("Available coupons:");
        for (Coupon coupon : Coupon.values()) {
            out.print(" " + coupon.getDescription());
        }
        out.println();
        String code = ask("Enter coupon code: ");
        try {
            appliedCoupon = Coupon.parse(code);
            success(appliedCoupon.name() + " applied. You get " + appliedCoupon.getDiscountPercent()
                    + "% off your next booking.");
        } catch (InvalidCouponException e) {
            error(e.getMessage());
        }
    }

    /** Option 6: shows the full details of one booking. */
    private void viewBookingDetails() {
        String id = ask("Enter booking ID (e.g. BK1001): ");
        try {
            Booking booking = theatre.findBooking(id);
            printSection("BOOKING DETAILS - " + booking.getBookingId());
            out.print(booking.getReceipt());
        } catch (BookingException e) {
            error(e.getMessage());
        }
    }

    /** Option 7: cancels a booking after confirmation and refunds 80% (UPDATE/DELETE). */
    private void cancelBooking() {
        String id = ask("Enter booking ID to cancel: ");
        try {
            Booking booking = theatre.findBooking(id);
            if (booking.getStatus() == BookingStatus.CANCELLED) {
                error("Booking " + booking.getBookingId() + " is already cancelled. Nothing to cancel.");
                return;
            }
            out.print(booking.getReceipt());
            String answer = ask("Cancel this booking? (Y/N): ");
            if (!answer.equalsIgnoreCase("Y")) {
                info("Cancellation aborted. Your booking is still active.");
                return;
            }
            double amountPaid = booking.getAmountPaid();
            double refund = theatre.cancelBooking(booking.getBookingId());
            success("Booking " + booking.getBookingId() + " cancelled. Seats released: "
                    + booking.getSeatCodes() + ".");
            out.println("        Refund: " + Money.format(refund) + " (80% of " + Money.format(amountPaid) + ")");
        } catch (BookingException e) {
            error(e.getMessage());
        }
    }

    /** Option 8: table of every booking made in this session, including cancelled ones. */
    private void viewAllBookings() {
        List<Booking> bookings = theatre.getBookings();
        if (bookings.isEmpty()) {
            info("No bookings have been made yet.");
            return;
        }
        printSection("ALL BOOKINGS (DATABASE SYNCED)");
        out.printf("%-9s %-18s %-10s %-14s %-12s %-10s%n",
                "ID", "Movie", "Time", "Seats", "Paid", "Status");
        for (Booking booking : bookings) {
            out.printf("%-9s %-18s %-10s %-14s %-12s %-10s%n",
                    booking.getBookingId(),
                    booking.getShow().getMovieName(),
                    booking.getShow().getTimeText(),
                    booking.getSeatCodes(),
                    Money.format(booking.getAmountPaid()),
                    booking.getStatus());
        }
    }

    /** @return the selected show, or null after printing an error if none is selected */
    private Show requireSelectedShow() {
        if (selectedShow == null) {
            error("No show selected. Use option 2 to choose a movie and show first.");
        }
        return selectedShow;
    }

    private int readNumber(String prompt, int min, int max) {
        String text = ask(prompt);
        if (text.isEmpty()) {
            error("Input cannot be empty. Please enter a number from " + min + " to " + max + ".");
            return -1;
        }
        try {
            int value = Integer.parseInt(text);
            if (value < min || value > max) {
                error("Invalid selection '" + text + "'. Please enter a number from " + min + " to " + max + ".");
                return -1;
            }
            return value;
        } catch (NumberFormatException e) {
            error("'" + text + "' is not a valid number. Please enter a number from " + min + " to " + max + ".");
            return -1;
        }
    }

    private String ask(String prompt) {
        out.print(prompt);
        out.flush();
        try {
            String line = in.readLine();
            if (line == null) {
                throw new InputClosedException();
            }
            return line.trim();
        } catch (IOException e) {
            throw new InputClosedException();
        }
    }

    private void printSection(String title) {
        out.println();
        out.println(LINE);
        out.println(" " + title);
        out.println(LINE);
    }

    private void success(String message) {
        out.println("[SUCCESS] " + message);
    }

    private void error(String message) {
        out.println("[ERROR] " + message);
    }

    private void info(String message) {
        out.println("[INFO] " + message);
    }

    private static final class InputClosedException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
}
