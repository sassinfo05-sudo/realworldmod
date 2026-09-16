package com.realworldmod.vice;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * The consequence {@link IntoxicationService#checkHangover} triggers:
 * Nausea and Mining Fatigue standing in for a real hangover, the same
 * "two vanilla debuffs plus a chat message" shape
 * {@code medical.WeatherIllnessEffect} already uses for illness — closing
 * part of Section 5's "alcohol/cigarettes need more variety" priority
 * item by giving heavy drinking an actual next-morning cost instead of
 * ending the moment a player sobers up.
 */
public final class HangoverEffect {
    private static final int HANGOVER_DURATION_TICKS = 20 * 60;

    private HangoverEffect() {
    }

    /** Checks whether {@code player} just finished sobering up from a peak of {@code IntoxicationCalculator.MAX_LEVEL} and, if so, applies a hangover. */
    public static void check(IntoxicationService intoxicationService, ServerPlayerEntity player, long currentTick) {
        if (!intoxicationService.checkHangover(player.getUuid(), currentTick)) {
            return;
        }
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, HANGOVER_DURATION_TICKS, 0));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, HANGOVER_DURATION_TICKS, 0));
        player.sendMessage(Text.translatable("message.realworldmod.hangover"), true);
    }
}
