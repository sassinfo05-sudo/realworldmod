package com.realworldmod.crime;

/** Pure clamp arithmetic for a player's wanted level (Section 7's municipal-law/police framing). */
public final class WantedLevelMath {
    public static final int MIN = 0;
    public static final int MAX = 5;

    private WantedLevelMath() {
    }

    public static int increase(int current, int amount) {
        return clamp(current + amount);
    }

    public static int decay(int current, int amount) {
        return clamp(current - amount);
    }

    private static int clamp(int level) {
        return Math.max(MIN, Math.min(MAX, level));
    }
}
