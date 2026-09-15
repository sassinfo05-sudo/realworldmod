package com.realworldmod.phone;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhoneBatteryTest {
    @Test
    void drainReducesLevelByAmount() {
        assertEquals(48, PhoneBattery.drain(50, 2));
    }

    @Test
    void drainClampsAtZero() {
        assertEquals(0, PhoneBattery.drain(1, 5));
    }

    @Test
    void chargeIncreasesLevelByAmount() {
        assertEquals(52, PhoneBattery.charge(50, 2));
    }

    @Test
    void chargeClampsAtMax() {
        assertEquals(100, PhoneBattery.charge(99, 5));
    }

    @Test
    void isDeadOnlyAtZeroOrBelow() {
        assertTrue(PhoneBattery.isDead(0));
        assertFalse(PhoneBattery.isDead(1));
    }

    @Test
    void isFullOnlyAtMaxOrAbove() {
        assertTrue(PhoneBattery.isFull(100));
        assertFalse(PhoneBattery.isFull(99));
    }
}
