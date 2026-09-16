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
    void dealWithdrawsTheAnte() {
        UUID player = UUID.randomUUID();
        bankService.deposit(player, 5000);

        assertTrue(service.deal(player));
        assertEquals(5000 - ThreeCardPokerService.ANTE_CENTS, bankService.getBalance(player));
        assertTrue(service.activeGame(player).isPresent());
    }

    @Test
    void dealFailsWithoutEnoughForTheAnte() {
        UUID player = UUID.randomUUID();
        assertFalse(service.deal(player));
        assertTrue(service.activeGame(player).isEmpty());
    }

    @Test
    void cannotDealWhileARoundIsAlreadyInProgress() {
        UUID player = UUID.randomUUID();
        bankService.deposit(player, 5000);
        service.deal(player);

        assertFalse(service.deal(player));
    }

    @Test
    void canDealAgainAfterTheLastRoundResolved() {
        UUID player = UUID.randomUUID();
        bankService.deposit(player, 5000);
        service.deal(player);
        service.fold(player);

        assertTrue(service.deal(player));
    }

    @Test
    void foldingForfeitsTheAnteAndPaysNothing() {
        UUID player = UUID.randomUUID();
        bankService.deposit(player, 5000);
        service.deal(player);
        long balanceAfterAnte = bankService.getBalance(player);

        Optional<ThreeCardPokerGame> game = service.fold(player);

        assertTrue(game.isPresent());
        assertEquals(ThreeCardPokerGame.Outcome.FOLDED, game.get().outcome());
        assertEquals(balanceAfterAnte, bankService.getBalance(player));
        assertEquals(0, service.lastPayoutCents(player));
    }

    @Test
    void playingWithdrawsTheMatchingPlayBet() {
        UUID player = UUID.randomUUID();
        bankService.deposit(player, 5000);
        service.deal(player);
        long balanceAfterAnte = bankService.getBalance(player);

        service.play(player);

        long balanceAfterPlay = bankService.getBalance(player) - service.lastPayoutCents(player);
        assertEquals(balanceAfterAnte - ThreeCardPokerService.PLAY_CENTS, balanceAfterPlay);
    }

    @Test
    void cannotPlayWithoutEnoughForThePlayBet() {
        UUID player = UUID.randomUUID();
        bankService.deposit(player, ThreeCardPokerService.ANTE_CENTS);
        service.deal(player);

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
        service.deal(player);
        service.fold(player);

        assertTrue(service.fold(player).isEmpty());
    }
}
