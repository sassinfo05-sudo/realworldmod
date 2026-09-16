package com.realworldmod.medical;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FallInjuryCalculatorTest {
    @Test
    void lightDamageCausesNoInjury() {
        assertEquals(LegInjury.NONE, FallInjuryCalculator.fromFallDamage(1.0f));
        assertEquals(LegInjury.NONE, FallInjuryCalculator.fromFallDamage(0f));
    }

    @Test
    void moderateDamageBruises() {
        assertEquals(LegInjury.BRUISED, FallInjuryCalculator.fromFallDamage(4.0f));
        assertEquals(LegInjury.BRUISED, FallInjuryCalculator.fromFallDamage(7.9f));
    }

    @Test
    void heavyDamageFractures() {
        assertEquals(LegInjury.FRACTURED, FallInjuryCalculator.fromFallDamage(8.0f));
        assertEquals(LegInjury.FRACTURED, FallInjuryCalculator.fromFallDamage(50.0f));
    }

    @Test
    void noInjuryHasNoSlownessEffect() {
        assertEquals(-1, FallInjuryCalculator.slownessAmplifierFor(LegInjury.NONE));
        assertEquals(0, FallInjuryCalculator.slownessDurationTicksFor(LegInjury.NONE));
    }

    @Test
    void fracturedIsMoreSevereThanBruised() {
        assertTrue(FallInjuryCalculator.slownessAmplifierFor(LegInjury.FRACTURED)
                > FallInjuryCalculator.slownessAmplifierFor(LegInjury.BRUISED));
        assertTrue(FallInjuryCalculator.slownessDurationTicksFor(LegInjury.FRACTURED)
                > FallInjuryCalculator.slownessDurationTicksFor(LegInjury.BRUISED));
    }
}
