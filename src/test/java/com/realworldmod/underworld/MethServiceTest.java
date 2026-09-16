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

class MethServiceTest {
    private BankService bankService;
    private MethService methService;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        bankService = new BankService();
        bankService.open(tempDir.resolve("bank.sqlite"));
        methService = new MethService(bankService);
    }

    @AfterEach
    void tearDown() {
        bankService.close();
    }

    @Test
    void firstCookAlwaysSucceeds() {
        UUID player = UUID.randomUUID();
        assertTrue(methService.tryCook(player, 0));
        assertEquals(1, methService.stashCount(player));
    }

    @Test
    void secondCookWithinCooldownIsRefused() {
        UUID player = UUID.randomUUID();
        methService.tryCook(player, 0);

        assertFalse(methService.tryCook(player, 10));
        assertEquals(1, methService.stashCount(player));
    }

    @Test
    void cookAfterCooldownExpiresSucceedsAgain() {
        UUID player = UUID.randomUUID();
        methService.tryCook(player, 0);

        assertTrue(methService.tryCook(player, MethService.COOK_COOLDOWN_TICKS));
        assertEquals(2, methService.stashCount(player));
    }

    @Test
    void dealingWithEmptyStashFails() {
        UUID player = UUID.randomUUID();
        assertFalse(methService.tryDeal(player, 0));
    }

    @Test
    void dealingSellsOneUnitAndPaysOut() {
        UUID player = UUID.randomUUID();
        methService.tryCook(player, 0);

        assertTrue(methService.tryDeal(player, 0));
        assertEquals(0, methService.stashCount(player));
        assertEquals(MethService.DEAL_PAYOUT_CENTS, bankService.getBalance(player));
    }

    @Test
    void secondDealWithinCooldownIsRefusedEvenWithStashRemaining() {
        UUID player = UUID.randomUUID();
        methService.tryCook(player, 0);
        methService.tryCook(player, MethService.COOK_COOLDOWN_TICKS);
        methService.tryDeal(player, MethService.COOK_COOLDOWN_TICKS);

        assertFalse(methService.tryDeal(player, MethService.COOK_COOLDOWN_TICKS + 5));
        assertEquals(1, methService.stashCount(player));
    }

    @Test
    void dealAfterCooldownExpiresSucceedsAgain() {
        UUID player = UUID.randomUUID();
        methService.tryCook(player, 0);
        methService.tryCook(player, MethService.COOK_COOLDOWN_TICKS);
        methService.tryDeal(player, MethService.COOK_COOLDOWN_TICKS);

        long secondDealTick = MethService.COOK_COOLDOWN_TICKS + MethService.DEAL_COOLDOWN_TICKS;
        assertTrue(methService.tryDeal(player, secondDealTick));
        assertEquals(MethService.DEAL_PAYOUT_CENTS * 2, bankService.getBalance(player));
    }

    @Test
    void differentPlayersHaveIndependentStashesAndCooldowns() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        methService.tryCook(first, 0);

        assertTrue(methService.tryCook(second, 5));
        assertEquals(1, methService.stashCount(first));
        assertEquals(1, methService.stashCount(second));
    }

    @Test
    void dealStreakIsZeroBeforeAnyDeal() {
        assertEquals(0, methService.getDealStreak(UUID.randomUUID()));
    }

    @Test
    void firstDealStartsTheStreakAtOne() {
        UUID player = UUID.randomUUID();
        methService.tryCook(player, 0);
        methService.tryDeal(player, 0);

        assertEquals(1, methService.getDealStreak(player));
    }

    @Test
    void aSecondDealWithinTheWindowExtendsTheStreak() {
        UUID player = UUID.randomUUID();
        methService.tryCook(player, 0);
        methService.tryDeal(player, 0);
        methService.tryCook(player, MethService.COOK_COOLDOWN_TICKS);
        methService.tryDeal(player, MethService.COOK_COOLDOWN_TICKS);

        assertEquals(2, methService.getDealStreak(player));
    }

    @Test
    void aDealStreakResetsAfterGoingLongEnoughWithoutAnotherDeal() {
        UUID player = UUID.randomUUID();
        methService.tryCook(player, 0);
        methService.tryDeal(player, 0);

        long farLaterTick = MethSeverity.STREAK_RESET_TICKS + 1;
        methService.tryCook(player, farLaterTick);
        methService.tryDeal(player, farLaterTick);

        assertEquals(1, methService.getDealStreak(player));
    }

    @Test
    void isTrackedIndependentlyFromNarcoticsService() {
        UUID player = UUID.randomUUID();
        NarcoticsService narcoticsService = new NarcoticsService(bankService);
        narcoticsService.tryCook(player, 0);
        narcoticsService.tryDeal(player, 0);

        assertEquals(0, methService.getDealStreak(player));
        assertEquals(0, methService.stashCount(player));
    }
}
