package com.realworldmod.commerce.net;

import com.realworldmod.RealWorldMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Client -> server: "roll the dice" on the player's in-progress craps round. */
public record CrapsRollPayload() implements CustomPayload {
    public static final CustomPayload.Id<CrapsRollPayload> ID =
            new CustomPayload.Id<>(Identifier.of(RealWorldMod.MOD_ID, "craps_roll"));
    public static final PacketCodec<RegistryByteBuf, CrapsRollPayload> CODEC =
            PacketCodec.unit(new CrapsRollPayload());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
