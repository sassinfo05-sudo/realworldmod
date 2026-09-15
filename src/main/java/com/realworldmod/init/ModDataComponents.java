package com.realworldmod.init;

import com.mojang.serialization.Codec;
import com.realworldmod.RealWorldMod;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/** Custom persistent item-stack data (Minecraft 1.20.5+ data component API). */
public final class ModDataComponents {
    /** Battery percentage (0-100) persisted on a smartphone item stack. */
    public static final ComponentType<Integer> PHONE_BATTERY = register("phone_battery",
            ComponentType.<Integer>builder()
                    .codec(Codec.intRange(0, 100))
                    .packetCodec(PacketCodecs.VAR_INT)
                    .build());

    private ModDataComponents() {
    }

    private static <T> ComponentType<T> register(String path, ComponentType<T> type) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(RealWorldMod.MOD_ID, path), type);
    }

    public static void register() {
        // Classloading this class runs the static initializers above.
    }
}
