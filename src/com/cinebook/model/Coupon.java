package com.cinebook.model;

import com.cinebook.exception.InvalidCouponException;

import java.util.Locale;

/**
 * Discount coupons accepted by the cinema (FR-6).
 * The enum name is the coupon code the customer types, e.g. CINE10.
 */
public enum Coupon {

    CINE10(10);

    private final int discountPercent;

    Coupon(int discountPercent) {
        this.discountPercent = discountPercent;
    }

    /** @return the percentage discount this coupon gives, e.g. 10 for CINE10 */
    public int getDiscountPercent() {
        return discountPercent;
    }

    /** @return a short description such as "CINE10 (10% off)" */
    public String getDescription() {
        return name() + " (" + discountPercent + "% off)";
    }

    /**
     * Looks up a coupon by code. The lookup ignores case and surrounding spaces.
     *
     * @param code the code typed by the customer
     * @return the matching coupon
     * @throws InvalidCouponException if the code is empty or unknown
     */
    public static Coupon parse(String code) throws InvalidCouponException {
        if (code == null || code.trim().isEmpty()) {
            throw new InvalidCouponException("Coupon code cannot be empty.");
        }
        String normalised = code.trim().toUpperCase(Locale.ENGLISH);
        for (Coupon coupon : values()) {
            if (coupon.name().equals(normalised)) {
                return coupon;
            }
        }
        throw new InvalidCouponException("Coupon code '" + code.trim() + "' is not valid.");
    }

    /**
     * Like {@link #parse(String)}, but a blank code means "no coupon" and returns null.
     */
    public static Coupon parseOptional(String code) throws InvalidCouponException {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        return parse(code);
    }
}
