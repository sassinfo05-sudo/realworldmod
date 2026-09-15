package com.realworldmod.wildlife;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;
import java.util.List;

/**
 * Drifts a {@link DeerEntity} toward the average position of nearby deer —
 * real prey-herding behavior (Section 8's "prey herding/migration" gap),
 * distinct from the solo wandering every other passive-mob-style entity in
 * the mod uses. Lower priority than {@link FleeFromPlayerGoal} so a scare
 * always breaks up the herd's drift rather than fighting it for navigation.
 */
public final class HerdWithOthersGoal extends Goal {
    private static final double HERD_SPEED = 0.35;
    private static final double SEARCH_RADIUS = 16.0;

    private final PathAwareEntity deer;

    public HerdWithOthersGoal(PathAwareEntity deer) {
        this.deer = deer;
        this.setControls(EnumSet.of(Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        return !nearbyHerdMatePositions().isEmpty() && deer.getNavigation().isIdle();
    }

    @Override
    public void start() {
        List<Vec3d> mates = nearbyHerdMatePositions();
        if (mates.isEmpty()) {
            return;
        }
        Vec3d center = WildlifeBehavior.averagePosition(mates);
        if (WildlifeBehavior.isCloseEnoughToHerd(deer.squaredDistanceTo(center.x, center.y, center.z))) {
            return;
        }
        deer.getNavigation().startMovingTo(center.x, center.y, center.z, HERD_SPEED);
    }

    @Override
    public boolean shouldContinue() {
        return !deer.getNavigation().isIdle();
    }

    private List<Vec3d> nearbyHerdMatePositions() {
        return deer.getWorld()
                .getEntitiesByClass(DeerEntity.class, deer.getBoundingBox().expand(SEARCH_RADIUS),
                        other -> other != deer)
                .stream()
                .map(DeerEntity::getPos)
                .toList();
    }
}
