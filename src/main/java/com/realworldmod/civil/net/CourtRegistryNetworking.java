package com.realworldmod.civil.net;

import com.realworldmod.civil.CivilCourtService;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/** Registers the court-registry status request/response payload types and the server-side answer. */
public final class CourtRegistryNetworking {
    private CourtRegistryNetworking() {
    }

    public static void registerPayloadTypes() {
        PayloadTypeRegistry.playC2S().register(CourtRegistryStatusRequestPayload.ID, CourtRegistryStatusRequestPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(CourtRegistryStatusResponsePayload.ID, CourtRegistryStatusResponsePayload.CODEC);
    }

    public static void registerServerReceiver(CivilCourtService civilCourtService) {
        ServerPlayNetworking.registerGlobalReceiver(CourtRegistryStatusRequestPayload.ID, (payload, context) -> {
            long currentTick = context.player().getWorld().getTime();
            civilCourtService.getCase(context.player().getUuid()).ifPresentOrElse(
                    civilCase -> context.responseSender().sendPacket(new CourtRegistryStatusResponsePayload(
                            true, civilCase.amountCents(), Math.max(0, civilCase.deadlineTick() - currentTick))),
                    () -> context.responseSender().sendPacket(new CourtRegistryStatusResponsePayload(false, 0L, 0L)));
        });
    }
}
