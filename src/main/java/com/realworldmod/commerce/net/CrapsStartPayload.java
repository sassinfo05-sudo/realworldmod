package com.realworldmod.commerce.net;

import com.realworldmod.RealWorldMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Client -> server: "start a new craps round" (the table screen's Bet/Start button). */
public record CrapsStartPayload() implements CustomPayload {
    public static final CustomPayload.Id<CrapsStartPayload> ID =
            new CustomPayload.Id<>(Identifier.of(RealWorldMod.MOD_ID, "craps_start"));
    public static final PacketCodec<RegistryByteBuf, CrapsStartPayload> CODEC =
            PacketCodec.unit(new CrapsStartPayload());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
