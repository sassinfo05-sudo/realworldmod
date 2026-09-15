package com.realworldmod.client;

import com.realworldmod.client.crime.ClientCrimeState;
import com.realworldmod.client.economy.ClientBankState;
import com.realworldmod.client.phone.PhoneLockScreen;
import com.realworldmod.crime.net.WantedLevelResponsePayload;
import com.realworldmod.economy.net.BankBalanceResponsePayload;
import com.realworldmod.init.ModDataComponents;
import com.realworldmod.init.ModItems;
import com.realworldmod.phone.PhoneBattery;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;

/**
 * Client-only entry point. Registers the purely visual half of smartphone
 * interaction (opening the lock screen); the shared battery-drain logic
 * lives in {@code PhoneUseHandler} so it also runs on a dedicated server.
 */
public final class RealWorldModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(BankBalanceResponsePayload.ID,
                (payload, context) -> ClientBankState.set(payload.balanceCents()));
        ClientPlayNetworking.registerGlobalReceiver(WantedLevelResponsePayload.ID,
                (payload, context) -> ClientCrimeState.set(payload.wantedLevel()));

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!world.isClient || hand != Hand.MAIN_HAND) {
                return TypedActionResult.pass(player.getStackInHand(hand));
            }
            var stack = player.getStackInHand(hand);
            if (!stack.isOf(ModItems.SMARTPHONE)) {
                return TypedActionResult.pass(stack);
            }

            int battery = stack.getOrDefault(ModDataComponents.PHONE_BATTERY, PhoneBattery.MAX_LEVEL);
            if (PhoneBattery.isDead(battery)) {
                return TypedActionResult.pass(stack);
            }

            MinecraftClient.getInstance().setScreen(new PhoneLockScreen(stack));
            return TypedActionResult.pass(stack);
        });
    }
}
