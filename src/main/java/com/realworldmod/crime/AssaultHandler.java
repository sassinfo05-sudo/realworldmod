package com.realworldmod.crime;

import com.realworldmod.economy.CurrencyFormatter;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

/**
 * First real consequence for hurting another player (Section 7:
 * "murder/assault as its own distinct, tracked crime type" — until now,
 * {@link CrimeService} only ever recorded trespassing- and
 * poaching/dealing-style offenses, nothing for actually attacking anyone).
 * Any player-on-player hit records an assault through the same
 * {@link LawEnforcementService#recordOffense} pipeline every other crime
 * in the mod already uses, at a higher severity than poaching or dealing
 * narcotics — a real fistfight is worse than a stolen deer, and this
 * reflects that without inventing a parallel enforcement system.
 */
public final class AssaultHandler {
    public static final int ASSAULT_SEVERITY = 3;

    private final LawEnforcementService lawEnforcementService;

    public AssaultHandler(LawEnforcementService lawEnforcementService) {
        this.lawEnforcementService = lawEnforcementService;
    }

    public void register() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) -> {
            if (blocked || !(entity instanceof PlayerEntity victim) || !source.isOf(DamageTypes.PLAYER_ATTACK)) {
                return;
            }
            if (!(source.getAttacker() instanceof PlayerEntity attacker)
                    || attacker.getUuid().equals(victim.getUuid())) {
                return;
            }

            OffenseOutcome outcome = lawEnforcementService.recordOffense(attacker.getUuid(), ASSAULT_SEVERITY);
            attacker.sendMessage(Text.translatable("message.realworldmod.assault_recorded"), true);
            if (outcome.fined()) {
                attacker.sendMessage(Text.translatable("message.realworldmod.fine_issued",
                        CurrencyFormatter.format(LawEnforcementService.FINE_CENTS)), true);
            }
        });
    }
}
