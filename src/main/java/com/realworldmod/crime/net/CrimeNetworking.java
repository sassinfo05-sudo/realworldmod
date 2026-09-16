package com.realworldmod.crime.net;

import com.realworldmod.crime.CrimeService;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/** Registers the wanted-level request/response payload types and the server-side answer. */
public final class CrimeNetworking {
    private CrimeNetworking() {
    }

    public static void registerPayloadTypes() {
        PayloadTypeRegistry.playC2S().register(WantedLevelRequestPayload.ID, WantedLevelRequestPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(WantedLevelResponsePayload.ID, WantedLevelResponsePayload.CODEC);
    }

    public static void registerServerReceiver(CrimeService crimeService) {
        ServerPlayNetworking.registerGlobalReceiver(WantedLevelRequestPayload.ID, (payload, context) -> {
            int level = crimeService.getWantedLevel(context.player().getUuid());
            context.responseSender().sendPacket(new WantedLevelResponsePayload(level));
        });
    }
}
