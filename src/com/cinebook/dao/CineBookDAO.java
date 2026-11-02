package com.cinebook.dao;

import com.cinebook.model.Booking;
import com.cinebook.model.BookingStatus;
import com.cinebook.model.Seat;
import com.cinebook.model.SeatType;
import com.cinebook.model.Show;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) implementing CRUD operations with MySQL via JDBC.
 *
 * Demonstrates:
 *  - CREATE: Insert bookings and reserve seats atomically using ACID transactions
 *  - READ  : Query shows, live seat grid, and booking records
 *  - UPDATE: Update seat reservation status and cancellation refunds
 *  - DELETE: Soft delete (cancel status) and hard delete options
 */
public class CineBookDAO {

    /**
     * Initializes tables and seeds baseline data if they do not exist yet.
     */
    public static void initializeDatabase() throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Create tables
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS movies (" +
                    "movie_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "title VARCHAR(150) NOT NULL UNIQUE, " +
                    "duration_mins INT NOT NULL, " +
                    "genre VARCHAR(50) NOT NULL) ENGINE=InnoDB");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS shows (" +
                    "show_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "movie_id INT NOT NULL, " +
                    "show_time TIME NOT NULL, " +
                    "theatre_name VARCHAR(100) NOT NULL, " +
                    "FOREIGN KEY (movie_id) REFERENCES movies(movie_id) ON DELETE CASCADE) ENGINE=InnoDB");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS seats (" +
                    "seat_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "show_id INT NOT NULL, " +
                    "seat_code VARCHAR(5) NOT NULL, " +
                    "row_letter CHAR(1) NOT NULL, " +
                    "column_number INT NOT NULL, " +
                    "seat_type VARCHAR(20) NOT NULL, " +
                    "price DECIMAL(8, 2) NOT NULL, " +
                    "is_booked BOOLEAN NOT NULL DEFAULT FALSE, " +
                    "FOREIGN KEY (show_id) REFERENCES shows(show_id) ON DELETE CASCADE, " +
                    "UNIQUE KEY uk_show_seat (show_id, seat_code)) ENGINE=InnoDB");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS coupons (" +
                    "coupon_code VARCHAR(20) PRIMARY KEY, " +
                    "discount_percent INT NOT NULL, " +
                    "description VARCHAR(150) NOT NULL) ENGINE=InnoDB");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS bookings (" +
                    "booking_id VARCHAR(15) PRIMARY KEY, " +
                    "show_id INT NOT NULL, " +
                    "movie_title VARCHAR(150) NOT NULL, " +
                    "show_time_str VARCHAR(20) NOT NULL, " +
                    "seat_codes VARCHAR(100) NOT NULL, " +
                    "base_amount DECIMAL(8, 2) NOT NULL, " +
                    "coupon_code VARCHAR(20) NULL, " +
                    "discount_amount DECIMAL(8, 2) NOT NULL DEFAULT 0.00, " +
                    "amount_paid DECIMAL(8, 2) NOT NULL, " +
                    "status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED', " +
                    "refund_amount DECIMAL(8, 2) NOT NULL DEFAULT 0.00, " +
                    "booking_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP) ENGINE=InnoDB");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS booking_seats (" +
                    "booking_id VARCHAR(15) NOT NULL, " +
                    "seat_code VARCHAR(5) NOT NULL, " +
                    "PRIMARY KEY (booking_id, seat_code), " +
                    "FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE) ENGINE=InnoDB");

