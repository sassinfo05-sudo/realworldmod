package com.realworldmod.utilities.net;

import com.realworldmod.RealWorldMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Client -> server: "what's my current water hookup status?" — the water counterpart to {@link UtilityStatusRequestPayload}. */
public record WaterStatusRequestPayload() implements CustomPayload {
    public static final CustomPayload.Id<WaterStatusRequestPayload> ID =
            new CustomPayload.Id<>(Identifier.of(RealWorldMod.MOD_ID, "water_status_request"));
    public static final PacketCodec<RegistryByteBuf, WaterStatusRequestPayload> CODEC =
            PacketCodec.unit(new WaterStatusRequestPayload());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
