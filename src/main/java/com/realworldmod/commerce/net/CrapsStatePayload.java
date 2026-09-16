package com.realworldmod.commerce.net;

import com.realworldmod.RealWorldMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Client -> server: "what's my current craps state?" (sent when the table screen opens). */
public record CrapsStatePayload() implements CustomPayload {
    public static final CustomPayload.Id<CrapsStatePayload> ID =
            new CustomPayload.Id<>(Identifier.of(RealWorldMod.MOD_ID, "craps_state_request"));
    public static final PacketCodec<RegistryByteBuf, CrapsStatePayload> CODEC =
            PacketCodec.unit(new CrapsStatePayload());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
