package com.realworldmod.wildlife;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.EnumSet;

/**
 * Makes a {@link DeerEntity} run from a nearby player based on proximity
 * alone — real wildlife behavior, distinct from vanilla passive animals
 * (which only flee once actually damaged). The proximity threshold itself
 * lives in the pure, unit-tested {@link WildlifeBehavior#shouldFlee}; this
 * class only wires that decision to a live world/entity, which is why it
 * isn't itself unit tested (same split as {@code CommuteGoal}/{@code
 * CommuteTarget} in slice 21).
 */
public final class FleeFromPlayerGoal extends Goal {
    private static final double FLEE_SPEED = 0.6;
    private static final double SEARCH_RADIUS = 12.0;
    private static final double FLEE_DISTANCE = 16.0;

    private final PathAwareEntity deer;
    private PlayerEntity fleeingFrom;

    public FleeFromPlayerGoal(PathAwareEntity deer) {
        this.deer = deer;
        this.setControls(EnumSet.of(Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        PlayerEntity nearest = deer.getWorld().getClosestPlayer(deer, SEARCH_RADIUS);
        if (nearest == null || nearest.isSpectator() || !WildlifeBehavior.shouldFlee(deer.squaredDistanceTo(nearest))) {
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

    private void runAwayFrom(PlayerEntity player) {
        double dx = deer.getX() - player.getX();
        double dz = deer.getZ() - player.getZ();
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
