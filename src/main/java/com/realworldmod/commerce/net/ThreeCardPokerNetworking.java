package com.realworldmod.commerce.net;

import com.realworldmod.commerce.Card;
import com.realworldmod.commerce.ThreeCardPokerGame;
import com.realworldmod.commerce.ThreeCardPokerService;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Registers the Three Card Poker action payloads (state/deal/fold/play)
 * and the response payload, plus the server-side handlers that apply each
 * action to {@link ThreeCardPokerService} and report the resulting state
 * back — the same action-then-respond shape {@code BlackjackNetworking}
 * uses.
 */
public final class ThreeCardPokerNetworking {
    private ThreeCardPokerNetworking() {
    }

    public static void registerPayloadTypes() {
        PayloadTypeRegistry.playC2S().register(ThreeCardPokerStatePayload.ID, ThreeCardPokerStatePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ThreeCardPokerDealPayload.ID, ThreeCardPokerDealPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ThreeCardPokerFoldPayload.ID, ThreeCardPokerFoldPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ThreeCardPokerPlayPayload.ID, ThreeCardPokerPlayPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ThreeCardPokerStateResponsePayload.ID, ThreeCardPokerStateResponsePayload.CODEC);
    }

    public static void registerServerReceivers(ThreeCardPokerService pokerService) {
        ServerPlayNetworking.registerGlobalReceiver(ThreeCardPokerStatePayload.ID, (payload, context) ->
                context.responseSender().sendPacket(buildResponse(pokerService, context.player().getUuid())));
        ServerPlayNetworking.registerGlobalReceiver(ThreeCardPokerDealPayload.ID, (payload, context) -> {
            pokerService.deal(context.player().getUuid());
            context.responseSender().sendPacket(buildResponse(pokerService, context.player().getUuid()));
        });
        ServerPlayNetworking.registerGlobalReceiver(ThreeCardPokerFoldPayload.ID, (payload, context) -> {
            pokerService.fold(context.player().getUuid());
            context.responseSender().sendPacket(buildResponse(pokerService, context.player().getUuid()));
        });
        ServerPlayNetworking.registerGlobalReceiver(ThreeCardPokerPlayPayload.ID, (payload, context) -> {
            pokerService.play(context.player().getUuid());
            context.responseSender().sendPacket(buildResponse(pokerService, context.player().getUuid()));
        });
    }

    private static ThreeCardPokerStateResponsePayload buildResponse(ThreeCardPokerService pokerService, UUID playerId) {
        Optional<ThreeCardPokerGame> game = pokerService.activeGame(playerId);
        if (game.isEmpty()) {
            return new ThreeCardPokerStateResponsePayload(false, List.of(), List.of(), false, -1, 0);
        }

        ThreeCardPokerGame activeGame = game.get();
        List<Integer> playerOrdinals = activeGame.playerHand().stream().map(Card::toOrdinal).toList();
        List<Integer> dealerOrdinals = activeGame.isResolved()
                ? activeGame.dealerHand().stream().map(Card::toOrdinal).toList()
                : List.of();
        int outcomeOrdinal = activeGame.isResolved() ? activeGame.outcome().ordinal() : -1;
        long payout = activeGame.isResolved() ? pokerService.lastPayoutCents(playerId) : 0;

        return new ThreeCardPokerStateResponsePayload(
                true, playerOrdinals, dealerOrdinals, activeGame.isResolved(), outcomeOrdinal, payout);
    }
}
