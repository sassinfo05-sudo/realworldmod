package com.realworldmod.economy.net;

import com.realworldmod.RealWorldMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Client -> server: "what's my current bank balance?" (sent when the Banking app opens). */
public record BankBalanceRequestPayload() implements CustomPayload {
    public static final CustomPayload.Id<BankBalanceRequestPayload> ID =
            new CustomPayload.Id<>(Identifier.of(RealWorldMod.MOD_ID, "bank_balance_request"));
    public static final PacketCodec<RegistryByteBuf, BankBalanceRequestPayload> CODEC =
            PacketCodec.unit(new BankBalanceRequestPayload());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
