package com.realworldmod.vehicle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FuelPricingTest {
    @Test
    void costScalesLinearlyWithLiters() {
        assertEquals(200, FuelPricing.costForLiters(10.0));
        assertEquals(2000, FuelPricing.costForLiters(100.0));
    }

    @Test
    void costRoundsToNearestCent() {
        assertEquals(3, FuelPricing.costForLiters(0.13));
    }

    @Test
    void zeroLitersCostsNothing() {
        assertEquals(0, FuelPricing.costForLiters(0.0));
    }

    @Test
    void budgetConvertsBackToLiters() {
        assertEquals(10.0, FuelPricing.litersForBudget(200), 0.0001);
    }

    @Test
    void costAndBudgetConversionsAreInverse() {
        double liters = 37.5;
        long cost = FuelPricing.costForLiters(liters);
        assertEquals(liters, FuelPricing.litersForBudget(cost), 0.1);
    }
}
