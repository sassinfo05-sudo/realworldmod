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
 * Drinking {@code ALCOHOL} raises the player's intoxication level
 * ({@link IntoxicationService}) and applies Slowness proportional to it —
 * a real, escalating, self-sobering-up mechanic in place of a single flat
 * effect, standing in for the fuller "fitness/lineage" model Section 5
 * still lacks (see ROADMAP.md).
 */
public final class AlcoholUseHandler {
    private static final int DURATION_TICKS = 20 * 60 * 2;

    private final IntoxicationService intoxicationService;

    public AlcoholUseHandler(IntoxicationService intoxicationService) {
        this.intoxicationService = intoxicationService;
    }

    public void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            if (world.isClient || hand != Hand.MAIN_HAND || !stack.isOf(ModItems.ALCOHOL)) {
                return TypedActionResult.pass(stack);
            }

            int level = intoxicationService.drink(player.getUuid(), world.getTime());
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SLOWNESS,
                    IntoxicationCalculator.slownessDurationTicksFor(level),
                    IntoxicationCalculator.slownessAmplifierFor(level)));
            if (IntoxicationCalculator.causesNausea(level)) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, DURATION_TICKS, 0));
            }

            stack.decrement(1);
            player.sendMessage(Text.translatable(messageKeyFor(level)), true);
            return TypedActionResult.success(stack);
        });
    }

    private static String messageKeyFor(int level) {
        return switch (level) {
            case 1 -> "message.realworldmod.alcohol_tipsy";
            case 2 -> "message.realworldmod.alcohol_drunk";
            default -> "message.realworldmod.alcohol_very_drunk";
        };
    }
}
