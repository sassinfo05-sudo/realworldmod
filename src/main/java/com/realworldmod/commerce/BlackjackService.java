package com.realworldmod.commerce;

import com.realworldmod.economy.BankService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

/**
 * Per-player blackjack session tracking: one active {@link BlackjackGame}
 * per player, holding the bet already withdrawn from {@link BankService}
 * until the round resolves and pays out. A resolved game stays visible
 * (so the client can show the final hands and outcome) until the player
 * starts a new one.
 */
public final class BlackjackService {
    public static final long BET_CENTS = 1000;

    private final BankService bankService;
    private final Random random = new Random();
    private final Map<UUID, BlackjackGame> games = new HashMap<>();
    private final Map<UUID, Long> activeBets = new HashMap<>();
    private final Map<UUID, Long> lastPayouts = new HashMap<>();

    public BlackjackService(BankService bankService) {
        this.bankService = bankService;
    }

    public Optional<BlackjackGame> activeGame(UUID playerId) {
        return Optional.ofNullable(games.get(playerId));
    }

    /** The payout from the most recently resolved round, or 0 if none has resolved yet. */
    public long lastPayoutCents(UUID playerId) {
        return lastPayouts.getOrDefault(playerId, 0L);
    }

    /** Withdraws the bet and deals a fresh round, if the player doesn't already have one in progress and can afford it. */
    public boolean startGame(UUID playerId) {
        BlackjackGame existing = games.get(playerId);
        if (existing != null && !existing.isResolved()) {
            return false;
        }
        if (bankService.withdraw(playerId, BET_CENTS).isEmpty()) {
            return false;
        }
        activeBets.put(playerId, BET_CENTS);
        BlackjackGame game = BlackjackGame.deal(random);
        games.put(playerId, game);
        if (game.isResolved()) {
            settle(playerId, game);
        }
        return true;
    }

    /** Draws another card for the player's in-progress round, if there is one. */
    public Optional<BlackjackGame> hit(UUID playerId) {
        BlackjackGame game = games.get(playerId);
        if (game == null || game.isResolved()) {
            return Optional.empty();
        }
        game.hit(random);
        if (game.isResolved()) {
            settle(playerId, game);
        }
        return Optional.of(game);
    }

    /** Stands on the player's in-progress round: the dealer plays out and the round resolves. */
    public Optional<BlackjackGame> stand(UUID playerId) {
        BlackjackGame game = games.get(playerId);
        if (game == null || game.isResolved()) {
            return Optional.empty();
        }
        game.stand(random);
        settle(playerId, game);
        return Optional.of(game);
    }

    private void settle(UUID playerId, BlackjackGame game) {
        Long bet = activeBets.remove(playerId);
        if (bet == null) {
            return;
        }
        long payout = Math.round(bet * BlackjackGame.payoutMultiplier(game.outcome()));
        if (payout > 0) {
            bankService.deposit(playerId, payout);
        }
        lastPayouts.put(playerId, payout);
    }
}
