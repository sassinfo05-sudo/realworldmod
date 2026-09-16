package com.realworldmod.economy;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Server-lifecycle-scoped facade over an in-memory balance cache and
 * {@link BankDatabase}, mirroring the shape of {@code PropertyService}: one
 * place that mutates balances, so the cache and the database can't drift
 * apart.
 */
public final class BankService {
    /** Reserved account receiving the municipal sales-tax cut of purchases — see {@link SalesTax}. */
    public static final UUID TREASURY_ACCOUNT_ID = new UUID(0L, 0L);

    private final Map<UUID, Long> balances = new HashMap<>();
    private BankDatabase database;

    public void open(Path dbFile) {
        database = new BankDatabase(dbFile);
        database.open();
        balances.clear();
        balances.putAll(database.findAll());
    }

    public void close() {
        if (database != null) {
            database.close();
        }
    }

    public long getBalance(UUID ownerId) {
        return balances.getOrDefault(ownerId, 0L);
    }

    public long deposit(UUID ownerId, long amountCents) {
        long newBalance = BankMath.deposit(getBalance(ownerId), amountCents);
        setBalance(ownerId, newBalance);
        return newBalance;
    }

    /** Empty (no state change) if {@code ownerId} doesn't have {@code amountCents} available. */
    public Optional<Long> withdraw(UUID ownerId, long amountCents) {
        Optional<Long> newBalance = BankMath.withdraw(getBalance(ownerId), amountCents);
        newBalance.ifPresent(balance -> setBalance(ownerId, balance));
        return newBalance;
    }

    /** Withdraws from {@code from} and deposits into {@code to} atomically; false (no state change) if funds are insufficient. */
    public boolean transfer(UUID from, UUID to, long amountCents) {
        Optional<Long> fromNewBalance = withdraw(from, amountCents);
        if (fromNewBalance.isEmpty()) {
            return false;
        }
        deposit(to, amountCents);
        return true;
    }

    /** Deposits the municipal sales-tax cut of a completed purchase into the government treasury account. */
    public void remitSalesTax(long priceCents) {
        deposit(TREASURY_ACCOUNT_ID, SalesTax.taxCents(priceCents));
    }

    private void setBalance(UUID ownerId, long balanceCents) {
        balances.put(ownerId, balanceCents);
        database.setBalance(ownerId, balanceCents);
    }
}
