package com.realworldmod.client;

import com.realworldmod.client.commerce.BlackjackScreen;
import com.realworldmod.client.commerce.ClientBlackjackState;
import com.realworldmod.client.crime.ClientCrimeState;
import com.realworldmod.client.economy.ClientBankState;
import com.realworldmod.client.economy.ClientTreasuryState;
import com.realworldmod.client.phone.PhoneLockScreen;
import com.realworldmod.client.utilities.ClientUtilityState;
import com.realworldmod.client.crime.PoliceEntityRenderer;
import com.realworldmod.client.npc.CitizenEntityRenderer;
import com.realworldmod.client.vehicle.CarEntityRenderer;
import com.realworldmod.client.wildlife.DeerEntityRenderer;
import com.realworldmod.client.wildlife.GameWardenEntityRenderer;
import com.realworldmod.commerce.net.BlackjackStateResponsePayload;
import com.realworldmod.crime.net.WantedLevelResponsePayload;
import com.realworldmod.economy.net.BankBalanceResponsePayload;
import com.realworldmod.economy.net.TreasuryBalanceResponsePayload;
import com.realworldmod.init.ModBlocks;
import com.realworldmod.init.ModDataComponents;
import com.realworldmod.init.ModEntities;
import com.realworldmod.init.ModItems;
import com.realworldmod.phone.PhoneBattery;
import com.realworldmod.utilities.net.UtilityStatusResponsePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.ActionResult;
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
        ClientPlayNetworking.registerGlobalReceiver(TreasuryBalanceResponsePayload.ID,
                (payload, context) -> ClientTreasuryState.set(payload.balanceCents()));
        ClientPlayNetworking.registerGlobalReceiver(WantedLevelResponsePayload.ID,
                (payload, context) -> ClientCrimeState.set(payload.wantedLevel()));
        ClientPlayNetworking.registerGlobalReceiver(UtilityStatusResponsePayload.ID,
                (payload, context) -> ClientUtilityState.set(payload.powerConnected(), payload.unpaidCents()));
        ClientPlayNetworking.registerGlobalReceiver(BlackjackStateResponsePayload.ID,
                (payload, context) -> ClientBlackjackState.set(new ClientBlackjackState.State(
                        payload.hasActiveGame(), payload.playerHandOrdinals(), payload.dealerHandOrdinals(),
                        payload.resolved(), payload.outcomeOrdinal(), payload.payoutCents())));

        EntityRendererRegistry.register(ModEntities.CAR, CarEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.CITIZEN, CitizenEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.DEER, DeerEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.POLICE, PoliceEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.GAME_WARDEN, GameWardenEntityRenderer::new);

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

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClient || hand != Hand.MAIN_HAND) {
                return ActionResult.PASS;
            }
            if (!world.getBlockState(hitResult.getBlockPos()).isOf(ModBlocks.BLACKJACK_TABLE)) {
                return ActionResult.PASS;
            }
            MinecraftClient.getInstance().setScreen(new BlackjackScreen());
            return ActionResult.SUCCESS;
        });
    }
}
