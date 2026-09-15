package com.realworldmod.vehicle;

import com.realworldmod.init.ModEntities;
import com.realworldmod.init.ModItems;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

/** Right-clicking any block with a {@code CAR_KEY} in hand spawns a {@link CarEntity} on top of it. */
public final class CarSpawnHandler {
    private CarSpawnHandler() {
    }

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient || hand != Hand.MAIN_HAND) {
                return ActionResult.PASS;
            }
            if (!player.getStackInHand(hand).isOf(ModItems.CAR_KEY)) {
                return ActionResult.PASS;
            }

            BlockPos spawnPos = hitResult.getBlockPos().up();
            CarEntity car = new CarEntity(ModEntities.CAR, world);
            car.refreshPositionAndAngles(
                    spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, player.getYaw(), 0.0f);
            ((ServerWorld) world).spawnEntity(car);
            return ActionResult.SUCCESS;
        });
    }
}
