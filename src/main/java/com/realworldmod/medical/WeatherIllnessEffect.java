package com.realworldmod.medical;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * Applies the actual gameplay consequence once {@link IllnessService}
 * decides a player has been rained on long enough to get sick: Nausea and
 * Weakness standing in for a real illness/hospital system (see
 * ROADMAP.md for what's not implemented — pharmacies, diagnosis, recovery
 * items).
 */
public final class WeatherIllnessEffect {
    private static final int ILLNESS_DURATION_TICKS = 20 * 60 * 2;

    private WeatherIllnessEffect() {
    }

    /** Checks {@code player}'s current rain exposure and applies illness if the exposure threshold is crossed this tick. */
    public static void check(IllnessService illnessService, ServerPlayerEntity player) {
        boolean exposedNow = player.getWorld().hasRain(player.getBlockPos());
        if (illnessService.tick(player.getUuid(), exposedNow)) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, ILLNESS_DURATION_TICKS, 0));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, ILLNESS_DURATION_TICKS, 0));
            player.sendMessage(Text.translatable("message.realworldmod.caught_a_cold"), true);
        }
    }
}
