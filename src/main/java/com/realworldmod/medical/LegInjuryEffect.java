package com.realworldmod.medical;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

/**
 * First slice of Section 5's localized anatomical damage system: fall
 * damage above a threshold leaves the sufferer limping (a Slowness effect
 * standing in for a real leg-bone model) instead of just subtracting from
 * a flat health bar.
 *
 * <p>As of slice 48, this is no longer player-only: {@code
 * ServerLivingEntityEvents.AFTER_DAMAGE} already hands back a
 * {@code LivingEntity}, not just a player, so the restriction to
 * {@code PlayerEntity} was an artificial one — removing it means any
 * living entity (a {@code CitizenEntity}, a {@code DeerEntity}, a stray
 * vanilla cow) that takes a bad enough fall limps exactly like a player
 * does. Only the player-facing chat message stays player-only, since
 * nothing else in the mod has a way to read one.
 *
 * <p>Still deliberately small: no persistence across relog, and no
 * hospital/cast treatment to clear it early — the effect just runs its
 * course. See ROADMAP.md.
 */
public final class LegInjuryEffect {
    private LegInjuryEffect() {
    }

    public static void register() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) -> {
            if (blocked || !source.isOf(DamageTypes.FALL)) {
                return;
            }

            LegInjury injury = FallInjuryCalculator.fromFallDamage(damageTaken);
            if (injury == LegInjury.NONE) {
                return;
            }

            entity.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SLOWNESS,
                    FallInjuryCalculator.slownessDurationTicksFor(injury),
                    FallInjuryCalculator.slownessAmplifierFor(injury)));

            if (entity instanceof PlayerEntity player) {
                player.sendMessage(Text.translatable(injury == LegInjury.FRACTURED
                        ? "message.realworldmod.leg_fractured"
                        : "message.realworldmod.leg_bruised"), true);
            }
        });
    }
}
