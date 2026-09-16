package com.realworldmod.crime;

/**
 * Static holder exposing the live {@link LawEnforcementService} to
 * {@code BlockItemMixin}, which Mixin instantiates outside the mod's own
 * constructor-injected object graph — same pattern as
 * {@code property.PropertyAccess}. Set once from
 * {@code RealWorldMod.onInitialize()}.
 */
public final class CrimeAccess {
    private static volatile LawEnforcementService lawEnforcementService;

    private CrimeAccess() {
    }

    public static void set(LawEnforcementService service) {
        lawEnforcementService = service;
    }

    public static LawEnforcementService get() {
        return lawEnforcementService;
    }
}
