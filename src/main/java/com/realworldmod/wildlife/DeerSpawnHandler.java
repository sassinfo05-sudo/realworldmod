package com.realworldmod.wildlife;

import com.realworldmod.init.ModEntities;
import com.realworldmod.init.ModItems;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

/**
 * Right-clicking any block with a {@code DEER_SPAWNER} in hand spawns a
 * {@link DeerEntity} on top of it — the same item-triggered pattern
 * {@code CitizenSpawnHandler} uses, since the mod has no biome-based
 * natural wildlife spawning yet (see ROADMAP.md). As of slice 63, each
 * spawn is also recorded in {@link WildlifePopulationService}, the other
 * half of {@link DeerDropHandler}'s population-count consequence for a
 * kill.
 */
public final class DeerSpawnHandler {
    private DeerSpawnHandler() {
    }

    public static void register(WildlifePopulationService wildlifePopulationService) {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient || hand != Hand.MAIN_HAND) {
                return ActionResult.PASS;
            }
            if (!player.getStackInHand(hand).isOf(ModItems.DEER_SPAWNER)) {
                return ActionResult.PASS;
            }

            BlockPos spawnPos = hitResult.getBlockPos().up();
            DeerEntity deer = new DeerEntity(ModEntities.DEER, world);
            deer.refreshPositionAndAngles(
                    spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, player.getYaw(), 0.0f);
            ((ServerWorld) world).spawnEntity(deer);
            wildlifePopulationService.recordDeerSpawn();
            return ActionResult.SUCCESS;
        });
    }
}
