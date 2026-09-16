package com.realworldmod.medical;

import com.realworldmod.economy.BankService;
import com.realworldmod.economy.CurrencyFormatter;
import com.realworldmod.init.ModBlocks;
import com.realworldmod.init.ModItems;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

/**
 * Right-clicking a {@code PHARMACY_COUNTER} with an empty hand buys one
 * {@code MEDICINE}, if affordable — remitting a municipal sales-tax cut
 * to the treasury (see {@link com.realworldmod.economy.SalesTax}). As of
 * slice 68, sneak-right-clicking instead buys a {@code NICOTINE_PATCH}
 * (used later via {@code vice.NicotinePatchUseHandler}, the same
 * buy-then-use shape {@code MEDICINE} already has) — the same
 * empty-hand/sneak split {@code vice.LiquorStoreUseHandler} already uses
 * to sell two items from one block.
 */
public final class PharmacyUseHandler {
    public static final long MEDICINE_PRICE_CENTS = 1500;
    public static final long NICOTINE_PATCH_PRICE_CENTS = 1000;

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

            return player.isSneaking() ? buyNicotinePatch(player) : buyMedicine(player);
        });
    }

    private ActionResult buyMedicine(PlayerEntity player) {
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
    }

    private ActionResult buyNicotinePatch(PlayerEntity player) {
        if (bankService.withdraw(player.getUuid(), NICOTINE_PATCH_PRICE_CENTS).isEmpty()) {
            player.sendMessage(Text.translatable("message.realworldmod.nicotine_patch_cannot_afford",
                    CurrencyFormatter.format(NICOTINE_PATCH_PRICE_CENTS)), true);
            return ActionResult.FAIL;
        }

        bankService.remitSalesTax(NICOTINE_PATCH_PRICE_CENTS);
        giveAndAnnounce(player, ModItems.NICOTINE_PATCH, NICOTINE_PATCH_PRICE_CENTS,
                "message.realworldmod.nicotine_patch_purchased");
        return ActionResult.SUCCESS;
    }

    private void giveAndAnnounce(PlayerEntity player, Item item, long priceCents, String messageKey) {
        player.giveItemStack(new ItemStack(item));
        player.sendMessage(Text.translatable(messageKey, CurrencyFormatter.format(priceCents)), true);
    }
}
