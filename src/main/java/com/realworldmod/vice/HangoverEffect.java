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
 * ending the moment a player sobers up. As of slice 67, the severity
 * {@code checkHangover} returns scales both the effect amplifier and its
 * duration, so a player who keeps binge-drinking gets worse hangovers in
 * a row instead of an identical one every time.
 */
public final class HangoverEffect {
    private static final int BASE_DURATION_TICKS = 20 * 60;

    private HangoverEffect() {
    }

    /** Checks whether {@code player} just finished sobering up from a peak of {@code IntoxicationCalculator.MAX_LEVEL} and, if so, applies a hangover scaled to its severity. */
    public static void check(IntoxicationService intoxicationService, ServerPlayerEntity player, long currentTick) {
        int severity = intoxicationService.checkHangover(player.getUuid(), currentTick);
        if (severity == 0) {
            return;
        }
        int amplifier = severity - 1;
        int duration = BASE_DURATION_TICKS * severity;
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, duration, amplifier));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, duration, amplifier));
        player.sendMessage(Text.translatable("message.realworldmod.hangover"), true);
    }
}
