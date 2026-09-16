package com.realworldmod.economy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SalesTaxTest {
    @Test
    void taxOnZeroIsZero() {
        assertEquals(0, SalesTax.taxCents(0));
    }

    @Test
    void taxIsAnExactPercentageWhenItDividesEvenly() {
        assertEquals(80, SalesTax.taxCents(1000));
        assertEquals(120, SalesTax.taxCents(1500));
    }

    @Test
    void taxIsRoundedToTheNearestCentOtherwise() {
        assertEquals(120, SalesTax.taxCents(1501));
        assertEquals(124, SalesTax.taxCents(1547));
    }

    @Test
    void rejectsANegativePrice() {
        assertThrows(IllegalArgumentException.class, () -> SalesTax.taxCents(-1));
    }
}
