package com.realworldmod.commerce;

import com.realworldmod.economy.BankService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrapsServiceTest {
    private BankService bankService;
    private CrapsService service;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        bankService = new BankService();
        bankService.open(tempDir.resolve("bank.sqlite"));
        service = new CrapsService(bankService);
    }

    @AfterEach
    void tearDown() {
        bankService.close();
    }

    @Test
    void startGameWithdrawsTheChosenBet() {
        UUID player = UUID.randomUUID();
        bankService.deposit(player, 5000);

        assertTrue(service.startGame(player, 2000));
        assertEquals(5000 - 2000, bankService.getBalance(player));
        assertTrue(service.activeGame(player).isPresent());
    }

    @Test
    void startGameClampsAnOutOfRangeBetToTheAllowedMaximum() {
        UUID player = UUID.randomUUID();
        bankService.deposit(player, 100_000);

        assertTrue(service.startGame(player, 999_999));
        assertEquals(100_000 - BetSizing.MAX_BET_CENTS, bankService.getBalance(player));
    }

    @Test
    void startGameFailsWithoutEnoughForTheBet() {
        UUID player = UUID.randomUUID();
        assertFalse(service.startGame(player, BetSizing.DEFAULT_BET_CENTS));
        assertTrue(service.activeGame(player).isEmpty());
    }

    @Test
    void cannotStartWhileARoundIsAlreadyInProgress() {
        UUID player = UUID.randomUUID();
        bankService.deposit(player, 5000);
        service.startGame(player, BetSizing.DEFAULT_BET_CENTS);

        assertFalse(service.startGame(player, BetSizing.DEFAULT_BET_CENTS));
    }

    @Test
    void rollingWithoutAnActiveGameReturnsEmpty() {
        UUID player = UUID.randomUUID();
        assertTrue(service.roll(player).isEmpty());
    }

    @Test
    void cannotRollAfterTheRoundResolves() {
        UUID player = UUID.randomUUID();
        bankService.deposit(player, 5000);
        service.startGame(player, BetSizing.DEFAULT_BET_CENTS);

        Optional<CrapsGame> firstResult;
        do {
            firstResult = service.roll(player);
        } while (!firstResult.orElseThrow().isResolved());

        assertTrue(service.roll(player).isEmpty());
    }

    @Test
    void aWinPaysEvenMoneyOnTopOfTheReturnedBet() {
        UUID player = UUID.randomUUID();
        bankService.deposit(player, 5000);
        service.startGame(player, BetSizing.DEFAULT_BET_CENTS);
        long balanceAfterBet = bankService.getBalance(player);

        CrapsGame game;
        do {
            game = service.roll(player).orElseThrow();
        } while (!game.isResolved());

        if (game.outcome() == CrapsGame.Outcome.PASS_WIN) {
            assertEquals(balanceAfterBet + BetSizing.DEFAULT_BET_CENTS * 2, bankService.getBalance(player));
            assertEquals(BetSizing.DEFAULT_BET_CENTS * 2, service.lastPayoutCents(player));
        } else {
            assertEquals(balanceAfterBet, bankService.getBalance(player));
            assertEquals(0, service.lastPayoutCents(player));
        }
    }

    @Test
    void canStartAgainAfterTheLastRoundResolved() {
        UUID player = UUID.randomUUID();
        bankService.deposit(player, 5000);
        service.startGame(player, BetSizing.DEFAULT_BET_CENTS);

        CrapsGame game;
        do {
            game = service.roll(player).orElseThrow();
        } while (!game.isResolved());

        assertTrue(service.startGame(player, BetSizing.DEFAULT_BET_CENTS));
    }
}
