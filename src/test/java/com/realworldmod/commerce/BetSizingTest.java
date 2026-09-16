package com.realworldmod.commerce;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BetSizingTest {
    @Test
    void aBetWithinRangeIsUnchanged() {
        assertEquals(2000, BetSizing.clamp(2000));
    }

    @Test
    void aBetBelowTheMinimumClampsUpToTheMinimum() {
        assertEquals(BetSizing.MIN_BET_CENTS, BetSizing.clamp(1));
    }

    @Test
    void aBetAboveTheMaximumClampsDownToTheMaximum() {
        assertEquals(BetSizing.MAX_BET_CENTS, BetSizing.clamp(999_999));
    }

    @Test
    void zeroClampsUpToTheMinimum() {
        assertEquals(BetSizing.MIN_BET_CENTS, BetSizing.clamp(0));
    }
}
