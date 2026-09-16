package com.realworldmod.client.utilities;

/** Client-side cache of the last utility status the server sent, mirroring {@code ClientBankState}. */
public final class ClientUtilityState {
    public record Status(boolean powerConnected, long unpaidCents) {
    }

    private static volatile Status status;

    private ClientUtilityState() {
    }

    public static void set(boolean powerConnected, long unpaidCents) {
        status = new Status(powerConnected, unpaidCents);
    }

    /** Null until the first response from the server arrives this session. */
    public static Status get() {
        return status;
    }
}
