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
 * Server -> client: the requesting player's current blackjack state.
 * {@code dealerHandOrdinals} only ever contains the dealer's revealed
 * cards — while a round is still in progress that's just the first card
 * (the real "hole card" concealment rule); the rest appear once
 * {@code resolved} is true. Ordinals index {@code BlackjackGame.Rank}.
 */
public record BlackjackStateResponsePayload(
        boolean hasActiveGame,
        List<Integer> playerHandOrdinals,
        List<Integer> dealerHandOrdinals,
        boolean resolved,
        int outcomeOrdinal,
        long payoutCents
) implements CustomPayload {
    public static final CustomPayload.Id<BlackjackStateResponsePayload> ID =
            new CustomPayload.Id<>(Identifier.of(RealWorldMod.MOD_ID, "blackjack_state_response"));
    public static final PacketCodec<RegistryByteBuf, BlackjackStateResponsePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.BOOL, BlackjackStateResponsePayload::hasActiveGame,
            PacketCodecs.collection(ArrayList::new, PacketCodecs.INTEGER), BlackjackStateResponsePayload::playerHandOrdinals,
            PacketCodecs.collection(ArrayList::new, PacketCodecs.INTEGER), BlackjackStateResponsePayload::dealerHandOrdinals,
            PacketCodecs.BOOL, BlackjackStateResponsePayload::resolved,
            PacketCodecs.VAR_INT, BlackjackStateResponsePayload::outcomeOrdinal,
            PacketCodecs.VAR_LONG, BlackjackStateResponsePayload::payoutCents,
            BlackjackStateResponsePayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
