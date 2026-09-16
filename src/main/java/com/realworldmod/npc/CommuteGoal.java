package com.realworldmod.npc;

import net.minecraft.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.Optional;

/**
 * Walks a {@link CitizenEntity} to its stored home or workplace coordinate
 * whenever its live {@link NpcProfile#currentState()} calls for it
 * ({@link CommuteTarget}), and otherwise gets out of the way so the lower
 * priority {@code WanderAroundGoal} can run during {@code LEISURE}.
 *
 * <p>Registered at higher priority than {@code WanderAroundGoal} (but lower
 * than {@code SwimGoal}) so Minecraft's own {@link Goal.Control#MOVE}
 * arbitration hands off navigation control cleanly between the two rather
 * than both fighting over {@code EntityNavigation} on the same tick.
 */
public final class CommuteGoal extends Goal {
    private static final double ARRIVAL_DISTANCE_SQUARED = 4.0;
    private static final double SPEED = 0.5;

    private final CitizenEntity citizen;
    private CommuteTarget.Destination activeDestination = CommuteTarget.Destination.NONE;

    public CommuteGoal(CitizenEntity citizen) {
        this.citizen = citizen;
        this.setControls(EnumSet.of(Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        Optional<NpcProfile> profile = currentProfile();
        if (profile.isEmpty()) {
            return false;
        }
        CommuteTarget.Destination destination = CommuteTarget.destinationFor(profile.get().currentState());
        return destination != CommuteTarget.Destination.NONE && !isAtDestination(profile.get(), destination);
    }

    @Override
    public void start() {
        currentProfile().ifPresent(profile -> {
            activeDestination = CommuteTarget.destinationFor(profile.currentState());
            moveTowards(profile, activeDestination);
        });
    }

    @Override
    public boolean shouldContinue() {
        Optional<NpcProfile> profile = currentProfile();
        if (profile.isEmpty()) {
            return false;
        }
        CommuteTarget.Destination destination = CommuteTarget.destinationFor(profile.get().currentState());
        if (destination != activeDestination) {
            return false;
        }
        return !citizen.getNavigation().isIdle() && !isAtDestination(profile.get(), destination);
    }

    @Override
    public void stop() {
        citizen.getNavigation().stop();
        activeDestination = CommuteTarget.Destination.NONE;
    }

    private void moveTowards(NpcProfile profile, CommuteTarget.Destination destination) {
        int[] target = coordinatesFor(profile, destination);
        citizen.getNavigation().startMovingTo(target[0] + 0.5, target[1], target[2] + 0.5, SPEED);
    }

    private boolean isAtDestination(NpcProfile profile, CommuteTarget.Destination destination) {
        int[] target = coordinatesFor(profile, destination);
        return citizen.squaredDistanceTo(target[0] + 0.5, target[1], target[2] + 0.5) <= ARRIVAL_DISTANCE_SQUARED;
    }

    private int[] coordinatesFor(NpcProfile profile, CommuteTarget.Destination destination) {
        return destination == CommuteTarget.Destination.HOME
                ? new int[] {profile.homeX(), profile.homeY(), profile.homeZ()}
                : new int[] {profile.workplaceX(), profile.workplaceY(), profile.workplaceZ()};
    }

    private Optional<NpcProfile> currentProfile() {
        NpcDatabase database = NpcAccess.get();
        return database == null ? Optional.empty() : database.findById(citizen.getUuid());
    }
}
