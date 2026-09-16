package com.realworldmod.vice;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NicotineRiskTest {
    @Test
    void countIncrements() {
        assertEquals(1, NicotineRisk.nextCount(0));
        assertEquals(6, NicotineRisk.nextCount(5));
    }

    @Test
    void belowThresholdDoesNotCauseIllness() {
        assertFalse(NicotineRisk.causesIllness(NicotineRisk.CIGARETTES_TO_ILLNESS - 1));
    }

    @Test
    void atOrAboveThresholdCausesIllness() {
        assertTrue(NicotineRisk.causesIllness(NicotineRisk.CIGARETTES_TO_ILLNESS));
        assertTrue(NicotineRisk.causesIllness(NicotineRisk.CIGARETTES_TO_ILLNESS + 1));
    }
}
