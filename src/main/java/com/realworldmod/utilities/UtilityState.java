package com.realworldmod.utilities;

/** A household's utility hookup status (Section 9): whether power is connected, and any accrued unpaid debt. */
public record UtilityState(boolean powerConnected, long unpaidCents) {
    public static UtilityState connected() {
        return new UtilityState(true, 0);
    }
}
