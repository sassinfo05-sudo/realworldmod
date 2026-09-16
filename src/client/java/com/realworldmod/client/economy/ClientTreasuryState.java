package com.realworldmod.client.economy;

/**
 * Client-side cache of the last treasury balance the server sent us —
 * same shape as {@link ClientBankState}, just for the government account
 * rather than the player's own.
 */
public final class ClientTreasuryState {
    private static volatile Long balanceCents;

    private ClientTreasuryState() {
    }

    public static void set(long value) {
        balanceCents = value;
    }

    /** Null until the first response from the server arrives this session. */
    public static Long get() {
        return balanceCents;
    }
}
