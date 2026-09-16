package com.realworldmod.utilities;

/**
 * Pure state transitions for a utility account, kept independent of how the
 * payment attempt itself is made (that's {@code BankService}'s job — see
 * {@code UtilityService}).
 */
public final class UtilityBillingMath {
    private UtilityBillingMath() {
    }

    /** Result of an automatic billing cycle: paying in full reconnects and clears debt; failing disconnects and adds to it. */
    public static UtilityState afterBillingAttempt(UtilityState previous, long billCents, boolean paymentSucceeded) {
        if (paymentSucceeded) {
            return new UtilityState(true, 0);
        }
        return new UtilityState(false, previous.unpaidCents() + billCents);
    }

    /** Result of the player proactively paying off their debt; a failed attempt leaves the state unchanged. */
    public static UtilityState afterManualPayment(UtilityState previous, boolean paymentSucceeded) {
        if (paymentSucceeded) {
            return new UtilityState(true, 0);
        }
        return previous;
    }
}
