package com.realworldmod.underworld;

import com.realworldmod.economy.BankService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A minimal stand-in for Section 7's entirely-unbuilt underworld/
 * narcotics system: cooking a batch (a cooldown-gated action, the same
 * shape {@code economy.JobService} uses for a legal wage) builds a
 * player's stash, and dealing sells one unit for real money — a higher
 * payout than any legal job in the mod, the actual incentive that makes
 * an underworld economy worth having. The crime risk of dealing lives in
 * {@code NarcoticsHandler}, not here — this class is purely the economics.
 * As of slice 69, it also tracks a consecutive-deal streak (reset after
 * {@link NarcoticsSeverity#STREAK_RESET_TICKS} without another deal) so
 * {@code NarcoticsHandler} can record a repeat dealer's offense at an
 * escalating severity instead of the same flat value every time.
 */
public final class NarcoticsService {
    public static final long COOK_COOLDOWN_TICKS = 20L * 30L;
    public static final long DEAL_COOLDOWN_TICKS = 20L * 10L;
    public static final long DEAL_PAYOUT_CENTS = 4_000L;

    private final BankService bankService;
    private final Map<UUID, Long> lastCookTick = new HashMap<>();
    private final Map<UUID, Integer> stash = new HashMap<>();
    private final Map<UUID, Long> lastDealTick = new HashMap<>();
    private final Map<UUID, Integer> dealStreak = new HashMap<>();

    public NarcoticsService(BankService bankService) {
        this.bankService = bankService;
    }

    /** Cooks one unit into the player's stash if they aren't still on cooldown; returns whether it succeeded. */
    public boolean tryCook(UUID playerId, long currentTick) {
        if (cookTicksRemaining(playerId, currentTick) > 0) {
            return false;
        }
        lastCookTick.put(playerId, currentTick);
        stash.merge(playerId, 1, Integer::sum);
        return true;
    }

    public long cookTicksRemaining(UUID playerId, long currentTick) {
        Long last = lastCookTick.get(playerId);
        if (last == null) {
            return 0;
        }
        return Math.max(0, COOK_COOLDOWN_TICKS - (currentTick - last));
    }

    public int stashCount(UUID playerId) {
        return stash.getOrDefault(playerId, 0);
    }

    /** Sells one unit from the stash for real money if the player has product and isn't still on cooldown. */
    public boolean tryDeal(UUID playerId, long currentTick) {
        if (stashCount(playerId) <= 0 || dealTicksRemaining(playerId, currentTick) > 0) {
            return false;
        }
        stash.merge(playerId, -1, Integer::sum);

        Long previousDealTick = lastDealTick.get(playerId);
        boolean streakContinues = previousDealTick != null
                && currentTick - previousDealTick < NarcoticsSeverity.STREAK_RESET_TICKS;
        dealStreak.put(playerId, streakContinues ? dealStreak.getOrDefault(playerId, 0) + 1 : 1);
        lastDealTick.put(playerId, currentTick);

        bankService.deposit(playerId, DEAL_PAYOUT_CENTS);
        return true;
    }

    public long dealTicksRemaining(UUID playerId, long currentTick) {
        Long last = lastDealTick.get(playerId);
        if (last == null) {
            return 0;
        }
        return Math.max(0, DEAL_COOLDOWN_TICKS - (currentTick - last));
    }

    /** The player's current consecutive-deal streak, for {@link NarcoticsSeverity#forStreak}; 0 before their first deal. */
    public int getDealStreak(UUID playerId) {
        return dealStreak.getOrDefault(playerId, 0);
    }
}
