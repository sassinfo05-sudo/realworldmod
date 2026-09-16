package com.realworldmod.commerce;

import com.realworldmod.economy.BankService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

/**
 * Per-player craps session tracking — the same withdraw-then-settle shape
 * {@link BlackjackService}/{@link ThreeCardPokerService} use, adapted for
 * a Pass Line bet that can take any number of rolls to resolve rather
 * than a fixed two-step flow.
 */
public final class CrapsService {
    public static final long BET_CENTS = 1000;

    private final BankService bankService;
    private final Random random = new Random();
    private final Map<UUID, CrapsGame> games = new HashMap<>();
    private final Map<UUID, Long> lastPayouts = new HashMap<>();

    public CrapsService(BankService bankService) {
        this.bankService = bankService;
    }

    public Optional<CrapsGame> activeGame(UUID playerId) {
        return Optional.ofNullable(games.get(playerId));
    }

    /** The payout from the most recently resolved round, or 0 if none has resolved yet. */
    public long lastPayoutCents(UUID playerId) {
        return lastPayouts.getOrDefault(playerId, 0L);
    }

    /** Withdraws the bet and starts a fresh come-out roll sequence, if the player doesn't already have one in progress and can afford it. */
    public boolean startGame(UUID playerId) {
        CrapsGame existing = games.get(playerId);
        if (existing != null && !existing.isResolved()) {
            return false;
        }
        if (bankService.withdraw(playerId, BET_CENTS).isEmpty()) {
            return false;
        }
        games.put(playerId, CrapsGame.start());
        lastPayouts.remove(playerId);
        return true;
    }

    /** Rolls the dice for the player's in-progress round, settling it if that roll resolves the round. */
    public Optional<CrapsGame> roll(UUID playerId) {
        CrapsGame game = games.get(playerId);
        if (game == null || game.isResolved()) {
            return Optional.empty();
        }
        game.roll(random);
        if (game.isResolved()) {
            settle(playerId, game);
        }
        return Optional.of(game);
    }

    private void settle(UUID playerId, CrapsGame game) {
        long payout = Math.round(BET_CENTS * CrapsGame.payoutMultiplier(game.outcome()));
        if (payout > 0) {
            bankService.deposit(playerId, payout);
        }
        lastPayouts.put(playerId, payout);
    }
}
