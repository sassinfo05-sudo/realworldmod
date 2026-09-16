package com.realworldmod.underworld;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NarcoticsCatchChanceTest {
    @Test
    void aCleanRecordUsesTheBaseChance() {
        assertEquals(NarcoticsCatchChance.BASE_CHANCE, NarcoticsCatchChance.forWantedLevel(0));
    }

    @Test
    void chanceScalesUpWithEachWantedLevel() {
        assertEquals(0.30, NarcoticsCatchChance.forWantedLevel(1), 1e-9);
        assertEquals(0.45, NarcoticsCatchChance.forWantedLevel(2), 1e-9);
    }

    @Test
    void chanceClampsAtTheMaximumForTheHighestWantedLevel() {
        assertEquals(NarcoticsCatchChance.MAX_CHANCE, NarcoticsCatchChance.forWantedLevel(5), 1e-9);
    }

    @Test
    void chanceNeverExceedsTheMaximumEvenBeyondTheRealWantedLevelRange() {
        assertEquals(NarcoticsCatchChance.MAX_CHANCE, NarcoticsCatchChance.forWantedLevel(50), 1e-9);
    }
}
