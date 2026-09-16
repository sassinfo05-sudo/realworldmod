package com.realworldmod.commerce.net;

import com.realworldmod.RealWorldMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Server -> client: the requesting player's current craps state.
 * {@code point} is -1 during the come-out phase (before one is
 * established); {@code lastRollTotal} is -1 before any roll has happened
 * this round.
 */
public record CrapsStateResponsePayload(
        boolean hasActiveGame,
        int point,
        int lastRollTotal,
        boolean resolved,
        int outcomeOrdinal,
        long payoutCents
) implements CustomPayload {
    public static final CustomPayload.Id<CrapsStateResponsePayload> ID =
            new CustomPayload.Id<>(Identifier.of(RealWorldMod.MOD_ID, "craps_state_response"));
    public static final PacketCodec<RegistryByteBuf, CrapsStateResponsePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.BOOL, CrapsStateResponsePayload::hasActiveGame,
            PacketCodecs.VAR_INT, CrapsStateResponsePayload::point,
            PacketCodecs.VAR_INT, CrapsStateResponsePayload::lastRollTotal,
            PacketCodecs.BOOL, CrapsStateResponsePayload::resolved,
            PacketCodecs.VAR_INT, CrapsStateResponsePayload::outcomeOrdinal,
            PacketCodecs.VAR_LONG, CrapsStateResponsePayload::payoutCents,
            CrapsStateResponsePayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
