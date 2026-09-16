package com.realworldmod.underworld;

/**
 * The meth economy's own crime-severity tier, mirroring
 * {@link NarcoticsSeverity}'s "escalate with a consecutive-deal streak"
 * shape but starting and capping higher — real-world drug law treats a
 * harder drug more harshly, and this mod's two lab types now do too.
 */
public final class MethSeverity {
    public static final int BASE_SEVERITY = 3;
    public static final int MAX_SEVERITY = 5;
    /** How long a dealer can go without another deal before their streak resets to zero. */
    public static final long STREAK_RESET_TICKS = 20L * 60L * 5L;

    private MethSeverity() {
    }

    /** Maps a (1-indexed) consecutive-deal streak to a severity level, capped at {@link #MAX_SEVERITY}. */
    public static int forStreak(int streak) {
        return Math.min(MAX_SEVERITY, BASE_SEVERITY + Math.max(0, streak - 1));
    }
}
