package com.cinebook.model;

import com.cinebook.exception.BookingException;
import com.cinebook.exception.InvalidCouponException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A customer's booking for one or more seats in a single show.
 * It implements {@link Bookable}: {@link #bookSeats()} confirms the booking and
 * {@link #cancelBooking()} releases the seats and calculates the refund.
 */
public class Booking implements Bookable {

    /** Share of the amount paid that is refunded on cancellation (80%). */
    public static final double REFUND_RATE = 0.80;

    private static final String ID_PREFIX = "BK";

    /** Static counter that generates unique booking IDs. The first ID issued is BK1001. */
    private static int bookingCounter = 1000;

    private final Show show;
    private final List<Seat> seats;
    private final Coupon coupon;          // null when no coupon was used
    private final double baseAmount;      // total before any discount
    private final double amountPaid;      // total after the coupon discount

    private String bookingId;             // assigned only when the booking is confirmed
    private BookingStatus status;
    private double refundAmount;

    /**
     * Creates a pending booking. Validation happens here, but no seat is reserved yet.
     *
     * @param show       the show being booked
     * @param seats      the seats to reserve (must be non-empty and unique)
     * @param couponCode an optional coupon code; null or blank means no coupon
     * @throws BookingException        if the show or seat list is missing, or a seat repeats
     * @throws InvalidCouponException  if the coupon code is not recognised
     */
    public Booking(Show show, List<Seat> seats, String couponCode)
            throws BookingException, InvalidCouponException {
        if (show == null) {
            throw new BookingException("Please select a show first.");
        }
        if (seats == null || seats.isEmpty()) {
            throw new BookingException("Please select at least one seat.");
        }
        Set<Seat> uniqueSeats = new HashSet<>();
        for (Seat seat : seats) {
            if (!uniqueSeats.add(seat)) {
                throw new BookingException("Seat " + seat.getCode() + " was selected more than once.");
            }
        }

        this.show = show;
        this.seats = Collections.unmodifiableList(new ArrayList<>(seats));
        this.coupon = Coupon.parseOptional(couponCode);
        this.baseAmount = calculateTotal();
        this.amountPaid = applyDiscount(baseAmount, coupon);
        this.status = BookingStatus.PENDING;
    }

    /**
     * Total price of the seats, with no coupon applied. Each price comes from the SeatType enum.
     */
    public double calculateTotal() {
        double total = 0;
        for (Seat seat : seats) {
            total += seat.getSeatType().getPrice();
        }
        return Money.round(total);
    }

    /**
     * Total price after applying a coupon. A null or blank code returns the full price.
     *
     * @throws InvalidCouponException if the coupon code is not recognised
     */
    public double calculateTotal(String couponCode) throws InvalidCouponException {
        return applyDiscount(calculateTotal(), Coupon.parseOptional(couponCode));
    }

    private static double applyDiscount(double amount, Coupon appliedCoupon) {
        if (appliedCoupon == null) {
            return amount;
        }
        return Money.round(amount * (100 - appliedCoupon.getDiscountPercent()) / 100.0);
    }

    @Override
    public void bookSeats() throws BookingException {
        if (status != BookingStatus.PENDING) {
            throw new BookingException("This booking has already been processed.");
        }
        // Throws before changing anything if any seat is taken, so no ID is wasted on a failure.
        show.reserveSeats(seats);
        bookingId = generateBookingId();
        status = BookingStatus.CONFIRMED;
    }

    @Override
    public double cancelBooking() throws BookingException {
        if (status == BookingStatus.CANCELLED) {
            throw new BookingException("Booking " + bookingId + " is already cancelled. No refund was issued again.");
        }
        if (status == BookingStatus.PENDING) {
            throw new BookingException("This booking was never confirmed, so it cannot be cancelled.");
        }
        show.releaseSeats(seats);
        refundAmount = Money.round(amountPaid * REFUND_RATE);
        status = BookingStatus.CANCELLED;
        return refundAmount;
    }

    private static String generateBookingId() {
        bookingCounter++;
        return ID_PREFIX + bookingCounter;
    }

    public String getBookingId() {
        return bookingId;
    }

    public Show getShow() {
        return show;
    }

    /** @return an unmodifiable view of the seats in this booking */
    public List<Seat> getSeats() {
        return seats;
    }

    /** @return the seat codes separated by commas, e.g. "C3, C4" */
    public String getSeatCodes() {
        List<String> codes = new ArrayList<>();
        for (Seat seat : seats) {
            codes.add(seat.getCode());
        }
        return String.join(", ", codes);
    }

    /** @return the seat categories in this booking, e.g. "Premium" or "Regular, VIP" */
    public String getSeatCategory() {
        Set<String> categories = new LinkedHashSet<>();
        for (Seat seat : seats) {
            categories.add(seat.getSeatType().getDisplayName());
        }
        return String.join(", ", categories);
    }

    /** @return the coupon used, or null if none was used */
    public Coupon getCoupon() {
        return coupon;
    }

    public double getBaseAmount() {
        return baseAmount;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    /** @return the discount given by the coupon, 0 if none */
    public double getDiscountAmount() {
        return Money.round(baseAmount - amountPaid);
    }

    public BookingStatus getStatus() {
        return status;
    }

    /** @return the refund given on cancellation, 0 if the booking is not cancelled */
    public double getRefundAmount() {
        return refundAmount;
    }

    /** @return a formatted booking summary used for confirmations and booking details */
    public String getReceipt() {
        StringBuilder receipt = new StringBuilder();
        receipt.append(String.format("  Booking ID   : %s%n", bookingId == null ? "Not issued" : bookingId));
        receipt.append(String.format("  Movie        : %s%n", show.getMovieName()));
        receipt.append(String.format("  Show time    : %s%n", show.getTimeText()));
        receipt.append(String.format("  Seats        : %s%n", getSeatCodes()));
        receipt.append(String.format("  Category     : %s%n", getSeatCategory()));
        receipt.append(String.format("  Base amount  : %s%n", Money.format(baseAmount)));
        receipt.append(String.format("  Coupon       : %s%n", coupon == null ? "None" : coupon.getDescription()));
        receipt.append(String.format("  Discount     : %s%n", Money.format(getDiscountAmount())));
        receipt.append(String.format("  Total amount : %s%n", Money.format(amountPaid)));
        receipt.append(String.format("  Status       : %s%n", status));
        if (status == BookingStatus.CANCELLED) {
            receipt.append(String.format("  Refund       : %s (80%% of amount paid)%n", Money.format(refundAmount)));
        }
        return receipt.toString();
    }

    @Override
    public String toString() {
        return (bookingId == null ? "Not issued" : bookingId) + " | " + show.getTitle() + " | " + getSeatCodes();
    }
}
