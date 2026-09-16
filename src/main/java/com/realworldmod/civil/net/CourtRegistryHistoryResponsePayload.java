package com.realworldmod.civil.net;

import com.realworldmod.RealWorldMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Server -> client: how many resolved civil cases the requesting player
 * has been a party to ({@code totalCount}), and up to
 * {@code CourtRegistryNetworking.MAX_HISTORY_ENTRIES} of the most
 * recently resolved ones, each carrying the opponent's resolved display
 * name, the claim amount, and whether it was contested rather than a
 * default judgment. As of slice 70, this is a real bounded list instead
 * of just the single most recent case.
 */
public record CourtRegistryHistoryResponsePayload(
        int totalCount,
        List<HistoryEntry> entries
) implements CustomPayload {
    public record HistoryEntry(String opponentName, long amountCents, boolean contested) {
        public static final PacketCodec<RegistryByteBuf, HistoryEntry> CODEC = PacketCodec.tuple(
                PacketCodecs.STRING, HistoryEntry::opponentName,
                PacketCodecs.VAR_LONG, HistoryEntry::amountCents,
                PacketCodecs.BOOL, HistoryEntry::contested,
                HistoryEntry::new);
    }

    public static final CustomPayload.Id<CourtRegistryHistoryResponsePayload> ID =
            new CustomPayload.Id<>(Identifier.of(RealWorldMod.MOD_ID, "court_registry_history_response"));
    public static final PacketCodec<RegistryByteBuf, CourtRegistryHistoryResponsePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, CourtRegistryHistoryResponsePayload::totalCount,
            PacketCodecs.collection(ArrayList::new, HistoryEntry.CODEC), CourtRegistryHistoryResponsePayload::entries,
            CourtRegistryHistoryResponsePayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
