package com.realworldmod.property;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PropertyServiceTest {
    private PropertyService service;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        service = new PropertyService(new ClaimRegistry());
        service.open(tempDir.resolve("claims.sqlite"));
    }

    @AfterEach
    void tearDown() {
        service.close();
    }

    @Test
    void claimPlotSucceedsOnFreeLand() {
        Optional<Claim> claim = service.claimPlot(UUID.randomUUID(), 100, 100, 16);
        assertTrue(claim.isPresent());
        assertEquals(1, service.registry().all().size());
    }

    @Test
    void claimPlotFailsWhenOverlappingAnExistingClaim() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        assertTrue(service.claimPlot(first, 0, 0, 16).isPresent());

        Optional<Claim> overlapping = service.claimPlot(second, 10, 10, 16);
        assertFalse(overlapping.isPresent());
        assertEquals(1, service.registry().all().size());
    }

    @Test
    void claimedPlotIsPersistedAndReloadable(@TempDir Path tempDir) {
        Path dbFile = tempDir.resolve("reload.sqlite");
        PropertyService first = new PropertyService(new ClaimRegistry());
        first.open(dbFile);
        UUID owner = UUID.randomUUID();
        first.claimPlot(owner, 5, 5, 16);
        first.close();

        PropertyService reopened = new PropertyService(new ClaimRegistry());
        reopened.open(dbFile);
        assertEquals(1, reopened.registry().all().size());
        assertTrue(reopened.registry().canModify(owner, 5, 5));
        reopened.close();
    }
}
