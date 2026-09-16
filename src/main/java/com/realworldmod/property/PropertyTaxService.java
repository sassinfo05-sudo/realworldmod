package com.realworldmod.property;

import com.realworldmod.economy.BankService;

/**
 * Periodic property tax on every owned claim (Section 7's tax-rate gap,
 * the property-tax third alongside {@code economy.SalesTax} and
 * {@code economy.IncomeTax}): once per in-game day, every claim owner is
 * charged a tax proportional to their plot's area, remitted to the same
 * government treasury the other two taxes use. An owner who can't afford
 * it simply isn't charged that cycle — there's no forfeiture/seizure
 * mechanic yet (see ROADMAP.md).
 */
public final class PropertyTaxService {
    public static final long TAX_INTERVAL_TICKS = 24_000L;
    public static final long RATE_CENTS_PER_BLOCK = 1;

    private final ClaimRegistry claimRegistry;
    private final BankService bankService;
    private long lastTaxedBucket = -1;

    public PropertyTaxService(ClaimRegistry claimRegistry, BankService bankService) {
        this.claimRegistry = claimRegistry;
        this.bankService = bankService;
    }

    public static long areaOf(Claim claim) {
        long width = (long) claim.maxX() - claim.minX() + 1;
        long depth = (long) claim.maxZ() - claim.minZ() + 1;
        return width * depth;
    }

    public static long taxFor(Claim claim) {
        return areaOf(claim) * RATE_CENTS_PER_BLOCK;
    }

    /** Call once per server tick; charges every claim owner once per {@link #TAX_INTERVAL_TICKS}. */
    public void tick(long currentTick) {
        long bucket = currentTick / TAX_INTERVAL_TICKS;
        if (bucket == lastTaxedBucket) {
            return;
        }
        lastTaxedBucket = bucket;

        for (Claim claim : claimRegistry.all()) {
            long tax = taxFor(claim);
            if (bankService.withdraw(claim.ownerId(), tax).isPresent()) {
                bankService.depositToTreasury(tax);
            }
        }
    }
}
