package com.realworldmod.client.civil;

import com.realworldmod.civil.net.CourtRegistryHistoryResponsePayload;

import java.util.List;

/**
 * Client-side cache of the last civil-case filing-history the server sent,
 * mirroring {@link ClientCourtRegistryState}. As of slice 70, this holds a
 * bounded list of entries (see {@code CourtRegistryNetworking.MAX_HISTORY_ENTRIES})
 * instead of just the single most recent case.
 */
public final class ClientCourtRegistryHistoryState {
    public record State(int totalCount, List<CourtRegistryHistoryResponsePayload.HistoryEntry> entries) {
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
