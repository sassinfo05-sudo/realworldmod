package com.realworldmod.commerce.net;

import com.realworldmod.RealWorldMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Client -> server: "start a new craps round" at {@code betCents} (the table screen's Bet/Start button), clamped server-side to {@code BetSizing}'s range. */
public record CrapsStartPayload(long betCents) implements CustomPayload {
    public static final CustomPayload.Id<CrapsStartPayload> ID =
            new CustomPayload.Id<>(Identifier.of(RealWorldMod.MOD_ID, "craps_start"));
    public static final PacketCodec<RegistryByteBuf, CrapsStartPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_LONG, CrapsStartPayload::betCents,
            CrapsStartPayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
