package com.realworldmod.vice;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * The consequence {@link NicotineService#checkWithdrawal} triggers: Nausea
 * and Slowness standing in for the shakes/craving of nicotine withdrawal —
 * a distinct symptom set from {@link HangoverEffect}'s Nausea+Mining
 * Fatigue, so the two vice systems don't feel identical. Closes the rest
 * of slice 38/60's "alcohol/cigarettes need more variety" priority item:
 * smoking now has a real cost for *stopping*, not just for smoking too
 * much in a row. As of slice 67, the severity {@code checkWithdrawal}
 * returns scales both the effect amplifier and its duration, mirroring
 * {@link HangoverEffect}'s own escalation for repeat benders.
 */
public final class NicotineWithdrawalEffect {
    private static final int BASE_DURATION_TICKS = 20 * 60;

    private NicotineWithdrawalEffect() {
    }

    /** Checks whether {@code player} just crossed into withdrawal and, if so, applies it scaled to its severity. */
    public static void check(NicotineService nicotineService, ServerPlayerEntity player, long currentTick) {
        int severity = nicotineService.checkWithdrawal(player.getUuid(), currentTick);
        if (severity == 0) {
            return;
        }
        int amplifier = severity - 1;
        int duration = BASE_DURATION_TICKS * severity;
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, duration, amplifier));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, duration, amplifier));
        player.sendMessage(Text.translatable("message.realworldmod.nicotine_withdrawal"), true);
    }
}
