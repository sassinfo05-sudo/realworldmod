package com.realworldmod.civil.net;

import com.realworldmod.RealWorldMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Client -> server: "do I have a pending civil case against me?" (sent when the Court Registry app opens). */
public record CourtRegistryStatusRequestPayload() implements CustomPayload {
    public static final CustomPayload.Id<CourtRegistryStatusRequestPayload> ID =
            new CustomPayload.Id<>(Identifier.of(RealWorldMod.MOD_ID, "court_registry_status_request"));
    public static final PacketCodec<RegistryByteBuf, CourtRegistryStatusRequestPayload> CODEC =
            PacketCodec.unit(new CourtRegistryStatusRequestPayload());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
