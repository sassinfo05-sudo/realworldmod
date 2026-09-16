package com.realworldmod.wildlife;

/**
 * A real, if minimal, deer population counter — closing "a killed deer
 * simply dies with no ... population-count consequence." Not a simulated
 * ecosystem (no births, no carrying capacity, no effect on anything else
 * in the mod yet): just an honest count that goes up when a deer spawns
 * and down when one dies, floored at zero.
 */
public final class WildlifePopulationService {
    private int deerPopulation;

    public void recordDeerSpawn() {
        deerPopulation++;
    }

    public void recordDeerDeath() {
        deerPopulation = Math.max(0, deerPopulation - 1);
    }

    public int getDeerPopulation() {
        return deerPopulation;
    }
}
