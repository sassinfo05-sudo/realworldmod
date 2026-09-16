package com.realworldmod.client.commerce;

import com.realworldmod.commerce.BetSizing;
import com.realworldmod.commerce.BlackjackGame;
import com.realworldmod.commerce.net.BlackjackHitPayload;
import com.realworldmod.commerce.net.BlackjackStandPayload;
import com.realworldmod.commerce.net.BlackjackStartPayload;
import com.realworldmod.commerce.net.BlackjackStatePayload;
import com.realworldmod.economy.CurrencyFormatter;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;

/**
 * The blackjack table's interactive screen — Deal/Hit/Stand buttons over
 * whatever {@link ClientBlackjackState} last reported. Unlike every other
 * casino interaction in the mod (a single right-click resolves instantly),
 * this is a real multi-step game, so it needed an actual UI rather than
 * just a block-interaction message.
 */
public final class BlackjackScreen extends Screen {
    private ButtonWidget dealButton;
    private ButtonWidget hitButton;
    private ButtonWidget standButton;
    private ButtonWidget betMinusButton;
    private ButtonWidget betPlusButton;
    private long selectedBetCents = BetSizing.DEFAULT_BET_CENTS;

    public BlackjackScreen() {
        super(Text.translatable("gui.realworldmod.blackjack.title"));
    }

    @Override
    protected void init() {
        ClientPlayNetworking.send(new BlackjackStatePayload());

        this.betMinusButton = this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("gui.realworldmod.casino.bet_minus"),
                        button -> adjustBet(-BetSizing.STEP_CENTS))
                .dimensions(this.width / 2 - 100, this.height / 2 + 25, 20, 20)
                .build());
        this.betPlusButton = this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("gui.realworldmod.casino.bet_plus"),
                        button -> adjustBet(BetSizing.STEP_CENTS))
                .dimensions(this.width / 2 + 80, this.height / 2 + 25, 20, 20)
                .build());
        this.dealButton = this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("gui.realworldmod.blackjack.deal"),
                        button -> ClientPlayNetworking.send(new BlackjackStartPayload(selectedBetCents)))
                .dimensions(this.width / 2 - 100, this.height / 2 + 50, 60, 20)
                .build());
        this.hitButton = this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("gui.realworldmod.blackjack.hit"),
                        button -> ClientPlayNetworking.send(new BlackjackHitPayload()))
                .dimensions(this.width / 2 - 30, this.height / 2 + 50, 60, 20)
                .build());
        this.standButton = this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("gui.realworldmod.blackjack.stand"),
                        button -> ClientPlayNetworking.send(new BlackjackStandPayload()))
                .dimensions(this.width / 2 + 40, this.height / 2 + 50, 60, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer, this.getTitle(),
                this.width / 2, this.height / 2 - 60, 0xFFFFFF);

        ClientBlackjackState.State state = ClientBlackjackState.get();
        boolean inProgress = state != null && state.hasActiveGame() && !state.resolved();
        this.dealButton.active = state == null || !inProgress;
        this.hitButton.active = inProgress;
        this.standButton.active = inProgress;
        this.betMinusButton.active = !inProgress;
        this.betPlusButton.active = !inProgress;

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.translatable("gui.realworldmod.casino.bet_label", CurrencyFormatter.format(selectedBetCents)),
                this.width / 2, this.height / 2 + 31, 0xFFFFFF);

        if (state == null || !state.hasActiveGame()) {
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.translatable("gui.realworldmod.blackjack.no_game"),
                    this.width / 2, this.height / 2 - 10, 0xAAAAAA);
            return;
        }

        List<BlackjackGame.Rank> playerHand = toRanks(state.playerHandOrdinals());
        List<BlackjackGame.Rank> dealerHand = toRanks(state.dealerHandOrdinals());

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.translatable("gui.realworldmod.blackjack.player_hand",
                        describe(playerHand), BlackjackGame.valueOf(playerHand)),
                this.width / 2, this.height / 2 - 30, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer,
                state.resolved()
                        ? Text.translatable("gui.realworldmod.blackjack.dealer_hand",
                                describe(dealerHand), BlackjackGame.valueOf(dealerHand))
                        : Text.translatable("gui.realworldmod.blackjack.dealer_hand_hidden", describe(dealerHand)),
                this.width / 2, this.height / 2 - 10, 0xFFFFFF);

        if (state.resolved()) {
            String outcomeKey = switch (state.outcomeOrdinal()) {
                case 0 -> "gui.realworldmod.blackjack.outcome.blackjack";
                case 1 -> "gui.realworldmod.blackjack.outcome.win";
                case 2 -> "gui.realworldmod.blackjack.outcome.lose";
                default -> "gui.realworldmod.blackjack.outcome.push";
            };
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.translatable(outcomeKey, CurrencyFormatter.format(state.payoutCents())),
                    this.width / 2, this.height / 2 + 15, 0x55FF55);
        }
    }

    private void adjustBet(long deltaCents) {
        selectedBetCents = BetSizing.clamp(selectedBetCents + deltaCents);
    }

    private static List<BlackjackGame.Rank> toRanks(List<Integer> ordinals) {
        BlackjackGame.Rank[] ranks = BlackjackGame.Rank.values();
        return ordinals.stream().map(ordinal -> ranks[ordinal]).toList();
    }

    private static String describe(List<BlackjackGame.Rank> hand) {
        return hand.stream().map(Enum::name).reduce((a, b) -> a + " " + b).orElse("");
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
