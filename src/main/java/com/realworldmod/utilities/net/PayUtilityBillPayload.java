package com.realworldmod.utilities.net;

import com.realworldmod.RealWorldMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Client -> server: "pay off my utility debt now" (the phone app's Pay Now button). */
public record PayUtilityBillPayload() implements CustomPayload {
    public static final CustomPayload.Id<PayUtilityBillPayload> ID =
            new CustomPayload.Id<>(Identifier.of(RealWorldMod.MOD_ID, "pay_utility_bill"));
    public static final PacketCodec<RegistryByteBuf, PayUtilityBillPayload> CODEC =
            PacketCodec.unit(new PayUtilityBillPayload());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
