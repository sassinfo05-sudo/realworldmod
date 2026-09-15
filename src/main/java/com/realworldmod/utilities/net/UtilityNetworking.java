package com.realworldmod.utilities.net;

import com.realworldmod.utilities.UtilityService;
import com.realworldmod.utilities.UtilityState;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/** Registers the utility status request/response and pay-now payload types plus the server-side handlers. */
public final class UtilityNetworking {
    private UtilityNetworking() {
    }

    public static void registerPayloadTypes() {
        PayloadTypeRegistry.playC2S().register(UtilityStatusRequestPayload.ID, UtilityStatusRequestPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(UtilityStatusResponsePayload.ID, UtilityStatusResponsePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(PayUtilityBillPayload.ID, PayUtilityBillPayload.CODEC);
    }

    public static void registerServerReceivers(UtilityService utilityService) {
        ServerPlayNetworking.registerGlobalReceiver(UtilityStatusRequestPayload.ID, (payload, context) -> {
            UtilityState state = utilityService.getState(context.player().getUuid());
            context.responseSender().sendPacket(
                    new UtilityStatusResponsePayload(state.powerConnected(), state.unpaidCents()));
        });

        ServerPlayNetworking.registerGlobalReceiver(PayUtilityBillPayload.ID, (payload, context) -> {
            utilityService.payNow(context.player().getUuid());
            UtilityState state = utilityService.getState(context.player().getUuid());
            context.responseSender().sendPacket(
                    new UtilityStatusResponsePayload(state.powerConnected(), state.unpaidCents()));
        });
    }
}
