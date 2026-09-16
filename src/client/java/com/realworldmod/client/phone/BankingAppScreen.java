package com.realworldmod.client.phone;

import com.realworldmod.client.economy.ClientBankState;
import com.realworldmod.economy.CurrencyFormatter;
import com.realworldmod.economy.net.BankBalanceRequestPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

/** First real "app" backed by server state: requests and displays the player's bank balance. */
public final class BankingAppScreen extends Screen {
    @SuppressWarnings("unused")
    private final ItemStack phoneStack;

    public BankingAppScreen(ItemStack phoneStack) {
        super(Text.translatable("gui.realworldmod.phone.app.banking"));
        this.phoneStack = phoneStack;
    }

    @Override
    protected void init() {
        ClientPlayNetworking.send(new BankBalanceRequestPayload());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.translatable("gui.realworldmod.phone.banking.title"),
                this.width / 2, this.height / 2 - 20, 0xFFFFFF);

        Long balance = ClientBankState.get();
        Text balanceText = balance != null
                ? Text.literal(CurrencyFormatter.format(balance))
                : Text.translatable("gui.realworldmod.phone.banking.loading");
        context.drawCenteredTextWithShadow(this.textRenderer, balanceText,
                this.width / 2, this.height / 2, 0x55FF55);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
