package com.realworldmod.client.commerce;

import com.realworldmod.commerce.net.CrapsRollPayload;
import com.realworldmod.commerce.net.CrapsStartPayload;
import com.realworldmod.commerce.net.CrapsStatePayload;
import com.realworldmod.economy.CurrencyFormatter;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * The craps table's interactive screen — a Bet button to start a round
 * and a Roll button to advance it, over whatever
 * {@link ClientCrapsState} last reported. Craps needs an unknown number
 * of rolls to resolve (unlike blackjack's fixed hit/stand shape), so the
 * screen just keeps the Roll button live until the round resolves.
 */
public final class CrapsScreen extends Screen {
    private ButtonWidget betButton;
    private ButtonWidget rollButton;

    public CrapsScreen() {
        super(Text.translatable("gui.realworldmod.craps.title"));
    }

    @Override
    protected void init() {
        ClientPlayNetworking.send(new CrapsStatePayload());

        this.betButton = this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("gui.realworldmod.craps.bet"),
                        button -> ClientPlayNetworking.send(new CrapsStartPayload()))
                .dimensions(this.width / 2 - 65, this.height / 2 + 50, 60, 20)
                .build());
        this.rollButton = this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("gui.realworldmod.craps.roll"),
                        button -> ClientPlayNetworking.send(new CrapsRollPayload()))
                .dimensions(this.width / 2 + 5, this.height / 2 + 50, 60, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer, this.getTitle(),
                this.width / 2, this.height / 2 - 60, 0xFFFFFF);

        ClientCrapsState.State state = ClientCrapsState.get();
        boolean inProgress = state != null && state.hasActiveGame() && !state.resolved();
        this.betButton.active = state == null || !inProgress;
        this.rollButton.active = inProgress;

        if (state == null || !state.hasActiveGame()) {
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.translatable("gui.realworldmod.craps.no_game"),
                    this.width / 2, this.height / 2 - 10, 0xAAAAAA);
            return;
        }

        Text pointText = state.point() < 0
                ? Text.translatable("gui.realworldmod.craps.come_out")
                : Text.translatable("gui.realworldmod.craps.point", state.point());
        context.drawCenteredTextWithShadow(this.textRenderer, pointText,
                this.width / 2, this.height / 2 - 30, 0xFFFFFF);

        if (state.lastRollTotal() >= 0) {
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.translatable("gui.realworldmod.craps.last_roll", state.lastRollTotal()),
                    this.width / 2, this.height / 2 - 10, 0xFFFFFF);
        }

        if (state.resolved()) {
            String outcomeKey = state.outcomeOrdinal() == 1
                    ? "gui.realworldmod.craps.outcome.win"
                    : "gui.realworldmod.craps.outcome.lose";
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.translatable(outcomeKey, CurrencyFormatter.format(state.payoutCents())),
                    this.width / 2, this.height / 2 + 15, 0x55FF55);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
