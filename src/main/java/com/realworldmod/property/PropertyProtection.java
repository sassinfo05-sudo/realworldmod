package com.realworldmod.property;

import com.realworldmod.crime.CrimeService;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.text.Text;

/**
 * Wires {@link ClaimRegistry} into world mutation: breaking a block inside a
 * claim you don't own is refused (Section: anti-griefing / "modifying
 * property requires owning the deed") and logged as trespassing. Block
 * placement is gated the same way, but via {@code BlockItemMixin} — Fabric
 * API has no generic "before block placed" event equivalent to this one.
 */
public final class PropertyProtection {
    private final ClaimRegistry registry;
    private final CrimeService crimeService;

    public PropertyProtection(ClaimRegistry registry, CrimeService crimeService) {
        this.registry = registry;
        this.crimeService = crimeService;
    }

    public void register() {
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient) {
                return true;
            }
            boolean allowed = registry.canModify(player.getUuid(), pos.getX(), pos.getZ());
            if (!allowed) {
                player.sendMessage(Text.translatable("message.realworldmod.no_permit"), true);
                crimeService.recordCrime(player.getUuid(), CrimeService.TRESPASS_SEVERITY);
            }
            return allowed;
        });
    }
}
