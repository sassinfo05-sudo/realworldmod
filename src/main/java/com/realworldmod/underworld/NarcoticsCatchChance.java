package com.realworldmod.underworld;

/**
 * Pure arithmetic for how likely a dealer is to be caught, scaled by their
 * current wanted level instead of {@code NarcoticsHandler}'s old flat 30%
 * for everyone — a clean wanted-for-trespassing player and a five-star
 * repeat offender no longer face identical odds.
 */
public final class NarcoticsCatchChance {
    public static final double BASE_CHANCE = 0.15;
    public static final double PER_LEVEL_INCREMENT = 0.15;
    public static final double MAX_CHANCE = 0.9;

    private NarcoticsCatchChance() {
    }

    public static double forWantedLevel(int wantedLevel) {
        double scaled = BASE_CHANCE + PER_LEVEL_INCREMENT * wantedLevel;
        return Math.min(MAX_CHANCE, scaled);
    }
}
