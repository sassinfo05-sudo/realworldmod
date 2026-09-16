package com.realworldmod.wildlife;

import com.realworldmod.init.ModEntities;
import com.realworldmod.init.ModItems;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

/**
 * Right-clicking any block with a {@code COYOTE_SPAWNER} in hand spawns a
 * {@link CoyoteEntity} on top of it — the same item-triggered pattern
 * {@code DeerSpawnHandler}/{@code CitizenSpawnHandler} use, since the mod
 * has no biome-based natural wildlife spawning yet (see ROADMAP.md).
 */
public final class CoyoteSpawnHandler {
    private CoyoteSpawnHandler() {
    }

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient || hand != Hand.MAIN_HAND) {
                return ActionResult.PASS;
            }
            if (!player.getStackInHand(hand).isOf(ModItems.COYOTE_SPAWNER)) {
                return ActionResult.PASS;
            }

            BlockPos spawnPos = hitResult.getBlockPos().up();
            CoyoteEntity coyote = new CoyoteEntity(ModEntities.COYOTE, world);
            coyote.refreshPositionAndAngles(
                    spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, player.getYaw(), 0.0f);
            ((ServerWorld) world).spawnEntity(coyote);
            return ActionResult.SUCCESS;
        });
    }
}
