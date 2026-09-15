package com.realworldmod.economy;

import java.util.Optional;

/** Pure balance arithmetic, in integer cents, kept free of any persistence or Minecraft dependency. */
public final class BankMath {
    private BankMath() {
    }

    public static long deposit(long balanceCents, long amountCents) {
        requireNonNegative(amountCents);
        return balanceCents + amountCents;
    }

    /** Empty if {@code amountCents} exceeds the available balance. */
    public static Optional<Long> withdraw(long balanceCents, long amountCents) {
        requireNonNegative(amountCents);
        if (amountCents > balanceCents) {
            return Optional.empty();
        }
        return Optional.of(balanceCents - amountCents);
    }

    private static void requireNonNegative(long amountCents) {
        if (amountCents < 0) {
            throw new IllegalArgumentException("amountCents must be >= 0, was " + amountCents);
        }
    }
}
