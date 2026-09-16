package com.realworldmod.client.phone;

import com.realworldmod.civil.net.CourtRegistryContestPayload;
import com.realworldmod.civil.net.CourtRegistryHistoryRequestPayload;
import com.realworldmod.civil.net.CourtRegistryHistoryResponsePayload;
import com.realworldmod.civil.net.CourtRegistryStatusRequestPayload;
import com.realworldmod.client.civil.ClientCourtRegistryHistoryState;
import com.realworldmod.client.civil.ClientCourtRegistryState;
import com.realworldmod.economy.CurrencyFormatter;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

/**
 * Seventh PhoneApp: shows whether the player has a pending civil case
 * against them, the claimed amount, and how long they have left to
 * contest it before a default judgment (see {@link com.realworldmod.civil.CivilCourtService}).
 * As of slice 56, a Contest button dismisses the case directly from here,
 * instead of requiring the in-world court flow. As of slice 64, it also
 * shows a filing-history summary — how many resolved cases the player has
 * been a party to. As of slice 70, that's a real bounded list of the most
 * recent {@code CourtRegistryNetworking.MAX_HISTORY_ENTRIES} cases (not
 * just the single most recent one), each on its own line.
 */
public final class CourtRegistryAppScreen extends Screen {
    @SuppressWarnings("unused")
    private final ItemStack phoneStack;
    private ButtonWidget contestButton;

    public CourtRegistryAppScreen(ItemStack phoneStack) {
        super(Text.translatable("gui.realworldmod.phone.app.court_registry"));
        this.phoneStack = phoneStack;
    }

    @Override
    protected void init() {
        ClientPlayNetworking.send(new CourtRegistryStatusRequestPayload());
        ClientPlayNetworking.send(new CourtRegistryHistoryRequestPayload());

        this.contestButton = this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("gui.realworldmod.phone.court_registry.contest"),
                        button -> ClientPlayNetworking.send(new CourtRegistryContestPayload()))
                .dimensions(this.width / 2 - 40, this.height / 2 + 30, 80, 20)
                .build());
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
            this.contestButton.active = false;
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.translatable("gui.realworldmod.phone.banking.loading"),
                    this.width / 2, this.height / 2, 0xFFFFFF);
            return;
        }

        this.contestButton.active = state.hasPendingCase();

        if (state.hasPendingCase()) {
            long secondsRemaining = state.ticksRemaining() / 20L;
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.translatable("gui.realworldmod.phone.court_registry.pending",
                            state.plaintiffName(), CurrencyFormatter.format(state.amountCents())),
                    this.width / 2, this.height / 2, 0xFF5555);
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.translatable("gui.realworldmod.phone.court_registry.deadline", secondsRemaining),
                    this.width / 2, this.height / 2 + 12, 0xFFFFFF);
        } else {
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.translatable("gui.realworldmod.phone.court_registry.none"),
                    this.width / 2, this.height / 2, 0x55FF55);
        }

        renderHistory(context);
    }

    private static final int HISTORY_LINE_HEIGHT = 11;

    private void renderHistory(DrawContext context) {
        ClientCourtRegistryHistoryState.State history = ClientCourtRegistryHistoryState.get();
        if (history == null) {
            return;
        }

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.translatable("gui.realworldmod.phone.court_registry.history_count", history.totalCount()),
                this.width / 2, this.height / 2 + 55, 0xAAAAAA);

        int y = this.height / 2 + 55 + HISTORY_LINE_HEIGHT;
        for (CourtRegistryHistoryResponsePayload.HistoryEntry entry : history.entries()) {
            String outcomeKey = entry.contested()
                    ? "gui.realworldmod.phone.court_registry.history_recent_contested"
                    : "gui.realworldmod.phone.court_registry.history_recent_default";
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.translatable(outcomeKey, entry.opponentName(), CurrencyFormatter.format(entry.amountCents())),
                    this.width / 2, y, 0xAAAAAA);
            y += HISTORY_LINE_HEIGHT;
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
