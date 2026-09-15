package com.realworldmod.wildlife;

import com.realworldmod.crime.LawEnforcementService;
import com.realworldmod.crime.OffenseOutcome;
import com.realworldmod.economy.CurrencyFormatter;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * Section 8's "hunting without a license alerts NPC game wardens," still
 * reduced to reusing the existing crime system rather than building warden
 * NPCs as agents (see ROADMAP.md), but as of slice 22 correctly scoped to
 * real wildlife: killing a {@link DeerEntity} without a license is a
 * recorded offense, same as trespassing. Vanilla livestock (cows, pigs,
 * chickens, sheep) is no longer treated as poachable game.
 */
public final class PoachingHandler {
    public static final int POACHING_SEVERITY = 1;

    private final LawEnforcementService lawEnforcementService;
    private final HuntingLicenseService licenseService;

    public PoachingHandler(LawEnforcementService lawEnforcementService, HuntingLicenseService licenseService) {
        this.lawEnforcementService = lawEnforcementService;
        this.licenseService = licenseService;
    }

    public void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity instanceof DeerEntity)) {
                return;
            }
            if (!(damageSource.getAttacker() instanceof ServerPlayerEntity player)) {
                return;
            }
            if (licenseService.hasLicense(player.getUuid())) {
                return;
            }

            player.sendMessage(Text.translatable("message.realworldmod.poaching_detected"), true);
            OffenseOutcome outcome = lawEnforcementService.recordOffense(player.getUuid(), POACHING_SEVERITY);
            if (outcome.fined()) {
                player.sendMessage(Text.translatable("message.realworldmod.fine_issued",
                        CurrencyFormatter.format(LawEnforcementService.FINE_CENTS)), true);
            }
        });
    }
}
