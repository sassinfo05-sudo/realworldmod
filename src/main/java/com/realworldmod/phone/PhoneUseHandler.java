package com.realworldmod.phone;

import com.realworldmod.init.ModDataComponents;
import com.realworldmod.init.ModItems;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;

/**
 * Common (server + client) handling for using the smartphone item: draining
 * battery and refusing use once dead. Opening the actual phone UI is a
 * client-only concern registered separately in {@code RealWorldModClient},
 * since {@code Screen} classes are not present on a dedicated server.
 */
public final class PhoneUseHandler {
    private PhoneUseHandler() {
    }

    public static void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (hand != Hand.MAIN_HAND) {
                return TypedActionResult.pass(player.getStackInHand(hand));
            }
            var stack = player.getStackInHand(hand);
            if (!stack.isOf(ModItems.SMARTPHONE)) {
                return TypedActionResult.pass(stack);
            }

            int battery = stack.getOrDefault(ModDataComponents.PHONE_BATTERY, PhoneBattery.MAX_LEVEL);
            if (PhoneBattery.isDead(battery)) {
                if (world.isClient) {
                    player.sendMessage(Text.translatable("message.realworldmod.phone_dead"), true);
                }
                return TypedActionResult.fail(stack);
            }

            if (!world.isClient) {
                stack.set(ModDataComponents.PHONE_BATTERY, PhoneBattery.drain(battery, PhoneBattery.DRAIN_PER_USE));
            }
            return new TypedActionResult<>(ActionResult.SUCCESS, stack);
        });
    }
}
