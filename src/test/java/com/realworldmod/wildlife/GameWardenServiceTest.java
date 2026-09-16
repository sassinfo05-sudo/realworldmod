package com.realworldmod.wildlife;

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

class GameWardenServiceTest {
    private BankService bankService;
    private GameWardenService gameWardenService;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        bankService = new BankService();
        bankService.open(tempDir.resolve("bank.sqlite"));
        gameWardenService = new GameWardenService(bankService);
    }

    @AfterEach
    void tearDown() {
        bankService.close();
    }

    @Test
    void aPlayerNeverFlaggedIsNotFlagged() {
        assertFalse(gameWardenService.isFlagged(UUID.randomUUID(), 0));
    }

    @Test
    void flaggedPlayerStaysFlaggedWithinTheAlertWindow() {
        UUID player = UUID.randomUUID();
        gameWardenService.flagPoacher(player, 0);

        assertTrue(gameWardenService.isFlagged(player, GameWardenService.ALERT_DURATION_TICKS - 1));
    }

    @Test
    void flagExpiresAfterTheAlertWindow() {
        UUID player = UUID.randomUUID();
        gameWardenService.flagPoacher(player, 0);

        assertFalse(gameWardenService.isFlagged(player, GameWardenService.ALERT_DURATION_TICKS));
    }

    @Test
    void apprehendingClearsTheFlagRegardlessOfPayment() {
        UUID player = UUID.randomUUID();
        gameWardenService.flagPoacher(player, 0);

        gameWardenService.apprehend(player);

        assertFalse(gameWardenService.isFlagged(player, 0));
    }

    @Test
    void apprehendingFinesWhenAffordable() {
        UUID player = UUID.randomUUID();
        bankService.deposit(player, 100_000);
        gameWardenService.flagPoacher(player, 0);

        boolean fined = gameWardenService.apprehend(player);

        assertTrue(fined);
        assertEquals(100_000 - GameWardenService.APPREHENSION_FINE_CENTS, bankService.getBalance(player));
    }

    @Test
    void apprehendingWithoutFundsIsNotFinedAndDoesNotThrow() {
        UUID player = UUID.randomUUID();
        gameWardenService.flagPoacher(player, 0);

        boolean fined = gameWardenService.apprehend(player);

        assertFalse(fined);
        assertEquals(0, bankService.getBalance(player));
    }
}
