package com.realworldmod.property;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.text.Text;

/**
 * Wires {@link ClaimRegistry} into world mutation: breaking a block inside a
 * claim you don't own is refused (Section: anti-griefing / "modifying
 * property requires owning the deed"). Block placement is not gated by this
 * slice — see {@code ROADMAP.md} — since Fabric API has no equivalent
 * generic "before block placed" event and doing it correctly needs a mixin.
 */
public final class PropertyProtection {
    private final ClaimRegistry registry;

    public PropertyProtection(ClaimRegistry registry) {
        this.registry = registry;
    }

    public void register() {
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient) {
                return true;
            }
            boolean allowed = registry.canModify(player.getUuid(), pos.getX(), pos.getZ());
            if (!allowed) {
                player.sendMessage(Text.translatable("message.realworldmod.no_permit"), true);
            }
            return allowed;
        });
    }
}
