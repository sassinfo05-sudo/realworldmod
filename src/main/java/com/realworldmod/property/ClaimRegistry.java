package com.realworldmod.property;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * In-memory index of every land claim, kept in sync with
 * {@link ClaimDatabase}. Pure query logic lives here so it is unit testable
 * without a running server.
 *
 * <p>Unclaimed land is left freely modifiable — only plots someone has
 * actually deeded are protected. This is a deliberate scope decision for
 * this slice: the full brief's "cannot punch trees, dig soil... randomly
 * anywhere" default-deny behavior needs a companion permit system (world
 * borders, wilderness-gathering permits) that is out of scope here and
 * tracked in ROADMAP.md.
 */
public final class ClaimRegistry {
    private final List<Claim> claims = new ArrayList<>();

    public void clear() {
        claims.clear();
    }

    public void add(Claim claim) {
        claims.add(claim);
    }

    public List<Claim> all() {
        return List.copyOf(claims);
    }

    public Optional<Claim> findClaimAt(int x, int z) {
        return claims.stream().filter(claim -> claim.contains(x, z)).findFirst();
    }

    /** Whether {@code playerId} may break/place blocks at ({@code x}, {@code z}). */
    public boolean canModify(UUID playerId, int x, int z) {
        return findClaimAt(x, z)
                .map(claim -> claim.ownerId().equals(playerId))
                .orElse(true);
    }
}
