package com.realworldmod.medical;

/**
 * Pure damage-to-injury and injury-to-effect rules for leg trauma, kept
 * free of any Minecraft entity/world dependency so the thresholds are unit
 * testable on their own.
 */
public final class FallInjuryCalculator {
    private static final float BRUISE_THRESHOLD = 4.0f;
    private static final float FRACTURE_THRESHOLD = 8.0f;

    private static final int BRUISED_SLOWNESS_AMPLIFIER = 0;
    private static final int FRACTURED_SLOWNESS_AMPLIFIER = 2;

    private static final int BRUISED_DURATION_TICKS = 20 * 30;
    private static final int FRACTURED_DURATION_TICKS = 20 * 120;

    private FallInjuryCalculator() {
    }

    public static LegInjury fromFallDamage(float damageTaken) {
        if (damageTaken < BRUISE_THRESHOLD) {
            return LegInjury.NONE;
        }
        if (damageTaken < FRACTURE_THRESHOLD) {
            return LegInjury.BRUISED;
        }
        return LegInjury.FRACTURED;
    }

    /** Minecraft's status-effect amplifier convention: 0 = level I, 1 = level II, etc. */
    public static int slownessAmplifierFor(LegInjury injury) {
        return switch (injury) {
            case NONE -> -1;
            case BRUISED -> BRUISED_SLOWNESS_AMPLIFIER;
            case FRACTURED -> FRACTURED_SLOWNESS_AMPLIFIER;
        };
    }

    public static int slownessDurationTicksFor(LegInjury injury) {
        return switch (injury) {
            case NONE -> 0;
            case BRUISED -> BRUISED_DURATION_TICKS;
            case FRACTURED -> FRACTURED_DURATION_TICKS;
        };
    }
}
