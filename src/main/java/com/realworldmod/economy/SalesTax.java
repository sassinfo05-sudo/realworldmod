package com.realworldmod.economy;

/**
 * Pure flat-rate municipal sales tax calculation (Section 7's "no
 * municipal border/tax-rate system" gap, deliberately scoped down: there
 * are no city boundaries in the world yet for a rate to vary *by*, so
 * this is one flat rate everywhere rather than a real per-city system).
 */
public final class SalesTax {
    public static final double RATE = 0.08;

    private SalesTax() {
    }

    public static long taxCents(long priceCents) {
        if (priceCents < 0) {
            throw new IllegalArgumentException("priceCents must be >= 0, was " + priceCents);
        }
        return Math.round(priceCents * RATE);
    }
}
