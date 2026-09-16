package com.realworldmod.vice;

import com.realworldmod.init.ModItems;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;

/**
 * Smoking a {@code CIGARETTE} gives a brief nicotine buzz (Speed) every
 * time, but every {@link NicotineRisk#CIGARETTES_TO_ILLNESS}th one in a
 * row triggers a real, lasting illness (Nausea + Weakness) — the same
 * consequence {@code medical.WeatherIllnessEffect} already applies for
 * rain exposure — so smoking has a genuine long-term health cost rather
 * than being a purely cosmetic item.
 */
public final class CigaretteUseHandler {
    private static final int BUZZ_DURATION_TICKS = 20 * 15;
    private static final int ILLNESS_DURATION_TICKS = 20 * 60 * 2;

    private final NicotineService nicotineService;

    public CigaretteUseHandler(NicotineService nicotineService) {
        this.nicotineService = nicotineService;
    }

    public void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            if (world.isClient || hand != Hand.MAIN_HAND || !stack.isOf(ModItems.CIGARETTE)) {
                return TypedActionResult.pass(stack);
            }

            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, BUZZ_DURATION_TICKS, 0));

            if (nicotineService.smoke(player.getUuid())) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, ILLNESS_DURATION_TICKS, 0));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, ILLNESS_DURATION_TICKS, 0));
                player.sendMessage(Text.translatable("message.realworldmod.smoking_sickness"), true);
            }

            stack.decrement(1);
            return TypedActionResult.success(stack);
        });
    }
}
