package com.realworldmod.civil.net;

import com.realworldmod.RealWorldMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Server -> client: the requesting player's pending civil case, if any.
 * {@code ticksRemaining} is the time left to contest before a default
 * judgment; both fields are 0 when {@code hasPendingCase} is false.
 */
public record CourtRegistryStatusResponsePayload(
        boolean hasPendingCase,
        long amountCents,
        long ticksRemaining
) implements CustomPayload {
    public static final CustomPayload.Id<CourtRegistryStatusResponsePayload> ID =
            new CustomPayload.Id<>(Identifier.of(RealWorldMod.MOD_ID, "court_registry_status_response"));
    public static final PacketCodec<RegistryByteBuf, CourtRegistryStatusResponsePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.BOOL, CourtRegistryStatusResponsePayload::hasPendingCase,
            PacketCodecs.VAR_LONG, CourtRegistryStatusResponsePayload::amountCents,
            PacketCodecs.VAR_LONG, CourtRegistryStatusResponsePayload::ticksRemaining,
            CourtRegistryStatusResponsePayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
