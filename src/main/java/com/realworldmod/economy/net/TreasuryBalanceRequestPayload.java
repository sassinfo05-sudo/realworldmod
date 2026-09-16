package com.realworldmod.economy.net;

import com.realworldmod.RealWorldMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Client -> server: "what's the government treasury's current balance?" (sent when the Government app opens). */
public record TreasuryBalanceRequestPayload() implements CustomPayload {
    public static final CustomPayload.Id<TreasuryBalanceRequestPayload> ID =
            new CustomPayload.Id<>(Identifier.of(RealWorldMod.MOD_ID, "treasury_balance_request"));
    public static final PacketCodec<RegistryByteBuf, TreasuryBalanceRequestPayload> CODEC =
            PacketCodec.unit(new TreasuryBalanceRequestPayload());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
