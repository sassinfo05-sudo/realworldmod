package com.realworldmod.underworld;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NarcoticsSeverityTest {
    @Test
    void aFirstDealIsTheBaseSeverity() {
        assertEquals(NarcoticsSeverity.BASE_SEVERITY, NarcoticsSeverity.forStreak(1));
    }

    @Test
    void severityScalesWithStreak() {
        assertEquals(NarcoticsSeverity.BASE_SEVERITY + 1, NarcoticsSeverity.forStreak(2));
        assertEquals(NarcoticsSeverity.BASE_SEVERITY + 2, NarcoticsSeverity.forStreak(3));
    }

    @Test
    void severityClampsAtTheMaximum() {
        assertEquals(NarcoticsSeverity.MAX_SEVERITY, NarcoticsSeverity.forStreak(100));
    }

    @Test
    void aZeroStreakStillReturnsTheBaseSeverity() {
        assertEquals(NarcoticsSeverity.BASE_SEVERITY, NarcoticsSeverity.forStreak(0));
    }
}
