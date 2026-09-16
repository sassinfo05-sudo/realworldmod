package com.realworldmod.client.commerce;

import com.realworldmod.commerce.Card;
import com.realworldmod.commerce.net.ThreeCardPokerDealPayload;
import com.realworldmod.commerce.net.ThreeCardPokerFoldPayload;
import com.realworldmod.commerce.net.ThreeCardPokerPlayPayload;
import com.realworldmod.commerce.net.ThreeCardPokerStatePayload;
import com.realworldmod.economy.CurrencyFormatter;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;

/**
 * The poker table's interactive screen — Deal/Fold/Play buttons over
 * whatever {@link ClientThreeCardPokerState} last reported, the same
 * multi-step-game shape {@link BlackjackScreen} established as the bar
 * for a real casino game in this mod (not a single right-click).
 */
public final class ThreeCardPokerScreen extends Screen {
    private ButtonWidget dealButton;
    private ButtonWidget foldButton;
    private ButtonWidget playButton;

    public ThreeCardPokerScreen() {
        super(Text.translatable("gui.realworldmod.poker.title"));
    }

    @Override
    protected void init() {
        ClientPlayNetworking.send(new ThreeCardPokerStatePayload());

        this.dealButton = this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("gui.realworldmod.poker.deal"),
                        button -> ClientPlayNetworking.send(new ThreeCardPokerDealPayload()))
                .dimensions(this.width / 2 - 100, this.height / 2 + 50, 60, 20)
                .build());
        this.foldButton = this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("gui.realworldmod.poker.fold"),
                        button -> ClientPlayNetworking.send(new ThreeCardPokerFoldPayload()))
                .dimensions(this.width / 2 - 30, this.height / 2 + 50, 60, 20)
                .build());
        this.playButton = this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("gui.realworldmod.poker.play"),
                        button -> ClientPlayNetworking.send(new ThreeCardPokerPlayPayload()))
                .dimensions(this.width / 2 + 40, this.height / 2 + 50, 60, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer, this.getTitle(),
                this.width / 2, this.height / 2 - 60, 0xFFFFFF);

        ClientThreeCardPokerState.State state = ClientThreeCardPokerState.get();
        boolean inProgress = state != null && state.hasActiveGame() && !state.resolved();
        this.dealButton.active = state == null || !inProgress;
        this.foldButton.active = inProgress;
        this.playButton.active = inProgress;

        if (state == null || !state.hasActiveGame()) {
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.translatable("gui.realworldmod.poker.no_game"),
                    this.width / 2, this.height / 2 - 10, 0xAAAAAA);
            return;
        }

        List<Card> playerHand = toCards(state.playerHandOrdinals());
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.translatable("gui.realworldmod.poker.player_hand", describe(playerHand)),
                this.width / 2, this.height / 2 - 30, 0xFFFFFF);

        if (state.resolved()) {
            List<Card> dealerHand = toCards(state.dealerHandOrdinals());
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.translatable("gui.realworldmod.poker.dealer_hand", describe(dealerHand)),
                    this.width / 2, this.height / 2 - 10, 0xFFFFFF);

            String outcomeKey = switch (state.outcomeOrdinal()) {
                case 0 -> "gui.realworldmod.poker.outcome.folded";
                case 1 -> "gui.realworldmod.poker.outcome.not_qualified";
                case 2 -> "gui.realworldmod.poker.outcome.win";
                case 3 -> "gui.realworldmod.poker.outcome.lose";
                default -> "gui.realworldmod.poker.outcome.push";
            };
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.translatable(outcomeKey, CurrencyFormatter.format(state.payoutCents())),
                    this.width / 2, this.height / 2 + 15, 0x55FF55);
        } else {
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.translatable("gui.realworldmod.poker.dealer_hand_hidden"),
                    this.width / 2, this.height / 2 - 10, 0xAAAAAA);
        }
    }

    private static List<Card> toCards(List<Integer> ordinals) {
        return ordinals.stream().map(Card::fromOrdinal).toList();
    }

    private static String describe(List<Card> hand) {
        return hand.stream()
                .map(card -> card.rank().name() + " of " + card.suit().name())
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
