package com.realworldmod.vice;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * In-memory per-player cigarette counter, the same shape
 * {@code medical.IllnessService} already established for rain exposure:
 * smoking enough in a row triggers a real illness rather than nothing
 * happening beyond a brief nicotine buzz. Not persisted across a relog —
 * the same acceptable gap {@code medical.IllnessService} documents.
 */
public final class NicotineService {
    private final Map<UUID, Integer> cigaretteCount = new HashMap<>();

    /** Call once per cigarette smoked; returns true exactly on the smoke that triggers illness. */
    public boolean smoke(UUID playerId) {
        int next = NicotineRisk.nextCount(cigaretteCount.getOrDefault(playerId, 0));
        boolean triggered = NicotineRisk.causesIllness(next);
        cigaretteCount.put(playerId, triggered ? 0 : next);
        return triggered;
    }
}
