package com.realworldmod.client.commerce;

/** Client-side cache of the last craps state the server sent, mirroring {@link ClientBlackjackState}. */
public final class ClientCrapsState {
    public record State(boolean hasActiveGame, int point, int lastRollTotal,
                         boolean resolved, int outcomeOrdinal, long payoutCents) {
    }

    private static volatile State state;

    private ClientCrapsState() {
    }

    public static void set(State newState) {
        state = newState;
    }

    /** Null until the first response from the server arrives this session. */
    public static State get() {
        return state;
    }
}
