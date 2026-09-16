package com.realworldmod.vehicle;

/**
 * A vehicle's instantaneous drivetrain state: forward speed (blocks per
 * tick; negative is reverse) and remaining fuel (liters).
 */
public record VehicleState(double speedBlocksPerTick, double fuelLiters) {
    public static VehicleState atRestWithFuel(double fuelLiters) {
        return new VehicleState(0, fuelLiters);
    }
}
