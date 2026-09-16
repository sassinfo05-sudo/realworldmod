package com.realworldmod.economy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IncomeTaxTest {
    @Test
    void taxOnZeroIsZero() {
        assertEquals(0, IncomeTax.taxCents(0));
    }

    @Test
    void taxIsFivePercentRoundedToTheNearestCent() {
        assertEquals(25, IncomeTax.taxCents(500));
        assertEquals(50, IncomeTax.taxCents(1000));
    }

    @Test
    void rejectsANegativeWage() {
        assertThrows(IllegalArgumentException.class, () -> IncomeTax.taxCents(-1));
    }
}
