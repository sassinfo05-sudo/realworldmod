package com.realworldmod.commerce.net;

import com.realworldmod.RealWorldMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Client -> server: "fold" on the player's in-progress Three Card Poker round. */
public record ThreeCardPokerFoldPayload() implements CustomPayload {
    public static final CustomPayload.Id<ThreeCardPokerFoldPayload> ID =
            new CustomPayload.Id<>(Identifier.of(RealWorldMod.MOD_ID, "poker_fold"));
    public static final PacketCodec<RegistryByteBuf, ThreeCardPokerFoldPayload> CODEC =
            PacketCodec.unit(new ThreeCardPokerFoldPayload());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
