package com.realworldmod.medical;

import com.realworldmod.init.ModItems;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;

import java.util.List;

/**
 * Using {@code MEDICINE} clears the effects the leg-injury and illness
 * slices apply (Nausea, Weakness, Slowness), giving players a way to treat
 * those early instead of waiting them out. Buy it at a
 * {@code PHARMACY_COUNTER} (see {@link PharmacyUseHandler}).
 */
public final class MedicineUseHandler {
    private static final List<RegistryEntry<StatusEffect>> CURABLE_EFFECTS =
            List.of(StatusEffects.NAUSEA, StatusEffects.WEAKNESS, StatusEffects.SLOWNESS);

    private MedicineUseHandler() {
    }

    public static void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            if (world.isClient || hand != Hand.MAIN_HAND || !stack.isOf(ModItems.MEDICINE)) {
                return TypedActionResult.pass(stack);
            }

            boolean curedAnything = false;
            for (RegistryEntry<StatusEffect> effect : CURABLE_EFFECTS) {
                if (player.removeStatusEffect(effect)) {
                    curedAnything = true;
                }
            }

            if (curedAnything) {
                stack.decrement(1);
                player.sendMessage(Text.translatable("message.realworldmod.medicine_used"), true);
                return TypedActionResult.success(stack);
            }
            player.sendMessage(Text.translatable("message.realworldmod.medicine_not_needed"), true);
            return TypedActionResult.fail(stack);
        });
    }
}
