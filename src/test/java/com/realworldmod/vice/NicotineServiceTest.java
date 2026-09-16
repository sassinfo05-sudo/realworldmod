package com.realworldmod.vice;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

        assertEquals(0, service.checkWithdrawal(player, NicotineWithdrawalRisk.WITHDRAWAL_TICKS * 10));
    }

    @Test
    void noWithdrawalBeforeEnoughTimeHasPassed() {
        NicotineService service = new NicotineService();
        UUID player = UUID.randomUUID();
        for (int i = 0; i < NicotineWithdrawalRisk.DEPENDENCY_THRESHOLD_CIGARETTES; i++) {
            service.smoke(player, 0);
        }

        assertEquals(0, service.checkWithdrawal(player, NicotineWithdrawalRisk.WITHDRAWAL_TICKS - 1));
    }

    @Test
    void firstWithdrawalTriggersExactlyOnceAtSeverityOne() {
        NicotineService service = new NicotineService();
        UUID player = UUID.randomUUID();
        for (int i = 0; i < NicotineWithdrawalRisk.DEPENDENCY_THRESHOLD_CIGARETTES; i++) {
            service.smoke(player, 0);
        }

        assertEquals(1, service.checkWithdrawal(player, NicotineWithdrawalRisk.WITHDRAWAL_TICKS));
        assertEquals(0, service.checkWithdrawal(player, NicotineWithdrawalRisk.WITHDRAWAL_TICKS + 1));
    }

    @Test
    void smokingAgainAfterWithdrawalCanTriggerAnotherOneAtEscalatedSeverity() {
        NicotineService service = new NicotineService();
        UUID player = UUID.randomUUID();
        for (int i = 0; i < NicotineWithdrawalRisk.DEPENDENCY_THRESHOLD_CIGARETTES; i++) {
            service.smoke(player, 0);
        }
        service.checkWithdrawal(player, NicotineWithdrawalRisk.WITHDRAWAL_TICKS);

        service.smoke(player, NicotineWithdrawalRisk.WITHDRAWAL_TICKS);
        long nextWithdrawalTick = NicotineWithdrawalRisk.WITHDRAWAL_TICKS + NicotineWithdrawalRisk.WITHDRAWAL_TICKS;

        assertEquals(2, service.checkWithdrawal(player, nextWithdrawalTick));
    }

    @Test
    void repeatedWithdrawalsEscalateUpToTheMaximumSeverity() {
        NicotineService service = new NicotineService();
        UUID player = UUID.randomUUID();
        for (int i = 0; i < NicotineWithdrawalRisk.DEPENDENCY_THRESHOLD_CIGARETTES; i++) {
            service.smoke(player, 0);
        }
        long tick = 0;
        int lastSeverity = 0;
        for (int i = 0; i < WithdrawalSeverity.MAX_SEVERITY + 2; i++) {
            tick += NicotineWithdrawalRisk.WITHDRAWAL_TICKS;
            lastSeverity = service.checkWithdrawal(player, tick);
            service.smoke(player, tick);
        }
        assertEquals(WithdrawalSeverity.MAX_SEVERITY, lastSeverity);
    }

    @Test
    void usingAPatchDelaysWithdrawalTheSameAsSmokingWould() {
        NicotineService service = new NicotineService();
        UUID player = UUID.randomUUID();
        for (int i = 0; i < NicotineWithdrawalRisk.DEPENDENCY_THRESHOLD_CIGARETTES; i++) {
            service.smoke(player, 0);
        }

        service.usePatch(player, NicotineWithdrawalRisk.WITHDRAWAL_TICKS - 1);

        assertEquals(0, service.checkWithdrawal(player, NicotineWithdrawalRisk.WITHDRAWAL_TICKS));
    }

    @Test
    void usingAPatchDoesNotResetTheIllnessStreak() {
        NicotineService service = new NicotineService();
        UUID player = UUID.randomUUID();
        for (int i = 0; i < NicotineRisk.CIGARETTES_TO_ILLNESS - 1; i++) {
            service.smoke(player, 0);
        }

        service.usePatch(player, 0);

        assertTrue(service.smoke(player, 0));
    }

    @Test
    void usingAPatchAfterAWithdrawalResetsTheEscalationStreak() {
        NicotineService service = new NicotineService();
        UUID player = UUID.randomUUID();
        for (int i = 0; i < NicotineWithdrawalRisk.DEPENDENCY_THRESHOLD_CIGARETTES; i++) {
            service.smoke(player, 0);
        }
        long firstWithdrawalTick = NicotineWithdrawalRisk.WITHDRAWAL_TICKS;
        assertEquals(1, service.checkWithdrawal(player, firstWithdrawalTick));

        service.usePatch(player, firstWithdrawalTick);
        long secondWithdrawalTick = firstWithdrawalTick + NicotineWithdrawalRisk.WITHDRAWAL_TICKS;

        assertEquals(1, service.checkWithdrawal(player, secondWithdrawalTick));
    }

    @Test
    void aWithdrawalStreakResetsAfterGoingLongEnoughWithoutAnother() {
        NicotineService service = new NicotineService();
        UUID player = UUID.randomUUID();
        for (int i = 0; i < NicotineWithdrawalRisk.DEPENDENCY_THRESHOLD_CIGARETTES; i++) {
            service.smoke(player, 0);
        }
        assertEquals(1, service.checkWithdrawal(player, NicotineWithdrawalRisk.WITHDRAWAL_TICKS));

        long farLaterSmokeTick = NicotineWithdrawalRisk.WITHDRAWAL_TICKS + WithdrawalSeverity.STREAK_RESET_TICKS + 1;
        service.smoke(player, farLaterSmokeTick);
        long secondWithdrawalTick = farLaterSmokeTick + NicotineWithdrawalRisk.WITHDRAWAL_TICKS;

        assertEquals(1, service.checkWithdrawal(player, secondWithdrawalTick));
    }
}
