package com.realworldmod.client.civil;

/** Client-side cache of the last civil-case filing-history summary the server sent, mirroring {@link ClientCourtRegistryState}. */
public final class ClientCourtRegistryHistoryState {
    public record State(int pastCaseCount, boolean hasMostRecent, String mostRecentOpponentName,
                         long mostRecentAmountCents, boolean mostRecentContested) {
    }

    private static volatile State state;

    private ClientCourtRegistryHistoryState() {
    }

    public static void set(State value) {
        state = value;
    }

    /** Null until the first response from the server arrives this session. */
    public static State get() {
        return state;
    }
}
