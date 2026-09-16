package com.realworldmod.vice;

/**
 * Pure streak-to-severity rule for repeated hangovers, the same shape
 * {@code IntoxicationCalculator} already established for intoxication
 * tiers: kept free of any Minecraft dependency so the thresholds are unit
 * testable on their own.
 */
public final class HangoverSeverity {
    /** How long a player can go without another hangover before their streak resets to zero. */
    public static final long STREAK_RESET_TICKS = 20L * 60L * 30L;
    public static final int MAX_SEVERITY = 3;

    private HangoverSeverity() {
    }

    /** Maps a (1-indexed) consecutive-hangover streak to a severity level, capped at {@link #MAX_SEVERITY}. */
    public static int forStreak(int streak) {
        return Math.min(MAX_SEVERITY, Math.max(1, streak));
    }
}
