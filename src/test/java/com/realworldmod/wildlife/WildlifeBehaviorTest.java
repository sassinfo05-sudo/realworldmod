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
    void doesNotHuntWhenPreyIsFarAway() {
        assertFalse(WildlifeBehavior.shouldHunt(20.0 * 20.0));
    }

    @Test
    void huntsWhenPreyIsWithinTriggerDistance() {
        assertTrue(WildlifeBehavior.shouldHunt(8.0 * 8.0));
    }

    @Test
    void huntsExactlyAtTheTriggerDistance() {
        assertTrue(WildlifeBehavior.shouldHunt(WildlifeBehavior.HUNT_TRIGGER_DISTANCE_SQUARED));
    }

    @Test
    void cannotAttackBeyondAttackRange() {
        assertFalse(WildlifeBehavior.canAttack(5.0 * 5.0));
    }

    @Test
    void canAttackWithinAttackRange() {
        assertTrue(WildlifeBehavior.canAttack(1.0));
        assertTrue(WildlifeBehavior.canAttack(WildlifeBehavior.ATTACK_RANGE_SQUARED));
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

    @Test
    void isPackMateWithinRadius() {
        assertTrue(WildlifeBehavior.isPackMate(4.0 * 4.0));
        assertTrue(WildlifeBehavior.isPackMate(WildlifeBehavior.PACK_RADIUS_SQUARED));
    }

    @Test
    void isNotPackMateBeyondRadius() {
        assertFalse(WildlifeBehavior.isPackMate(20.0 * 20.0));
    }

    @Test
    void aLoneCoyoteAttacksAtTheBaseMultiplier() {
        assertEquals(1.0, WildlifeBehavior.packAttackMultiplier(0), 1e-9);
    }

    @Test
    void eachPackMateAddsARealDamageBonus() {
        assertEquals(1.5, WildlifeBehavior.packAttackMultiplier(1), 1e-9);
        assertEquals(2.0, WildlifeBehavior.packAttackMultiplier(2), 1e-9);
    }

    @Test
    void packAttackMultiplierClampsAtTheMaximum() {
        assertEquals(WildlifeBehavior.MAX_PACK_ATTACK_MULTIPLIER, WildlifeBehavior.packAttackMultiplier(10), 1e-9);
    }
}
