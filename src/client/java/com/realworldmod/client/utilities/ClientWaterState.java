package com.realworldmod.client.utilities;

/** Client-side cache of the last water status the server sent, mirroring {@link ClientUtilityState}. */
public final class ClientWaterState {
    public record Status(boolean connected, long unpaidCents) {
    }

    private static volatile Status status;

    private ClientWaterState() {
    }

    public static void set(boolean connected, long unpaidCents) {
        status = new Status(connected, unpaidCents);
    }

    /** Null until the first response from the server arrives this session. */
    public static Status get() {
        return status;
    }
}
