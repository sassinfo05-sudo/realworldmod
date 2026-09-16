package com.realworldmod.property;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClaimDatabaseTest {
    private ClaimDatabase database;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        database = new ClaimDatabase(tempDir.resolve("claims.sqlite"));
        database.open();
    }

    @AfterEach
    void tearDown() {
        database.close();
    }

    @Test
    void insertThenFindAllReturnsTheClaim() {
        UUID owner = UUID.randomUUID();
        database.insert(new Claim("plot-1", owner, -10, -10, 10, 10));

        List<Claim> all = database.findAll();
        assertEquals(1, all.size());
        assertEquals("plot-1", all.get(0).id());
        assertEquals(owner, all.get(0).ownerId());
    }

    @Test
    void insertOnExistingIdUpdatesRatherThanDuplicates() {
        UUID owner = UUID.randomUUID();
        UUID newOwner = UUID.randomUUID();
        database.insert(new Claim("plot-1", owner, 0, 0, 5, 5));
        database.insert(new Claim("plot-1", newOwner, 0, 0, 5, 5));

        List<Claim> all = database.findAll();
        assertEquals(1, all.size());
        assertEquals(newOwner, all.get(0).ownerId());
    }
}
