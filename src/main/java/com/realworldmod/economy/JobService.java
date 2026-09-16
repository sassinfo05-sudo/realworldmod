package com.realworldmod.economy;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A minimal stand-in for Section 2/6's "job tasks" and "corporate payroll":
 * working a shift at a job site pays a flat wage, gated by a cooldown so it
 * can't be spammed. One cooldown per player regardless of which job site
 * they use — good enough for this slice; per-workplace shifts tied to
 * {@code NpcProfile}-style jobs are a later refinement. As of slice 31,
 * each shift withholds {@link IncomeTax} into the government treasury,
 * so the player actually takes home {@link #NET_WAGE_CENTS}.
 */
public final class JobService {
    public static final long WAGE_CENTS = 500;
    public static final long NET_WAGE_CENTS = WAGE_CENTS - IncomeTax.taxCents(WAGE_CENTS);
    public static final long COOLDOWN_TICKS = 200;

    private final BankService bankService;
    private final Map<UUID, Long> lastShiftTick = new HashMap<>();

    public JobService(BankService bankService) {
        this.bankService = bankService;
    }

    /** Pays the after-tax wage and starts a new cooldown if the player isn't still on one; returns whether it paid out. */
    public boolean tryWorkShift(UUID playerId, long currentTick) {
        if (ticksRemaining(playerId, currentTick) > 0) {
            return false;
        }
        lastShiftTick.put(playerId, currentTick);
        long tax = IncomeTax.taxCents(WAGE_CENTS);
        bankService.deposit(playerId, WAGE_CENTS - tax);
        bankService.depositToTreasury(tax);
        return true;
    }

    public long ticksRemaining(UUID playerId, long currentTick) {
        Long last = lastShiftTick.get(playerId);
        if (last == null) {
            return 0;
        }
        return Math.max(0, COOLDOWN_TICKS - (currentTick - last));
    }
}
