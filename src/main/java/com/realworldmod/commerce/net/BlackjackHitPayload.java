package com.realworldmod.commerce.net;

import com.realworldmod.RealWorldMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Client -> server: "hit" on the player's in-progress blackjack round. */
public record BlackjackHitPayload() implements CustomPayload {
    public static final CustomPayload.Id<BlackjackHitPayload> ID =
            new CustomPayload.Id<>(Identifier.of(RealWorldMod.MOD_ID, "blackjack_hit"));
    public static final PacketCodec<RegistryByteBuf, BlackjackHitPayload> CODEC =
            PacketCodec.unit(new BlackjackHitPayload());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
