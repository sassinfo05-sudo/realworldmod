package com.realworldmod.client.commerce;

import java.util.List;

/** Client-side cache of the last blackjack state the server sent, mirroring {@code ClientUtilityState}. */
public final class ClientBlackjackState {
    public record State(boolean hasActiveGame, List<Integer> playerHandOrdinals, List<Integer> dealerHandOrdinals,
                         boolean resolved, int outcomeOrdinal, long payoutCents) {
    }

    private static volatile State state;

    private ClientBlackjackState() {
    }

    public static void set(State newState) {
        state = newState;
    }

    /** Null until the first response from the server arrives this session. */
    public static State get() {
        return state;
    }
}
