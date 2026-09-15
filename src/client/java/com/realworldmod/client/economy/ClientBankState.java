package com.realworldmod.client.economy;

/**
 * Client-side cache of the last bank balance the server sent us. A single
 * shared holder (rather than per-screen state) means a response that
 * arrives after its requesting screen has already closed isn't wasted, and
 * a freshly opened Banking screen shows the last known value immediately
 * while its own request is in flight.
 */
public final class ClientBankState {
    private static volatile Long balanceCents;

    private ClientBankState() {
    }

    public static void set(long value) {
        balanceCents = value;
    }

    /** Null until the first response from the server arrives this session. */
    public static Long get() {
        return balanceCents;
    }
}
