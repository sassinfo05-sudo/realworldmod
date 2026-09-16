package com.realworldmod.underworld;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MethSeverityTest {
    @Test
    void aFirstDealIsTheBaseSeverity() {
        assertEquals(MethSeverity.BASE_SEVERITY, MethSeverity.forStreak(1));
    }

    @Test
    void severityScalesWithStreak() {
        assertEquals(MethSeverity.BASE_SEVERITY + 1, MethSeverity.forStreak(2));
        assertEquals(MethSeverity.BASE_SEVERITY + 2, MethSeverity.forStreak(3));
    }

    @Test
    void severityClampsAtTheMaximum() {
        assertEquals(MethSeverity.MAX_SEVERITY, MethSeverity.forStreak(100));
    }

    @Test
    void aZeroStreakStillReturnsTheBaseSeverity() {
        assertEquals(MethSeverity.BASE_SEVERITY, MethSeverity.forStreak(0));
    }

    @Test
    void isHarsherThanNarcoticsSeverityAtEveryStreak() {
        for (int streak = 0; streak <= 10; streak++) {
            assertTrue(MethSeverity.forStreak(streak) >= NarcoticsSeverity.forStreak(streak));
        }
    }
}
