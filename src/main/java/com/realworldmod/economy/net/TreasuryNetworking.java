package com.realworldmod.economy.net;

import com.realworldmod.economy.BankService;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * Registers the treasury-balance-request/response payload types and the
 * server-side handler that answers a request from
 * {@link BankService#TREASURY_ACCOUNT_ID} — the government account
 * {@code economy.SalesTax}/{@code IncomeTax}/{@code property.
 * PropertyTaxService} all feed. Mirrors {@link BankNetworking} exactly,
 * just against the treasury account instead of the requesting player's
 * own.
 */
public final class TreasuryNetworking {
    private TreasuryNetworking() {
    }

    public static void registerPayloadTypes() {
        PayloadTypeRegistry.playC2S().register(TreasuryBalanceRequestPayload.ID, TreasuryBalanceRequestPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(TreasuryBalanceResponsePayload.ID, TreasuryBalanceResponsePayload.CODEC);
    }

    public static void registerServerReceiver(BankService bankService) {
        ServerPlayNetworking.registerGlobalReceiver(TreasuryBalanceRequestPayload.ID, (payload, context) -> {
            long balance = bankService.getBalance(BankService.TREASURY_ACCOUNT_ID);
            context.responseSender().sendPacket(new TreasuryBalanceResponsePayload(balance));
        });
    }
}
