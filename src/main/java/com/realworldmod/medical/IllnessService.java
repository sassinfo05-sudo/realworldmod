package com.realworldmod.medical;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * In-memory per-player rain-exposure tracker. Not persisted across a
 * relog (see ROADMAP.md) — an exposure timer resetting on rejoin is a
 * minor, acceptable gap.
 */
public final class IllnessService {
    private final Map<UUID, Integer> exposureTicks = new HashMap<>();

    /** Call once per server tick per online player; returns true exactly on the tick illness is triggered. */
    public boolean tick(UUID playerId, boolean isExposedNow) {
        int next = IllnessRisk.nextExposureTicks(exposureTicks.getOrDefault(playerId, 0), isExposedNow);
        boolean triggered = IllnessRisk.causesIllness(next);
        exposureTicks.put(playerId, triggered ? 0 : next);
        return triggered;
    }
}
