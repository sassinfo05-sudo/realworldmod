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
    private final BankService bankService;
    private final Random random = new Random();
    private final Map<UUID, ThreeCardPokerGame> games = new HashMap<>();
    private final Map<UUID, Long> antes = new HashMap<>();
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

    /** Withdraws the ante (clamped to {@link BetSizing}'s range) and deals a fresh round, if the player doesn't already have one in progress and can afford it. */
    public boolean deal(UUID playerId, long anteCents) {
        ThreeCardPokerGame existing = games.get(playerId);
        if (existing != null && !existing.isResolved()) {
            return false;
        }
        long ante = BetSizing.clamp(anteCents);
        if (bankService.withdraw(playerId, ante).isEmpty()) {
            return false;
        }
        antes.put(playerId, ante);
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
        antes.remove(playerId);
        lastPayouts.put(playerId, 0L);
        return Optional.of(game);
    }

    /** Withdraws a play bet matching the ante and resolves the round, if the player can afford it. Leaves the round untouched if they can't. */
    public Optional<ThreeCardPokerGame> play(UUID playerId) {
        ThreeCardPokerGame game = games.get(playerId);
        if (game == null || game.isResolved()) {
            return Optional.empty();
        }
        Long ante = antes.get(playerId);
        if (ante == null || bankService.withdraw(playerId, ante).isEmpty()) {
            return Optional.empty();
        }
        antes.remove(playerId);

        ThreeCardPokerGame.Outcome outcome = game.play();
        long antePayout = Math.round(ante * ThreeCardPokerGame.anteMultiplier(outcome));
        long playPayout = Math.round(ante * ThreeCardPokerGame.playMultiplier(outcome));
        long totalPayout = antePayout + playPayout;
        if (totalPayout > 0) {
            bankService.deposit(playerId, totalPayout);
        }
        lastPayouts.put(playerId, totalPayout);
        return Optional.of(game);
    }
}
