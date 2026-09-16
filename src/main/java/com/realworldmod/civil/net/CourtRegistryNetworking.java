package com.realworldmod.civil.net;

import com.mojang.authlib.GameProfile;
import com.realworldmod.civil.CivilCourtService;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Registers the court-registry status request/response payload types, the
 * Contest action, the filing-history request/response, and their
 * server-side handlers.
 */
public final class CourtRegistryNetworking {
    /** How many of a player's most-recently-resolved cases {@code buildHistoryResponse} sends, capping the response size. */
    public static final int MAX_HISTORY_ENTRIES = 5;

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

        List<CourtRegistryHistoryResponsePayload.HistoryEntry> entries = new ArrayList<>();
        for (int i = 0; i < Math.min(history.size(), MAX_HISTORY_ENTRIES); i++) {
            CivilCourtService.ArchivedCase archivedCase = history.get(i);
            UUID opponentId = archivedCase.plaintiffId().equals(playerId)
                    ? archivedCase.defendantId() : archivedCase.plaintiffId();
            entries.add(new CourtRegistryHistoryResponsePayload.HistoryEntry(
                    resolveName(player, opponentId), archivedCase.amountCents(), archivedCase.contested()));
        }
        return new CourtRegistryHistoryResponsePayload(history.size(), entries);
    }

    private static String resolveName(ServerPlayerEntity player, UUID playerId) {
        return player.getServer().getUserCache().getByUuid(playerId)
                .map(GameProfile::getName)
                .orElse(playerId.toString());
    }
}
