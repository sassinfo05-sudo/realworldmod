package com.realworldmod.commerce;

import com.realworldmod.economy.BankService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link BlackjackService} deals real random hands internally (it isn't
 * seeded/injectable), so these tests assert the session-management
 * contract — bets withdrawn, one game at a time, resolution unlocks a new
 * one — rather than exact post-hand balances, which depend on whatever
 * hand actually got dealt.
 */
class BlackjackServiceTest {
    private BankService bankService;
    private BlackjackService blackjackService;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        bankService = new BankService();
        bankService.open(tempDir.resolve("bank.sqlite"));
        blackjackService = new BlackjackService(bankService);
    }

    @AfterEach
    void tearDown() {
        bankService.close();
    }

    @Test
    void startingAGameWithoutFundsFails() {
        UUID player = UUID.randomUUID();
        assertFalse(blackjackService.startGame(player));
        assertTrue(blackjackService.activeGame(player).isEmpty());
    }

    @Test
    void startingAGameWithdrawsTheBetAndCreatesAGame() {
        UUID player = UUID.randomUUID();
        bankService.deposit(player, 100_000);

        assertTrue(blackjackService.startGame(player));

        assertTrue(blackjackService.activeGame(player).isPresent());
        assertTrue(bankService.getBalance(player) < 100_000);
    }

    @Test
    void cannotStartASecondGameWhileTheFirstIsUnresolved() {
        UUID player = UUID.randomUUID();
        bankService.deposit(player, 100_000);
        blackjackService.startGame(player);

        boolean stillUnresolved = blackjackService.activeGame(player).map(g -> !g.isResolved()).orElse(false);
        if (stillUnresolved) {
            assertFalse(blackjackService.startGame(player));
        }
    }

    @Test
    void standingAlwaysResolvesAnInProgressGame() {
        UUID player = UUID.randomUUID();
        bankService.deposit(player, 100_000);
        blackjackService.startGame(player);

        blackjackService.activeGame(player).ifPresent(game -> {
            if (!game.isResolved()) {
                blackjackService.stand(player);
            }
        });

        assertTrue(blackjackService.activeGame(player).map(BlackjackGame::isResolved).orElse(true));
    }

    @Test
    void canStartANewGameAfterTheFirstResolves() {
        UUID player = UUID.randomUUID();
        bankService.deposit(player, 1_000_000);
        blackjackService.startGame(player);
        blackjackService.activeGame(player).ifPresent(game -> {
            if (!game.isResolved()) {
                blackjackService.stand(player);
            }
        });

        assertTrue(blackjackService.startGame(player));
    }

    @Test
    void hittingWithNoActiveGameReturnsEmpty() {
        UUID player = UUID.randomUUID();
        assertTrue(blackjackService.hit(player).isEmpty());
    }

    @Test
    void standingWithNoActiveGameReturnsEmpty() {
        UUID player = UUID.randomUUID();
        assertTrue(blackjackService.stand(player).isEmpty());
    }
}
