package com.realworldmod.property;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClaimRegistryTest {
    private final UUID owner = UUID.randomUUID();
    private final UUID stranger = UUID.randomUUID();
    private ClaimRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new ClaimRegistry();
        registry.add(new Claim("plot-1", owner, 0, 0, 10, 10));
    }

    @Test
    void unclaimedLandIsModifiableByAnyone() {
        assertTrue(registry.canModify(stranger, 100, 100));
    }

    @Test
    void ownerCanModifyTheirOwnClaim() {
        assertTrue(registry.canModify(owner, 5, 5));
    }

    @Test
    void strangerCannotModifySomeoneElsesClaim() {
        assertFalse(registry.canModify(stranger, 5, 5));
    }

    @Test
    void claimBoundaryIsInclusive() {
        assertTrue(registry.canModify(owner, 0, 0));
        assertTrue(registry.canModify(owner, 10, 10));
    }

    @Test
    void justOutsideClaimBoundaryIsUnclaimed() {
        assertTrue(registry.canModify(stranger, 11, 0));
        assertTrue(registry.canModify(stranger, 0, -1));
    }

    @Test
    void clearRemovesAllClaims() {
        registry.clear();
        assertTrue(registry.canModify(stranger, 5, 5));
    }
}
