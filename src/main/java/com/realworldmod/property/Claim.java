package com.realworldmod.property;

import java.util.UUID;

/**
 * A rectangular (full-height, X/Z bounded) owned plot of land — the "deed"
 * from the design doc's anti-griefing constraint. Claims never overlap by
 * construction of {@link ClaimRegistry#canModify}, which always resolves a
 * position against exactly the first claim that contains it.
 */
public record Claim(String id, UUID ownerId, int minX, int minZ, int maxX, int maxZ) {
    public Claim {
        if (minX > maxX || minZ > maxZ) {
            throw new IllegalArgumentException("Claim bounds must satisfy min <= max");
        }
    }

    public boolean contains(int x, int z) {
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }
}
