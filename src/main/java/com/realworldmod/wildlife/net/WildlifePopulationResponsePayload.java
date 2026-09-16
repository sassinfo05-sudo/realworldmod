package com.realworldmod.wildlife.net;

import com.realworldmod.RealWorldMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Server -> client: the current deer population, from {@code wildlife.WildlifePopulationService}. */
public record WildlifePopulationResponsePayload(int deerPopulation) implements CustomPayload {
    public static final CustomPayload.Id<WildlifePopulationResponsePayload> ID =
            new CustomPayload.Id<>(Identifier.of(RealWorldMod.MOD_ID, "wildlife_population_response"));
    public static final PacketCodec<RegistryByteBuf, WildlifePopulationResponsePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, WildlifePopulationResponsePayload::deerPopulation,
            WildlifePopulationResponsePayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
