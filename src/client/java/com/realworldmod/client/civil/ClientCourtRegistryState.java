package com.realworldmod.client.civil;

/** Client-side cache of the last court-registry status the server sent, mirroring {@code ClientCrimeState}. */
public final class ClientCourtRegistryState {
    public record State(boolean hasPendingCase, long amountCents, long ticksRemaining, String plaintiffName) {
    }

    private static volatile State state;

    private ClientCourtRegistryState() {
    }

    public static void set(State value) {
        state = value;
    }

    /** Null until the first response from the server arrives this session. */
    public static State get() {
        return state;
    }
}
