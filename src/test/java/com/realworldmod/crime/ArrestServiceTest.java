package com.realworldmod.crime;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArrestServiceTest {
    @Test
    void belowMaxWantedLevelDoesNothing() {
        CrimeService crimeService = new CrimeService();
        ArrestService arrestService = new ArrestService(crimeService);
        UUID player = UUID.randomUUID();
        crimeService.recordCrime(player, WantedLevelMath.MAX - 1);

        assertEquals(ArrestOutcome.NOT_ARRESTED, arrestService.tick(player, 0));
        assertFalse(arrestService.isDetained(player));
    }

    @Test
    void reachingMaxWantedLevelTriggersArrest() {
        CrimeService crimeService = new CrimeService();
        ArrestService arrestService = new ArrestService(crimeService);
        UUID player = UUID.randomUUID();
        crimeService.recordCrime(player, WantedLevelMath.MAX);

        assertEquals(ArrestOutcome.JUST_ARRESTED, arrestService.tick(player, 0));
        assertTrue(arrestService.isDetained(player));
    }

    @Test
    void remainsDetainedDuringSentence() {
        CrimeService crimeService = new CrimeService();
        ArrestService arrestService = new ArrestService(crimeService);
        UUID player = UUID.randomUUID();
        crimeService.recordCrime(player, WantedLevelMath.MAX);
        arrestService.tick(player, 0);

        assertEquals(ArrestOutcome.STILL_DETAINED, arrestService.tick(player, 10));
        assertTrue(arrestService.isDetained(player));
    }

    @Test
    void releaseAfterSentenceClearsWantedLevel() {
        CrimeService crimeService = new CrimeService();
        ArrestService arrestService = new ArrestService(crimeService);
        UUID player = UUID.randomUUID();
        crimeService.recordCrime(player, WantedLevelMath.MAX);
        arrestService.tick(player, 0);

        ArrestOutcome outcome = arrestService.tick(player, ArrestService.SENTENCE_TICKS);

        assertEquals(ArrestOutcome.JUST_RELEASED, outcome);
        assertFalse(arrestService.isDetained(player));
        assertEquals(0, crimeService.getWantedLevel(player));
    }

    @Test
    void canBeArrestedAgainAfterRelease() {
        CrimeService crimeService = new CrimeService();
        ArrestService arrestService = new ArrestService(crimeService);
        UUID player = UUID.randomUUID();
        crimeService.recordCrime(player, WantedLevelMath.MAX);
        arrestService.tick(player, 0);
        arrestService.tick(player, ArrestService.SENTENCE_TICKS);

        crimeService.recordCrime(player, WantedLevelMath.MAX);
        assertEquals(ArrestOutcome.JUST_ARRESTED, arrestService.tick(player, ArrestService.SENTENCE_TICKS + 1));
    }
}
