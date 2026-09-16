package com.realworldmod.crime;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrialServiceTest {
    @Test
    void aPlayerNeverPutOnTrialIsNotOnTrial() {
        TrialService trialService = new TrialService();
        assertFalse(trialService.isOnTrial(UUID.randomUUID()));
    }

    @Test
    void tickReturnsEmptyWhileTheTrialIsStillRunning() {
        TrialService trialService = new TrialService();
        UUID player = UUID.randomUUID();
        trialService.beginTrial(player, 0);

        assertTrue(trialService.isOnTrial(player));
        assertEquals(Optional.empty(), trialService.tick(player, TrialService.TRIAL_DURATION_TICKS - 1, WantedLevelMath.MAX));
    }

    @Test
    void tickReturnsGuiltyOnceTheTrialEndsAtMaxWantedLevel() {
        TrialService trialService = new TrialService();
        UUID player = UUID.randomUUID();
        trialService.beginTrial(player, 0);

        assertEquals(Optional.of(TrialVerdict.GUILTY),
                trialService.tick(player, TrialService.TRIAL_DURATION_TICKS, WantedLevelMath.MAX));
        assertFalse(trialService.isOnTrial(player));
    }

    @Test
    void tickReturnsNotGuiltyOnceTheTrialEndsBelowMaxWantedLevel() {
        TrialService trialService = new TrialService();
        UUID player = UUID.randomUUID();
        trialService.beginTrial(player, 0);

        assertEquals(Optional.of(TrialVerdict.NOT_GUILTY),
                trialService.tick(player, TrialService.TRIAL_DURATION_TICKS, WantedLevelMath.MAX - 1));
    }

    @Test
    void verdictIsOnlyReturnedOnce() {
        TrialService trialService = new TrialService();
        UUID player = UUID.randomUUID();
        trialService.beginTrial(player, 0);
        trialService.tick(player, TrialService.TRIAL_DURATION_TICKS, WantedLevelMath.MAX);

        assertEquals(Optional.empty(), trialService.tick(player, TrialService.TRIAL_DURATION_TICKS + 1, WantedLevelMath.MAX));
    }

    @Test
    void beginTrialIsIdempotentWhileAlreadyOnTrial() {
        TrialService trialService = new TrialService();
        UUID player = UUID.randomUUID();
        trialService.beginTrial(player, 0);
        trialService.beginTrial(player, 100);

        assertEquals(Optional.empty(), trialService.tick(player, TrialService.TRIAL_DURATION_TICKS - 1, WantedLevelMath.MAX));
        assertTrue(trialService.tick(player, TrialService.TRIAL_DURATION_TICKS, WantedLevelMath.MAX).isPresent());
    }
}
