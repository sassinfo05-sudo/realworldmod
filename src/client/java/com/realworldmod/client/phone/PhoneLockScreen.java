package com.realworldmod.client.phone;

import com.realworldmod.init.ModDataComponents;
import com.realworldmod.phone.PhoneBattery;
import com.realworldmod.phone.TimeOfDayFormatter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

/** First screen shown when a smartphone is used: clock, battery, unlock button. */
public final class PhoneLockScreen extends Screen {
    private final ItemStack phoneStack;

    public PhoneLockScreen(ItemStack phoneStack) {
        super(Text.translatable("item.realworldmod.smartphone"));
        this.phoneStack = phoneStack;
    }

    @Override
    protected void init() {
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("gui.realworldmod.phone.unlock"),
                        button -> this.client.setScreen(new PhoneHomeScreen(phoneStack)))
                .dimensions(this.width / 2 - 50, this.height / 2 + 40, 100, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        long timeOfDay = this.client.world != null ? this.client.world.getTimeOfDay() : 0L;
        int battery = phoneStack.getOrDefault(ModDataComponents.PHONE_BATTERY, PhoneBattery.MAX_LEVEL);

        context.drawCenteredTextWithShadow(this.textRenderer, TimeOfDayFormatter.format(timeOfDay),
                this.width / 2, this.height / 2 - 40, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer,
                String.format("Battery: %d%%", battery),
                this.width / 2, this.height / 2 - 20, 0xAAAAAA);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
