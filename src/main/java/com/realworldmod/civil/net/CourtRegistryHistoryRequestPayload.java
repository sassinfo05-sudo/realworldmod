package com.realworldmod.civil.net;

import com.realworldmod.RealWorldMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Client -> server: "send me my civil-case filing history" (sent once, on Court Registry app open). */
public record CourtRegistryHistoryRequestPayload() implements CustomPayload {
    public static final CustomPayload.Id<CourtRegistryHistoryRequestPayload> ID =
            new CustomPayload.Id<>(Identifier.of(RealWorldMod.MOD_ID, "court_registry_history_request"));
    public static final PacketCodec<RegistryByteBuf, CourtRegistryHistoryRequestPayload> CODEC =
            PacketCodec.unit(new CourtRegistryHistoryRequestPayload());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
