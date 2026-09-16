package com.realworldmod.vice;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntoxicationCalculatorTest {
    @Test
    void levelIncrementsUpToMax() {
        assertEquals(1, IntoxicationCalculator.nextLevel(0));
        assertEquals(2, IntoxicationCalculator.nextLevel(1));
        assertEquals(3, IntoxicationCalculator.nextLevel(2));
    }

    @Test
    void levelCapsAtMax() {
        assertEquals(IntoxicationCalculator.MAX_LEVEL,
                IntoxicationCalculator.nextLevel(IntoxicationCalculator.MAX_LEVEL));
    }

    @Test
    void soberHasNoSlownessEffect() {
        assertEquals(-1, IntoxicationCalculator.slownessAmplifierFor(0));
        assertEquals(0, IntoxicationCalculator.slownessDurationTicksFor(0));
    }

    @Test
    void higherLevelsAreMoreSevere() {
        assertTrue(IntoxicationCalculator.slownessAmplifierFor(2) > IntoxicationCalculator.slownessAmplifierFor(1));
        assertTrue(IntoxicationCalculator.slownessAmplifierFor(3) > IntoxicationCalculator.slownessAmplifierFor(2));
        assertTrue(IntoxicationCalculator.slownessDurationTicksFor(3)
                > IntoxicationCalculator.slownessDurationTicksFor(1));
    }

    @Test
    void nauseaOnlyAtHigherLevels() {
        assertFalse(IntoxicationCalculator.causesNausea(0));
        assertFalse(IntoxicationCalculator.causesNausea(1));
        assertTrue(IntoxicationCalculator.causesNausea(2));
        assertTrue(IntoxicationCalculator.causesNausea(3));
    }
}
