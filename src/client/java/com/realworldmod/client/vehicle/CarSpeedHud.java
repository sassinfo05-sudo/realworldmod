package com.realworldmod.client.vehicle;

import com.realworldmod.vehicle.CarEntity;
import com.realworldmod.vehicle.VehicleSpeedDisplay;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

/**
 * The other half of closing "no speed HUD" (Section 4): a persistent HUD
 * element showing the rider's current speed, replacing what would
 * otherwise require a chat message like {@code CarEntity}'s own fuel
 * gauge readout. Renders only while the player is actually riding a
 * {@link CarEntity} — otherwise draws nothing.
 */
public final class CarSpeedHud {
    private static final int MARGIN = 10;

    private CarSpeedHud() {
    }

    public static void register() {
        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || !(client.player.getVehicle() instanceof CarEntity car)) {
                return;
            }

            int mph = (int) Math.round(VehicleSpeedDisplay.milesPerHour(car.vehicleState().speedBlocksPerTick()));
            Text text = Text.translatable("hud.realworldmod.car_speed", mph);
            int y = client.getWindow().getScaledHeight() - MARGIN - client.textRenderer.fontHeight;
            context.drawTextWithShadow(client.textRenderer, text, MARGIN, y, 0xFFFFFF);
        });
    }
}
