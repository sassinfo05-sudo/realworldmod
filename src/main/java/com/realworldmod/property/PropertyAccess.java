package com.realworldmod.property;

/**
 * Static holder exposing the live {@link ClaimRegistry} to code that can't
 * be constructor-injected — namely {@code BlockItemMixin}, which Mixin
 * instantiates outside our own object graph. Set once from
 * {@code RealWorldMod.onInitialize()}.
 */
public final class PropertyAccess {
    private static volatile ClaimRegistry registry;

    private PropertyAccess() {
    }

    public static void set(ClaimRegistry claimRegistry) {
        registry = claimRegistry;
    }

    /** Null until the mod has finished initializing (should never be observed in practice). */
    public static ClaimRegistry get() {
        return registry;
    }
}
