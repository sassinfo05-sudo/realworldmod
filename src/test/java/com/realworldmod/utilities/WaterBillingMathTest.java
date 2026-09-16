package com.realworldmod.utilities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WaterBillingMathTest {
    @Test
    void successfulBillingReconnectsAndClearsDebt() {
        WaterState previous = new WaterState(false, 1500);
        WaterState next = WaterBillingMath.afterBillingAttempt(previous, 800, true);
        assertTrue(next.connected());
        assertEquals(0, next.unpaidCents());
    }

    @Test
    void failedBillingDisconnectsAndAccruesDebt() {
        WaterState previous = WaterState.freshlyConnected();
        WaterState next = WaterBillingMath.afterBillingAttempt(previous, 800, false);
        assertFalse(next.connected());
        assertEquals(800, next.unpaidCents());
    }

    @Test
    void repeatedFailedBillingAccumulatesDebt() {
        WaterState state = WaterState.freshlyConnected();
        state = WaterBillingMath.afterBillingAttempt(state, 800, false);
        state = WaterBillingMath.afterBillingAttempt(state, 800, false);
        assertEquals(1600, state.unpaidCents());
        assertFalse(state.connected());
    }

    @Test
    void successfulManualPaymentReconnects() {
        WaterState previous = new WaterState(false, 2000);
        WaterState next = WaterBillingMath.afterManualPayment(previous, true);
        assertTrue(next.connected());
        assertEquals(0, next.unpaidCents());
    }

    @Test
    void failedManualPaymentLeavesStateUnchanged() {
        WaterState previous = new WaterState(false, 2000);
        WaterState next = WaterBillingMath.afterManualPayment(previous, false);
        assertEquals(previous, next);
    }
}
