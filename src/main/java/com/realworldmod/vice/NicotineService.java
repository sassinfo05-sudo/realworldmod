package com.realworldmod.vice;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * In-memory per-player cigarette counter, the same shape
 * {@code medical.IllnessService} already established for rain exposure:
 * smoking enough in a row triggers a real illness rather than nothing
 * happening beyond a brief nicotine buzz. Not persisted across a relog —
 * the same acceptable gap {@code medical.IllnessService} documents. As of
 * slice 62, it also tracks a cumulative (never-resetting) smoke count and
 * the tick of a player's last cigarette, so {@link #checkWithdrawal} can
 * trigger real withdrawal symptoms once a dependent player goes too long
 * without one — the same "cross a threshold, trigger once" shape
 * {@code vice.IntoxicationService#checkHangover} already established for
 * alcohol.
 */
public final class NicotineService {
    private final Map<UUID, Integer> cigaretteCount = new HashMap<>();
    private final Map<UUID, Integer> totalSmoked = new HashMap<>();
    private final Map<UUID, Long> lastSmokeTick = new HashMap<>();
    private final Map<UUID, Boolean> withdrawalApplied = new HashMap<>();

    /** Call once per cigarette smoked; returns true exactly on the smoke that triggers illness. */
    public boolean smoke(UUID playerId, long currentTick) {
        int next = NicotineRisk.nextCount(cigaretteCount.getOrDefault(playerId, 0));
        boolean triggered = NicotineRisk.causesIllness(next);
        cigaretteCount.put(playerId, triggered ? 0 : next);

        totalSmoked.merge(playerId, 1, Integer::sum);
        lastSmokeTick.put(playerId, currentTick);
        withdrawalApplied.put(playerId, false);
        return triggered;
    }

    /** Call once per server tick per online player; returns true exactly on the tick a dependent player's withdrawal kicks in. */
    public boolean checkWithdrawal(UUID playerId, long currentTick) {
        if (!NicotineWithdrawalRisk.isDependent(totalSmoked.getOrDefault(playerId, 0))
                || withdrawalApplied.getOrDefault(playerId, false)) {
            return false;
        }
        Long last = lastSmokeTick.get(playerId);
        if (last == null || !NicotineWithdrawalRisk.causesWithdrawal(currentTick - last)) {
            return false;
        }
        withdrawalApplied.put(playerId, true);
        return true;
    }
}
