package com.realworldmod.wildlife;

/**
 * Static holder exposing the live {@link GameWardenService} to
 * {@link ChasePoacherGoal}, which {@code EntityType.Builder} instantiates
 * outside the mod's own constructor-injected object graph — same pattern
 * as {@code crime.TrialAccess}/{@code crime.ArrestAccess}. Set once from
 * {@code RealWorldMod.onInitialize()}.
 */
public final class PoachingAccess {
    private static volatile GameWardenService gameWardenService;

    private PoachingAccess() {
    }

    public static void set(GameWardenService service) {
        gameWardenService = service;
    }

    public static GameWardenService get() {
        return gameWardenService;
    }
}
