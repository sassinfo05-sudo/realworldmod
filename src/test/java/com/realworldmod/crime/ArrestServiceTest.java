package com.realworldmod.crime;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArrestServiceTest {
    @Test
    void reachingMaxWantedLevelAloneDoesNothingWithoutBeingCaught() {
        CrimeService crimeService = new CrimeService();
        ArrestService arrestService = new ArrestService(crimeService, new TrialService());
        UUID player = UUID.randomUUID();
        crimeService.recordCrime(player, WantedLevelMath.MAX);

        assertEquals(ArrestOutcome.NOT_ARRESTED, arrestService.tick(player, 0));
        assertFalse(arrestService.isDetained(player));
    }

    @Test
    void guiltyVerdictAtTrialsEndTriggersArrest() {
        CrimeService crimeService = new CrimeService();
        TrialService trialService = new TrialService();
        ArrestService arrestService = new ArrestService(crimeService, trialService);
        UUID player = UUID.randomUUID();
        crimeService.recordCrime(player, WantedLevelMath.MAX);
        trialService.beginTrial(player, 0);

        assertEquals(ArrestOutcome.NOT_ARRESTED, arrestService.tick(player, TrialService.TRIAL_DURATION_TICKS - 1));
        assertEquals(ArrestOutcome.JUST_ARRESTED, arrestService.tick(player, TrialService.TRIAL_DURATION_TICKS));
        assertTrue(arrestService.isDetained(player));
    }

    @Test
    void notGuiltyVerdictAcquitsWithoutDetaining() {
        CrimeService crimeService = new CrimeService();
        TrialService trialService = new TrialService();
        ArrestService arrestService = new ArrestService(crimeService, trialService);
        UUID player = UUID.randomUUID();
        crimeService.recordCrime(player, WantedLevelMath.MAX);
        trialService.beginTrial(player, 0);
        crimeService.clear(player);

        assertEquals(ArrestOutcome.ACQUITTED, arrestService.tick(player, TrialService.TRIAL_DURATION_TICKS));
        assertFalse(arrestService.isDetained(player));
    }

    @Test
    void remainsDetainedDuringSentence() {
        CrimeService crimeService = new CrimeService();
        TrialService trialService = new TrialService();
        ArrestService arrestService = new ArrestService(crimeService, trialService);
        UUID player = UUID.randomUUID();
        crimeService.recordCrime(player, WantedLevelMath.MAX);
        trialService.beginTrial(player, 0);
        arrestService.tick(player, TrialService.TRIAL_DURATION_TICKS);

        assertEquals(ArrestOutcome.STILL_DETAINED,
                arrestService.tick(player, TrialService.TRIAL_DURATION_TICKS + 10));
        assertTrue(arrestService.isDetained(player));
    }

    @Test
    void releaseAfterSentenceClearsWantedLevel() {
        CrimeService crimeService = new CrimeService();
        TrialService trialService = new TrialService();
        ArrestService arrestService = new ArrestService(crimeService, trialService);
        UUID player = UUID.randomUUID();
        crimeService.recordCrime(player, WantedLevelMath.MAX);
        trialService.beginTrial(player, 0);
        arrestService.tick(player, TrialService.TRIAL_DURATION_TICKS);

        ArrestOutcome outcome = arrestService.tick(
                player, TrialService.TRIAL_DURATION_TICKS + ArrestService.SENTENCE_TICKS);

        assertEquals(ArrestOutcome.JUST_RELEASED, outcome);
        assertFalse(arrestService.isDetained(player));
        assertEquals(0, crimeService.getWantedLevel(player));
    }

    @Test
    void canBeArrestedAgainAfterRelease() {
        CrimeService crimeService = new CrimeService();
        TrialService trialService = new TrialService();
        ArrestService arrestService = new ArrestService(crimeService, trialService);
        UUID player = UUID.randomUUID();
        crimeService.recordCrime(player, WantedLevelMath.MAX);
        trialService.beginTrial(player, 0);
        arrestService.tick(player, TrialService.TRIAL_DURATION_TICKS);
        arrestService.tick(player, TrialService.TRIAL_DURATION_TICKS + ArrestService.SENTENCE_TICKS);

        crimeService.recordCrime(player, WantedLevelMath.MAX);
        long secondCatchTick = TrialService.TRIAL_DURATION_TICKS + ArrestService.SENTENCE_TICKS + 1;
        trialService.beginTrial(player, secondCatchTick);

        assertEquals(ArrestOutcome.JUST_ARRESTED,
                arrestService.tick(player, secondCatchTick + TrialService.TRIAL_DURATION_TICKS));
    }
}
