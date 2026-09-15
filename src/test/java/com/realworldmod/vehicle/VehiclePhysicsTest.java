package com.realworldmod.vehicle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VehiclePhysicsTest {
    @Test
    void fullThrottleAcceleratesFromRest() {
        VehicleState state = VehicleState.atRestWithFuel(10);
        VehicleState next = VehiclePhysics.tick(state, 1.0);
        assertTrue(next.speedBlocksPerTick() > 0);
        assertEquals(VehiclePhysics.ACCELERATION_PER_TICK, next.speedBlocksPerTick(), 1e-9);
    }

    @Test
    void speedNeverExceedsMaxSpeed() {
        VehicleState state = VehicleState.atRestWithFuel(1000);
        for (int i = 0; i < 10_000; i++) {
            state = VehiclePhysics.tick(state, 1.0);
        }
        assertEquals(VehiclePhysics.MAX_SPEED, state.speedBlocksPerTick(), 1e-9);
    }

    @Test
    void speedNeverExceedsMaxReverseSpeed() {
        VehicleState state = VehicleState.atRestWithFuel(1000);
        for (int i = 0; i < 10_000; i++) {
            state = VehiclePhysics.tick(state, -1.0);
        }
        assertEquals(VehiclePhysics.MAX_REVERSE_SPEED, state.speedBlocksPerTick(), 1e-9);
    }

    @Test
    void coastingGraduallyDeceleratesToZero() {
        VehicleState state = new VehicleState(0.5, 10);
        VehicleState next = VehiclePhysics.tick(state, 0.0);
        assertTrue(next.speedBlocksPerTick() < 0.5);
        assertTrue(next.speedBlocksPerTick() > 0);
    }

    @Test
    void coastingNeverOvershootsPastZero() {
        VehicleState state = new VehicleState(0.005, 10);
        VehicleState next = VehiclePhysics.tick(state, 0.0);
        assertEquals(0.0, next.speedBlocksPerTick(), 1e-9);
    }

    @Test
    void brakingDeceleratesFasterThanCoasting() {
        VehicleState moving = new VehicleState(0.5, 10);
        VehicleState braked = VehiclePhysics.tick(moving, -1.0);
        VehicleState coasted = VehiclePhysics.tick(moving, 0.0);
        assertTrue(braked.speedBlocksPerTick() < coasted.speedBlocksPerTick());
    }

    @Test
    void reverseThrottleMovesBackwardWhenStopped() {
        VehicleState stopped = VehicleState.atRestWithFuel(10);
        VehicleState next = VehiclePhysics.tick(stopped, -1.0);
        assertTrue(next.speedBlocksPerTick() < 0);
    }

    @Test
    void fullThrottleConsumesFuel() {
        VehicleState state = VehicleState.atRestWithFuel(10);
        VehicleState next = VehiclePhysics.tick(state, 1.0);
        assertTrue(next.fuelLiters() < 10);
        assertEquals(10 - VehiclePhysics.FUEL_LITERS_PER_TICK_AT_FULL_THROTTLE, next.fuelLiters(), 1e-9);
    }

    @Test
    void coastingConsumesNoFuel() {
        VehicleState state = new VehicleState(0.3, 10);
        VehicleState next = VehiclePhysics.tick(state, 0.0);
        assertEquals(10.0, next.fuelLiters(), 1e-9);
    }

    @Test
    void emptyTankIgnoresThrottleAndJustCoasts() {
        VehicleState outOfFuel = new VehicleState(0.5, 0);
        VehicleState next = VehiclePhysics.tick(outOfFuel, 1.0);
        assertTrue(next.speedBlocksPerTick() < 0.5, "should coast down, not accelerate, with no fuel");
        assertEquals(0.0, next.fuelLiters(), 1e-9);
    }

    @Test
    void throttleInputIsClampedBeyondFullRange() {
        VehicleState state = VehicleState.atRestWithFuel(10);
        VehicleState next = VehiclePhysics.tick(state, 5.0);
        assertEquals(VehiclePhysics.ACCELERATION_PER_TICK, next.speedBlocksPerTick(), 1e-9);
    }
}
