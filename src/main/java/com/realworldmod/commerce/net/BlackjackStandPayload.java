package com.realworldmod.commerce.net;

import com.realworldmod.RealWorldMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Client -> server: "stand" on the player's in-progress blackjack round. */
public record BlackjackStandPayload() implements CustomPayload {
    public static final CustomPayload.Id<BlackjackStandPayload> ID =
            new CustomPayload.Id<>(Identifier.of(RealWorldMod.MOD_ID, "blackjack_stand"));
    public static final PacketCodec<RegistryByteBuf, BlackjackStandPayload> CODEC =
            PacketCodec.unit(new BlackjackStandPayload());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
