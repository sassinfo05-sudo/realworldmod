package com.realworldmod.crime;

import com.realworldmod.economy.CurrencyFormatter;
import com.realworldmod.npc.CitizenEntity;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

/**
 * Closes the "murder as a crime distinct from assault" gap: slice 40's
 * {@link AssaultHandler} records every hit landed on a player or
 * {@link CitizenEntity} as an assault, but a killing blow deserves a
 * separate, harsher consequence than merely landing a hit. Hooks
 * {@link ServerLivingEntityEvents#AFTER_DEATH} — fired once, right as the
 * victim actually dies, rather than on every hit — and records the
 * offense through the same {@link LawEnforcementService#recordOffense}
 * pipeline every other crime in the mod uses, at a severity that jumps
 * a player straight to the maximum wanted level in one offense rather
 * than escalating gradually like a lesser crime would. As of slice 55,
 * this also closes the "no consequence at all for killing an NPC" gap:
 * a citizen has real health and can actually die, so killing one is now
 * murder exactly like killing a player.
 */
public final class MurderHandler {
    public static final int MURDER_SEVERITY = WantedLevelMath.MAX;

    private final LawEnforcementService lawEnforcementService;

    public MurderHandler(LawEnforcementService lawEnforcementService) {
        this.lawEnforcementService = lawEnforcementService;
    }

    public void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (!isProtectedVictim(entity) || !source.isOf(DamageTypes.PLAYER_ATTACK)) {
                return;
            }
            if (!(source.getAttacker() instanceof PlayerEntity attacker)
                    || attacker.getUuid().equals(entity.getUuid())) {
                return;
            }

            OffenseOutcome outcome = lawEnforcementService.recordOffense(attacker.getUuid(), MURDER_SEVERITY);
            attacker.sendMessage(Text.translatable("message.realworldmod.murder_recorded"), true);
            if (outcome.fined()) {
                attacker.sendMessage(Text.translatable("message.realworldmod.fine_issued",
                        CurrencyFormatter.format(LawEnforcementService.FINE_CENTS)), true);
            }
        });
    }

    private static boolean isProtectedVictim(LivingEntity entity) {
        return entity instanceof PlayerEntity || entity instanceof CitizenEntity;
    }
}
