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
 * alcohol. As of slice 67, repeated withdrawal episodes within
 * {@link WithdrawalSeverity#STREAK_RESET_TICKS} of each other escalate in
 * severity, mirroring {@code IntoxicationService}'s own hangover streak.
 */
public final class NicotineService {
    private final Map<UUID, Integer> cigaretteCount = new HashMap<>();
    private final Map<UUID, Integer> totalSmoked = new HashMap<>();
    private final Map<UUID, Long> lastSmokeTick = new HashMap<>();
    private final Map<UUID, Boolean> withdrawalApplied = new HashMap<>();
    private final Map<UUID, Integer> withdrawalStreak = new HashMap<>();
    private final Map<UUID, Long> lastWithdrawalTick = new HashMap<>();

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

    /**
     * Call once per server tick per online player. Returns 0 on every tick except the one a dependent
     * player's withdrawal kicks in, when it returns the withdrawal's severity (1 or higher) — escalating
     * with each withdrawal episode that follows a previous one within {@link WithdrawalSeverity#STREAK_RESET_TICKS}.
     */
    public int checkWithdrawal(UUID playerId, long currentTick) {
        if (!NicotineWithdrawalRisk.isDependent(totalSmoked.getOrDefault(playerId, 0))
                || withdrawalApplied.getOrDefault(playerId, false)) {
            return 0;
        }
        Long last = lastSmokeTick.get(playerId);
        if (last == null || !NicotineWithdrawalRisk.causesWithdrawal(currentTick - last)) {
            return 0;
        }
        withdrawalApplied.put(playerId, true);

        Long lastWithdrawal = lastWithdrawalTick.get(playerId);
        boolean streakContinues = lastWithdrawal != null
                && currentTick - lastWithdrawal < WithdrawalSeverity.STREAK_RESET_TICKS;
        int nextStreak = streakContinues ? withdrawalStreak.getOrDefault(playerId, 0) + 1 : 1;
        withdrawalStreak.put(playerId, nextStreak);
        lastWithdrawalTick.put(playerId, currentTick);
        return WithdrawalSeverity.forStreak(nextStreak);
    }

    /**
     * Applies a nicotine patch: delays the next possible withdrawal the same way an actual cigarette
     * would (resetting the clock {@link NicotineWithdrawalRisk#causesWithdrawal} checks against), but
     * without counting as smoking — {@code totalSmoked} and the illness streak are untouched. Also resets
     * the escalation streak, since managing a craving without relapsing breaks the pattern that built it.
     */
    public void usePatch(UUID playerId, long currentTick) {
        lastSmokeTick.put(playerId, currentTick);
        withdrawalApplied.put(playerId, false);
        withdrawalStreak.put(playerId, 0);
    }
}
