package com.realworldmod.npc;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

/**
 * Makes a sick or injured {@link CitizenEntity} actually walk to the
 * nearest real {@code PHARMACY_COUNTER} before it can be treated, closing
 * "no NPC pathfinding-to-a-shop behavior yet" — the gap
 * {@link CitizenSelfMedicationHandler}'s own javadoc has called out since
 * slice 49. Registered above {@link CommuteGoal} in {@link CitizenEntity}
 * so a sick citizen diverts to seek treatment instead of finishing its
 * normal commute first, the same "more urgent goal pre-empts the routine
 * one" priority ordering {@code FleeFromPredatorGoal} already uses over
 * herding.
 */
public final class WalkToPharmacyGoal extends Goal {
    private static final List<RegistryEntry<StatusEffect>> CURABLE_EFFECTS =
            List.of(StatusEffects.NAUSEA, StatusEffects.WEAKNESS, StatusEffects.SLOWNESS);
    private static final double SPEED = 0.5;
    private static final double ARRIVAL_DISTANCE_SQUARED = 4.0;

    private final PathAwareEntity citizen;
    private BlockPos target;

    public WalkToPharmacyGoal(PathAwareEntity citizen) {
        this.citizen = citizen;
        this.setControls(EnumSet.of(Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        if (!isSick()) {
            return false;
        }
        Optional<BlockPos> nearest = PharmacyLocator.findNearest(citizen.getWorld(), citizen.getBlockPos(),
                PharmacyLocator.WALK_SEARCH_RADIUS, PharmacyLocator.WALK_SEARCH_VERTICAL_RADIUS);
        if (nearest.isEmpty() || isAtTarget(nearest.get())) {
            return false;
        }
        target = nearest.get();
        return true;
    }

    @Override
    public void start() {
        moveTowardsTarget();
    }

    @Override
    public boolean shouldContinue() {
        return isSick() && target != null && !isAtTarget(target);
    }

    @Override
    public void tick() {
        if (citizen.getNavigation().isIdle()) {
            moveTowardsTarget();
        }
    }

    @Override
    public void stop() {
        citizen.getNavigation().stop();
        target = null;
    }

    private void moveTowardsTarget() {
        citizen.getNavigation().startMovingTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, SPEED);
    }

    private boolean isSick() {
        return CURABLE_EFFECTS.stream().anyMatch(citizen::hasStatusEffect);
    }

    private boolean isAtTarget(BlockPos pos) {
        return citizen.squaredDistanceTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5) <= ARRIVAL_DISTANCE_SQUARED;
    }
}
