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
 * much in a row.
 */
public final class NicotineWithdrawalEffect {
    private static final int WITHDRAWAL_DURATION_TICKS = 20 * 60;

    private NicotineWithdrawalEffect() {
    }

    /** Checks whether {@code player} just crossed into withdrawal and, if so, applies it. */
    public static void check(NicotineService nicotineService, ServerPlayerEntity player, long currentTick) {
        if (!nicotineService.checkWithdrawal(player.getUuid(), currentTick)) {
            return;
        }
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, WITHDRAWAL_DURATION_TICKS, 0));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, WITHDRAWAL_DURATION_TICKS, 0));
        player.sendMessage(Text.translatable("message.realworldmod.nicotine_withdrawal"), true);
    }
}
