package com.realworldmod.medical;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IllnessServiceTest {
    @Test
    void doesNotTriggerBeforeThreshold() {
        IllnessService service = new IllnessService();
        UUID player = UUID.randomUUID();
        for (int i = 0; i < IllnessRisk.EXPOSURE_TICKS_TO_ILLNESS - 1; i++) {
            assertFalse(service.tick(player, true));
        }
    }

    @Test
    void triggersExactlyOnceAtThreshold() {
        IllnessService service = new IllnessService();
        UUID player = UUID.randomUUID();
        boolean triggered = false;
        for (int i = 0; i < IllnessRisk.EXPOSURE_TICKS_TO_ILLNESS; i++) {
            triggered = service.tick(player, true);
        }
        assertTrue(triggered);
    }

    @Test
    void resetsAfterTriggeringRatherThanTriggeringEveryTick() {
        IllnessService service = new IllnessService();
        UUID player = UUID.randomUUID();
        for (int i = 0; i < IllnessRisk.EXPOSURE_TICKS_TO_ILLNESS; i++) {
            service.tick(player, true);
        }
        assertFalse(service.tick(player, true));
    }

    @Test
    void leavingExposureResetsProgress() {
        IllnessService service = new IllnessService();
        UUID player = UUID.randomUUID();
        for (int i = 0; i < IllnessRisk.EXPOSURE_TICKS_TO_ILLNESS - 1; i++) {
            service.tick(player, true);
        }
        service.tick(player, false);
        assertFalse(service.tick(player, true));
    }

    @Test
    void differentPlayersAreTrackedIndependently() {
        IllnessService service = new IllnessService();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        service.tick(first, true);
        assertFalse(service.tick(second, false));
    }
}
