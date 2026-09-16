package com.realworldmod.vice;

/**
 * Pure cigarette-count-to-illness rule, the same shape
 * {@code medical.IllnessRisk} already established for rain exposure: a
 * fixed count of cigarettes causes a real illness rather than nothing at
 * all happening beyond a brief nicotine buzz.
 */
public final class NicotineRisk {
    public static final int CIGARETTES_TO_ILLNESS = 5;

    private NicotineRisk() {
    }

    public static int nextCount(int currentCount) {
        return currentCount + 1;
    }

    public static boolean causesIllness(int count) {
        return count >= CIGARETTES_TO_ILLNESS;
    }
}
