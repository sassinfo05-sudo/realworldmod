package com.realworldmod.underworld;

import com.realworldmod.crime.LawEnforcementService;
import com.realworldmod.crime.OffenseOutcome;
import com.realworldmod.economy.CurrencyFormatter;
import com.realworldmod.init.ModBlocks;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

import java.util.Random;

/**
 * A second, distinct lab loop for a {@code METH_LAB} block, closing part
 * of "the underworld/narcotics system built in slice 37 is a single
 * cook/deal loop at one block type." Same right-click-cooks,
 * sneak-right-click-deals shape as {@link NarcoticsHandler}, tracked
 * through its own independent {@link MethService}/{@link MethCatchChance}/
 * {@link MethSeverity} — a higher payout, a longer cook cycle, and a
 * steeper catch chance and severity ceiling than the original narcotics
 * loop, so the two lab types feel like genuinely different drugs rather
 * than a palette swap.
 */
public final class MethHandler {
    private final MethService methService;
    private final LawEnforcementService lawEnforcementService;
    private final Random random = new Random();

    public MethHandler(MethService methService, LawEnforcementService lawEnforcementService) {
        this.methService = methService;
        this.lawEnforcementService = lawEnforcementService;
    }

    public void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient || hand != Hand.MAIN_HAND) {
                return ActionResult.PASS;
            }
            if (!world.getBlockState(hitResult.getBlockPos()).isOf(ModBlocks.METH_LAB)) {
                return ActionResult.PASS;
            }
            if (!player.getStackInHand(hand).isEmpty()) {
                return ActionResult.PASS;
            }

            long currentTick = world.getTime();
            return player.isSneaking() ? handleDeal(player, currentTick) : handleCook(player, currentTick);
        });
    }

    private ActionResult handleCook(PlayerEntity player, long currentTick) {
        if (!methService.tryCook(player.getUuid(), currentTick)) {
            long secondsRemaining = methService.cookTicksRemaining(player.getUuid(), currentTick) / 20;
            player.sendMessage(Text.translatable("message.realworldmod.meth_cook_cooldown", secondsRemaining), true);
            return ActionResult.FAIL;
        }
        player.sendMessage(Text.translatable("message.realworldmod.meth_cooked",
                methService.stashCount(player.getUuid())), true);
        return ActionResult.SUCCESS;
    }

    private ActionResult handleDeal(PlayerEntity player, long currentTick) {
        if (!methService.tryDeal(player.getUuid(), currentTick)) {
            player.sendMessage(Text.translatable("message.realworldmod.meth_nothing_to_deal"), true);
            return ActionResult.FAIL;
        }
        player.sendMessage(Text.translatable("message.realworldmod.meth_dealt",
                CurrencyFormatter.format(MethService.DEAL_PAYOUT_CENTS)), true);

        int wantedLevel = lawEnforcementService.crimeService().getWantedLevel(player.getUuid());
        double catchChance = MethCatchChance.forWantedLevel(wantedLevel);
        if (random.nextDouble() < catchChance) {
            player.sendMessage(Text.translatable("message.realworldmod.meth_caught"), true);
            int severity = MethSeverity.forStreak(methService.getDealStreak(player.getUuid()));
            OffenseOutcome outcome = lawEnforcementService.recordOffense(player.getUuid(), severity);
            if (outcome.fined()) {
                player.sendMessage(Text.translatable("message.realworldmod.fine_issued",
                        CurrencyFormatter.format(LawEnforcementService.FINE_CENTS)), true);
            }
        }
        return ActionResult.SUCCESS;
    }
}
