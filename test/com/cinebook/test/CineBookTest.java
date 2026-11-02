package com.cinebook.test;

import com.cinebook.exception.BookingException;
import com.cinebook.exception.InvalidCouponException;
import com.cinebook.model.Booking;
import com.cinebook.model.BookingStatus;
import com.cinebook.model.Seat;
import com.cinebook.model.SeatType;
import com.cinebook.model.Show;
import com.cinebook.service.Theatre;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Test suite for CineBook. It needs no external libraries, so it runs with only the JDK:
 *   java -cp out com.cinebook.test.CineBookTest
 * TC-01 to TC-10 are the required test cases. TC-11 to TC-15 are extra checks.
 */
public class CineBookTest {

    private static final double DELTA = 0.001;
    private static final int TOTAL_SEATS = 40;

    /** A test body. It may throw any exception; an uncaught exception counts as a failure. */
    @FunctionalInterface
    private interface TestBody {
        void run() throws Exception;
    }

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("CineBook test suite");
        System.out.println("===================");

        run("TC-01 Successful booking", CineBookTest::successfulBooking);
        run("TC-02 Booking multiple seats", CineBookTest::bookingMultipleSeats);
        run("TC-03 Booking an already occupied seat", CineBookTest::bookingOccupiedSeat);
        run("TC-04 Invalid seat selection", CineBookTest::invalidSeatSelection);
        run("TC-05 Booking with a valid coupon", CineBookTest::bookingWithValidCoupon);
        run("TC-06 Booking with an invalid coupon", CineBookTest::bookingWithInvalidCoupon);
        run("TC-07 Successful cancellation", CineBookTest::successfulCancellation);
        run("TC-08 Invalid booking ID", CineBookTest::invalidBookingId);
        run("TC-09 Double cancellation", CineBookTest::doubleCancellation);
        run("TC-10 Independent seat arrangements for different shows", CineBookTest::independentShowGrids);
        run("TC-11 Duplicate seat selection in one request", CineBookTest::duplicateSeatSelection);
        run("TC-12 Seat prices come from the SeatType enum", CineBookTest::seatPricesFromEnum);
        run("TC-13 Booking IDs are unique and sequential", CineBookTest::uniqueBookingIds);
        run("TC-14 Empty seat and booking ID input", CineBookTest::emptyInput);
        run("TC-15 Overloaded calculateTotal() methods", CineBookTest::overloadedCalculateTotal);

        System.out.println();
        System.out.println("Result: " + passed + " passed, " + failed + " failed, " + (passed + failed) + " total");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void run(String name, TestBody body) {
        try {
            body.run();
            passed++;
            System.out.println("[PASS] " + name);
        } catch (Throwable t) {
            failed++;
            System.out.println("[FAIL] " + name + " -> " + t.getMessage());
        }
    }

    // ---------------------------------------------------------------- TC-01 to TC-10 (required)

    /** TC-01: a single seat is booked, confirmed, priced from the enum and shown as taken. */
    private static void successfulBooking() throws Exception {
        Theatre theatre = Theatre.withSampleShows();
        Show show = theatre.getShows().get(1);            // Interstellar 7:30 PM
        Booking booking = theatre.bookTickets(show, "C3", null);

        assertEquals(BookingStatus.CONFIRMED, booking.getStatus(), "status should be CONFIRMED");
        assertTrue(booking.getBookingId().startsWith("BK"), "booking ID should start with BK");
        assertEquals("C3", booking.getSeatCodes(), "seat list");
        assertEquals("Premium", booking.getSeatCategory(), "seat category");
        assertAmount(250.0, booking.getAmountPaid(), "total for one premium seat");
        assertTrue(!show.isAvailable(Seat.parse("C3")), "C3 should be marked booked in the grid");
        assertEquals(1, theatre.getBookings().size(), "one booking should be stored");
    }

