package com.realworldmod.commerce.net;

import com.realworldmod.RealWorldMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Client -> server: "deal me a new blackjack round" (the table screen's Deal button). */
public record BlackjackStartPayload() implements CustomPayload {
    public static final CustomPayload.Id<BlackjackStartPayload> ID =
            new CustomPayload.Id<>(Identifier.of(RealWorldMod.MOD_ID, "blackjack_start"));
    public static final PacketCodec<RegistryByteBuf, BlackjackStartPayload> CODEC =
            PacketCodec.unit(new BlackjackStartPayload());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
