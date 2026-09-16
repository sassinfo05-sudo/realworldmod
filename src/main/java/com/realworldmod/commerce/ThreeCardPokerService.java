package com.realworldmod.commerce;

import com.realworldmod.economy.BankService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

/**
 * Per-player Three Card Poker session tracking — the same
 * withdraw-then-settle shape {@link BlackjackService} uses, adapted for a
 * two-stage bet (ante up front, an equal play bet only if the player
 * chooses to play after seeing their hand, the real game's structure).
 */
public final class ThreeCardPokerService {
    public static final long ANTE_CENTS = 1000;
    public static final long PLAY_CENTS = 1000;

    private final BankService bankService;
    private final Random random = new Random();
    private final Map<UUID, ThreeCardPokerGame> games = new HashMap<>();
    private final Map<UUID, Long> lastPayouts = new HashMap<>();

    public ThreeCardPokerService(BankService bankService) {
        this.bankService = bankService;
    }

    public Optional<ThreeCardPokerGame> activeGame(UUID playerId) {
        return Optional.ofNullable(games.get(playerId));
    }

    /** The payout from the most recently resolved round, or 0 if none has resolved yet. */
    public long lastPayoutCents(UUID playerId) {
        return lastPayouts.getOrDefault(playerId, 0L);
    }

    /** Withdraws the ante and deals a fresh round, if the player doesn't already have one in progress and can afford it. */
    public boolean deal(UUID playerId) {
        ThreeCardPokerGame existing = games.get(playerId);
        if (existing != null && !existing.isResolved()) {
            return false;
        }
        if (bankService.withdraw(playerId, ANTE_CENTS).isEmpty()) {
            return false;
        }
        games.put(playerId, ThreeCardPokerGame.deal(random));
        lastPayouts.remove(playerId);
        return true;
    }

    /** Folds the player's in-progress round, forfeiting the ante with no further bet ever placed. */
    public Optional<ThreeCardPokerGame> fold(UUID playerId) {
        ThreeCardPokerGame game = games.get(playerId);
        if (game == null || game.isResolved()) {
            return Optional.empty();
        }
        game.fold();
        lastPayouts.put(playerId, 0L);
        return Optional.of(game);
    }

    /** Withdraws the matching play bet and resolves the round, if the player can afford it. Leaves the round untouched if they can't. */
    public Optional<ThreeCardPokerGame> play(UUID playerId) {
        ThreeCardPokerGame game = games.get(playerId);
        if (game == null || game.isResolved()) {
            return Optional.empty();
        }
        if (bankService.withdraw(playerId, PLAY_CENTS).isEmpty()) {
            return Optional.empty();
        }

        ThreeCardPokerGame.Outcome outcome = game.play();
        long antePayout = Math.round(ANTE_CENTS * ThreeCardPokerGame.anteMultiplier(outcome));
        long playPayout = Math.round(PLAY_CENTS * ThreeCardPokerGame.playMultiplier(outcome));
        long totalPayout = antePayout + playPayout;
        if (totalPayout > 0) {
            bankService.deposit(playerId, totalPayout);
        }
        lastPayouts.put(playerId, totalPayout);
        return Optional.of(game);
    }
}