    /** TC-02: several seats in one transaction, including mixed categories, are all booked. */
    private static void bookingMultipleSeats() throws Exception {
        Theatre theatre = Theatre.withSampleShows();
        Show show = theatre.getShows().get(0);
        Booking booking = theatre.bookTickets(show, "A1, A2,E8", null);

        assertEquals(3, booking.getSeats().size(), "three seats expected");
        assertAmount(150 + 150 + 400, booking.getAmountPaid(), "two regular seats plus one VIP seat");
        assertEquals("Regular, VIP", booking.getSeatCategory(), "categories in the booking");
        assertTrue(!show.isAvailable(Seat.parse("A1")), "A1 should be booked");
        assertTrue(!show.isAvailable(Seat.parse("A2")), "A2 should be booked");
        assertTrue(!show.isAvailable(Seat.parse("E8")), "E8 should be booked");
        assertEquals(TOTAL_SEATS - 3, show.countAvailableSeats(), "available seat count");
    }

    /** TC-03: an already-booked seat is rejected, and the other seats in the same request are not booked. */
    private static void bookingOccupiedSeat() throws Exception {
        Theatre theatre = Theatre.withSampleShows();
        Show show = theatre.getShows().get(1);
        theatre.bookTickets(show, "D4", null);

        String message = expectRejected(() -> theatre.bookTickets(show, "D4,D5", null));
        assertTrue(message.contains("D4 is already booked"), "message should name the taken seat: " + message);
        assertTrue(show.isAvailable(Seat.parse("D5")), "D5 must stay free (all-or-nothing)");

        // The taken seat comes last here, so a free seat is checked before the failure.
        expectRejected(() -> theatre.bookTickets(show, "D5,D4", null));
        assertTrue(show.isAvailable(Seat.parse("D5")), "D5 must stay free when the taken seat is listed last");
        assertEquals(1, theatre.getBookings().size(), "rejected requests must not create a booking");
    }

    /** TC-04: invalid seat codes are rejected and the grid stays unchanged. */
    private static void invalidSeatSelection() throws Exception {
        Theatre theatre = Theatre.withSampleShows();
        Show show = theatre.getShows().get(1);
        String[] invalidInputs = {"Z9", "C10", "F1", "0A", "A0", "A1,,A2", "A1,Z9", "", "   "};

        for (String input : invalidInputs) {
            expectRejected(() -> theatre.bookTickets(show, input, null));
        }
        assertEquals(TOTAL_SEATS, show.countAvailableSeats(), "no seat should be booked by invalid requests");
        assertEquals(0, theatre.getBookings().size(), "no booking should be stored");
    }

    /** TC-05: CINE10 gives 10% off, so two VIP seats cost Rs. 720 instead of Rs. 800. */
    private static void bookingWithValidCoupon() throws Exception {
        Theatre theatre = Theatre.withSampleShows();
        Show show = theatre.getShows().get(0);
        Booking booking = theatre.bookTickets(show, "E1,E2", "CINE10");

        assertAmount(800.0, booking.getBaseAmount(), "base amount before discount");
        assertAmount(720.0, booking.getAmountPaid(), "amount after 10% coupon");
        assertAmount(80.0, booking.getDiscountAmount(), "discount amount");
        assertEquals("CINE10", booking.getCoupon().name(), "coupon recorded on booking");

        Booking lowerCase = theatre.bookTickets(show, "E3", "cine10");
        assertAmount(360.0, lowerCase.getAmountPaid(), "lower-case coupon code should also work");
    }

    /** TC-06: an invalid coupon stops the booking, and no seats are reserved. */
    private static void bookingWithInvalidCoupon() throws Exception {
        Theatre theatre = Theatre.withSampleShows();
        Show show = theatre.getShows().get(0);

        Exception rejected = expectRejectedException(() -> theatre.bookTickets(show, "A5", "SAVE50"));
        assertTrue(rejected instanceof InvalidCouponException, "expected InvalidCouponException");
        assertTrue(show.isAvailable(Seat.parse("A5")), "A5 must stay free when the coupon is invalid");
        assertEquals(0, theatre.getBookings().size(), "no booking should be stored");
    }

