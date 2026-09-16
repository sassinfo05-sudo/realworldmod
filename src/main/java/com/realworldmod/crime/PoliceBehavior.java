package com.realworldmod.crime;

/**
 * Pure decision logic behind {@link ChaseWantedPlayerGoal}, kept separate
 * from the {@code Goal} subclass itself so it's unit-testable without a
 * running Minecraft world — the same split {@code CommuteTarget}/
 * {@code CommuteGoal} and {@code WildlifeBehavior}/{@code FleeFromPlayerGoal}
 * used for their own goals.
 */
public final class PoliceBehavior {
    /** Police start pursuing once a player is at least "Wanted" (see WantedLevelDescriptions), not merely fined. */
    public static final int CHASE_TRIGGER_WANTED_LEVEL = LawEnforcementService.FINE_THRESHOLD;
    public static final double APPREHEND_DISTANCE_SQUARED = 2.5 * 2.5;

    private PoliceBehavior() {
    }

    public static boolean shouldChase(int wantedLevel) {
        return wantedLevel >= CHASE_TRIGGER_WANTED_LEVEL;
    }

    /**
     * Apprehension only actually sticks if the player is still at maximum
     * heat on contact — being spotted while merely "Wanted" and then
     * losing the chase (or lying low long enough for the level to decay)
     * avoids it even after a police entity closes the distance.
     */
    public static boolean canApprehend(int wantedLevel, double distanceToPlayerSquared) {
        return wantedLevel >= WantedLevelMath.MAX && distanceToPlayerSquared <= APPREHEND_DISTANCE_SQUARED;
    }
}
