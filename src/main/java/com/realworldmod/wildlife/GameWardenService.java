package com.realworldmod.wildlife;

import com.realworldmod.economy.BankService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks which players are actively "hot" for poaching — flagged the
 * moment {@link PoachingHandler} catches an unlicensed kill — so a
 * {@link GameWardenEntity} has a real, evadable target rather than every
 * poacher being punished only by an invisible fine (Section 8's "game
 * wardens are simulated only as an automatic fine, not an agent" gap).
 * Deliberately separate from {@code CrimeService}'s general wanted level:
 * that tracks a single overall number with no per-offense-type breakdown,
 * so it can't represent "wanted specifically for poaching" on its own.
 */
public final class GameWardenService {
    public static final long ALERT_DURATION_TICKS = 20L * 45L;
    public static final long APPREHENSION_FINE_CENTS = 5_000L;

    private final BankService bankService;
    private final Map<UUID, Long> alertExpiryTicks = new HashMap<>();

    public GameWardenService(BankService bankService) {
        this.bankService = bankService;
    }

    /** Starts (or refreshes) a player's poaching alert window. */
    public void flagPoacher(UUID playerId, long currentTick) {
        alertExpiryTicks.put(playerId, currentTick + ALERT_DURATION_TICKS);
    }

    public boolean isFlagged(UUID playerId, long currentTick) {
        Long expiry = alertExpiryTicks.get(playerId);
        return expiry != null && currentTick < expiry;
    }

    /**
     * A warden physically catching a flagged poacher: always ends the
     * chase (evading is what's supposed to clear it, not this), and fines
     * them if they can afford it.
     */
    public boolean apprehend(UUID playerId) {
        alertExpiryTicks.remove(playerId);
        return bankService.withdraw(playerId, APPREHENSION_FINE_CENTS).isPresent();
    }
}
