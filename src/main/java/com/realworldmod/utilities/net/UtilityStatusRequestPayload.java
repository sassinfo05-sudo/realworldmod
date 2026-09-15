package com.realworldmod.utilities.net;

import com.realworldmod.RealWorldMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Client -> server: "what's my current utility hookup status?" */
public record UtilityStatusRequestPayload() implements CustomPayload {
    public static final CustomPayload.Id<UtilityStatusRequestPayload> ID =
            new CustomPayload.Id<>(Identifier.of(RealWorldMod.MOD_ID, "utility_status_request"));
    public static final PacketCodec<RegistryByteBuf, UtilityStatusRequestPayload> CODEC =
            PacketCodec.unit(new UtilityStatusRequestPayload());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
