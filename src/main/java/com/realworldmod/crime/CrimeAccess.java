package com.realworldmod.crime;

/**
 * Static holder exposing the live {@link CrimeService} to
 * {@code BlockItemMixin}, which Mixin instantiates outside the mod's own
 * constructor-injected object graph — same pattern as
 * {@code property.PropertyAccess}. Set once from
 * {@code RealWorldMod.onInitialize()}.
 */
public final class CrimeAccess {
    private static volatile CrimeService crimeService;

    private CrimeAccess() {
    }

    public static void set(CrimeService service) {
        crimeService = service;
    }

    public static CrimeService get() {
        return crimeService;
    }
}
