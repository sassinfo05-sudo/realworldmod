package com.realworldmod.commerce.net;

import com.realworldmod.RealWorldMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Client -> server: "deal me a new Three Card Poker round" (the table screen's Deal button). */
public record ThreeCardPokerDealPayload() implements CustomPayload {
    public static final CustomPayload.Id<ThreeCardPokerDealPayload> ID =
            new CustomPayload.Id<>(Identifier.of(RealWorldMod.MOD_ID, "poker_deal"));
    public static final PacketCodec<RegistryByteBuf, ThreeCardPokerDealPayload> CODEC =
            PacketCodec.unit(new ThreeCardPokerDealPayload());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
