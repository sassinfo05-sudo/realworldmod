package com.realworldmod.civil.net;

import com.realworldmod.civil.CivilCourtService;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

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
                context.responseSender().sendPacket(buildResponse(civilCourtService, context.player().getUuid(),
                        context.player().getWorld().getTime())));
        ServerPlayNetworking.registerGlobalReceiver(CourtRegistryContestPayload.ID, (payload, context) -> {
            civilCourtService.contest(context.player().getUuid());
            context.responseSender().sendPacket(buildResponse(civilCourtService, context.player().getUuid(),
                    context.player().getWorld().getTime()));
        });
    }

    private static CourtRegistryStatusResponsePayload buildResponse(
            CivilCourtService civilCourtService, UUID playerId, long currentTick) {
        return civilCourtService.getCase(playerId)
                .map(civilCase -> new CourtRegistryStatusResponsePayload(
                        true, civilCase.amountCents(), Math.max(0, civilCase.deadlineTick() - currentTick)))
                .orElse(new CourtRegistryStatusResponsePayload(false, 0L, 0L));
    }
}
