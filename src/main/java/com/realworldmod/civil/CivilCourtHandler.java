package com.realworldmod.civil;

import com.realworldmod.economy.CurrencyFormatter;
import com.realworldmod.init.ModBlocks;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

/**
 * Right-clicking a {@code COURTHOUSE}: if the player is currently a
 * defendant in a pending civil case, this always contests (dismisses) it
 * first — an existing case against you takes priority over filing a new
 * one. Otherwise it files a new claim against the nearest other player.
 * No text-entry UI is needed since "nearest player" stands in for
 * naming a defendant, the same aim-based targeting
 * {@code ChaseWantedPlayerGoal}/{@code ChasePoacherGoal} use for their own
 * nearest-player lookups.
 */
public final class CivilCourtHandler {
    private static final double SEARCH_RADIUS = 8.0;

    private final CivilCourtService civilCourtService;

    public CivilCourtHandler(CivilCourtService civilCourtService) {
        this.civilCourtService = civilCourtService;
    }

    public void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient || hand != Hand.MAIN_HAND) {
                return ActionResult.PASS;
            }
            if (!world.getBlockState(hitResult.getBlockPos()).isOf(ModBlocks.COURTHOUSE)) {
                return ActionResult.PASS;
            }
            ServerPlayerEntity plaintiff = (ServerPlayerEntity) player;

            if (civilCourtService.contest(plaintiff.getUuid(), world.getTime())) {
                plaintiff.sendMessage(Text.translatable("message.realworldmod.civil_case_contested"), true);
                return ActionResult.SUCCESS;
            }

            PlayerEntity nearest = world.getClosestPlayer(plaintiff, SEARCH_RADIUS);
            if (nearest == null || nearest.getUuid().equals(plaintiff.getUuid())) {
                plaintiff.sendMessage(Text.translatable("message.realworldmod.civil_no_defendant_nearby"), true);
                return ActionResult.FAIL;
            }

            if (!civilCourtService.fileClaim(plaintiff.getUuid(), nearest.getUuid(), world.getTime())) {
                plaintiff.sendMessage(Text.translatable("message.realworldmod.civil_case_already_pending"), true);
                return ActionResult.FAIL;
            }

            String amount = CurrencyFormatter.format(CivilCourtService.CLAIM_AMOUNT_CENTS);
            plaintiff.sendMessage(Text.translatable("message.realworldmod.civil_case_filed",
                    nearest.getName().getString(), amount), true);
            if (nearest instanceof ServerPlayerEntity defendant) {
                defendant.sendMessage(Text.translatable("message.realworldmod.civil_case_notice",
                        plaintiff.getName().getString(), amount), true);
            }
            return ActionResult.SUCCESS;
        });
    }
}
