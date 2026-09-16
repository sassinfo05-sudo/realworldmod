package com.realworldmod.economy;

import com.realworldmod.init.ModBlocks;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

/** Right-clicking a {@code CASH_REGISTER} block works a shift and pays a wage, subject to a cooldown. */
public final class JobUseHandler {
    private final JobService jobService;

    public JobUseHandler(JobService jobService) {
        this.jobService = jobService;
    }

    public void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient || hand != Hand.MAIN_HAND) {
                return ActionResult.PASS;
            }
            BlockPos pos = hitResult.getBlockPos();
            if (!world.getBlockState(pos).isOf(ModBlocks.CASH_REGISTER)) {
                return ActionResult.PASS;
            }

            long currentTick = world.getTime();
            if (jobService.tryWorkShift(player.getUuid(), currentTick)) {
                player.sendMessage(Text.translatable("message.realworldmod.wage_paid",
                        CurrencyFormatter.format(JobService.NET_WAGE_CENTS)), true);
                return ActionResult.SUCCESS;
            }

            long secondsRemaining = jobService.ticksRemaining(player.getUuid(), currentTick) / 20;
            player.sendMessage(Text.translatable("message.realworldmod.wage_cooldown", secondsRemaining), true);
            return ActionResult.FAIL;
        });
    }
}
