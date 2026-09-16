package com.realworldmod.init;

import com.realworldmod.RealWorldMod;
import com.realworldmod.crime.PoliceEntity;
import com.realworldmod.npc.CitizenEntity;
import com.realworldmod.vehicle.CarEntity;
import com.realworldmod.wildlife.CoyoteEntity;
import com.realworldmod.wildlife.DeerEntity;
import com.realworldmod.wildlife.GameWardenEntity;
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

    public static final EntityType<PoliceEntity> POLICE = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(RealWorldMod.MOD_ID, "police"),
            EntityType.Builder.<PoliceEntity>create(PoliceEntity::new, SpawnGroup.MISC)
                    .dimensions(0.6f, 1.8f)
                    .build("police"));

    public static final EntityType<GameWardenEntity> GAME_WARDEN = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(RealWorldMod.MOD_ID, "game_warden"),
            EntityType.Builder.<GameWardenEntity>create(GameWardenEntity::new, SpawnGroup.MISC)
                    .dimensions(0.6f, 1.8f)
                    .build("game_warden"));

    public static final EntityType<CoyoteEntity> COYOTE = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(RealWorldMod.MOD_ID, "coyote"),
            EntityType.Builder.<CoyoteEntity>create(CoyoteEntity::new, SpawnGroup.CREATURE)
                    .dimensions(0.6f, 0.85f)
                    .build("coyote"));

    private ModEntities() {
    }

    public static void register() {
        FabricDefaultAttributeRegistry.register(CITIZEN, CitizenEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(DEER, DeerEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(POLICE, PoliceEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(GAME_WARDEN, GameWardenEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(COYOTE, CoyoteEntity.createAttributes());
    }
}
