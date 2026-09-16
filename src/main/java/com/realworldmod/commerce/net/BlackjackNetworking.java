package com.realworldmod.commerce.net;

import com.realworldmod.commerce.BlackjackGame;
import com.realworldmod.commerce.BlackjackService;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Registers the blackjack action payloads (start/hit/stand) and the
 * response payload, plus the server-side handlers that apply each action
 * to {@link BlackjackService} and report the resulting state back — same
 * action-then-respond shape {@code UtilityNetworking} uses.
 */
public final class BlackjackNetworking {
    private BlackjackNetworking() {
    }

    public static void registerPayloadTypes() {
        PayloadTypeRegistry.playC2S().register(BlackjackStatePayload.ID, BlackjackStatePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(BlackjackStartPayload.ID, BlackjackStartPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(BlackjackHitPayload.ID, BlackjackHitPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(BlackjackStandPayload.ID, BlackjackStandPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(BlackjackStateResponsePayload.ID, BlackjackStateResponsePayload.CODEC);
    }

    public static void registerServerReceivers(BlackjackService blackjackService) {
        ServerPlayNetworking.registerGlobalReceiver(BlackjackStatePayload.ID, (payload, context) ->
                context.responseSender().sendPacket(buildResponse(blackjackService, context.player().getUuid())));
        ServerPlayNetworking.registerGlobalReceiver(BlackjackStartPayload.ID, (payload, context) -> {
            blackjackService.startGame(context.player().getUuid(), payload.betCents());
            context.responseSender().sendPacket(buildResponse(blackjackService, context.player().getUuid()));
        });
        ServerPlayNetworking.registerGlobalReceiver(BlackjackHitPayload.ID, (payload, context) -> {
            blackjackService.hit(context.player().getUuid());
            context.responseSender().sendPacket(buildResponse(blackjackService, context.player().getUuid()));
        });
        ServerPlayNetworking.registerGlobalReceiver(BlackjackStandPayload.ID, (payload, context) -> {
            blackjackService.stand(context.player().getUuid());
            context.responseSender().sendPacket(buildResponse(blackjackService, context.player().getUuid()));
        });
    }

    private static BlackjackStateResponsePayload buildResponse(BlackjackService blackjackService, UUID playerId) {
        Optional<BlackjackGame> game = blackjackService.activeGame(playerId);
        if (game.isEmpty()) {
            return new BlackjackStateResponsePayload(false, List.of(), List.of(), false, -1, 0);
        }

        BlackjackGame activeGame = game.get();
        List<Integer> playerOrdinals = activeGame.playerHand().stream().map(Enum::ordinal).toList();
        List<BlackjackGame.Rank> dealerHand = activeGame.dealerHand();
        List<Integer> dealerOrdinals = (activeGame.isResolved() ? dealerHand : dealerHand.subList(0, 1))
                .stream().map(Enum::ordinal).toList();
        int outcomeOrdinal = activeGame.isResolved() ? activeGame.outcome().ordinal() : -1;
        long payout = activeGame.isResolved() ? blackjackService.lastPayoutCents(playerId) : 0;

        return new BlackjackStateResponsePayload(
                true, playerOrdinals, dealerOrdinals, activeGame.isResolved(), outcomeOrdinal, payout);
    }
}
