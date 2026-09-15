package com.realworldmod.init;

import com.realworldmod.RealWorldMod;
import com.realworldmod.npc.CitizenEntity;
import com.realworldmod.vehicle.CarEntity;
import com.realworldmod.wildlife.DeerEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
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

    public static final EntityType<CitizenEntity> CITIZEN = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(RealWorldMod.MOD_ID, "citizen"),
            EntityType.Builder.<CitizenEntity>create(CitizenEntity::new, SpawnGroup.MISC)
                    .dimensions(0.6f, 1.8f)
                    .build("citizen"));

    public static final EntityType<DeerEntity> DEER = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(RealWorldMod.MOD_ID, "deer"),
            EntityType.Builder.<DeerEntity>create(DeerEntity::new, SpawnGroup.CREATURE)
                    .dimensions(0.7f, 1.3f)
                    .build("deer"));

    private ModEntities() {
    }

    public static void register() {
        FabricDefaultAttributeRegistry.register(CITIZEN, CitizenEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(DEER, DeerEntity.createAttributes());
    }
}
