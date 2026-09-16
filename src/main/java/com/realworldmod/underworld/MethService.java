package com.realworldmod.underworld;

import com.realworldmod.economy.BankService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A second, distinct drug/lab type for Section 7's underworld system —
 * closing part of "the underworld/narcotics system built in slice 37 is a
 * single cook/deal loop at one block type." Same cook/deal/stash shape as
 * {@link NarcoticsService}, but a genuinely different risk/reward profile
 * rather than a reskin: a longer cook cycle, a higher payout per deal, and
 * (via {@link MethCatchChance}/{@link MethSeverity}) both a steeper catch
 * chance and a harsher severity ceiling than the original narcotics
 * economy — the "harder drug, harder consequences" distinction real-world
 * drug law actually draws. Tracked entirely independently of
 * {@code NarcoticsService}: a player can have a stash and a deal streak in
 * both at once.
 */
public final class MethService {
    public static final long COOK_COOLDOWN_TICKS = 20L * 45L;
    public static final long DEAL_COOLDOWN_TICKS = 20L * 15L;
    public static final long DEAL_PAYOUT_CENTS = 7_000L;

    private final BankService bankService;
    private final Map<UUID, Long> lastCookTick = new HashMap<>();
    private final Map<UUID, Integer> stash = new HashMap<>();
    private final Map<UUID, Long> lastDealTick = new HashMap<>();
    private final Map<UUID, Integer> dealStreak = new HashMap<>();

    public MethService(BankService bankService) {
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
                && currentTick - previousDealTick < MethSeverity.STREAK_RESET_TICKS;
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

    /** The player's current consecutive-deal streak, for {@link MethSeverity#forStreak}; 0 before their first deal. */
    public int getDealStreak(UUID playerId) {
        return dealStreak.getOrDefault(playerId, 0);
    }
}
