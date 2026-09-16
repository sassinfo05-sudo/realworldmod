package com.realworldmod.underworld;

/**
 * Pure streak-to-severity rule giving narcotics dealing its own distinct
 * crime-severity tier, instead of sharing the generic severity-2 value
 * {@code vehicle.CarEntity.CAR_THEFT_SEVERITY} also uses — closing the
 * rest of slice 37/58's "no distinct narcotics crime severity tier" gap.
 * The same "map a consecutive-episode streak to an escalating severity"
 * shape {@code vice.HangoverSeverity}/{@code vice.WithdrawalSeverity}
 * already established.
 */
public final class NarcoticsSeverity {
    public static final int BASE_SEVERITY = 2;
    public static final int MAX_SEVERITY = 4;
    /** How long a dealer can go without another deal before their streak resets to zero. */
    public static final long STREAK_RESET_TICKS = 20L * 60L * 5L;

    private NarcoticsSeverity() {
    }

    /** Maps a (1-indexed) consecutive-deal streak to a severity level, capped at {@link #MAX_SEVERITY}. */
    public static int forStreak(int streak) {
        return Math.min(MAX_SEVERITY, BASE_SEVERITY + Math.max(0, streak - 1));
    }
}
