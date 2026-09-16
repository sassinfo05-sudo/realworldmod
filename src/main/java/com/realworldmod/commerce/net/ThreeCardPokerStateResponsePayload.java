package com.realworldmod.commerce.net;

import com.realworldmod.RealWorldMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Server -> client: the requesting player's current Three Card Poker
 * state. {@code dealerHandOrdinals} is empty while the round is still
 * awaiting a fold/play decision — the dealer's hand stays fully hidden
 * until the player commits, the real game's information flow. Ordinals
 * encode both rank and suit via {@code Card.toOrdinal}.
 */
public record ThreeCardPokerStateResponsePayload(
        boolean hasActiveGame,
        List<Integer> playerHandOrdinals,
        List<Integer> dealerHandOrdinals,
        boolean resolved,
        int outcomeOrdinal,
        long payoutCents
) implements CustomPayload {
    public static final CustomPayload.Id<ThreeCardPokerStateResponsePayload> ID =
            new CustomPayload.Id<>(Identifier.of(RealWorldMod.MOD_ID, "poker_state_response"));
    public static final PacketCodec<RegistryByteBuf, ThreeCardPokerStateResponsePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.BOOL, ThreeCardPokerStateResponsePayload::hasActiveGame,
            PacketCodecs.collection(ArrayList::new, PacketCodecs.INTEGER), ThreeCardPokerStateResponsePayload::playerHandOrdinals,
            PacketCodecs.collection(ArrayList::new, PacketCodecs.INTEGER), ThreeCardPokerStateResponsePayload::dealerHandOrdinals,
            PacketCodecs.BOOL, ThreeCardPokerStateResponsePayload::resolved,
            PacketCodecs.VAR_INT, ThreeCardPokerStateResponsePayload::outcomeOrdinal,
            PacketCodecs.VAR_LONG, ThreeCardPokerStateResponsePayload::payoutCents,
            ThreeCardPokerStateResponsePayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
