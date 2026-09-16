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
            assertFalse(service.smoke(player, 0));
        }
    }

    @Test
    void triggersExactlyAtThreshold() {
        NicotineService service = new NicotineService();
        UUID player = UUID.randomUUID();
        boolean triggered = false;
        for (int i = 0; i < NicotineRisk.CIGARETTES_TO_ILLNESS; i++) {
            triggered = service.smoke(player, 0);
        }
        assertTrue(triggered);
    }

    @Test
    void resetsAfterTriggeringRatherThanTriggeringEverySmoke() {
        NicotineService service = new NicotineService();
        UUID player = UUID.randomUUID();
        for (int i = 0; i < NicotineRisk.CIGARETTES_TO_ILLNESS; i++) {
            service.smoke(player, 0);
        }
        assertFalse(service.smoke(player, 0));
    }

    @Test
    void differentPlayersAreTrackedIndependently() {
        NicotineService service = new NicotineService();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        for (int i = 0; i < NicotineRisk.CIGARETTES_TO_ILLNESS - 1; i++) {
            service.smoke(first, 0);
        }
        assertFalse(service.smoke(second, 0));
    }

    @Test
    void noWithdrawalWithoutEverBecomingDependent() {
        NicotineService service = new NicotineService();
        UUID player = UUID.randomUUID();
        service.smoke(player, 0);

        assertFalse(service.checkWithdrawal(player, NicotineWithdrawalRisk.WITHDRAWAL_TICKS * 10));
    }

    @Test
    void noWithdrawalBeforeEnoughTimeHasPassed() {
        NicotineService service = new NicotineService();
        UUID player = UUID.randomUUID();
        for (int i = 0; i < NicotineWithdrawalRisk.DEPENDENCY_THRESHOLD_CIGARETTES; i++) {
            service.smoke(player, 0);
        }

        assertFalse(service.checkWithdrawal(player, NicotineWithdrawalRisk.WITHDRAWAL_TICKS - 1));
    }

    @Test
    void withdrawalTriggersExactlyOnceOnceDependentAndOverdue() {
        NicotineService service = new NicotineService();
        UUID player = UUID.randomUUID();
        for (int i = 0; i < NicotineWithdrawalRisk.DEPENDENCY_THRESHOLD_CIGARETTES; i++) {
            service.smoke(player, 0);
        }

        assertTrue(service.checkWithdrawal(player, NicotineWithdrawalRisk.WITHDRAWAL_TICKS));
        assertFalse(service.checkWithdrawal(player, NicotineWithdrawalRisk.WITHDRAWAL_TICKS + 1));
    }

    @Test
    void smokingAgainAfterWithdrawalCanTriggerAnotherOne() {
        NicotineService service = new NicotineService();
        UUID player = UUID.randomUUID();
        for (int i = 0; i < NicotineWithdrawalRisk.DEPENDENCY_THRESHOLD_CIGARETTES; i++) {
            service.smoke(player, 0);
        }
        service.checkWithdrawal(player, NicotineWithdrawalRisk.WITHDRAWAL_TICKS);

        service.smoke(player, NicotineWithdrawalRisk.WITHDRAWAL_TICKS);
        long nextWithdrawalTick = NicotineWithdrawalRisk.WITHDRAWAL_TICKS + NicotineWithdrawalRisk.WITHDRAWAL_TICKS;

        assertTrue(service.checkWithdrawal(player, nextWithdrawalTick));
    }
}
