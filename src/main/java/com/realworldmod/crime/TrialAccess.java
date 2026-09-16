package com.realworldmod.crime;

/**
 * Static holder exposing the live {@link TrialService} to
 * {@link ChaseWantedPlayerGoal}, which {@code EntityType.Builder}
 * instantiates outside the mod's own constructor-injected object graph —
 * same pattern as {@code npc.NpcAccess}. Set once from
 * {@code RealWorldMod.onInitialize()}.
 */
public final class TrialAccess {
    private static volatile TrialService trialService;

    private TrialAccess() {
    }

    public static void set(TrialService service) {
        trialService = service;
    }

    public static TrialService get() {
        return trialService;
    }
}
