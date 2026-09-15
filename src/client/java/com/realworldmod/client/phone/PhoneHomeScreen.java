package com.realworldmod.client.phone;

import com.realworldmod.phone.PhoneApp;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

/** Home screen: a grid of installed app icons (Section 3's phone "OS" surface). */
public final class PhoneHomeScreen extends Screen {
    private static final int ICON_SIZE = 70;
    private static final int ICON_GAP = 10;

    private final ItemStack phoneStack;

    public PhoneHomeScreen(ItemStack phoneStack) {
        super(Text.translatable("item.realworldmod.smartphone"));
        this.phoneStack = phoneStack;
    }

    @Override
    protected void init() {
        PhoneApp[] apps = PhoneApp.values();
        int columns = 3;
        int gridWidth = columns * ICON_SIZE + (columns - 1) * ICON_GAP;
        int startX = this.width / 2 - gridWidth / 2;
        int startY = this.height / 2 - ICON_SIZE;

        for (int i = 0; i < apps.length; i++) {
            PhoneApp app = apps[i];
            int col = i % columns;
            int row = i / columns;
            int x = startX + col * (ICON_SIZE + ICON_GAP);
            int y = startY + row * (ICON_SIZE + ICON_GAP);

            this.addDrawableChild(ButtonWidget.builder(
                            Text.literal(app.displayName()),
                            button -> this.client.setScreen(ClientPhoneApps.createScreen(app, phoneStack)))
                    .dimensions(x, y, ICON_SIZE, ICON_SIZE)
                    .build());
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
