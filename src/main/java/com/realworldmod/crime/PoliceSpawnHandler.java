package com.realworldmod.crime;

import com.realworldmod.init.ModEntities;
import com.realworldmod.init.ModItems;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

/**
 * Right-clicking any block with a {@code POLICE_SPAWNER} in hand spawns a
 * {@link PoliceEntity} on top of it — the same item-triggered pattern
 * {@code CitizenSpawnHandler}/{@code DeerSpawnHandler} use, since the mod
 * has no biome/structure-based natural spawning yet (see ROADMAP.md).
 */
public final class PoliceSpawnHandler {
    private PoliceSpawnHandler() {
    }

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient || hand != Hand.MAIN_HAND) {
                return ActionResult.PASS;
            }
            if (!player.getStackInHand(hand).isOf(ModItems.POLICE_SPAWNER)) {
                return ActionResult.PASS;
            }

            BlockPos spawnPos = hitResult.getBlockPos().up();
            PoliceEntity police = new PoliceEntity(ModEntities.POLICE, world);
            police.refreshPositionAndAngles(
                    spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, player.getYaw(), 0.0f);
            ((ServerWorld) world).spawnEntity(police);
            return ActionResult.SUCCESS;
        });
    }
}
