package com.realworldmod.utilities;

/**
 * Static holder exposing the live {@link UtilityService} to
 * {@code UtilityLampBlock}, whose instance is created at class-registration
 * time (a static field initializer) rather than through the mod's own
 * constructor-injected object graph — same pattern as
 * {@code property.PropertyAccess}. Set once from
 * {@code RealWorldMod.onInitialize()}.
 */
public final class UtilityAccess {
    private static volatile UtilityService utilityService;

    private UtilityAccess() {
    }

    public static void set(UtilityService service) {
        utilityService = service;
    }

    public static UtilityService get() {
        return utilityService;
    }
}
