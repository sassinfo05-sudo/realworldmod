package com.realworldmod.npc;

import com.realworldmod.npc.goap.DailyState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NpcDatabaseTest {
    private NpcDatabase database;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        database = new NpcDatabase(tempDir.resolve("citizens.sqlite"));
        database.open();
    }

    @AfterEach
    void tearDown() {
        database.close();
    }

    @Test
    void upsertThenFindAllReturnsTheCitizen() {
        UUID id = UUID.randomUUID();
        NpcProfile profile = new NpcProfile(id, "Alex Rivera", "12 Maple St", "Downtown Diner",
                150_000L, 9, 17, DailyState.SLEEPING);

        database.upsert(profile);

        List<NpcProfile> all = database.findAll();
        assertEquals(1, all.size());
        assertEquals("Alex Rivera", all.get(0).name());
        assertEquals(DailyState.SLEEPING, all.get(0).currentState());
    }

    @Test
    void upsertOnExistingIdUpdatesRatherThanDuplicates() {
        UUID id = UUID.randomUUID();
        database.upsert(new NpcProfile(id, "Jordan Lee", "1 Oak Ave", "Steel Mill",
                200_000L, 22, 6, DailyState.WORKING));
        database.upsert(new NpcProfile(id, "Jordan Lee", "1 Oak Ave", "Steel Mill",
                210_000L, 22, 6, DailyState.WORKING));

        List<NpcProfile> all = database.findAll();
        assertEquals(1, all.size());
        assertEquals(210_000L, all.get(0).incomeCentsPerPayPeriod());
    }

    @Test
    void updateStatePersistsAcrossReload() {
        UUID id = UUID.randomUUID();
        database.upsert(new NpcProfile(id, "Sam Chen", "9 Birch Ln", "City Hall",
                180_000L, 9, 17, DailyState.SLEEPING));

        database.updateState(id, DailyState.WAKING);

        NpcProfile reloaded = database.findAll().stream()
                .filter(p -> p.id().equals(id))
                .findFirst()
                .orElseThrow();
        assertEquals(DailyState.WAKING, reloaded.currentState());
    }

    @Test
    void findByIdReturnsTheMatchingCitizen() {
        UUID id = UUID.randomUUID();
        database.upsert(new NpcProfile(id, "Morgan Diaz", "5 Elm St", "City Hall",
                160_000L, 9, 17, DailyState.LEISURE));

        assertTrue(database.findById(id).isPresent());
        assertEquals("Morgan Diaz", database.findById(id).orElseThrow().name());
    }

    @Test
    void findByIdIsEmptyForUnknownCitizen() {
        assertFalse(database.findById(UUID.randomUUID()).isPresent());
    }
}
