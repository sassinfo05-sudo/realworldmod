package com.realworldmod.vehicle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VehicleSpeedDisplayTest {
    @Test
    void zeroSpeedIsZeroMph() {
        assertEquals(0.0, VehicleSpeedDisplay.milesPerHour(0.0), 0.0001);
    }

    @Test
    void oneBlockPerTickConvertsToRealMph() {
        // 1 block/tick * 20 ticks/sec = 20 m/s, * 2.23694 = 44.7388 mph.
        assertEquals(44.7388, VehicleSpeedDisplay.milesPerHour(1.0), 0.001);
    }

    @Test
    void maxCarSpeedConvertsToARealisticHighwaySpeed() {
        double mph = VehicleSpeedDisplay.milesPerHour(VehiclePhysics.MAX_SPEED);
        assertEquals(53.687, mph, 0.01);
    }

    @Test
    void reverseSpeedDisplaysAsPositive() {
        double forward = VehicleSpeedDisplay.milesPerHour(0.5);
        double reverse = VehicleSpeedDisplay.milesPerHour(-0.5);
        assertEquals(forward, reverse, 0.0001);
    }

    @Test
    void speedScalesLinearly() {
        double half = VehicleSpeedDisplay.milesPerHour(0.5);
        double full = VehicleSpeedDisplay.milesPerHour(1.0);
        assertEquals(full, half * 2, 0.0001);
    }
}
