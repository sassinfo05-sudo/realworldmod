package com.realworldmod.crime;

import com.realworldmod.economy.CurrencyFormatter;
import com.realworldmod.npc.CitizenEntity;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

/**
 * First real consequence for hurting another player (Section 7:
 * "murder/assault as its own distinct, tracked crime type" — until now,
 * {@link CrimeService} only ever recorded trespassing- and
 * poaching/dealing-style offenses, nothing for actually attacking anyone).
 * Any hit a player lands on another player or on a {@link CitizenEntity}
 * records an assault through the same
 * {@link LawEnforcementService#recordOffense} pipeline every other crime
 * in the mod already uses, at a higher severity than poaching or dealing
 * narcotics — a real fistfight is worse than a stolen deer, and this
 * reflects that without inventing a parallel enforcement system. Deer,
 * coyotes, police, and game wardens are deliberately excluded — they have
 * their own distinct consequences (poaching, predation, arrest) rather
 * than being ordinary assault victims.
 */
public final class AssaultHandler {
    public static final int ASSAULT_SEVERITY = 3;

    private final LawEnforcementService lawEnforcementService;

    public AssaultHandler(LawEnforcementService lawEnforcementService) {
        this.lawEnforcementService = lawEnforcementService;
    }

    public void register() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) -> {
            if (blocked || !isProtectedVictim(entity) || !source.isOf(DamageTypes.PLAYER_ATTACK)) {
                return;
            }
            if (!(source.getAttacker() instanceof PlayerEntity attacker)
                    || attacker.getUuid().equals(entity.getUuid())) {
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

    private static boolean isProtectedVictim(LivingEntity entity) {
        return entity instanceof PlayerEntity || entity instanceof CitizenEntity;
    }
}
