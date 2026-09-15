package com.realworldmod.client.phone;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

/** Placeholder Messages app — establishes where NPC/player messaging plugs in later. */
public final class MessagesAppScreen extends Screen {
    @SuppressWarnings("unused")
    private final ItemStack phoneStack;

    public MessagesAppScreen(ItemStack phoneStack) {
        super(Text.translatable("gui.realworldmod.phone.app.messages"));
        this.phoneStack = phoneStack;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.translatable("gui.realworldmod.phone.messages.empty"),
                this.width / 2, this.height / 2, 0xAAAAAA);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
