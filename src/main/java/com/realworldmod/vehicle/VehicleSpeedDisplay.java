package com.realworldmod.vehicle;

/**
 * Pure conversion from {@link VehicleState}'s internal blocks-per-tick
 * speed to a real-world mph reading, kept free of any Minecraft
 * client dependency so it's unit testable on its own — the display half
 * of closing "no speed HUD" (Section 4). Minecraft runs at 20 ticks per
 * second and treats one block as one meter, so blocks-per-tick converts
 * to meters-per-second by multiplying by the tick rate, then to mph by
 * the standard real-world conversion factor.
 */
public final class VehicleSpeedDisplay {
    private static final double TICKS_PER_SECOND = 20.0;
    private static final double METERS_PER_SECOND_TO_MPH = 2.23694;

    private VehicleSpeedDisplay() {
    }

    /** Always non-negative — reverse and forward travel both show as a positive speed reading, like a real speedometer. */
    public static double milesPerHour(double speedBlocksPerTick) {
        double metersPerSecond = Math.abs(speedBlocksPerTick) * TICKS_PER_SECOND;
        return metersPerSecond * METERS_PER_SECOND_TO_MPH;
    }
}
