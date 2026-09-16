package com.realworldmod.npc;

import com.realworldmod.init.ModBlocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

/**
 * Bounded block search for the nearest real {@code PHARMACY_COUNTER} —
 * closing "no NPC pathfinding-to-a-shop behavior yet," the gap slice 49's
 * own javadoc called out. A brute-force scan of the given radius, not a
 * spatial index: a counter outside the search box is never found, the
 * same real limitation a player walking around blind would face too.
 */
public final class PharmacyLocator {
    public static final int WALK_SEARCH_RADIUS = 16;
    public static final int WALK_SEARCH_VERTICAL_RADIUS = 4;
    public static final int ARRIVAL_RADIUS = 3;
    public static final int ARRIVAL_VERTICAL_RADIUS = 2;

    private PharmacyLocator() {
    }

    /** Nearest {@code PHARMACY_COUNTER} to {@code origin} within the given box, or empty if none is that close. */
    public static Optional<BlockPos> findNearest(World world, BlockPos origin, int horizontalRadius, int verticalRadius) {
        BlockPos.Mutable cursor = new BlockPos.Mutable();
        BlockPos nearest = null;
        double nearestDistanceSquared = Double.MAX_VALUE;

        for (int x = -horizontalRadius; x <= horizontalRadius; x++) {
            for (int z = -horizontalRadius; z <= horizontalRadius; z++) {
                for (int y = -verticalRadius; y <= verticalRadius; y++) {
                    cursor.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    if (!world.getBlockState(cursor).isOf(ModBlocks.PHARMACY_COUNTER)) {
                        continue;
                    }
                    double distanceSquared = cursor.getSquaredDistance(origin);
                    if (distanceSquared < nearestDistanceSquared) {
                        nearestDistanceSquared = distanceSquared;
                        nearest = cursor.toImmutable();
                    }
                }
            }
        }
        return Optional.ofNullable(nearest);
    }
}
