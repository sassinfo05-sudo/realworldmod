package com.realworldmod.utilities;

/**
 * A household's water hookup status (Section 9), billed and disconnected
 * independently of {@link UtilityState}'s power — a real household can
 * have power with no water, or water with no power.
 */
public record WaterState(boolean connected, long unpaidCents) {
    public static WaterState freshlyConnected() {
        return new WaterState(true, 0);
    }
}
