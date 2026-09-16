package com.realworldmod.property;

import com.realworldmod.crime.CrimeService;
import com.realworldmod.crime.LawEnforcementService;
import com.realworldmod.crime.OffenseOutcome;
import com.realworldmod.economy.CurrencyFormatter;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.text.Text;

/**
 * Wires {@link ClaimRegistry} into world mutation: breaking a block inside a
 * claim you don't own is refused (Section: anti-griefing / "modifying
 * property requires owning the deed"), logged as trespassing, and — once
 * the offender's wanted level is high enough — fined. Block placement is
 * gated the same way, but via {@code BlockItemMixin} — Fabric API has no
 * generic "before block placed" event equivalent to this one.
 */
public final class PropertyProtection {
    private final ClaimRegistry registry;
    private final LawEnforcementService lawEnforcementService;

    public PropertyProtection(ClaimRegistry registry, LawEnforcementService lawEnforcementService) {
        this.registry = registry;
        this.lawEnforcementService = lawEnforcementService;
    }

    public void register() {
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient) {
                return true;
            }
            boolean allowed = registry.canModify(player.getUuid(), pos.getX(), pos.getZ());
            if (!allowed) {
                player.sendMessage(Text.translatable("message.realworldmod.no_permit"), true);
                OffenseOutcome outcome = lawEnforcementService.recordOffense(
                        player.getUuid(), CrimeService.TRESPASS_SEVERITY);
                if (outcome.fined()) {
                    player.sendMessage(Text.translatable("message.realworldmod.fine_issued",
                            CurrencyFormatter.format(LawEnforcementService.FINE_CENTS)), true);
                }
            }
            return allowed;
        });
    }
}
