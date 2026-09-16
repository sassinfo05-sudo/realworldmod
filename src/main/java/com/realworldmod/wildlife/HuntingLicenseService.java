package com.realworldmod.wildlife;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Tracks which players currently hold a state hunting license (Section 8:
 * "Hunting without state licenses alerts NPC game wardens"). A license
 * here is a permanent permit once bought — no renewal or expiry — and not
 * persisted across a relog yet; see ROADMAP.md.
 */
public final class HuntingLicenseService {
    private final Set<UUID> licensedPlayers = new HashSet<>();

    public boolean hasLicense(UUID playerId) {
        return licensedPlayers.contains(playerId);
    }

    public void grantLicense(UUID playerId) {
        licensedPlayers.add(playerId);
    }
}
