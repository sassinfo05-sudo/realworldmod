package com.realworldmod.underworld;

import com.realworldmod.economy.BankService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NarcoticsServiceTest {
    private BankService bankService;
    private NarcoticsService narcoticsService;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        bankService = new BankService();
        bankService.open(tempDir.resolve("bank.sqlite"));
        narcoticsService = new NarcoticsService(bankService);
    }

    @AfterEach
    void tearDown() {
        bankService.close();
    }

    @Test
    void firstCookAlwaysSucceeds() {
        UUID player = UUID.randomUUID();
        assertTrue(narcoticsService.tryCook(player, 0));
        assertEquals(1, narcoticsService.stashCount(player));
    }

    @Test
    void secondCookWithinCooldownIsRefused() {
        UUID player = UUID.randomUUID();
        narcoticsService.tryCook(player, 0);

        assertFalse(narcoticsService.tryCook(player, 10));
        assertEquals(1, narcoticsService.stashCount(player));
    }

    @Test
    void cookAfterCooldownExpiresSucceedsAgain() {
        UUID player = UUID.randomUUID();
        narcoticsService.tryCook(player, 0);

        assertTrue(narcoticsService.tryCook(player, NarcoticsService.COOK_COOLDOWN_TICKS));
        assertEquals(2, narcoticsService.stashCount(player));
    }

    @Test
    void dealingWithEmptyStashFails() {
        UUID player = UUID.randomUUID();
        assertFalse(narcoticsService.tryDeal(player, 0));
    }

    @Test
    void dealingSellsOneUnitAndPaysOut() {
        UUID player = UUID.randomUUID();
        narcoticsService.tryCook(player, 0);

        assertTrue(narcoticsService.tryDeal(player, 0));
        assertEquals(0, narcoticsService.stashCount(player));
        assertEquals(NarcoticsService.DEAL_PAYOUT_CENTS, bankService.getBalance(player));
    }

    @Test
    void secondDealWithinCooldownIsRefusedEvenWithStashRemaining() {
        UUID player = UUID.randomUUID();
        narcoticsService.tryCook(player, 0);
        narcoticsService.tryCook(player, NarcoticsService.COOK_COOLDOWN_TICKS);
        narcoticsService.tryDeal(player, NarcoticsService.COOK_COOLDOWN_TICKS);

        assertFalse(narcoticsService.tryDeal(player, NarcoticsService.COOK_COOLDOWN_TICKS + 5));
        assertEquals(1, narcoticsService.stashCount(player));
    }

    @Test
    void dealAfterCooldownExpiresSucceedsAgain() {
        UUID player = UUID.randomUUID();
        narcoticsService.tryCook(player, 0);
        narcoticsService.tryCook(player, NarcoticsService.COOK_COOLDOWN_TICKS);
        narcoticsService.tryDeal(player, NarcoticsService.COOK_COOLDOWN_TICKS);

        long secondDealTick = NarcoticsService.COOK_COOLDOWN_TICKS + NarcoticsService.DEAL_COOLDOWN_TICKS;
        assertTrue(narcoticsService.tryDeal(player, secondDealTick));
        assertEquals(NarcoticsService.DEAL_PAYOUT_CENTS * 2, bankService.getBalance(player));
    }

    @Test
    void differentPlayersHaveIndependentStashesAndCooldowns() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        narcoticsService.tryCook(first, 0);

        assertTrue(narcoticsService.tryCook(second, 5));
        assertEquals(1, narcoticsService.stashCount(first));
        assertEquals(1, narcoticsService.stashCount(second));
    }

    @Test
    void dealStreakIsZeroBeforeAnyDeal() {
        assertEquals(0, narcoticsService.getDealStreak(UUID.randomUUID()));
    }

    @Test
    void firstDealStartsTheStreakAtOne() {
        UUID player = UUID.randomUUID();
        narcoticsService.tryCook(player, 0);
        narcoticsService.tryDeal(player, 0);

        assertEquals(1, narcoticsService.getDealStreak(player));
    }

    @Test
    void aSecondDealWithinTheWindowExtendsTheStreak() {
        UUID player = UUID.randomUUID();
        narcoticsService.tryCook(player, 0);
        narcoticsService.tryDeal(player, 0);
        narcoticsService.tryCook(player, NarcoticsService.COOK_COOLDOWN_TICKS);
        narcoticsService.tryDeal(player, NarcoticsService.COOK_COOLDOWN_TICKS);

        assertEquals(2, narcoticsService.getDealStreak(player));
    }

    @Test
    void aDealStreakResetsAfterGoingLongEnoughWithoutAnotherDeal() {
        UUID player = UUID.randomUUID();
        narcoticsService.tryCook(player, 0);
        narcoticsService.tryDeal(player, 0);

        long farLaterTick = NarcoticsSeverity.STREAK_RESET_TICKS + 1;
        narcoticsService.tryCook(player, farLaterTick);
        narcoticsService.tryDeal(player, farLaterTick);

        assertEquals(1, narcoticsService.getDealStreak(player));
    }
}
