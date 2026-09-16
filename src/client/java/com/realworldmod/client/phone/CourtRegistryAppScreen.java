package com.realworldmod.client.phone;

import com.realworldmod.civil.net.CourtRegistryStatusRequestPayload;
import com.realworldmod.client.civil.ClientCourtRegistryState;
import com.realworldmod.economy.CurrencyFormatter;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

/**
 * Seventh PhoneApp: shows whether the player has a pending civil case
 * against them, the claimed amount, and how long they have left to
 * contest it before a default judgment (see {@link com.realworldmod.civil.CivilCourtService}).
 * Read-only for now — contesting still requires the in-world court flow.
 */
public final class CourtRegistryAppScreen extends Screen {
    @SuppressWarnings("unused")
    private final ItemStack phoneStack;

    public CourtRegistryAppScreen(ItemStack phoneStack) {
        super(Text.translatable("gui.realworldmod.phone.app.court_registry"));
        this.phoneStack = phoneStack;
    }

    @Override
    protected void init() {
        ClientPlayNetworking.send(new CourtRegistryStatusRequestPayload());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.translatable("gui.realworldmod.phone.app.court_registry"),
                this.width / 2, this.height / 2 - 20, 0xFFFFFF);

        ClientCourtRegistryState.State state = ClientCourtRegistryState.get();
        if (state == null) {
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.translatable("gui.realworldmod.phone.banking.loading"),
                    this.width / 2, this.height / 2, 0xFFFFFF);
            return;
        }

        if (!state.hasPendingCase()) {
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.translatable("gui.realworldmod.phone.court_registry.none"),
                    this.width / 2, this.height / 2, 0x55FF55);
            return;
        }

        long secondsRemaining = state.ticksRemaining() / 20L;
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.translatable("gui.realworldmod.phone.court_registry.pending",
                        CurrencyFormatter.format(state.amountCents())),
                this.width / 2, this.height / 2, 0xFF5555);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.translatable("gui.realworldmod.phone.court_registry.deadline", secondsRemaining),
                this.width / 2, this.height / 2 + 12, 0xFFFFFF);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
