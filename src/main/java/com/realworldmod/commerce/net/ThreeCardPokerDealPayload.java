package com.realworldmod.commerce.net;

import com.realworldmod.RealWorldMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Client -> server: "deal me a new Three Card Poker round" at {@code anteCents} (the table screen's Deal button), clamped server-side to {@code BetSizing}'s range. */
public record ThreeCardPokerDealPayload(long anteCents) implements CustomPayload {
    public static final CustomPayload.Id<ThreeCardPokerDealPayload> ID =
            new CustomPayload.Id<>(Identifier.of(RealWorldMod.MOD_ID, "poker_deal"));
    public static final PacketCodec<RegistryByteBuf, ThreeCardPokerDealPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_LONG, ThreeCardPokerDealPayload::anteCents,
            ThreeCardPokerDealPayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
