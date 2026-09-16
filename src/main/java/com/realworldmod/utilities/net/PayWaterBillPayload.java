package com.realworldmod.utilities.net;

import com.realworldmod.RealWorldMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Client -> server: "pay off my water debt now" (the phone app's water Pay Now button). */
public record PayWaterBillPayload() implements CustomPayload {
    public static final CustomPayload.Id<PayWaterBillPayload> ID =
            new CustomPayload.Id<>(Identifier.of(RealWorldMod.MOD_ID, "pay_water_bill"));
    public static final PacketCodec<RegistryByteBuf, PayWaterBillPayload> CODEC =
            PacketCodec.unit(new PayWaterBillPayload());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
