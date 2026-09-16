package com.realworldmod.client.wildlife;

/** Client-side cache of the last deer population count the server sent, mirroring {@code economy.ClientTreasuryState}. */
public final class ClientWildlifePopulationState {
    private static volatile Integer deerPopulation;

    private ClientWildlifePopulationState() {
    }

    public static void set(int value) {
        deerPopulation = value;
    }

    /** Null until the first response from the server arrives this session. */
    public static Integer get() {
        return deerPopulation;
    }
}
