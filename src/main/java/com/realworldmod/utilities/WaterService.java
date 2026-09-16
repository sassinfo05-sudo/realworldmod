package com.realworldmod.utilities;

import com.realworldmod.economy.BankService;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Server-lifecycle-scoped facade over a cached {@link WaterState} per
 * player and {@link WaterDatabase} — the water counterpart to
 * {@link UtilityService}, billed and disconnected on its own independent
 * cycle rather than sharing power's connection status.
 */
public final class WaterService {
    public static final long BILL_CENTS = 800;
    public static final long BILLING_INTERVAL_TICKS = 20L * 60L * 5L;

    private final BankService bankService;
    private final Map<UUID, WaterState> states = new HashMap<>();
    private WaterDatabase database;
    private long lastBillingBucket = -1;

    public WaterService(BankService bankService) {
        this.bankService = bankService;
    }

    public void open(Path dbFile) {
        database = new WaterDatabase(dbFile);
        database.open();
        states.clear();
        states.putAll(database.findAll());
    }

    public void close() {
        if (database != null) {
            database.close();
        }
    }

    public WaterState getState(UUID ownerId) {
        return states.getOrDefault(ownerId, WaterState.freshlyConnected());
    }

    /** Attempts to pay off all accrued debt right now; returns whether it succeeded. */
    public boolean payNow(UUID ownerId) {
        WaterState previous = getState(ownerId);
        boolean paid = previous.unpaidCents() <= 0
                || bankService.withdraw(ownerId, previous.unpaidCents()).isPresent();
        setState(ownerId, WaterBillingMath.afterManualPayment(previous, paid));
        return paid;
    }

    /**
     * Call once per server tick with the currently online players; runs a
     * billing cycle for each of them at most once per
     * {@link #BILLING_INTERVAL_TICKS}. Returns the set of players who were
     * just disconnected by this cycle (for player-facing notification),
     * empty on ticks that didn't bill.
     */
    public List<UUID> tick(long currentTick, Iterable<UUID> onlinePlayerIds) {
        long bucket = currentTick / BILLING_INTERVAL_TICKS;
        if (bucket == lastBillingBucket) {
            return List.of();
        }
        lastBillingBucket = bucket;

        List<UUID> justDisconnected = new ArrayList<>();
        for (UUID playerId : onlinePlayerIds) {
            WaterState previous = getState(playerId);
            long amountOwed = previous.unpaidCents() + BILL_CENTS;
            boolean paid = bankService.withdraw(playerId, amountOwed).isPresent();
            WaterState next = WaterBillingMath.afterBillingAttempt(previous, BILL_CENTS, paid);
            setState(playerId, next);
            if (previous.connected() && !next.connected()) {
                justDisconnected.add(playerId);
            }
        }
        return justDisconnected;
    }

    private void setState(UUID ownerId, WaterState state) {
        states.put(ownerId, state);
        database.setState(ownerId, state);
    }
}
