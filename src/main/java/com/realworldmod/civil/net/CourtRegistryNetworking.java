package com.realworldmod.civil.net;

import com.mojang.authlib.GameProfile;
import com.realworldmod.civil.CivilCourtService;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

/**
 * Registers the court-registry status request/response payload types, the
 * Contest action, and their server-side handlers.
 */
public final class CourtRegistryNetworking {
    private CourtRegistryNetworking() {
    }

    public static void registerPayloadTypes() {
        PayloadTypeRegistry.playC2S().register(CourtRegistryStatusRequestPayload.ID, CourtRegistryStatusRequestPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(CourtRegistryContestPayload.ID, CourtRegistryContestPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(CourtRegistryStatusResponsePayload.ID, CourtRegistryStatusResponsePayload.CODEC);
    }

    public static void registerServerReceiver(CivilCourtService civilCourtService) {
        ServerPlayNetworking.registerGlobalReceiver(CourtRegistryStatusRequestPayload.ID, (payload, context) ->
                context.responseSender().sendPacket(buildResponse(civilCourtService, context.player())));
        ServerPlayNetworking.registerGlobalReceiver(CourtRegistryContestPayload.ID, (payload, context) -> {
            civilCourtService.contest(context.player().getUuid());
            context.responseSender().sendPacket(buildResponse(civilCourtService, context.player()));
        });
    }

    private static CourtRegistryStatusResponsePayload buildResponse(
            CivilCourtService civilCourtService, ServerPlayerEntity player) {
        long currentTick = player.getWorld().getTime();
        return civilCourtService.getCase(player.getUuid())
                .map(civilCase -> new CourtRegistryStatusResponsePayload(
                        true, civilCase.amountCents(), Math.max(0, civilCase.deadlineTick() - currentTick),
                        resolvePlaintiffName(player, civilCase.plaintiffId())))
                .orElse(new CourtRegistryStatusResponsePayload(false, 0L, 0L, ""));
    }

    private static String resolvePlaintiffName(ServerPlayerEntity player, UUID plaintiffId) {
        return player.getServer().getUserCache().getByUuid(plaintiffId)
                .map(GameProfile::getName)
                .orElse(plaintiffId.toString());
    }
}
