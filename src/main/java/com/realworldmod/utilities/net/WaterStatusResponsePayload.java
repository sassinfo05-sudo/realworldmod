package com.realworldmod.utilities.net;

import com.realworldmod.RealWorldMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Server -> client: the requesting player's current water-connected status and any unpaid balance. */
public record WaterStatusResponsePayload(boolean connected, long unpaidCents) implements CustomPayload {
    public static final CustomPayload.Id<WaterStatusResponsePayload> ID =
            new CustomPayload.Id<>(Identifier.of(RealWorldMod.MOD_ID, "water_status_response"));
    public static final PacketCodec<RegistryByteBuf, WaterStatusResponsePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.BOOL, WaterStatusResponsePayload::connected,
            PacketCodecs.VAR_LONG, WaterStatusResponsePayload::unpaidCents,
            WaterStatusResponsePayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