    /** TC-07: cancelling releases the seats, refunds 80% and keeps the record with status CANCELLED. */
    private static void successfulCancellation() throws Exception {
        Theatre theatre = Theatre.withSampleShows();
        Show show = theatre.getShows().get(0);
        Booking booking = theatre.bookTickets(show, "A7,A8", null);   // 2 x Rs. 150 = Rs. 300

        double refund = theatre.cancelBooking(booking.getBookingId());

        assertAmount(240.0, refund, "80% of Rs. 300");
        assertEquals(BookingStatus.CANCELLED, booking.getStatus(), "status should be CANCELLED");
        assertTrue(show.isAvailable(Seat.parse("A7")), "A7 should be released");
        assertTrue(show.isAvailable(Seat.parse("A8")), "A8 should be released");
        assertEquals(1, theatre.getBookings().size(), "cancelled booking should remain in the history");
    }

    /** TC-08: unknown, malformed and empty booking IDs are all rejected. */
    private static void invalidBookingId() throws Exception {
        Theatre theatre = Theatre.withSampleShows();
        theatre.bookTickets(theatre.getShows().get(0), "A1", null);

        String unknown = expectRejected(() -> theatre.cancelBooking("BK9999"));
        assertTrue(unknown.contains("No booking found"), "unknown ID message: " + unknown);

        String malformed = expectRejected(() -> theatre.cancelBooking("XYZ"));
        assertTrue(malformed.contains("Invalid booking ID"), "malformed ID message: " + malformed);

        expectRejected(() -> theatre.cancelBooking("BK"));
        expectRejected(() -> theatre.cancelBooking(""));
        expectRejected(() -> theatre.cancelBooking(null));
    }

    /** TC-09: a second cancellation is refused, no second refund is issued, and a new booking's seat is untouched. */
    private static void doubleCancellation() throws Exception {
        Theatre theatre = Theatre.withSampleShows();
        Show show = theatre.getShows().get(0);
        Booking first = theatre.bookTickets(show, "B2", null);
        assertAmount(120.0, theatre.cancelBooking(first.getBookingId()), "first refund (80% of Rs. 150)");

        // Another customer books the freed seat. The old booking must not release it again.
        Booking second = theatre.bookTickets(show, "B2", null);

        String message = expectRejected(() -> theatre.cancelBooking(first.getBookingId()));
        assertTrue(message.contains("already cancelled"), "double cancel message: " + message);
        assertAmount(120.0, first.getRefundAmount(), "refund must not be recalculated or repeated");
        assertEquals(BookingStatus.CONFIRMED, second.getStatus(), "second booking should still be confirmed");
        assertTrue(!show.isAvailable(Seat.parse("B2")), "B2 must still be booked for the second customer");
    }

    /** TC-10: bookings in one show never change the grid of another show (FR-8). */
    private static void independentShowGrids() throws Exception {
        Theatre theatre = Theatre.withSampleShows();
        Show interstellarMorning = theatre.getShows().get(0);   // Interstellar 10:00 AM
        Show interstellarEvening = theatre.getShows().get(1);   // Interstellar 7:30 PM
        Show dune = theatre.getShows().get(2);                  // Dune: Part Two 4:15 PM

        theatre.bookTickets(interstellarEvening, "A1,A2,C5", null);

        assertTrue(!interstellarEvening.isAvailable(Seat.parse("A1")), "A1 booked in the evening show");
        assertTrue(interstellarMorning.isAvailable(Seat.parse("A1")), "A1 must stay free in the morning show");
        assertTrue(dune.isAvailable(Seat.parse("A1")), "A1 must stay free in another movie's show");
        assertEquals(TOTAL_SEATS, interstellarMorning.countAvailableSeats(), "morning show should be untouched");
        assertEquals(TOTAL_SEATS - 3, interstellarEvening.countAvailableSeats(), "evening show count");

        Booking sameSeatElsewhere = theatre.bookTickets(interstellarMorning, "A1", null);
        assertEquals(BookingStatus.CONFIRMED, sameSeatElsewhere.getStatus(),
                "A1 should be bookable in the other show");
    }

