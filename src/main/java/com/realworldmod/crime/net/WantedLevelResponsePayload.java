package com.realworldmod.crime.net;

import com.realworldmod.RealWorldMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Server -> client: the requesting player's current wanted level (0-5). */
public record WantedLevelResponsePayload(int wantedLevel) implements CustomPayload {
    public static final CustomPayload.Id<WantedLevelResponsePayload> ID =
            new CustomPayload.Id<>(Identifier.of(RealWorldMod.MOD_ID, "wanted_level_response"));
    public static final PacketCodec<RegistryByteBuf, WantedLevelResponsePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, WantedLevelResponsePayload::wantedLevel,
            WantedLevelResponsePayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
