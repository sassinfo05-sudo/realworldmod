package com.realworldmod.utilities;

/**
 * Static holder exposing the live {@link WaterService} to
 * {@code WaterOutletBlock}, whose instance is created at
 * class-registration time rather than through the mod's own
 * constructor-injected object graph — same pattern as
 * {@link UtilityAccess}. Set once from {@code RealWorldMod.onInitialize()}.
 */
public final class WaterAccess {
    private static volatile WaterService waterService;

    private WaterAccess() {
    }

    public static void set(WaterService service) {
        waterService = service;
    }

    public static WaterService get() {
        return waterService;
    }
}
