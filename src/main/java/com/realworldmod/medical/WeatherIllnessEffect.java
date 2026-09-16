package com.realworldmod.medical;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * Applies the actual gameplay consequence once {@link IllnessService}
 * decides someone has been rained on long enough to get sick: Nausea and
 * Weakness standing in for a real illness/hospital system (see
 * ROADMAP.md for what's not implemented — pharmacies, diagnosis, recovery
 * items). As of slice 46, this is no longer player-only:
 * {@link #checkEntity} applies the same consequence to any
 * {@code LivingEntity} sharing the same {@link IllnessService}, so a
 * {@code CitizenEntity} caught in the rain gets sick exactly like a
 * player does — the first system in the mod where NPCs are no longer
 * exempt from something that already governs players.
 */
public final class WeatherIllnessEffect {
    private static final int ILLNESS_DURATION_TICKS = 20 * 60 * 2;

    private WeatherIllnessEffect() {
    }

    /** Checks {@code player}'s current rain exposure, applies illness if the exposure threshold is crossed this tick, and tells them so. */
    public static void check(IllnessService illnessService, ServerPlayerEntity player) {
        if (applyIfSick(illnessService, player)) {
            player.sendMessage(Text.translatable("message.realworldmod.caught_a_cold"), true);
        }
    }

    /** The same rain-exposure check and consequence as {@link #check}, for any living entity that has no chat to notify. */
    public static void checkEntity(IllnessService illnessService, LivingEntity entity) {
        applyIfSick(illnessService, entity);
    }

    private static boolean applyIfSick(IllnessService illnessService, LivingEntity entity) {
        boolean exposedNow = entity.getWorld().hasRain(entity.getBlockPos());
        if (!illnessService.tick(entity.getUuid(), exposedNow)) {
            return false;
        }
        entity.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, ILLNESS_DURATION_TICKS, 0));
        entity.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, ILLNESS_DURATION_TICKS, 0));
        return true;
    }
}
