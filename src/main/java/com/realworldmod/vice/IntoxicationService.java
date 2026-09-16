package com.realworldmod.vice;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * In-memory per-player intoxication tracker. A player sobers up gradually
 * — one level clears every {@link #DECAY_TICKS_PER_LEVEL} ticks since
 * their last drink — computed lazily from elapsed ticks rather than a
 * separate periodic tick call, the same "derive from elapsed ticks"
 * approach the cooldown-remaining calculations elsewhere in the mod use.
 * Not persisted across a relog — the same acceptable gap
 * {@code medical.IllnessService} already documents. As of slice 60, it
 * also tracks whether a player peaked at {@link IntoxicationCalculator#MAX_LEVEL}
 * so {@link #checkHangover} can trigger a real hangover once they've
 * fully sobered back up — the same "cross a threshold, trigger once"
 * shape {@code medical.IllnessService#tick} already established. As of
 * slice 67, repeated hangovers within {@link HangoverSeverity#STREAK_RESET_TICKS}
 * of each other escalate in severity instead of always being identical.
 * As of slice 71, a player can head one off before it starts:
 * {@link #useRecoveryDrink} cancels an impending hangover and resets its
 * escalation streak, the same preventive shape
 * {@code vice.NicotineService#usePatch} already established for
 * withdrawal.
 */
public final class IntoxicationService {
    public static final long DECAY_TICKS_PER_LEVEL = 20L * 60L;

    private final Map<UUID, Integer> level = new HashMap<>();
    private final Map<UUID, Long> lastDrinkTick = new HashMap<>();
    private final Map<UUID, Boolean> peakedAtMaxLevel = new HashMap<>();
    private final Map<UUID, Integer> hangoverStreak = new HashMap<>();
    private final Map<UUID, Long> lastHangoverTick = new HashMap<>();

    public int drink(UUID playerId, long currentTick) {
        int next = IntoxicationCalculator.nextLevel(currentLevel(playerId, currentTick));
        level.put(playerId, next);
        lastDrinkTick.put(playerId, currentTick);
        if (next == IntoxicationCalculator.MAX_LEVEL) {
            peakedAtMaxLevel.put(playerId, true);
        }
        return next;
    }

    public int currentLevel(UUID playerId, long currentTick) {
        int stored = level.getOrDefault(playerId, 0);
        if (stored == 0) {
            return 0;
        }
        long elapsed = currentTick - lastDrinkTick.getOrDefault(playerId, currentTick);
        long decayedLevels = elapsed / DECAY_TICKS_PER_LEVEL;
        return (int) Math.max(0, stored - decayedLevels);
    }

    /**
     * Call once per server tick per online player. Returns 0 on every tick except the one a player who
     * peaked at max level finishes sobering up, when it returns the hangover's severity (1 or higher) —
     * escalating with each hangover that follows a previous one within {@link HangoverSeverity#STREAK_RESET_TICKS}.
     */
    public int checkHangover(UUID playerId, long currentTick) {
        if (!peakedAtMaxLevel.getOrDefault(playerId, false) || currentLevel(playerId, currentTick) > 0) {
            return 0;
        }
        peakedAtMaxLevel.put(playerId, false);

        Long lastHangover = lastHangoverTick.get(playerId);
        boolean streakContinues = lastHangover != null && currentTick - lastHangover < HangoverSeverity.STREAK_RESET_TICKS;
        int nextStreak = streakContinues ? hangoverStreak.getOrDefault(playerId, 0) + 1 : 1;
        hangoverStreak.put(playerId, nextStreak);
        lastHangoverTick.put(playerId, currentTick);
        return HangoverSeverity.forStreak(nextStreak);
    }

    /** Cancels an impending hangover and resets its escalation streak; returns whether there was one pending. */
    public boolean useRecoveryDrink(UUID playerId) {
        if (!peakedAtMaxLevel.getOrDefault(playerId, false)) {
            return false;
        }
        peakedAtMaxLevel.put(playerId, false);
        hangoverStreak.put(playerId, 0);
        return true;
    }
}
