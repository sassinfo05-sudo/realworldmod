package com.realworldmod.commerce.net;

import com.realworldmod.commerce.CrapsGame;
import com.realworldmod.commerce.CrapsService;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import java.util.Optional;
import java.util.UUID;

/**
 * Registers the craps action payloads (state/start/roll) and the response
 * payload, plus the server-side handlers that apply each action to
 * {@link CrapsService} and report the resulting state back — the same
 * action-then-respond shape {@code BlackjackNetworking}/
 * {@code ThreeCardPokerNetworking} use.
 */
public final class CrapsNetworking {
    private CrapsNetworking() {
    }

    public static void registerPayloadTypes() {
        PayloadTypeRegistry.playC2S().register(CrapsStatePayload.ID, CrapsStatePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(CrapsStartPayload.ID, CrapsStartPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(CrapsRollPayload.ID, CrapsRollPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(CrapsStateResponsePayload.ID, CrapsStateResponsePayload.CODEC);
    }

    public static void registerServerReceivers(CrapsService crapsService) {
        ServerPlayNetworking.registerGlobalReceiver(CrapsStatePayload.ID, (payload, context) ->
                context.responseSender().sendPacket(buildResponse(crapsService, context.player().getUuid())));
        ServerPlayNetworking.registerGlobalReceiver(CrapsStartPayload.ID, (payload, context) -> {
            crapsService.startGame(context.player().getUuid());
            context.responseSender().sendPacket(buildResponse(crapsService, context.player().getUuid()));
        });
        ServerPlayNetworking.registerGlobalReceiver(CrapsRollPayload.ID, (payload, context) -> {
            crapsService.roll(context.player().getUuid());
            context.responseSender().sendPacket(buildResponse(crapsService, context.player().getUuid()));
        });
    }

    private static CrapsStateResponsePayload buildResponse(CrapsService crapsService, UUID playerId) {
        Optional<CrapsGame> game = crapsService.activeGame(playerId);
        if (game.isEmpty()) {
            return new CrapsStateResponsePayload(false, -1, -1, false, -1, 0);
        }

        CrapsGame activeGame = game.get();
        int outcomeOrdinal = activeGame.isResolved() ? activeGame.outcome().ordinal() : -1;
        long payout = activeGame.isResolved() ? crapsService.lastPayoutCents(playerId) : 0;

        return new CrapsStateResponsePayload(
                true, activeGame.point(), activeGame.lastRollTotal(), activeGame.isResolved(), outcomeOrdinal, payout);
    }
}
