package com.realworldmod.crime.net;

import com.realworldmod.RealWorldMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Client -> server: "what's my current wanted level?" (sent when the Criminal Record app opens). */
public record WantedLevelRequestPayload() implements CustomPayload {
    public static final CustomPayload.Id<WantedLevelRequestPayload> ID =
            new CustomPayload.Id<>(Identifier.of(RealWorldMod.MOD_ID, "wanted_level_request"));
    public static final PacketCodec<RegistryByteBuf, WantedLevelRequestPayload> CODEC =
            PacketCodec.unit(new WantedLevelRequestPayload());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
