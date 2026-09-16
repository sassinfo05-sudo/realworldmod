package com.realworldmod.vice;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
