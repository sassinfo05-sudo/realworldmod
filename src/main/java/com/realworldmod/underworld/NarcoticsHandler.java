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
 * Right-clicking a {@code NARCOTICS_LAB} with an empty hand cooks one
 * unit into the player's stash (cooldown-gated); sneak-right-clicking
 * deals one unit for real money, with a real chance of being caught and
 * recorded through the same {@link LawEnforcementService} pipeline every
 * other crime in the mod uses — dealing enough raises the player's
 * wanted level just like any other offense, which means the
 * {@code crime.PoliceEntity} from slice 23 will come looking for a
 * repeat dealer the same way it would for anyone else. Section 7's
 * underworld/narcotics gap, reduced to its smallest real shape: an
 * illegal, higher-paying alternative to a legal job, with actual risk
 * attached rather than none.
 */
public final class NarcoticsHandler {
    public static final int DEALING_SEVERITY = 2;
    public static final double CATCH_CHANCE = 0.3;

    private final NarcoticsService narcoticsService;
    private final LawEnforcementService lawEnforcementService;
    private final Random random = new Random();

    public NarcoticsHandler(NarcoticsService narcoticsService, LawEnforcementService lawEnforcementService) {
        this.narcoticsService = narcoticsService;
        this.lawEnforcementService = lawEnforcementService;
    }

    public void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient || hand != Hand.MAIN_HAND) {
                return ActionResult.PASS;
            }
            if (!world.getBlockState(hitResult.getBlockPos()).isOf(ModBlocks.NARCOTICS_LAB)) {
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
        if (!narcoticsService.tryCook(player.getUuid(), currentTick)) {
            long secondsRemaining = narcoticsService.cookTicksRemaining(player.getUuid(), currentTick) / 20;
            player.sendMessage(Text.translatable("message.realworldmod.narcotics_cook_cooldown", secondsRemaining), true);
            return ActionResult.FAIL;
        }
        player.sendMessage(Text.translatable("message.realworldmod.narcotics_cooked",
                narcoticsService.stashCount(player.getUuid())), true);
        return ActionResult.SUCCESS;
    }

    private ActionResult handleDeal(PlayerEntity player, long currentTick) {
        if (!narcoticsService.tryDeal(player.getUuid(), currentTick)) {
            player.sendMessage(Text.translatable("message.realworldmod.narcotics_nothing_to_deal"), true);
            return ActionResult.FAIL;
        }
        player.sendMessage(Text.translatable("message.realworldmod.narcotics_dealt",
                CurrencyFormatter.format(NarcoticsService.DEAL_PAYOUT_CENTS)), true);

        if (random.nextDouble() < CATCH_CHANCE) {
            player.sendMessage(Text.translatable("message.realworldmod.narcotics_caught"), true);
            OffenseOutcome outcome = lawEnforcementService.recordOffense(player.getUuid(), DEALING_SEVERITY);
            if (outcome.fined()) {
                player.sendMessage(Text.translatable("message.realworldmod.fine_issued",
                        CurrencyFormatter.format(LawEnforcementService.FINE_CENTS)), true);
            }
        }
        return ActionResult.SUCCESS;
    }
}
