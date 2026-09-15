package com.realworldmod.vehicle;

/**
 * Pure per-tick drivetrain simulation (Section 4: "realistic engine torque,
 * ... tire friction, fuel consumption") — deliberately independent of any
 * Minecraft entity/world class so the physics itself is fully unit
 * testable. Wiring this into an actual rideable, rendered entity is a
 * separate, larger follow-up tracked in ROADMAP.md: that needs a custom
 * {@code Entity} subclass, an {@code EntityType} registration, input
 * capture, and a client-side renderer/model, none of which this slice
 * attempts.
 */
public final class VehiclePhysics {
    public static final double MAX_SPEED = 1.2;
    public static final double MAX_REVERSE_SPEED = -0.4;
    public static final double ACCELERATION_PER_TICK = 0.02;
    public static final double BRAKE_DECELERATION_PER_TICK = 0.05;
    public static final double COAST_DECELERATION_PER_TICK = 0.01;
    public static final double FUEL_LITERS_PER_TICK_AT_FULL_THROTTLE = 0.001;

    private VehiclePhysics() {
    }

    /**
     * Advances one tick. {@code throttle} is clamped to [-1, 1]: positive
     * accelerates forward, negative brakes while moving forward or
     * accelerates in reverse once stopped, zero coasts to a stop. A vehicle
     * with no fuel ignores throttle entirely (coasts regardless of input).
     */
    public static VehicleState tick(VehicleState state, double throttle) {
        double clampedThrottle = Math.max(-1.0, Math.min(1.0, throttle));
        boolean hasFuel = state.fuelLiters() > 0;
        double effectiveThrottle = hasFuel ? clampedThrottle : 0.0;

        double speed = nextSpeed(state.speedBlocksPerTick(), effectiveThrottle);
        double fuelUsed = hasFuel ? FUEL_LITERS_PER_TICK_AT_FULL_THROTTLE * Math.abs(clampedThrottle) : 0.0;
        double fuel = Math.max(0.0, state.fuelLiters() - fuelUsed);

        return new VehicleState(speed, fuel);
    }

    private static double nextSpeed(double speed, double throttle) {
        double next;
        if (throttle > 0) {
            next = speed + ACCELERATION_PER_TICK * throttle;
        } else if (throttle < 0) {
            next = speed > 0
                    ? speed + BRAKE_DECELERATION_PER_TICK * throttle
                    : speed + ACCELERATION_PER_TICK * throttle;
        } else {
            next = decelerateTowardZero(speed);
        }
        return Math.max(MAX_REVERSE_SPEED, Math.min(MAX_SPEED, next));
    }

    private static double decelerateTowardZero(double speed) {
        if (speed > 0) {
            return Math.max(0.0, speed - COAST_DECELERATION_PER_TICK);
        }
        if (speed < 0) {
            return Math.min(0.0, speed + COAST_DECELERATION_PER_TICK);
        }
        return 0.0;
    }
}
