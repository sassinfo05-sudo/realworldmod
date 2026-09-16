package com.realworldmod.utilities.net;

import com.realworldmod.utilities.WaterService;
import com.realworldmod.utilities.WaterState;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/** Registers the water status request/response and pay-now payload types plus the server-side handlers. */
public final class WaterNetworking {
    private WaterNetworking() {
    }

    public static void registerPayloadTypes() {
        PayloadTypeRegistry.playC2S().register(WaterStatusRequestPayload.ID, WaterStatusRequestPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(WaterStatusResponsePayload.ID, WaterStatusResponsePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(PayWaterBillPayload.ID, PayWaterBillPayload.CODEC);
    }

    public static void registerServerReceivers(WaterService waterService) {
        ServerPlayNetworking.registerGlobalReceiver(WaterStatusRequestPayload.ID, (payload, context) -> {
            WaterState state = waterService.getState(context.player().getUuid());
            context.responseSender().sendPacket(
                    new WaterStatusResponsePayload(state.connected(), state.unpaidCents()));
        });

        ServerPlayNetworking.registerGlobalReceiver(PayWaterBillPayload.ID, (payload, context) -> {
            waterService.payNow(context.player().getUuid());
            WaterState state = waterService.getState(context.player().getUuid());
            context.responseSender().sendPacket(
                    new WaterStatusResponsePayload(state.connected(), state.unpaidCents()));
        });
    }
}
