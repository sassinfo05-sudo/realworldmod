package com.realworldmod.economy.net;

import com.realworldmod.RealWorldMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Server -> client: the government treasury's current balance, in cents. */
public record TreasuryBalanceResponsePayload(long balanceCents) implements CustomPayload {
    public static final CustomPayload.Id<TreasuryBalanceResponsePayload> ID =
            new CustomPayload.Id<>(Identifier.of(RealWorldMod.MOD_ID, "treasury_balance_response"));
    public static final PacketCodec<RegistryByteBuf, TreasuryBalanceResponsePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_LONG, TreasuryBalanceResponsePayload::balanceCents,
            TreasuryBalanceResponsePayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