            // Seed sample movies and shows if empty
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM movies");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.executeUpdate("INSERT INTO movies (movie_id, title, duration_mins, genre) VALUES " +
                        "(1, 'Interstellar', 169, 'Sci-Fi / Adventure'), " +
                        "(2, 'Dune: Part Two', 166, 'Sci-Fi / Action'), " +
                        "(3, 'The Dark Knight', 152, 'Action / Crime')");

                stmt.executeUpdate("INSERT INTO shows (show_id, movie_id, show_time, theatre_name) VALUES " +
                        "(1, 1, '10:00:00', 'CineBook Cinema, Chennai'), " +
                        "(2, 1, '19:30:00', 'CineBook Cinema, Chennai'), " +
                        "(3, 2, '16:15:00', 'CineBook Cinema, Chennai'), " +
                        "(4, 3, '21:45:00', 'CineBook Cinema, Chennai')");

                stmt.executeUpdate("INSERT INTO coupons (coupon_code, discount_percent, description) VALUES " +
                        "('CINE10', 10, '10% off entire booking (max discount)')");

                // Seed 40 seats per show
                seedSeats(conn);
            }
        }
    }

    private static void seedSeats(Connection conn) throws SQLException {
        String insertSql = "INSERT INTO seats (show_id, seat_code, row_letter, column_number, seat_type, price, is_booked) " +
                "VALUES (?, ?, ?, ?, ?, ?, FALSE)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            for (int showId = 1; showId <= 4; showId++) {
                for (int r = 0; r < Seat.ROW_COUNT; r++) {
                    char rowChar = Seat.rowLetter(r);
                    SeatType type = SeatType.forRow(rowChar);
                    for (int c = 1; c <= Seat.COLUMN_COUNT; c++) {
                        String code = "" + rowChar + c;
                        ps.setInt(1, showId);
                        ps.setString(2, code);
                        ps.setString(3, String.valueOf(rowChar));
                        ps.setInt(4, c);
                        ps.setString(5, type.name());
                        ps.setDouble(6, type.getPrice());
                        ps.addBatch();
                    }
                }
            }
            ps.executeBatch();
        }
    }

    // -------------------------------------------------------------------------
    // CRUD: CREATE (Insert booking and mark seats as booked with Transaction)
    // -------------------------------------------------------------------------
    public static void saveBooking(Booking booking, int showId) throws SQLException {
        String insertBookingSql = "INSERT INTO bookings " +
                "(booking_id, show_id, movie_title, show_time_str, seat_codes, base_amount, coupon_code, discount_amount, amount_paid, status, refund_amount) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String insertSeatSql = "INSERT INTO booking_seats (booking_id, seat_code) VALUES (?, ?)";
        String updateSeatSql = "UPDATE seats SET is_booked = TRUE WHERE show_id = ? AND seat_code = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Begin ACID Transaction
            try {
                // 1. Insert booking record
                try (PreparedStatement ps = conn.prepareStatement(insertBookingSql)) {
                    ps.setString(1, booking.getBookingId());
                    ps.setInt(2, showId);
                    ps.setString(3, booking.getShow().getMovieName());
                    ps.setString(4, booking.getShow().getTimeText());
                    ps.setString(5, booking.getSeatCodes());
                    ps.setDouble(6, booking.getBaseAmount());
                    ps.setString(7, booking.getCoupon() != null ? booking.getCoupon().name() : null);
                    ps.setDouble(8, booking.getDiscountAmount());
                    ps.setDouble(9, booking.getAmountPaid());
                    ps.setString(10, booking.getStatus().name());
                    ps.setDouble(11, booking.getRefundAmount());
                    ps.executeUpdate();
                }

                // 2. Insert mapped seats and update seat availability
                try (PreparedStatement psSeat = conn.prepareStatement(insertSeatSql);
                     PreparedStatement psUpdate = conn.prepareStatement(updateSeatSql)) {
                    for (Seat seat : booking.getSeats()) {
                        psSeat.setString(1, booking.getBookingId());
                        psSeat.setString(2, seat.getCode());
                        psSeat.executeUpdate();

                        psUpdate.setInt(1, showId);
                        psUpdate.setString(2, seat.getCode());
                        psUpdate.executeUpdate();
                    }
                }

                conn.commit(); // Commit Transaction
            } catch (SQLException e) {
                conn.rollback(); // Rollback on failure
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // -------------------------------------------------------------------------
    // CRUD: READ (Fetch seat grid, bookings, and details)
    // -------------------------------------------------------------------------
    public static char[][] loadSeatGrid(int showId) throws SQLException {
        char[][] grid = new char[Seat.ROW_COUNT][Seat.COLUMN_COUNT];
        for (int r = 0; r < Seat.ROW_COUNT; r++) {
            for (int c = 0; c < Seat.COLUMN_COUNT; c++) {
                grid[r][c] = Show.AVAILABLE;
            }
        }

        String sql = "SELECT row_letter, column_number, is_booked FROM seats WHERE show_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    char rLetter = rs.getString("row_letter").charAt(0);
                    int rIdx = "ABCDE".indexOf(rLetter);
                    int cIdx = rs.getInt("column_number") - 1;
                    boolean booked = rs.getBoolean("is_booked");
                    if (rIdx >= 0 && rIdx < Seat.ROW_COUNT && cIdx >= 0 && cIdx < Seat.COLUMN_COUNT) {
                        grid[rIdx][cIdx] = booked ? Show.BOOKED : Show.AVAILABLE;
                    }
                }
            }
        }
        return grid;
    }

    public static List<String> getAllBookingsSummary() throws SQLException {
        List<String> list = new ArrayList<>();
        String sql = "SELECT booking_id, movie_title, show_time_str, seat_codes, amount_paid, status FROM bookings ORDER BY booking_id";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(String.format("%-9s %-18s %-10s %-14s %-12.2f %-10s",
                        rs.getString("booking_id"),
                        rs.getString("movie_title"),
                        rs.getString("show_time_str"),
                        rs.getString("seat_codes"),
                        rs.getDouble("amount_paid"),
                        rs.getString("status")));
            }
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // CRUD: UPDATE (Update booking status on cancellation & release seats)
    // -------------------------------------------------------------------------
    public static void updateCancelBooking(String bookingId, double refundAmount, int showId, List<Seat> seats)
            throws SQLException {
        String updateBookingSql = "UPDATE bookings SET status = 'CANCELLED', refund_amount = ? WHERE booking_id = ?";
        String releaseSeatSql = "UPDATE seats SET is_booked = FALSE WHERE show_id = ? AND seat_code = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(updateBookingSql)) {
                    ps.setDouble(1, refundAmount);
                    ps.setString(2, bookingId);
                    ps.executeUpdate();
                }

                try (PreparedStatement psSeat = conn.prepareStatement(releaseSeatSql)) {
                    for (Seat seat : seats) {
                        psSeat.setInt(1, showId);
                        psSeat.setString(2, seat.getCode());
                        psSeat.executeUpdate();
                    }
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // -------------------------------------------------------------------------
    // CRUD: DELETE (Hard delete booking for testing / admin cleanup)
    // -------------------------------------------------------------------------
    public static boolean deleteBookingRecord(String bookingId) throws SQLException {
        String sql = "DELETE FROM bookings WHERE booking_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookingId);
            return ps.executeUpdate() > 0;
        }
    }
}
