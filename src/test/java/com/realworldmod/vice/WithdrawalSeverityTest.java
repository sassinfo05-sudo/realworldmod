package com.realworldmod.vice;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WithdrawalSeverityTest {
    @Test
    void aFirstWithdrawalIsSeverityOne() {
        assertEquals(1, WithdrawalSeverity.forStreak(1));
    }

    @Test
    void severityScalesWithStreak() {
        assertEquals(2, WithdrawalSeverity.forStreak(2));
        assertEquals(3, WithdrawalSeverity.forStreak(3));
    }

    @Test
    void severityClampsAtTheMaximum() {
        assertEquals(WithdrawalSeverity.MAX_SEVERITY, WithdrawalSeverity.forStreak(WithdrawalSeverity.MAX_SEVERITY));
        assertEquals(WithdrawalSeverity.MAX_SEVERITY, WithdrawalSeverity.forStreak(WithdrawalSeverity.MAX_SEVERITY + 10));
    }

    @Test
    void severityNeverGoesBelowOneEvenForAZeroStreak() {
        assertEquals(1, WithdrawalSeverity.forStreak(0));
    }
}
