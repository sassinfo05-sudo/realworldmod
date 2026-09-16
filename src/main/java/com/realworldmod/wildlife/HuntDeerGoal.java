package com.realworldmod.wildlife;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;

import java.util.EnumSet;
import java.util.List;

/**
 * Real predator AI (Section 8: "no predator AI at all — only prey
 * flee/herd behavior exists, nothing stalks or hunts anything"). Makes a
 * {@link CoyoteEntity} find the nearest live {@link DeerEntity} within
 * range, chase it, and deal real damage on contact — a hunt that can
 * actually kill the deer, not a cosmetic chase. The proximity thresholds
 * live in the pure, unit-tested {@link WildlifeBehavior#shouldHunt}/
 * {@link WildlifeBehavior#canAttack}; this class only wires that decision
 * to a live world/entity, the same split every other {@code Goal} in this
 * package uses.
 */
public final class HuntDeerGoal extends Goal {
    private static final double CHASE_SPEED = 0.35;
    private static final double SEARCH_RADIUS = 16.0;
    private static final float ATTACK_DAMAGE = 3.0f;
    private static final int ATTACK_COOLDOWN_TICKS = 20;

    private final PathAwareEntity coyote;
    private DeerEntity target;
    private int attackCooldown;

    public HuntDeerGoal(PathAwareEntity coyote) {
        this.coyote = coyote;
        this.setControls(EnumSet.of(Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        DeerEntity nearest = findNearestDeer();
        if (nearest == null || !WildlifeBehavior.shouldHunt(coyote.squaredDistanceTo(nearest))) {
            return false;
        }
        target = nearest;
        return true;
    }

    @Override
    public void start() {
        attackCooldown = 0;
        coyote.getNavigation().startMovingTo(target, CHASE_SPEED);
    }

    @Override
    public boolean shouldContinue() {
        return target != null && target.isAlive() && WildlifeBehavior.shouldHunt(coyote.squaredDistanceTo(target));
    }

    @Override
    public void tick() {
        if (coyote.getNavigation().isIdle()) {
            coyote.getNavigation().startMovingTo(target, CHASE_SPEED);
        }
        if (attackCooldown > 0) {
            attackCooldown--;
        }
        tryAttack();
    }

    @Override
    public void stop() {
        target = null;
        coyote.getNavigation().stop();
    }

    private void tryAttack() {
        if (attackCooldown > 0 || !WildlifeBehavior.canAttack(coyote.squaredDistanceTo(target))) {
            return;
        }
        target.damage(coyote.getWorld().getDamageSources().mobAttack(coyote), ATTACK_DAMAGE);
        attackCooldown = ATTACK_COOLDOWN_TICKS;
    }

    private DeerEntity findNearestDeer() {
        List<DeerEntity> candidates = coyote.getWorld().getEntitiesByClass(
                DeerEntity.class, coyote.getBoundingBox().expand(SEARCH_RADIUS), DeerEntity::isAlive);

        DeerEntity nearest = null;
        double nearestDistanceSquared = Double.MAX_VALUE;
        for (DeerEntity candidate : candidates) {
            double distanceSquared = coyote.squaredDistanceTo(candidate);
            if (distanceSquared < nearestDistanceSquared) {
                nearestDistanceSquared = distanceSquared;
                nearest = candidate;
            }
        }
        return nearest;
    }
}
