package com.realworldmod.wildlife.net;

import com.realworldmod.RealWorldMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Client -> server: "what's the current deer population?" (sent when the Government app opens). */
public record WildlifePopulationRequestPayload() implements CustomPayload {
    public static final CustomPayload.Id<WildlifePopulationRequestPayload> ID =
            new CustomPayload.Id<>(Identifier.of(RealWorldMod.MOD_ID, "wildlife_population_request"));
    public static final PacketCodec<RegistryByteBuf, WildlifePopulationRequestPayload> CODEC =
            PacketCodec.unit(new WildlifePopulationRequestPayload());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
