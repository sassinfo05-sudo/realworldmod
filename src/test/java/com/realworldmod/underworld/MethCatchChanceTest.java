package com.realworldmod.underworld;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MethCatchChanceTest {
    @Test
    void aCleanRecordUsesTheBaseChance() {
        assertEquals(MethCatchChance.BASE_CHANCE, MethCatchChance.forWantedLevel(0));
    }

    @Test
    void chanceScalesUpWithEachWantedLevel() {
        assertEquals(0.40, MethCatchChance.forWantedLevel(1), 1e-9);
        assertEquals(0.55, MethCatchChance.forWantedLevel(2), 1e-9);
    }

    @Test
    void chanceClampsAtTheMaximumForTheHighestWantedLevel() {
        assertEquals(MethCatchChance.MAX_CHANCE, MethCatchChance.forWantedLevel(5), 1e-9);
    }

    @Test
    void chanceNeverExceedsTheMaximumEvenBeyondTheRealWantedLevelRange() {
        assertEquals(MethCatchChance.MAX_CHANCE, MethCatchChance.forWantedLevel(50), 1e-9);
    }

    @Test
    void isHarsherThanNarcoticsCatchChanceAtEveryLevel() {
        for (int level = 0; level <= 5; level++) {
            assertTrue(MethCatchChance.forWantedLevel(level) >= NarcoticsCatchChance.forWantedLevel(level));
        }
    }
}
