package com.realworldmod.vice;

import com.realworldmod.init.ModItems;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;

/**
 * Using a {@code RECOVERY_DRINK} (crafted from Medicine and milk, see the
 * {@code recovery_drink} recipe) cancels an impending hangover via
 * {@link IntoxicationService#useRecoveryDrink} — the same "prevent it
 * before it starts" shape {@link NicotinePatchUseHandler} already
 * established for nicotine withdrawal, closing the rest of slice 68's
 * "no equivalent item exists to ease an alcohol hangover" gap. Only
 * consumed if there was actually a hangover to cancel: drinking one
 * while sober, or before ever peaking, does nothing.
 */
public final class RecoveryDrinkUseHandler {
    private final IntoxicationService intoxicationService;

    public RecoveryDrinkUseHandler(IntoxicationService intoxicationService) {
        this.intoxicationService = intoxicationService;
    }

    public void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            if (world.isClient || hand != Hand.MAIN_HAND || !stack.isOf(ModItems.RECOVERY_DRINK)) {
                return TypedActionResult.pass(stack);
            }

            if (!intoxicationService.useRecoveryDrink(player.getUuid())) {
                player.sendMessage(Text.translatable("message.realworldmod.recovery_drink_not_needed"), true);
                return TypedActionResult.fail(stack);
            }

            stack.decrement(1);
            player.sendMessage(Text.translatable("message.realworldmod.recovery_drink_used"), true);
            return TypedActionResult.success(stack);
        });
    }
}
