package com.realworldmod.npc;

import com.realworldmod.npc.goap.DailyState;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;

import java.util.EnumSet;
import java.util.Optional;

/**
 * Closes a real, if small, crack in "no job-task mini-behaviors
 * (cashiering, patrols, factory work)": before this slice, once
 * {@link CommuteGoal} finished walking a {@link CitizenEntity} to its
 * workplace during {@link DailyState#WORKING}, nothing kept it there —
 * the lower-priority {@code WanderAroundGoal} would immediately take over
 * and wander it away from its own job site during working hours. This
 * goal claims {@link Goal.Control#MOVE}/{@link Goal.Control#LOOK} for the
 * whole {@code WORKING} state (registered above {@code WanderAroundGoal}
 * but below {@link CommuteGoal}, so commuting there still takes priority
 * over staying put), keeping the citizen anchored at its post and
 * periodically facing its {@code CASH_REGISTER} — a real "at work"
 * behavior, not a full cashiering animation.
 */
public final class WorkTaskGoal extends Goal {
    private static final int LOOK_INTERVAL_TICKS = 100;

    private final PathAwareEntity citizen;
    private int lookCooldown;

    public WorkTaskGoal(PathAwareEntity citizen) {
        this.citizen = citizen;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        return isWorking();
    }

    @Override
    public boolean shouldContinue() {
        return isWorking();
    }

    @Override
    public void start() {
        lookCooldown = 0;
    }

    @Override
    public void tick() {
        if (lookCooldown > 0) {
            lookCooldown--;
            return;
        }
        currentProfile().ifPresent(profile -> citizen.getLookControl().lookAt(
                profile.workplaceX() + 0.5, profile.workplaceY() + 1.5, profile.workplaceZ() + 0.5));
        lookCooldown = LOOK_INTERVAL_TICKS;
    }

    private boolean isWorking() {
        return currentProfile().map(profile -> profile.currentState() == DailyState.WORKING).orElse(false);
    }

    private Optional<NpcProfile> currentProfile() {
        NpcDatabase database = NpcAccess.get();
        return database == null ? Optional.empty() : database.findById(citizen.getUuid());
    }
}
