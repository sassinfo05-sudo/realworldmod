package com.realworldmod.client.phone;

import com.realworldmod.client.utilities.ClientUtilityState;
import com.realworldmod.economy.CurrencyFormatter;
import com.realworldmod.utilities.net.PayUtilityBillPayload;
import com.realworldmod.utilities.net.UtilityStatusRequestPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

/** Fifth PhoneApp: shows power hookup status and lets the player pay off any unpaid balance (Section 9 MVP). */
public final class UtilitiesAppScreen extends Screen {
    @SuppressWarnings("unused")
    private final ItemStack phoneStack;

    public UtilitiesAppScreen(ItemStack phoneStack) {
        super(Text.translatable("gui.realworldmod.phone.app.utilities"));
        this.phoneStack = phoneStack;
    }

    @Override
    protected void init() {
        ClientPlayNetworking.send(new UtilityStatusRequestPayload());

        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("gui.realworldmod.phone.utilities.pay_now"),
                        button -> ClientPlayNetworking.send(new PayUtilityBillPayload()))
                .dimensions(this.width / 2 - 60, this.height / 2 + 40, 120, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.translatable("gui.realworldmod.phone.app.utilities"),
                this.width / 2, this.height / 2 - 40, 0xFFFFFF);

        ClientUtilityState.Status status = ClientUtilityState.get();
        if (status == null) {
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.translatable("gui.realworldmod.phone.banking.loading"),
                    this.width / 2, this.height / 2 - 10, 0xAAAAAA);
            return;
        }

        Text statusText = Text.translatable(status.powerConnected()
                ? "gui.realworldmod.phone.utilities.connected"
                : "gui.realworldmod.phone.utilities.disconnected");
        context.drawCenteredTextWithShadow(this.textRenderer, statusText,
                this.width / 2, this.height / 2 - 10, status.powerConnected() ? 0x55FF55 : 0xFF5555);

        if (status.unpaidCents() > 0) {
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.translatable("gui.realworldmod.phone.utilities.owed",
                            CurrencyFormatter.format(status.unpaidCents())),
                    this.width / 2, this.height / 2 + 10, 0xFFFFFF);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
