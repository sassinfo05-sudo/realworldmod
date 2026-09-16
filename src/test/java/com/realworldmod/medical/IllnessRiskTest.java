package com.realworldmod.medical;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IllnessRiskTest {
    @Test
    void exposureIncrementsWhileExposed() {
        assertEquals(1, IllnessRisk.nextExposureTicks(0, true));
        assertEquals(11, IllnessRisk.nextExposureTicks(10, true));
    }

    @Test
    void exposureResetsWhenNotExposed() {
        assertEquals(0, IllnessRisk.nextExposureTicks(500, false));
    }

    @Test
    void belowThresholdDoesNotCauseIllness() {
        assertFalse(IllnessRisk.causesIllness(IllnessRisk.EXPOSURE_TICKS_TO_ILLNESS - 1));
    }

    @Test
    void atOrAboveThresholdCausesIllness() {
        assertTrue(IllnessRisk.causesIllness(IllnessRisk.EXPOSURE_TICKS_TO_ILLNESS));
        assertTrue(IllnessRisk.causesIllness(IllnessRisk.EXPOSURE_TICKS_TO_ILLNESS + 100));
    }
}
