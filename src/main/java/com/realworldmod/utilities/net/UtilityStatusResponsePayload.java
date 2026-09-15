package com.realworldmod.utilities.net;

import com.realworldmod.RealWorldMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Server -> client: the requesting player's current power-connected status and any unpaid balance. */
public record UtilityStatusResponsePayload(boolean powerConnected, long unpaidCents) implements CustomPayload {
    public static final CustomPayload.Id<UtilityStatusResponsePayload> ID =
            new CustomPayload.Id<>(Identifier.of(RealWorldMod.MOD_ID, "utility_status_response"));
    public static final PacketCodec<RegistryByteBuf, UtilityStatusResponsePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.BOOL, UtilityStatusResponsePayload::powerConnected,
            PacketCodecs.VAR_LONG, UtilityStatusResponsePayload::unpaidCents,
            UtilityStatusResponsePayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
