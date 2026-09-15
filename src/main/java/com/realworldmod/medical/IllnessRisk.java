package com.realworldmod.medical;

/**
 * Pure exposure-to-illness rule (Section 5: "staying in freezing rain
 * causes illness"). Not yet gated by biome temperature — see ROADMAP.md —
 * any continuous rain exposure counts for this slice.
 */
public final class IllnessRisk {
    public static final int EXPOSURE_TICKS_TO_ILLNESS = 20 * 60;

    private IllnessRisk() {
    }

    /** Exposure resets the instant the player is no longer exposed, rather than decaying gradually. */
    public static int nextExposureTicks(int currentExposureTicks, boolean isExposedNow) {
        return isExposedNow ? currentExposureTicks + 1 : 0;
    }

    public static boolean causesIllness(int exposureTicks) {
        return exposureTicks >= EXPOSURE_TICKS_TO_ILLNESS;
    }
}
