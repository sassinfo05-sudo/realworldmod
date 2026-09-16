package com.realworldmod.wildlife;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WildlifePopulationServiceTest {
    @Test
    void startsAtZero() {
        assertEquals(0, new WildlifePopulationService().getDeerPopulation());
    }

    @Test
    void recordingASpawnIncrementsThePopulation() {
        WildlifePopulationService service = new WildlifePopulationService();
        service.recordDeerSpawn();
        assertEquals(1, service.getDeerPopulation());

        service.recordDeerSpawn();
        assertEquals(2, service.getDeerPopulation());
    }

    @Test
    void recordingADeathDecrementsThePopulation() {
        WildlifePopulationService service = new WildlifePopulationService();
        service.recordDeerSpawn();
        service.recordDeerSpawn();

        service.recordDeerDeath();
        assertEquals(1, service.getDeerPopulation());
    }

    @Test
    void populationNeverGoesBelowZero() {
        WildlifePopulationService service = new WildlifePopulationService();
        service.recordDeerDeath();
        assertEquals(0, service.getDeerPopulation());
    }
}
