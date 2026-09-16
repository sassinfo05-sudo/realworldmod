package com.realworldmod.property;

import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

/**
 * Server-lifecycle-scoped facade over {@link ClaimRegistry} and
 * {@link ClaimDatabase}: opens/closes the database with the world save, and
 * is the single place new claims get created (keeping the registry and the
 * database from drifting out of sync).
 */
public final class PropertyService {
    private final ClaimRegistry registry;
    private ClaimDatabase database;

    public PropertyService(ClaimRegistry registry) {
        this.registry = registry;
    }

    public void open(Path dbFile) {
        database = new ClaimDatabase(dbFile);
        database.open();
        registry.clear();
        database.findAll().forEach(registry::add);
    }

    public void close() {
        if (database != null) {
            database.close();
        }
    }

    public ClaimRegistry registry() {
        return registry;
    }

    /**
     * Attempts to claim a square plot of the given radius centered on
     * ({@code centerX}, {@code centerZ}). Fails (returns empty) if the plot
     * would overlap any existing claim.
     */
    public Optional<Claim> claimPlot(UUID owner, int centerX, int centerZ, int radius) {
        int minX = centerX - radius;
        int maxX = centerX + radius;
        int minZ = centerZ - radius;
        int maxZ = centerZ + radius;

        if (registry.overlapsAny(minX, minZ, maxX, maxZ)) {
            return Optional.empty();
        }

        Claim claim = new Claim(UUID.randomUUID().toString(), owner, minX, minZ, maxX, maxZ);
        registry.add(claim);
        database.insert(claim);
        return Optional.of(claim);
    }
}
