package com.realworldmod.crime;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The "complete penitentiary simulation loop" from Section 7, reduced to
 * its smallest real shape for this slice: hitting the maximum wanted level
 * gets you detained for a fixed sentence, and serving it clears your
 * record. No actual prison structure, jobs, or breakout mechanics yet —
 * see ROADMAP.md.
 */
public final class ArrestService {
    public static final long SENTENCE_TICKS = 20L * 60L;

    private final CrimeService crimeService;
    private final Map<UUID, Long> releaseTicks = new HashMap<>();

    public ArrestService(CrimeService crimeService) {
        this.crimeService = crimeService;
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

        if (crimeService.getWantedLevel(playerId) >= WantedLevelMath.MAX) {
            releaseTicks.put(playerId, currentTick + SENTENCE_TICKS);
            return ArrestOutcome.JUST_ARRESTED;
        }
        return ArrestOutcome.NOT_ARRESTED;
    }
}
