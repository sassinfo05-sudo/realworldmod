package com.realworldmod.commerce.net;

import com.realworldmod.RealWorldMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Client -> server: "what's my current blackjack state?" (sent when the
 * table screen opens, so reopening it after closing mid-round doesn't
 * lose track of an in-progress game — the session lives in
 * {@code BlackjackService} regardless of whether the screen is open).
 */
public record BlackjackStatePayload() implements CustomPayload {
    public static final CustomPayload.Id<BlackjackStatePayload> ID =
            new CustomPayload.Id<>(Identifier.of(RealWorldMod.MOD_ID, "blackjack_state_request"));
    public static final PacketCodec<RegistryByteBuf, BlackjackStatePayload> CODEC =
            PacketCodec.unit(new BlackjackStatePayload());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
