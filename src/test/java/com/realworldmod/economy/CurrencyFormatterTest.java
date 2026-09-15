package com.realworldmod.economy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CurrencyFormatterTest {
    @Test
    void formatsWholeDollars() {
        assertEquals("$5.00", CurrencyFormatter.format(500));
    }

    @Test
    void formatsCentsWithLeadingZero() {
        assertEquals("$5.05", CurrencyFormatter.format(505));
    }

    @Test
    void formatsZero() {
        assertEquals("$0.00", CurrencyFormatter.format(0));
    }

    @Test
    void formatsNegativeAmounts() {
        assertEquals("-$5.00", CurrencyFormatter.format(-500));
    }
}
