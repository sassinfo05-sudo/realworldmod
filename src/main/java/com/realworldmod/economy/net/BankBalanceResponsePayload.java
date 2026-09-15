package com.realworldmod.economy.net;

import com.realworldmod.RealWorldMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Server -> client: the requesting player's current bank balance, in cents. */
public record BankBalanceResponsePayload(long balanceCents) implements CustomPayload {
    public static final CustomPayload.Id<BankBalanceResponsePayload> ID =
            new CustomPayload.Id<>(Identifier.of(RealWorldMod.MOD_ID, "bank_balance_response"));
    public static final PacketCodec<RegistryByteBuf, BankBalanceResponsePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_LONG, BankBalanceResponsePayload::balanceCents,
            BankBalanceResponsePayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
