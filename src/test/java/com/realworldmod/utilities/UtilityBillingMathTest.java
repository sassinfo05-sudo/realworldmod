package com.realworldmod.utilities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UtilityBillingMathTest {
    @Test
    void successfulBillingReconnectsAndClearsDebt() {
        UtilityState previous = new UtilityState(false, 1500);
        UtilityState next = UtilityBillingMath.afterBillingAttempt(previous, 1000, true);
        assertTrue(next.powerConnected());
        assertEquals(0, next.unpaidCents());
    }

    @Test
    void failedBillingDisconnectsAndAccruesDebt() {
        UtilityState previous = UtilityState.connected();
        UtilityState next = UtilityBillingMath.afterBillingAttempt(previous, 1000, false);
        assertFalse(next.powerConnected());
        assertEquals(1000, next.unpaidCents());
    }

    @Test
    void repeatedFailedBillingAccumulatesDebt() {
        UtilityState state = UtilityState.connected();
        state = UtilityBillingMath.afterBillingAttempt(state, 1000, false);
        state = UtilityBillingMath.afterBillingAttempt(state, 1000, false);
        assertEquals(2000, state.unpaidCents());
        assertFalse(state.powerConnected());
    }

    @Test
    void successfulManualPaymentReconnects() {
        UtilityState previous = new UtilityState(false, 2000);
        UtilityState next = UtilityBillingMath.afterManualPayment(previous, true);
        assertTrue(next.powerConnected());
        assertEquals(0, next.unpaidCents());
    }

    @Test
    void failedManualPaymentLeavesStateUnchanged() {
        UtilityState previous = new UtilityState(false, 2000);
        UtilityState next = UtilityBillingMath.afterManualPayment(previous, false);
        assertEquals(previous, next);
    }
}
