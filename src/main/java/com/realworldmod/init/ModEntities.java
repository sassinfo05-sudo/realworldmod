package com.realworldmod.init;

import com.realworldmod.RealWorldMod;
import com.realworldmod.vehicle.CarEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModEntities {
    public static final EntityType<CarEntity> CAR = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(RealWorldMod.MOD_ID, "car"),
            EntityType.Builder.<CarEntity>create(CarEntity::new, SpawnGroup.MISC)
                    .dimensions(1.4f, 1.0f)
                    .build("car"));

    private ModEntities() {
    }

    public static void register() {
        // Classloading this class runs the static initializer above.
    }
}
