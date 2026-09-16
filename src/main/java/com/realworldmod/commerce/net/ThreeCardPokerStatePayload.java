package com.realworldmod.commerce.net;

import com.realworldmod.RealWorldMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Client -> server: "what's my current Three Card Poker state?" (sent when the table screen opens). */
public record ThreeCardPokerStatePayload() implements CustomPayload {
    public static final CustomPayload.Id<ThreeCardPokerStatePayload> ID =
            new CustomPayload.Id<>(Identifier.of(RealWorldMod.MOD_ID, "poker_state_request"));
    public static final PacketCodec<RegistryByteBuf, ThreeCardPokerStatePayload> CODEC =
            PacketCodec.unit(new ThreeCardPokerStatePayload());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
