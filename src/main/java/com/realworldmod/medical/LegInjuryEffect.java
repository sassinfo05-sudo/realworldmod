package com.realworldmod.medical;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

/**
 * First slice of Section 5's localized anatomical damage system: fall
 * damage above a threshold leaves the player limping (a Slowness effect
 * standing in for a real leg-bone model) instead of just subtracting from
 * a flat health bar.
 *
 * <p>Scoped deliberately small for this slice: players only (not NPCs
 * yet), no persistence across relog, and no hospital/cast treatment to
 * clear it early — the effect just runs its course. See ROADMAP.md.
 */
public final class LegInjuryEffect {
    private LegInjuryEffect() {
    }

    public static void register() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) -> {
            if (blocked || !(entity instanceof PlayerEntity player) || !source.isOf(DamageTypes.FALL)) {
                return;
            }

            LegInjury injury = FallInjuryCalculator.fromFallDamage(damageTaken);
            if (injury == LegInjury.NONE) {
                return;
            }

            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SLOWNESS,
                    FallInjuryCalculator.slownessDurationTicksFor(injury),
                    FallInjuryCalculator.slownessAmplifierFor(injury)));

            player.sendMessage(Text.translatable(injury == LegInjury.FRACTURED
                    ? "message.realworldmod.leg_fractured"
                    : "message.realworldmod.leg_bruised"), true);
        });
    }
}
