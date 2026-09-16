package com.realworldmod.civil.net;

import com.realworldmod.RealWorldMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Client -> server: "dismiss my pending civil case" (the Court Registry app's Contest button). */
public record CourtRegistryContestPayload() implements CustomPayload {
    public static final CustomPayload.Id<CourtRegistryContestPayload> ID =
            new CustomPayload.Id<>(Identifier.of(RealWorldMod.MOD_ID, "court_registry_contest"));
    public static final PacketCodec<RegistryByteBuf, CourtRegistryContestPayload> CODEC =
            PacketCodec.unit(new CourtRegistryContestPayload());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
