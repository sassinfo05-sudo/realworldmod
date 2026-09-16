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

class ThreeCardPokerServiceTest {
    private BankService bankService;
    private ThreeCardPokerService service;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        bankService = new BankService();
        bankService.open(tempDir.resolve("bank.sqlite"));
        service = new ThreeCardPokerService(bankService);
    }

    @AfterEach
    void tearDown() {
        bankService.close();
    }

    @Test
    void dealWithdrawsTheChosenAnte() {
        UUID player = UUID.randomUUID();
        bankService.deposit(player, 5000);

        assertTrue(service.deal(player, 2000));
        assertEquals(5000 - 2000, bankService.getBalance(player));
        assertTrue(service.activeGame(player).isPresent());
    }

    @Test
    void dealClampsAnOutOfRangeAnteToTheAllowedMaximum() {
        UUID player = UUID.randomUUID();
        bankService.deposit(player, 100_000);

        assertTrue(service.deal(player, 999_999));
        assertEquals(100_000 - BetSizing.MAX_BET_CENTS, bankService.getBalance(player));
    }

    @Test
    void dealFailsWithoutEnoughForTheAnte() {
        UUID player = UUID.randomUUID();
        assertFalse(service.deal(player, BetSizing.DEFAULT_BET_CENTS));
        assertTrue(service.activeGame(player).isEmpty());
    }

    @Test
    void cannotDealWhileARoundIsAlreadyInProgress() {
        UUID player = UUID.randomUUID();
        bankService.deposit(player, 5000);
        service.deal(player, BetSizing.DEFAULT_BET_CENTS);

        assertFalse(service.deal(player, BetSizing.DEFAULT_BET_CENTS));
    }

    @Test
    void canDealAgainAfterTheLastRoundResolved() {
        UUID player = UUID.randomUUID();
        bankService.deposit(player, 5000);
        service.deal(player, BetSizing.DEFAULT_BET_CENTS);
        service.fold(player);

        assertTrue(service.deal(player, BetSizing.DEFAULT_BET_CENTS));
    }

    @Test
    void foldingForfeitsTheAnteAndPaysNothing() {
        UUID player = UUID.randomUUID();
        bankService.deposit(player, 5000);
        service.deal(player, BetSizing.DEFAULT_BET_CENTS);
        long balanceAfterAnte = bankService.getBalance(player);

        Optional<ThreeCardPokerGame> game = service.fold(player);

        assertTrue(game.isPresent());
        assertEquals(ThreeCardPokerGame.Outcome.FOLDED, game.get().outcome());
        assertEquals(balanceAfterAnte, bankService.getBalance(player));
        assertEquals(0, service.lastPayoutCents(player));
    }

    @Test
    void playingWithdrawsAPlayBetMatchingTheChosenAnte() {
        UUID player = UUID.randomUUID();
        bankService.deposit(player, 5000);
        service.deal(player, 2000);
        long balanceAfterAnte = bankService.getBalance(player);

        service.play(player);

        long balanceAfterPlay = bankService.getBalance(player) - service.lastPayoutCents(player);
        assertEquals(balanceAfterAnte - 2000, balanceAfterPlay);
    }

    @Test
    void cannotPlayWithoutEnoughForThePlayBet() {
        UUID player = UUID.randomUUID();
        bankService.deposit(player, BetSizing.DEFAULT_BET_CENTS);
        service.deal(player, BetSizing.DEFAULT_BET_CENTS);

        Optional<ThreeCardPokerGame> result = service.play(player);

        assertTrue(result.isEmpty());
        assertFalse(service.activeGame(player).orElseThrow().isResolved());
    }

    @Test
    void actingWithoutAnActiveGameReturnsEmpty() {
        UUID player = UUID.randomUUID();
        assertTrue(service.fold(player).isEmpty());
        assertTrue(service.play(player).isEmpty());
    }

    @Test
    void cannotActTwiceOnAResolvedGame() {
        UUID player = UUID.randomUUID();
        bankService.deposit(player, 5000);
        service.deal(player, BetSizing.DEFAULT_BET_CENTS);
        service.fold(player);

        assertTrue(service.fold(player).isEmpty());
    }
}
