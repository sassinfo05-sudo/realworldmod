package com.realworldmod.vice;

import com.realworldmod.init.ModItems;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;

/**
 * Using a {@code NICOTINE_PATCH} (bought at a {@code PHARMACY_COUNTER}, see
 * {@code medical.PharmacyUseHandler}) delays the next possible nicotine
 * withdrawal via {@link NicotineService#usePatch} and resets the
 * escalation streak {@link WithdrawalSeverity} scales off — closing "no
 * way to ease withdrawal short of smoking again" from slice 62/67, without
 * the illness risk or buzz an actual cigarette carries.
 */
public final class NicotinePatchUseHandler {
    private final NicotineService nicotineService;

    public NicotinePatchUseHandler(NicotineService nicotineService) {
        this.nicotineService = nicotineService;
    }

    public void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            if (world.isClient || hand != Hand.MAIN_HAND || !stack.isOf(ModItems.NICOTINE_PATCH)) {
                return TypedActionResult.pass(stack);
            }

            nicotineService.usePatch(player.getUuid(), world.getTime());
            stack.decrement(1);
            player.sendMessage(Text.translatable("message.realworldmod.nicotine_patch_used"), true);
            return TypedActionResult.success(stack);
        });
    }
}
