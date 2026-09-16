package com.realworldmod.underworld;

/**
 * The meth economy's own catch-chance curve, mirroring
 * {@link NarcoticsCatchChance}'s "scale by wanted level" shape but
 * starting and capping higher — a genuinely steeper risk than the
 * original narcotics loop, not just a reskin with the same odds.
 */
public final class MethCatchChance {
    public static final double BASE_CHANCE = 0.25;
    public static final double PER_LEVEL_INCREMENT = 0.15;
    public static final double MAX_CHANCE = 0.95;

    private MethCatchChance() {
    }

    public static double forWantedLevel(int wantedLevel) {
        double scaled = BASE_CHANCE + PER_LEVEL_INCREMENT * wantedLevel;
        return Math.min(MAX_CHANCE, scaled);
    }
}
