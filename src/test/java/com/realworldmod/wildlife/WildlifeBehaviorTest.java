package com.realworldmod.wildlife;

import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WildlifeBehaviorTest {
    @Test
    void doesNotFleeWhenFarFromThePlayer() {
        assertFalse(WildlifeBehavior.shouldFlee(20.0 * 20.0));
    }

    @Test
    void fleesWhenWithinTheTriggerDistance() {
        assertTrue(WildlifeBehavior.shouldFlee(5.0 * 5.0));
    }

    @Test
    void fleesExactlyAtTheTriggerDistance() {
        assertTrue(WildlifeBehavior.shouldFlee(WildlifeBehavior.FLEE_TRIGGER_DISTANCE_SQUARED));
    }

    @Test
    void isCloseEnoughToHerdWithinRange() {
        assertTrue(WildlifeBehavior.isCloseEnoughToHerd(1.0));
        assertFalse(WildlifeBehavior.isCloseEnoughToHerd(10.0 * 10.0));
    }

    @Test
    void averagePositionOfOnePointIsThatPoint() {
        assertEquals(new Vec3d(1, 2, 3), WildlifeBehavior.averagePosition(List.of(new Vec3d(1, 2, 3))));
    }

    @Test
    void averagePositionOfMultiplePointsIsTheirCentroid() {
        Vec3d result = WildlifeBehavior.averagePosition(List.of(
                new Vec3d(0, 0, 0), new Vec3d(10, 0, 0), new Vec3d(5, 0, 10)));
        assertEquals(5.0, result.x, 1e-9);
        assertEquals(0.0, result.y, 1e-9);
        assertEquals(10.0 / 3.0, result.z, 1e-9);
    }

    @Test
    void averagePositionRejectsAnEmptyList() {
        assertThrows(IllegalArgumentException.class, () -> WildlifeBehavior.averagePosition(List.of()));
    }
}