    // ---------------------------------------------------------------- TC-11 to TC-15 (extra)

    /** TC-11: the same seat repeated in one request is rejected. */
    private static void duplicateSeatSelection() throws Exception {
        Theatre theatre = Theatre.withSampleShows();
        Show show = theatre.getShows().get(0);
        String message = expectRejected(() -> theatre.bookTickets(show, "B3,b3", null));
        assertTrue(message.contains("more than once"), "duplicate message: " + message);
        assertTrue(show.isAvailable(Seat.parse("B3")), "B3 must stay free");
    }

    /** TC-12: each category has the right price and rows, read from the enum. */
    private static void seatPricesFromEnum() throws Exception {
        assertAmount(150.0, SeatType.REGULAR.getPrice(), "REGULAR price");
        assertAmount(250.0, SeatType.PREMIUM.getPrice(), "PREMIUM price");
        assertAmount(400.0, SeatType.VIP.getPrice(), "VIP price");
        assertEquals(SeatType.REGULAR, SeatType.forRow('B'), "row B category");
        assertEquals(SeatType.PREMIUM, SeatType.forRow('D'), "row D category");
        assertEquals(SeatType.VIP, SeatType.forRow('E'), "row E category");
    }

    /** TC-13: the static counter gives unique, increasing IDs. */
    private static void uniqueBookingIds() throws Exception {
        Theatre theatre = Theatre.withSampleShows();
        Show show = theatre.getShows().get(0);
        Booking first = theatre.bookTickets(show, "A1", null);
        Booking second = theatre.bookTickets(show, "A2", null);
        int firstNumber = Integer.parseInt(first.getBookingId().substring(2));
        int secondNumber = Integer.parseInt(second.getBookingId().substring(2));
        assertTrue(!first.getBookingId().equals(second.getBookingId()), "IDs must differ");
        assertEquals(firstNumber + 1, secondNumber, "IDs should increase by one");
    }

    /** TC-14: empty inputs are rejected with a clear message. */
    private static void emptyInput() throws Exception {
        Theatre theatre = Theatre.withSampleShows();
        expectRejected(() -> Seat.parseList(""));
        expectRejected(() -> Seat.parse("  "));
        expectRejected(() -> theatre.findBooking(""));
        expectRejected(() -> theatre.findBooking("   "));
    }

    /** TC-15: calculateTotal() and calculateTotal(String) return the expected totals. */
    private static void overloadedCalculateTotal() throws Exception {
        Theatre theatre = Theatre.withSampleShows();
        Show show = theatre.getShows().get(2);
        Booking booking = theatre.bookTickets(show, "C1,D1,E1", null);   // 250 + 250 + 400 = 900

        assertAmount(900.0, booking.calculateTotal(), "calculateTotal() without a coupon");
        assertAmount(900.0, booking.calculateTotal(null), "null coupon means no discount");
        assertAmount(900.0, booking.calculateTotal("   "), "blank coupon means no discount");
        assertAmount(810.0, booking.calculateTotal("CINE10"), "calculateTotal(\"CINE10\") gives 10% off");
        expectRejected(() -> booking.calculateTotal("FAKE"));
    }

    // ---------------------------------------------------------------- assertion helpers

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(message + " | expected <" + expected + "> but was <" + actual + ">");
        }
    }

    private static void assertAmount(double expected, double actual, String message) {
        if (Math.abs(expected - actual) > DELTA) {
            throw new AssertionError(message + " | expected Rs. " + expected + " but was Rs. " + actual);
        }
    }

    /** Runs an action that must fail with a BookingException and returns its message. */
    private static String expectRejected(TestBody action) throws Exception {
        return expectRejectedException(action).getMessage();
    }

    private static Exception expectRejectedException(TestBody action) throws Exception {
        try {
            action.run();
        } catch (BookingException e) {
            return e;
        }
        throw new AssertionError("expected the action to be rejected, but it succeeded");
    }
}
