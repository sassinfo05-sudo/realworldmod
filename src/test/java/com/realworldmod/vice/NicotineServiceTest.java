package com.realworldmod.vice;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NicotineServiceTest {
    @Test
    void doesNotTriggerBeforeThreshold() {
        NicotineService service = new NicotineService();
        UUID player = UUID.randomUUID();
        for (int i = 0; i < NicotineRisk.CIGARETTES_TO_ILLNESS - 1; i++) {
            assertFalse(service.smoke(player));
        }
    }

    @Test
    void triggersExactlyAtThreshold() {
        NicotineService service = new NicotineService();
        UUID player = UUID.randomUUID();
        boolean triggered = false;
        for (int i = 0; i < NicotineRisk.CIGARETTES_TO_ILLNESS; i++) {
            triggered = service.smoke(player);
        }
        assertTrue(triggered);
    }

    @Test
    void resetsAfterTriggeringRatherThanTriggeringEverySmoke() {
        NicotineService service = new NicotineService();
        UUID player = UUID.randomUUID();
        for (int i = 0; i < NicotineRisk.CIGARETTES_TO_ILLNESS; i++) {
            service.smoke(player);
        }
        assertFalse(service.smoke(player));
    }

    @Test
    void differentPlayersAreTrackedIndependently() {
        NicotineService service = new NicotineService();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        for (int i = 0; i < NicotineRisk.CIGARETTES_TO_ILLNESS - 1; i++) {
            service.smoke(first);
        }
        assertFalse(service.smoke(second));
    }
}
