package com.realworldmod.wildlife;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;

import java.util.EnumSet;
import java.util.List;

/**
 * The other half of real predator AI (Section 8): a {@link DeerEntity}
 * now reacts to a nearby {@link CoyoteEntity} the same way it already
 * reacts to a nearby player ({@link FleeFromPlayerGoal}), rather than
 * only ever fleeing humans. Reuses {@link WildlifeBehavior#shouldFlee}'s
 * same distance thresholds — prey doesn't need a different trigger range
 * per predator species for this slice.
 */
public final class FleeFromPredatorGoal extends Goal {
    private static final double FLEE_SPEED = 0.7;
    private static final double SEARCH_RADIUS = 12.0;
    private static final double FLEE_DISTANCE = 16.0;

    private final PathAwareEntity deer;
    private CoyoteEntity fleeingFrom;

    public FleeFromPredatorGoal(PathAwareEntity deer) {
        this.deer = deer;
        this.setControls(EnumSet.of(Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        CoyoteEntity nearest = findNearestCoyote();
        if (nearest == null || !WildlifeBehavior.shouldFlee(deer.squaredDistanceTo(nearest))) {
            return false;
        }
        fleeingFrom = nearest;
        return true;
    }

    @Override
    public void start() {
        runAwayFrom(fleeingFrom);
    }

    @Override
    public boolean shouldContinue() {
        return fleeingFrom != null && fleeingFrom.isAlive()
                && WildlifeBehavior.shouldFlee(deer.squaredDistanceTo(fleeingFrom));
    }

    @Override
    public void tick() {
        if (deer.getNavigation().isIdle()) {
            runAwayFrom(fleeingFrom);
        }
    }

    @Override
    public void stop() {
        fleeingFrom = null;
        deer.getNavigation().stop();
    }

    private CoyoteEntity findNearestCoyote() {
        List<CoyoteEntity> candidates = deer.getWorld().getEntitiesByClass(
                CoyoteEntity.class, deer.getBoundingBox().expand(SEARCH_RADIUS), CoyoteEntity::isAlive);

        CoyoteEntity nearest = null;
        double nearestDistanceSquared = Double.MAX_VALUE;
        for (CoyoteEntity candidate : candidates) {
            double distanceSquared = deer.squaredDistanceTo(candidate);
            if (distanceSquared < nearestDistanceSquared) {
                nearestDistanceSquared = distanceSquared;
                nearest = candidate;
            }
        }
        return nearest;
    }

    private void runAwayFrom(CoyoteEntity predator) {
        double dx = deer.getX() - predator.getX();
        double dz = deer.getZ() - predator.getZ();
        double length = Math.sqrt(dx * dx + dz * dz);
        if (length < 1.0e-4) {
            dx = deer.getRandom().nextBoolean() ? 1.0 : -1.0;
            dz = deer.getRandom().nextBoolean() ? 1.0 : -1.0;
            length = Math.sqrt(dx * dx + dz * dz);
        }
        double targetX = deer.getX() + (dx / length) * FLEE_DISTANCE;
        double targetZ = deer.getZ() + (dz / length) * FLEE_DISTANCE;
        deer.getNavigation().startMovingTo(targetX, deer.getY(), targetZ, FLEE_SPEED);
    }
}
