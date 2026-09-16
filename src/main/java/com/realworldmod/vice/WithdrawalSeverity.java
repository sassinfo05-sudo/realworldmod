package com.realworldmod.vice;

/**
 * Pure streak-to-severity rule for repeated nicotine withdrawal, mirroring
 * {@link HangoverSeverity} exactly for the other vice system: kept free of
 * any Minecraft dependency so the thresholds are unit testable on their
 * own.
 */
public final class WithdrawalSeverity {
    /** How long a player can go without another withdrawal before their streak resets to zero. */
    public static final long STREAK_RESET_TICKS = 20L * 60L * 30L;
    public static final int MAX_SEVERITY = 3;

    private WithdrawalSeverity() {
    }

    /** Maps a (1-indexed) consecutive-withdrawal streak to a severity level, capped at {@link #MAX_SEVERITY}. */
    public static int forStreak(int streak) {
        return Math.min(MAX_SEVERITY, Math.max(1, streak));
    }
}
