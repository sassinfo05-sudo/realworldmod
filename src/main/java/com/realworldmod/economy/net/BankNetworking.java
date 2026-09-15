package com.realworldmod.economy.net;

import com.realworldmod.economy.BankService;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * Registers the balance-request/response payload types (both sides need to
 * agree on these even though only the server ever handles the request) and
 * the server-side handler that answers a request from the bank ledger.
 */
public final class BankNetworking {
    private BankNetworking() {
    }

    public static void registerPayloadTypes() {
        PayloadTypeRegistry.playC2S().register(BankBalanceRequestPayload.ID, BankBalanceRequestPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(BankBalanceResponsePayload.ID, BankBalanceResponsePayload.CODEC);
    }

    public static void registerServerReceiver(BankService bankService) {
        ServerPlayNetworking.registerGlobalReceiver(BankBalanceRequestPayload.ID, (payload, context) -> {
            long balance = bankService.getBalance(context.player().getUuid());
            context.responseSender().sendPacket(new BankBalanceResponsePayload(balance));
        });
    }
}
