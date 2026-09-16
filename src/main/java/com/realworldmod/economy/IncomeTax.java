package com.realworldmod.economy;

/**
 * Pure flat-rate income tax withholding on wages (Section 7's tax-rate
 * gap, the income-tax third alongside {@link SalesTax} and
 * {@code property.PropertyTaxService}).
 */
public final class IncomeTax {
    public static final double RATE = 0.05;

    private IncomeTax() {
    }

    public static long taxCents(long grossWageCents) {
        if (grossWageCents < 0) {
            throw new IllegalArgumentException("grossWageCents must be >= 0, was " + grossWageCents);
        }
        return Math.round(grossWageCents * RATE);
    }
}
