package com.realworldmod.commerce.net;

import com.realworldmod.RealWorldMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Client -> server: "play" (match the ante with a play bet) on the player's in-progress Three Card Poker round. */
public record ThreeCardPokerPlayPayload() implements CustomPayload {
    public static final CustomPayload.Id<ThreeCardPokerPlayPayload> ID =
            new CustomPayload.Id<>(Identifier.of(RealWorldMod.MOD_ID, "poker_play"));
    public static final PacketCodec<RegistryByteBuf, ThreeCardPokerPlayPayload> CODEC =
            PacketCodec.unit(new ThreeCardPokerPlayPayload());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
