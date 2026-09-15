package com.realworldmod.crime;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CrimeServiceTest {
    @Test
    void newPlayerStartsClean() {
        CrimeService service = new CrimeService();
        assertEquals(0, service.getWantedLevel(UUID.randomUUID()));
    }

    @Test
    void recordCrimeIncreasesLevel() {
        CrimeService service = new CrimeService();
        UUID player = UUID.randomUUID();
        service.recordCrime(player, CrimeService.TRESPASS_SEVERITY);
        assertEquals(1, service.getWantedLevel(player));
    }

    @Test
    void levelClampsAtMaxAcrossRepeatedCrimes() {
        CrimeService service = new CrimeService();
        UUID player = UUID.randomUUID();
        for (int i = 0; i < 20; i++) {
            service.recordCrime(player, CrimeService.TRESPASS_SEVERITY);
        }
        assertEquals(WantedLevelMath.MAX, service.getWantedLevel(player));
    }

    @Test
    void tickDoesNothingWithinSameDecayBucket() {
        CrimeService service = new CrimeService();
        UUID player = UUID.randomUUID();
        service.tick(0); // establishes the baseline decay bucket (0)
        service.recordCrime(player, 2);

        service.tick(500); // still bucket 0 (500 / 1200 == 0)
        assertEquals(2, service.getWantedLevel(player));
    }

    @Test
    void tickDecaysOnceIntervalPasses() {
        CrimeService service = new CrimeService();
        UUID player = UUID.randomUUID();
        service.tick(0); // establishes the baseline decay bucket (0)
        service.recordCrime(player, 2);

        service.tick(20L * 60L); // bucket 1: first decay
        assertEquals(1, service.getWantedLevel(player));

        service.tick(20L * 60L * 2L); // bucket 2: second decay
        assertEquals(0, service.getWantedLevel(player));
    }

    @Test
    void differentPlayersAreTrackedIndependently() {
        CrimeService service = new CrimeService();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        service.recordCrime(first, 3);

        assertEquals(3, service.getWantedLevel(first));
        assertEquals(0, service.getWantedLevel(second));
    }
}
