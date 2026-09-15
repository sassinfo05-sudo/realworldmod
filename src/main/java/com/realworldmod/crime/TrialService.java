package com.realworldmod.crime;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * The court/trial adjudication step between being physically apprehended by
 * a {@link PoliceEntity} ({@link ChaseWantedPlayerGoal}) and actually being
 * detained by {@link ArrestService} (Section 7): apprehension no longer
 * snaps a player straight into a cell. It starts a fixed-length trial
 * instead, and the verdict is decided against whatever the player's wanted
 * level has decayed to *by the time the trial ends*, not the level at the
 * moment they were caught — a player who stays clean (or lucky with
 * {@code CrimeService}'s decay) for the whole trial is acquitted rather
 * than convicted on stale evidence.
 */
public final class TrialService {
    public static final long TRIAL_DURATION_TICKS = 20L * 30L;

    private final Map<UUID, Long> trialEndTicks = new HashMap<>();

    public boolean isOnTrial(UUID playerId) {
        return trialEndTicks.containsKey(playerId);
    }

    /** Starts a trial for this player if one isn't already running; a no-op otherwise. */
    public void beginTrial(UUID playerId, long currentTick) {
        trialEndTicks.putIfAbsent(playerId, currentTick + TRIAL_DURATION_TICKS);
    }

    /**
     * Call once per server tick per player. Returns the verdict exactly on
     * the tick a trial concludes; empty every other tick, including when
     * the player isn't on trial at all.
     */
    public Optional<TrialVerdict> tick(UUID playerId, long currentTick, int currentWantedLevel) {
        Long endTick = trialEndTicks.get(playerId);
        if (endTick == null || currentTick < endTick) {
            return Optional.empty();
        }
        trialEndTicks.remove(playerId);
        return Optional.of(TrialVerdict.forWantedLevel(currentWantedLevel));
    }
}
