package com.realworldmod.vice;

/**
 * Pure dependency/withdrawal-timing rules, the same shape
 * {@code IntoxicationCalculator} already established for intoxication
 * tiers: kept free of any Minecraft dependency so the thresholds are unit
 * testable on their own.
 */
public final class NicotineWithdrawalRisk {
    public static final int DEPENDENCY_THRESHOLD_CIGARETTES = 3;
    public static final long WITHDRAWAL_TICKS = 20L * 60L * 3L;

    private NicotineWithdrawalRisk() {
    }

    /** True once a player has smoked enough, cumulatively, to actually crave the next one. */
    public static boolean isDependent(int totalCigarettesSmoked) {
        return totalCigarettesSmoked >= DEPENDENCY_THRESHOLD_CIGARETTES;
    }

    /** True once a dependent player has gone long enough without a cigarette to feel it. */
    public static boolean causesWithdrawal(long ticksSinceLastSmoke) {
        return ticksSinceLastSmoke >= WITHDRAWAL_TICKS;
    }
}
