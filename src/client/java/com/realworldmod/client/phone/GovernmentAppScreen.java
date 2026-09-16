package com.realworldmod.client.phone;

import com.realworldmod.client.economy.ClientTreasuryState;
import com.realworldmod.client.wildlife.ClientWildlifePopulationState;
import com.realworldmod.economy.CurrencyFormatter;
import com.realworldmod.economy.net.TreasuryBalanceRequestPayload;
import com.realworldmod.wildlife.net.WildlifePopulationRequestPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

/**
 * Requests and displays the government treasury's balance — the sales,
 * income, and property tax slices (29/31) all feed this account, but
 * until now nothing let a player see it. Same request/render shape as
 * {@link BankingAppScreen}, against the treasury account instead of the
 * player's own. As of slice 66, it also shows the current deer
 * population from {@code wildlife.WildlifePopulationService} — a
 * municipal wildlife-management stat, closing "the population count
 * isn't surfaced anywhere yet" from slice 63.
 */
public final class GovernmentAppScreen extends Screen {
    @SuppressWarnings("unused")
    private final ItemStack phoneStack;

    public GovernmentAppScreen(ItemStack phoneStack) {
        super(Text.translatable("gui.realworldmod.phone.app.government"));
        this.phoneStack = phoneStack;
    }

    @Override
    protected void init() {
        ClientPlayNetworking.send(new TreasuryBalanceRequestPayload());
        ClientPlayNetworking.send(new WildlifePopulationRequestPayload());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.translatable("gui.realworldmod.phone.government.title"),
                this.width / 2, this.height / 2 - 20, 0xFFFFFF);

        Long balance = ClientTreasuryState.get();
        Text balanceText = balance != null
                ? Text.literal(CurrencyFormatter.format(balance))
                : Text.translatable("gui.realworldmod.phone.government.loading");
        context.drawCenteredTextWithShadow(this.textRenderer, balanceText,
                this.width / 2, this.height / 2, 0x55FF55);

        Integer deerPopulation = ClientWildlifePopulationState.get();
        Text populationText = deerPopulation != null
                ? Text.translatable("gui.realworldmod.phone.government.deer_population", deerPopulation)
                : Text.translatable("gui.realworldmod.phone.government.loading");
        context.drawCenteredTextWithShadow(this.textRenderer, populationText,
                this.width / 2, this.height / 2 + 14, 0xAAFFAA);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
