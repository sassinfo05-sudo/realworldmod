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

    private WildlifeBehavior() {
    }

    /** Real wildlife flees on proximity alone, unlike vanilla passive animals which only flee once hit. */
    public static boolean shouldFlee(double distanceToPlayerSquared) {
        return distanceToPlayerSquared <= FLEE_TRIGGER_DISTANCE_SQUARED;
    }

    /** True once a herd-mate is close enough that closing further would just cause crowding. */
    public static boolean isCloseEnoughToHerd(double distanceToHerdMateSquared) {
        return distanceToHerdMateSquared <= HERD_TOO_CLOSE_SQUARED;
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
