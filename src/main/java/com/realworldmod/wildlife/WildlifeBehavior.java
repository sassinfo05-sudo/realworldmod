package com.realworldmod.wildlife;

import net.minecraft.util.math.Vec3d;

import java.util.List;

/**
 * Pure decision logic behind {@link DeerEntity}'s AI goals, kept separate
 * from the {@code Goal} subclasses themselves ({@link FleeFromPlayerGoal},
 * {@link HerdWithOthersGoal}) so it's unit-testable without a running
 * Minecraft world — the same split {@code CommuteTarget}/{@code CommuteGoal}
 * used for slice 21.
 */
public final class WildlifeBehavior {
    public static final double FLEE_TRIGGER_DISTANCE_SQUARED = 10.0 * 10.0;
    public static final double HERD_RANGE_SQUARED = 16.0 * 16.0;
    public static final double HERD_TOO_CLOSE_SQUARED = 3.0 * 3.0;
    public static final double HUNT_TRIGGER_DISTANCE_SQUARED = 14.0 * 14.0;
    public static final double ATTACK_RANGE_SQUARED = 2.0 * 2.0;
    public static final double PACK_RADIUS_SQUARED = 8.0 * 8.0;
    public static final double PACK_BONUS_PER_ALLY = 0.5;
    public static final double MAX_PACK_ATTACK_MULTIPLIER = 2.5;

    private WildlifeBehavior() {
    }

    /** Real wildlife flees on proximity alone, unlike vanilla passive animals which only flee once hit. */
    public static boolean shouldFlee(double distanceToPlayerSquared) {
        return distanceToPlayerSquared <= FLEE_TRIGGER_DISTANCE_SQUARED;
    }

    /** A predator starts stalking prey once it's within this range — wider than an attack, so it has to close the gap. */
    public static boolean shouldHunt(double distanceToPreySquared) {
        return distanceToPreySquared <= HUNT_TRIGGER_DISTANCE_SQUARED;
    }

    /** True once a hunting predator has actually closed to striking range. */
    public static boolean canAttack(double distanceToPreySquared) {
        return distanceToPreySquared <= ATTACK_RANGE_SQUARED;
    }

    /** True once a herd-mate is close enough that closing further would just cause crowding. */
    public static boolean isCloseEnoughToHerd(double distanceToHerdMateSquared) {
        return distanceToHerdMateSquared <= HERD_TOO_CLOSE_SQUARED;
    }

    /** True for another coyote close enough to the attacker to count as hunting the same target alongside it. */
    public static boolean isPackMate(double distanceToOtherPredatorSquared) {
        return distanceToOtherPredatorSquared <= PACK_RADIUS_SQUARED;
    }

    /** A lone coyote hits at the normal rate; each nearby pack mate adds a real damage bonus, capped so a huge pack can't one-shot prey. */
    public static double packAttackMultiplier(int nearbyAllyCount) {
        return Math.min(MAX_PACK_ATTACK_MULTIPLIER, 1.0 + PACK_BONUS_PER_ALLY * nearbyAllyCount);
    }

    public static Vec3d averagePosition(List<Vec3d> positions) {
        if (positions.isEmpty()) {
            throw new IllegalArgumentException("Cannot average an empty list of positions");
        }
        double x = 0;
        double y = 0;
        double z = 0;
        for (Vec3d pos : positions) {
            x += pos.x;
            y += pos.y;
            z += pos.z;
        }
        int count = positions.size();
        return new Vec3d(x / count, y / count, z / count);
    }
}
