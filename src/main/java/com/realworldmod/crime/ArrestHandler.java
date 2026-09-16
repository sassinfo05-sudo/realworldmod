package com.realworldmod.crime;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

/**
 * Applies the actual in-world consequence of an {@link ArrestOutcome}:
 * teleporting the player to a holding area and blinding them for the
 * sentence, then releasing them at world spawn once it's served. Stands in
 * for a real prison structure/loop (see ROADMAP.md) — there's no cell,
 * no yard, and no way to reduce the sentence early.
 */
public final class ArrestHandler {
    /** Offset from world spawn used as a stand-in holding area — no actual structure is built there. */
    private static final BlockPos HOLDING_AREA_OFFSET = new BlockPos(0, 200, 0);

    private ArrestHandler() {
    }

    public static void apply(ArrestOutcome outcome, MinecraftServer server, ServerPlayerEntity player) {
        switch (outcome) {
            case JUST_ARRESTED -> {
                BlockPos holdingArea = server.getOverworld().getSpawnPos().add(HOLDING_AREA_OFFSET);
                player.requestTeleport(holdingArea.getX() + 0.5, holdingArea.getY(), holdingArea.getZ() + 0.5);
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.BLINDNESS, (int) ArrestService.SENTENCE_TICKS, 0));
                player.sendMessage(Text.translatable("message.realworldmod.arrested"), false);
            }
            case JUST_RELEASED -> {
                BlockPos spawn = server.getOverworld().getSpawnPos();
                player.requestTeleport(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5);
                player.sendMessage(Text.translatable("message.realworldmod.released"), false);
            }
            case ACQUITTED -> player.sendMessage(Text.translatable("message.realworldmod.acquitted"), false);
            case STILL_DETAINED, NOT_ARRESTED -> {
                // No action needed on these ticks.
            }
        }
    }
}
