package com.realworldmod.crime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * In-memory wanted-level tracker. Not persisted across a relog yet (see
 * ROADMAP.md) — good enough for a first slice, since a wanted level is
 * meant to fade rather than follow a player forever anyway.
 */
public final class CrimeService {
    public static final int TRESPASS_SEVERITY = 1;
    private static final long DECAY_INTERVAL_TICKS = 20L * 60L;

    private final Map<UUID, Integer> wantedLevels = new HashMap<>();
    private long lastDecayBucket = -1;

    public int getWantedLevel(UUID playerId) {
        return wantedLevels.getOrDefault(playerId, WantedLevelMath.MIN);
    }

    public int recordCrime(UUID playerId, int severity) {
        int next = WantedLevelMath.increase(getWantedLevel(playerId), severity);
        wantedLevels.put(playerId, next);
        return next;
    }

    /** Call once per server tick; decays every tracked player's level by 1 once per {@code DECAY_INTERVAL_TICKS}. */
    public void tick(long currentTick) {
        long bucket = currentTick / DECAY_INTERVAL_TICKS;
        if (bucket == lastDecayBucket) {
            return;
        }
        lastDecayBucket = bucket;

        for (UUID playerId : new ArrayList<>(wantedLevels.keySet())) {
            int next = WantedLevelMath.decay(wantedLevels.get(playerId), 1);
            if (next == WantedLevelMath.MIN) {
                wantedLevels.remove(playerId);
            } else {
                wantedLevels.put(playerId, next);
            }
        }
    }
}
