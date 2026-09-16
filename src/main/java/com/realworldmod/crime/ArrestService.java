package com.realworldmod.crime;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * The "complete penitentiary simulation loop" from Section 7, reduced to
 * its smallest real shape for this slice: getting physically apprehended
 * by a {@link PoliceEntity} starts a {@link TrialService} trial, and a
 * {@code GUILTY} verdict at the end of it is what actually detains a
 * player for a fixed sentence — hitting the max wanted level alone no
 * longer teleports anyone anywhere, since as of slice 23 nothing happens
 * without a police entity making contact first (see ROADMAP.md; still no
 * actual prison structure, jobs, or breakout mechanics).
 */
public final class ArrestService {
    public static final long SENTENCE_TICKS = 20L * 60L;

    private final CrimeService crimeService;
    private final TrialService trialService;
    private final Map<UUID, Long> releaseTicks = new HashMap<>();

    public ArrestService(CrimeService crimeService, TrialService trialService) {
        this.crimeService = crimeService;
        this.trialService = trialService;
    }

    public boolean isDetained(UUID playerId) {
        return releaseTicks.containsKey(playerId);
    }

    /** Call once per server tick per online player. */
    public ArrestOutcome tick(UUID playerId, long currentTick) {
        Long releaseTick = releaseTicks.get(playerId);
        if (releaseTick != null) {
            if (currentTick < releaseTick) {
                return ArrestOutcome.STILL_DETAINED;
            }
            releaseTicks.remove(playerId);
            crimeService.clear(playerId);
            return ArrestOutcome.JUST_RELEASED;
        }

        Optional<TrialVerdict> verdict = trialService.tick(playerId, currentTick, crimeService.getWantedLevel(playerId));
        if (verdict.isEmpty()) {
            return ArrestOutcome.NOT_ARRESTED;
        }
        if (verdict.get() == TrialVerdict.GUILTY) {
            releaseTicks.put(playerId, currentTick + SENTENCE_TICKS);
            return ArrestOutcome.JUST_ARRESTED;
        }
        return ArrestOutcome.ACQUITTED;
    }
}
