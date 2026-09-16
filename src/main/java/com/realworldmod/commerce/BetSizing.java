package com.realworldmod.commerce;

/**
 * Shared wager bounds for every casino table that lets the player choose a
 * bet size (slice 53's Court Registry priority list called this out once
 * all five tables existed at one fixed bet each). {@link #clamp} is the
 * single place every service applies these bounds, so a client that sends
 * a bogus or out-of-range wager can never withdraw more than
 * {@link #MAX_BET_CENTS} or less than {@link #MIN_BET_CENTS}.
 */
public final class BetSizing {
    public static final long MIN_BET_CENTS = 500;
    public static final long MAX_BET_CENTS = 5000;
    public static final long STEP_CENTS = 500;
    public static final long DEFAULT_BET_CENTS = 1000;

    private BetSizing() {
    }

    public static long clamp(long requestedCents) {
        return Math.max(MIN_BET_CENTS, Math.min(MAX_BET_CENTS, requestedCents));
    }
}
