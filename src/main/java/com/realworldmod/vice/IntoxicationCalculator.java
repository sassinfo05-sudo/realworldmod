package com.realworldmod.vice;

/**
 * Pure drink-count-to-intoxication-tier rules, kept free of any Minecraft
 * dependency so the thresholds are unit testable on their own — the same
 * shape {@code medical.FallInjuryCalculator} already established for leg
 * injury severity.
 */
public final class IntoxicationCalculator {
    public static final int MAX_LEVEL = 3;

    private static final int TIPSY_SLOWNESS_AMPLIFIER = 0;
    private static final int DRUNK_SLOWNESS_AMPLIFIER = 1;
    private static final int VERY_DRUNK_SLOWNESS_AMPLIFIER = 3;

    private static final int TIPSY_DURATION_TICKS = 20 * 30;
    private static final int DRUNK_DURATION_TICKS = 20 * 60;
    private static final int VERY_DRUNK_DURATION_TICKS = 20 * 90;

    private IntoxicationCalculator() {
    }

    public static int nextLevel(int currentLevel) {
        return Math.min(currentLevel + 1, MAX_LEVEL);
    }

    /** Minecraft's status-effect amplifier convention: 0 = level I, 1 = level II, etc. -1 means "no effect". */
    public static int slownessAmplifierFor(int level) {
        return switch (level) {
            case 0 -> -1;
            case 1 -> TIPSY_SLOWNESS_AMPLIFIER;
            case 2 -> DRUNK_SLOWNESS_AMPLIFIER;
            default -> VERY_DRUNK_SLOWNESS_AMPLIFIER;
        };
    }

    public static int slownessDurationTicksFor(int level) {
        return switch (level) {
            case 0 -> 0;
            case 1 -> TIPSY_DURATION_TICKS;
            case 2 -> DRUNK_DURATION_TICKS;
            default -> VERY_DRUNK_DURATION_TICKS;
        };
    }

    public static boolean causesNausea(int level) {
        return level >= 2;
    }
}
