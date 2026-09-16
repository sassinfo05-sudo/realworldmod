package com.realworldmod.medical;

import com.realworldmod.economy.BankService;
import com.realworldmod.economy.CurrencyFormatter;
import com.realworldmod.init.ModBlocks;
import com.realworldmod.init.ModItems;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

/**
 * Right-clicking a {@code PHARMACY_COUNTER} with an empty hand buys one
 * {@code MEDICINE}, if affordable — remitting a municipal sales-tax cut
 * to the treasury (see {@link com.realworldmod.economy.SalesTax}).
 */
public final class PharmacyUseHandler {
    public static final long MEDICINE_PRICE_CENTS = 1500;

    private final BankService bankService;

    public PharmacyUseHandler(BankService bankService) {
        this.bankService = bankService;
    }

    public void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient || hand != Hand.MAIN_HAND) {
                return ActionResult.PASS;
            }
            if (!world.getBlockState(hitResult.getBlockPos()).isOf(ModBlocks.PHARMACY_COUNTER)) {
                return ActionResult.PASS;
            }
            if (!player.getStackInHand(hand).isEmpty()) {
                return ActionResult.PASS;
            }

            if (bankService.withdraw(player.getUuid(), MEDICINE_PRICE_CENTS).isEmpty()) {
                player.sendMessage(Text.translatable("message.realworldmod.medicine_cannot_afford",
                        CurrencyFormatter.format(MEDICINE_PRICE_CENTS)), true);
                return ActionResult.FAIL;
            }

            bankService.remitSalesTax(MEDICINE_PRICE_CENTS);
            player.giveItemStack(new ItemStack(ModItems.MEDICINE));
            player.sendMessage(Text.translatable("message.realworldmod.medicine_purchased",
                    CurrencyFormatter.format(MEDICINE_PRICE_CENTS)), true);
            return ActionResult.SUCCESS;
        });
    }
}
