package com.realworldmod.crime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PoliceBehaviorTest {
    @Test
    void doesNotChaseBelowTheTriggerLevel() {
        assertFalse(PoliceBehavior.shouldChase(PoliceBehavior.CHASE_TRIGGER_WANTED_LEVEL - 1));
    }

    @Test
    void chasesAtOrAboveTheTriggerLevel() {
        assertTrue(PoliceBehavior.shouldChase(PoliceBehavior.CHASE_TRIGGER_WANTED_LEVEL));
        assertTrue(PoliceBehavior.shouldChase(WantedLevelMath.MAX));
    }

    @Test
    void cannotApprehendBelowMaxWantedLevelEvenAtPointBlankRange() {
        assertFalse(PoliceBehavior.canApprehend(WantedLevelMath.MAX - 1, 0.0));
    }

    @Test
    void cannotApprehendAtMaxWantedLevelIfTooFarAway() {
        assertFalse(PoliceBehavior.canApprehend(WantedLevelMath.MAX, PoliceBehavior.APPREHEND_DISTANCE_SQUARED + 1.0));
    }

    @Test
    void apprehendsAtMaxWantedLevelWithinRange() {
        assertTrue(PoliceBehavior.canApprehend(WantedLevelMath.MAX, PoliceBehavior.APPREHEND_DISTANCE_SQUARED));
    }
}
