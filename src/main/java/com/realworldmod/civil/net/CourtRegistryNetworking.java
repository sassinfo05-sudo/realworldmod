package com.realworldmod.civil.net;

import com.mojang.authlib.GameProfile;
import com.realworldmod.civil.CivilCourtService;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.UUID;

/**
 * Registers the court-registry status request/response payload types, the
 * Contest action, the filing-history request/response, and their
 * server-side handlers.
 */
public final class CourtRegistryNetworking {
    private CourtRegistryNetworking() {
    }

    public static void registerPayloadTypes() {
        PayloadTypeRegistry.playC2S().register(CourtRegistryStatusRequestPayload.ID, CourtRegistryStatusRequestPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(CourtRegistryContestPayload.ID, CourtRegistryContestPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(CourtRegistryHistoryRequestPayload.ID, CourtRegistryHistoryRequestPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(CourtRegistryStatusResponsePayload.ID, CourtRegistryStatusResponsePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(CourtRegistryHistoryResponsePayload.ID, CourtRegistryHistoryResponsePayload.CODEC);
    }

    public static void registerServerReceiver(CivilCourtService civilCourtService) {
        ServerPlayNetworking.registerGlobalReceiver(CourtRegistryStatusRequestPayload.ID, (payload, context) ->
                context.responseSender().sendPacket(buildResponse(civilCourtService, context.player())));
        ServerPlayNetworking.registerGlobalReceiver(CourtRegistryContestPayload.ID, (payload, context) -> {
            civilCourtService.contest(context.player().getUuid(), context.player().getWorld().getTime());
            context.responseSender().sendPacket(buildResponse(civilCourtService, context.player()));
        });
        ServerPlayNetworking.registerGlobalReceiver(CourtRegistryHistoryRequestPayload.ID, (payload, context) ->
                context.responseSender().sendPacket(buildHistoryResponse(civilCourtService, context.player())));
    }

    private static CourtRegistryStatusResponsePayload buildResponse(
            CivilCourtService civilCourtService, ServerPlayerEntity player) {
        long currentTick = player.getWorld().getTime();
        return civilCourtService.getCase(player.getUuid())
                .map(civilCase -> new CourtRegistryStatusResponsePayload(
                        true, civilCase.amountCents(), Math.max(0, civilCase.deadlineTick() - currentTick),
                        resolveName(player, civilCase.plaintiffId())))
                .orElse(new CourtRegistryStatusResponsePayload(false, 0L, 0L, ""));
    }

    private static CourtRegistryHistoryResponsePayload buildHistoryResponse(
            CivilCourtService civilCourtService, ServerPlayerEntity player) {
        UUID playerId = player.getUuid();
        List<CivilCourtService.ArchivedCase> history = civilCourtService.getHistoryFor(playerId);
        if (history.isEmpty()) {
            return new CourtRegistryHistoryResponsePayload(0, false, "", 0L, false);
        }

        CivilCourtService.ArchivedCase mostRecent = history.get(0);
        UUID opponentId = mostRecent.plaintiffId().equals(playerId) ? mostRecent.defendantId() : mostRecent.plaintiffId();
        return new CourtRegistryHistoryResponsePayload(history.size(), true,
                resolveName(player, opponentId), mostRecent.amountCents(), mostRecent.contested());
    }

    private static String resolveName(ServerPlayerEntity player, UUID playerId) {
        return player.getServer().getUserCache().getByUuid(playerId)
                .map(GameProfile::getName)
                .orElse(playerId.toString());
    }
}
