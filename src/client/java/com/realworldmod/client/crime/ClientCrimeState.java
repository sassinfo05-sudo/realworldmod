package com.realworldmod.client.crime;

/** Client-side cache of the last wanted level the server sent, mirroring {@code ClientBankState}. */
public final class ClientCrimeState {
    private static volatile Integer wantedLevel;

    private ClientCrimeState() {
    }

    public static void set(int value) {
        wantedLevel = value;
    }

    /** Null until the first response from the server arrives this session. */
    public static Integer get() {
        return wantedLevel;
    }
}
