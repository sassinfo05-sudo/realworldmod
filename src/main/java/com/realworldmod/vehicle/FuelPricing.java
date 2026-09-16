package com.realworldmod.vehicle;

/**
 * Pure liters-to-cost conversion for gas station refueling, kept free of
 * any Minecraft/entity dependency so it's unit testable on its own — the
 * same shape every other pure calculator in the mod uses.
 */
public final class FuelPricing {
    public static final long PRICE_CENTS_PER_LITER = 20;

    private FuelPricing() {
    }

    public static long costForLiters(double liters) {
        return Math.round(liters * PRICE_CENTS_PER_LITER);
    }

    public static double litersForBudget(long budgetCents) {
        return budgetCents / (double) PRICE_CENTS_PER_LITER;
    }
}
