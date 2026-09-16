package com.realworldmod.vice;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HangoverSeverityTest {
    @Test
    void aFirstHangoverIsSeverityOne() {
        assertEquals(1, HangoverSeverity.forStreak(1));
    }

    @Test
    void severityScalesWithStreak() {
        assertEquals(2, HangoverSeverity.forStreak(2));
        assertEquals(3, HangoverSeverity.forStreak(3));
    }

    @Test
    void severityClampsAtTheMaximum() {
        assertEquals(HangoverSeverity.MAX_SEVERITY, HangoverSeverity.forStreak(HangoverSeverity.MAX_SEVERITY));
        assertEquals(HangoverSeverity.MAX_SEVERITY, HangoverSeverity.forStreak(HangoverSeverity.MAX_SEVERITY + 10));
    }

    @Test
    void severityNeverGoesBelowOneEvenForAZeroStreak() {
        assertEquals(1, HangoverSeverity.forStreak(0));
    }
}
