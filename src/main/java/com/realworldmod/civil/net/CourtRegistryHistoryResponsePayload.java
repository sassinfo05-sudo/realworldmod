package com.realworldmod.civil.net;

import com.realworldmod.RealWorldMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Server -> client: how many resolved civil cases the requesting player
 * has been a party to, and a summary of the most recent one (the
 * opponent's resolved display name, the claim amount, and whether it was
 * contested rather than a default judgment). {@code hasMostRecent} is
 * false, and the other {@code mostRecent*} fields are their defaults,
 * when {@code pastCaseCount} is zero.
 */
public record CourtRegistryHistoryResponsePayload(
        int pastCaseCount,
        boolean hasMostRecent,
        String mostRecentOpponentName,
        long mostRecentAmountCents,
        boolean mostRecentContested
) implements CustomPayload {
    public static final CustomPayload.Id<CourtRegistryHistoryResponsePayload> ID =
            new CustomPayload.Id<>(Identifier.of(RealWorldMod.MOD_ID, "court_registry_history_response"));
    public static final PacketCodec<RegistryByteBuf, CourtRegistryHistoryResponsePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, CourtRegistryHistoryResponsePayload::pastCaseCount,
            PacketCodecs.BOOL, CourtRegistryHistoryResponsePayload::hasMostRecent,
            PacketCodecs.STRING, CourtRegistryHistoryResponsePayload::mostRecentOpponentName,
            PacketCodecs.VAR_LONG, CourtRegistryHistoryResponsePayload::mostRecentAmountCents,
            PacketCodecs.BOOL, CourtRegistryHistoryResponsePayload::mostRecentContested,
            CourtRegistryHistoryResponsePayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
