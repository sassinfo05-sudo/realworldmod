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
 * {@code medical.IllnessService} already documents.
 */
public final class IntoxicationService {
    public static final long DECAY_TICKS_PER_LEVEL = 20L * 60L;

    private final Map<UUID, Integer> level = new HashMap<>();
    private final Map<UUID, Long> lastDrinkTick = new HashMap<>();

    public int drink(UUID playerId, long currentTick) {
        int next = IntoxicationCalculator.nextLevel(currentLevel(playerId, currentTick));
        level.put(playerId, next);
        lastDrinkTick.put(playerId, currentTick);
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
}
