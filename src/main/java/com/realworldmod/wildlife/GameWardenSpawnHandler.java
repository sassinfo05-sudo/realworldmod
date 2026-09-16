package com.realworldmod.wildlife;

import com.realworldmod.init.ModEntities;
import com.realworldmod.init.ModItems;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

/**
 * Right-clicking any block with a {@code GAME_WARDEN_SPAWNER} in hand
 * spawns a {@link GameWardenEntity} on top of it — the same item-triggered
 * pattern every other entity in the mod uses, since there's no
 * biome/structure-based natural spawning yet (see ROADMAP.md).
 */
public final class GameWardenSpawnHandler {
    private GameWardenSpawnHandler() {
    }

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient || hand != Hand.MAIN_HAND) {
                return ActionResult.PASS;
            }
            if (!player.getStackInHand(hand).isOf(ModItems.GAME_WARDEN_SPAWNER)) {
                return ActionResult.PASS;
            }

            BlockPos spawnPos = hitResult.getBlockPos().up();
            GameWardenEntity warden = new GameWardenEntity(ModEntities.GAME_WARDEN, world);
            warden.refreshPositionAndAngles(
                    spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, player.getYaw(), 0.0f);
            ((ServerWorld) world).spawnEntity(warden);
            return ActionResult.SUCCESS;
        });
    }
}
