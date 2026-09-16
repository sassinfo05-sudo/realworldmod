package com.realworldmod.phone;

/**
 * Pure battery-level arithmetic for smartphones (Section 3: "Phones require
 * charging cables, have battery drain curves"). Kept free of any Minecraft
 * dependency so the curve itself is unit testable.
 */
public final class PhoneBattery {
    public static final int MAX_LEVEL = 100;
    public static final int MIN_LEVEL = 0;

    /** Percentage points consumed each time the phone screen is opened. */
    public static final int DRAIN_PER_USE = 2;

    /** Percentage points restored per in-game tick while on a charging cable. */
    public static final int CHARGE_PER_TICK = 1;

    private PhoneBattery() {
    }

    public static int drain(int currentLevel, int amount) {
        return clamp(currentLevel - amount);
    }

    public static int charge(int currentLevel, int amount) {
        return clamp(currentLevel + amount);
    }

    public static boolean isDead(int level) {
        return level <= MIN_LEVEL;
    }

    public static boolean isFull(int level) {
        return level >= MAX_LEVEL;
    }

    private static int clamp(int level) {
        return Math.max(MIN_LEVEL, Math.min(MAX_LEVEL, level));
    }
}
