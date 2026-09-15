package com.realworldmod.client.phone;

import com.realworldmod.client.crime.ClientCrimeState;
import com.realworldmod.crime.WantedLevelDescriptions;
import com.realworldmod.crime.net.WantedLevelRequestPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

/** Fourth PhoneApp: requests and displays the player's current wanted level (Section 7 MVP). */
public final class CriminalRecordAppScreen extends Screen {
    @SuppressWarnings("unused")
    private final ItemStack phoneStack;

    public CriminalRecordAppScreen(ItemStack phoneStack) {
        super(Text.translatable("gui.realworldmod.phone.app.criminal_record"));
        this.phoneStack = phoneStack;
    }

    @Override
    protected void init() {
        ClientPlayNetworking.send(new WantedLevelRequestPayload());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.translatable("gui.realworldmod.phone.app.criminal_record"),
                this.width / 2, this.height / 2 - 20, 0xFFFFFF);

        Integer level = ClientCrimeState.get();
        Text statusText = level != null
                ? Text.translatable(WantedLevelDescriptions.translationKeyFor(level), level)
                : Text.translatable("gui.realworldmod.phone.banking.loading");
        int color = level != null && level >= 3 ? 0xFF5555 : 0xFFFFFF;
        context.drawCenteredTextWithShadow(this.textRenderer, statusText, this.width / 2, this.height / 2, color);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
