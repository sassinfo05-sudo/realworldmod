package com.realworldmod.utilities;

/**
 * Pure state transitions for a water account — the same shape
 * {@link UtilityBillingMath} uses for power, kept as its own class rather
 * than a shared generic since the two utilities are billed, disconnected,
 * and (eventually) exposed to the player entirely independently.
 */
public final class WaterBillingMath {
    private WaterBillingMath() {
    }

    /** Result of an automatic billing cycle: paying in full reconnects and clears debt; failing disconnects and adds to it. */
    public static WaterState afterBillingAttempt(WaterState previous, long billCents, boolean paymentSucceeded) {
        if (paymentSucceeded) {
            return new WaterState(true, 0);
        }
        return new WaterState(false, previous.unpaidCents() + billCents);
    }

    /** Result of the player proactively paying off their debt; a failed attempt leaves the state unchanged. */
    public static WaterState afterManualPayment(WaterState previous, boolean paymentSucceeded) {
        if (paymentSucceeded) {
            return new WaterState(true, 0);
        }
        return previous;
    }
}
