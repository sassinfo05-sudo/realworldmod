package com.realworldmod.vice;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntoxicationServiceTest {
    @Test
    void firstDrinkRaisesLevelToOne() {
        IntoxicationService service = new IntoxicationService();
        UUID player = UUID.randomUUID();

        assertEquals(1, service.drink(player, 0));
        assertEquals(1, service.currentLevel(player, 0));
    }

    @Test
    void repeatedDrinksRaiseLevelUpToMax() {
        IntoxicationService service = new IntoxicationService();
        UUID player = UUID.randomUUID();

        service.drink(player, 0);
        service.drink(player, 0);
        assertEquals(3, service.drink(player, 0));
        assertEquals(IntoxicationCalculator.MAX_LEVEL, service.drink(player, 0));
    }

    @Test
    void levelDecaysOverTimeSinceLastDrink() {
        IntoxicationService service = new IntoxicationService();
        UUID player = UUID.randomUUID();
        service.drink(player, 0);
        service.drink(player, 0);

        assertEquals(2, service.currentLevel(player, 0));
        assertEquals(1, service.currentLevel(player, IntoxicationService.DECAY_TICKS_PER_LEVEL));
        assertEquals(0, service.currentLevel(player, IntoxicationService.DECAY_TICKS_PER_LEVEL * 2));
    }

    @Test
    void decayNeverGoesBelowZero() {
        IntoxicationService service = new IntoxicationService();
        UUID player = UUID.randomUUID();
        service.drink(player, 0);

        assertEquals(0, service.currentLevel(player, IntoxicationService.DECAY_TICKS_PER_LEVEL * 100));
    }

    @Test
    void drinkingAgainBeforeFullyDecayedStacksOnTheRemainingLevel() {
        IntoxicationService service = new IntoxicationService();
        UUID player = UUID.randomUUID();
        service.drink(player, 0);

        long secondDrinkTick = IntoxicationService.DECAY_TICKS_PER_LEVEL - 1;
        assertEquals(2, service.drink(player, secondDrinkTick));
        assertEquals(2, service.currentLevel(player, secondDrinkTick));
    }

    @Test
    void sensibleWithNoDrinksYet() {
        IntoxicationService service = new IntoxicationService();
        assertEquals(0, service.currentLevel(UUID.randomUUID(), 1000));
    }

    @Test
    void differentPlayersAreIndependent() {
        IntoxicationService service = new IntoxicationService();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        service.drink(first, 0);

        assertEquals(0, service.currentLevel(second, 0));
    }

    @Test
    void noHangoverWithoutEverPeakingAtMaxLevel() {
        IntoxicationService service = new IntoxicationService();
        UUID player = UUID.randomUUID();
        service.drink(player, 0);

        assertEquals(0, service.checkHangover(player, IntoxicationService.DECAY_TICKS_PER_LEVEL * 10));
    }

    @Test
    void noHangoverWhileStillSoberingUpFromMaxLevel() {
        IntoxicationService service = new IntoxicationService();
        UUID player = UUID.randomUUID();
        service.drink(player, 0);
        service.drink(player, 0);
        service.drink(player, 0);

        assertEquals(0, service.checkHangover(player, 0));
    }

    @Test
    void firstHangoverTriggersExactlyOnceAtSeverityOne() {
        IntoxicationService service = new IntoxicationService();
        UUID player = UUID.randomUUID();
        service.drink(player, 0);
        service.drink(player, 0);
        service.drink(player, 0);

        long soberTick = IntoxicationService.DECAY_TICKS_PER_LEVEL * IntoxicationCalculator.MAX_LEVEL;
        assertEquals(1, service.checkHangover(player, soberTick));
        assertEquals(0, service.checkHangover(player, soberTick + 1));
    }

    @Test
    void drinkingAgainAfterAHangoverCanTriggerAnotherOne() {
        IntoxicationService service = new IntoxicationService();
        UUID player = UUID.randomUUID();
        service.drink(player, 0);
        service.drink(player, 0);
        service.drink(player, 0);
        long soberTick = IntoxicationService.DECAY_TICKS_PER_LEVEL * IntoxicationCalculator.MAX_LEVEL;
        service.checkHangover(player, soberTick);

        service.drink(player, soberTick);
        service.drink(player, soberTick);
        service.drink(player, soberTick);
        long nextSoberTick = soberTick + IntoxicationService.DECAY_TICKS_PER_LEVEL * IntoxicationCalculator.MAX_LEVEL;

        assertEquals(2, service.checkHangover(player, nextSoberTick));
    }

    @Test
    void repeatedHangoversEscalateUpToTheMaximumSeverity() {
        IntoxicationService service = new IntoxicationService();
        UUID player = UUID.randomUUID();
        long tick = 0;
        int lastSeverity = 0;
        for (int i = 0; i < HangoverSeverity.MAX_SEVERITY + 2; i++) {
            service.drink(player, tick);
            service.drink(player, tick);
            service.drink(player, tick);
            tick += IntoxicationService.DECAY_TICKS_PER_LEVEL * IntoxicationCalculator.MAX_LEVEL;
            lastSeverity = service.checkHangover(player, tick);
        }
        assertEquals(HangoverSeverity.MAX_SEVERITY, lastSeverity);
    }

    @Test
    void aHangoverStreakResetsAfterGoingLongEnoughWithoutAnother() {
        IntoxicationService service = new IntoxicationService();
        UUID player = UUID.randomUUID();
        service.drink(player, 0);
        service.drink(player, 0);
        service.drink(player, 0);
        long firstSoberTick = IntoxicationService.DECAY_TICKS_PER_LEVEL * IntoxicationCalculator.MAX_LEVEL;
        assertEquals(1, service.checkHangover(player, firstSoberTick));

        long farLaterDrinkTick = firstSoberTick + HangoverSeverity.STREAK_RESET_TICKS + 1;
        service.drink(player, farLaterDrinkTick);
        service.drink(player, farLaterDrinkTick);
        service.drink(player, farLaterDrinkTick);
        long secondSoberTick = farLaterDrinkTick + IntoxicationService.DECAY_TICKS_PER_LEVEL * IntoxicationCalculator.MAX_LEVEL;

        assertEquals(1, service.checkHangover(player, secondSoberTick));
    }

    @Test
    void recoveryDrinkDoesNothingWithoutEverPeaking() {
        IntoxicationService service = new IntoxicationService();
        UUID player = UUID.randomUUID();
        service.drink(player, 0);

        assertFalse(service.useRecoveryDrink(player));
    }

    @Test
    void recoveryDrinkCancelsAnImpendingHangover() {
        IntoxicationService service = new IntoxicationService();
        UUID player = UUID.randomUUID();
        service.drink(player, 0);
        service.drink(player, 0);
        service.drink(player, 0);

        assertTrue(service.useRecoveryDrink(player));

        long soberTick = IntoxicationService.DECAY_TICKS_PER_LEVEL * IntoxicationCalculator.MAX_LEVEL;
        assertEquals(0, service.checkHangover(player, soberTick));
    }

    @Test
    void recoveryDrinkResetsTheEscalationStreakForTheNextRealHangover() {
        IntoxicationService service = new IntoxicationService();
        UUID player = UUID.randomUUID();
        service.drink(player, 0);
        service.drink(player, 0);
        service.drink(player, 0);
        long firstSoberTick = IntoxicationService.DECAY_TICKS_PER_LEVEL * IntoxicationCalculator.MAX_LEVEL;
        assertEquals(1, service.checkHangover(player, firstSoberTick));

        service.drink(player, firstSoberTick);
        service.drink(player, firstSoberTick);
        service.drink(player, firstSoberTick);
        assertTrue(service.useRecoveryDrink(player));

        long secondSoberTick = firstSoberTick
                + IntoxicationService.DECAY_TICKS_PER_LEVEL * IntoxicationCalculator.MAX_LEVEL;
        assertEquals(0, service.checkHangover(player, secondSoberTick));

        service.drink(player, secondSoberTick);
        service.drink(player, secondSoberTick);
        service.drink(player, secondSoberTick);
        long thirdSoberTick = secondSoberTick
                + IntoxicationService.DECAY_TICKS_PER_LEVEL * IntoxicationCalculator.MAX_LEVEL;

        assertEquals(1, service.checkHangover(player, thirdSoberTick));
    }
}
