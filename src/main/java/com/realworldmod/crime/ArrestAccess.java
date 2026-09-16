package com.realworldmod.crime;

/**
 * Static holder exposing the live {@link ArrestService} to
 * {@link ChaseWantedPlayerGoal}, so it can avoid re-apprehending a player
 * who is already detained or already on trial — same pattern as
 * {@link TrialAccess}. Set once from {@code RealWorldMod.onInitialize()}.
 */
public final class ArrestAccess {
    private static volatile ArrestService arrestService;

    private ArrestAccess() {
    }

    public static void set(ArrestService service) {
        arrestService = service;
    }

    public static ArrestService get() {
        return arrestService;
    }
}
