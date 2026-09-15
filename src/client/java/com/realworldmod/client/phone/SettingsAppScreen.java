package com.realworldmod.client.phone;

import com.realworldmod.init.ModDataComponents;
import com.realworldmod.phone.PhoneBattery;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

/** Stub Settings app: shows battery level and lets the player back out. */
public final class SettingsAppScreen extends Screen {
    private final ItemStack phoneStack;

    public SettingsAppScreen(ItemStack phoneStack) {
        super(Text.translatable("gui.realworldmod.phone.app.settings"));
        this.phoneStack = phoneStack;
    }

    @Override
    protected void init() {
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("gui.realworldmod.phone.settings.power_off"),
                        button -> this.close())
                .dimensions(this.width / 2 - 60, this.height / 2 + 40, 120, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        int battery = phoneStack.getOrDefault(ModDataComponents.PHONE_BATTERY, PhoneBattery.MAX_LEVEL);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.translatable("gui.realworldmod.phone.settings.battery", battery),
                this.width / 2, this.height / 2 - 10, 0xFFFFFF);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
