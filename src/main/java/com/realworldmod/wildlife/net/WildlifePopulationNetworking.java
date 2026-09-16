package com.realworldmod.wildlife.net;

import com.realworldmod.wildlife.WildlifePopulationService;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * Registers the wildlife-population-request/response payload types and the
 * server-side handler that answers a request from
 * {@link WildlifePopulationService#getDeerPopulation()} — closing "the
 * population count isn't surfaced anywhere yet," the gap slice 63's own
 * javadoc called out. Mirrors {@code economy.net.TreasuryNetworking}
 * exactly, just against the deer population count instead of a bank
 * balance.
 */
public final class WildlifePopulationNetworking {
    private WildlifePopulationNetworking() {
    }

    public static void registerPayloadTypes() {
        PayloadTypeRegistry.playC2S().register(WildlifePopulationRequestPayload.ID, WildlifePopulationRequestPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(WildlifePopulationResponsePayload.ID, WildlifePopulationResponsePayload.CODEC);
    }

    public static void registerServerReceiver(WildlifePopulationService wildlifePopulationService) {
        ServerPlayNetworking.registerGlobalReceiver(WildlifePopulationRequestPayload.ID, (payload, context) ->
                context.responseSender().sendPacket(
                        new WildlifePopulationResponsePayload(wildlifePopulationService.getDeerPopulation())));
    }
}
