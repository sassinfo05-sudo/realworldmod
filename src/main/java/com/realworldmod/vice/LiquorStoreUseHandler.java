package com.realworldmod.vice;

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
 * Right-clicking a {@code LIQUOR_STORE} with an empty hand buys one
 * {@code ALCOHOL}; sneak-right-click buys one {@code CIGARETTE} instead —
 * the same withdraw-or-refuse pattern {@code medical.PharmacyUseHandler}
 * already established, remitting the same municipal sales-tax cut.
 */
public final class LiquorStoreUseHandler {
    public static final long ALCOHOL_PRICE_CENTS = 800;
    public static final long CIGARETTE_PRICE_CENTS = 500;

    private final BankService bankService;

    public LiquorStoreUseHandler(BankService bankService) {
        this.bankService = bankService;
    }

    public void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient || hand != Hand.MAIN_HAND) {
                return ActionResult.PASS;
            }
            if (!world.getBlockState(hitResult.getBlockPos()).isOf(ModBlocks.LIQUOR_STORE)) {
                return ActionResult.PASS;
            }
            if (!player.getStackInHand(hand).isEmpty()) {
                return ActionResult.PASS;
            }

            return player.isSneaking() ? buyCigarette(player) : buyAlcohol(player);
        });
    }

    private ActionResult buyAlcohol(PlayerEntity player) {
        if (bankService.withdraw(player.getUuid(), ALCOHOL_PRICE_CENTS).isEmpty()) {
            player.sendMessage(Text.translatable("message.realworldmod.alcohol_cannot_afford",
                    CurrencyFormatter.format(ALCOHOL_PRICE_CENTS)), true);
            return ActionResult.FAIL;
        }
        bankService.remitSalesTax(ALCOHOL_PRICE_CENTS);
        giveAndAnnounce(player, ModItems.ALCOHOL, ALCOHOL_PRICE_CENTS, "message.realworldmod.alcohol_purchased");
        return ActionResult.SUCCESS;
    }

    private ActionResult buyCigarette(PlayerEntity player) {
        if (bankService.withdraw(player.getUuid(), CIGARETTE_PRICE_CENTS).isEmpty()) {
            player.sendMessage(Text.translatable("message.realworldmod.cigarette_cannot_afford",
                    CurrencyFormatter.format(CIGARETTE_PRICE_CENTS)), true);
            return ActionResult.FAIL;
        }
        bankService.remitSalesTax(CIGARETTE_PRICE_CENTS);
        giveAndAnnounce(player, ModItems.CIGARETTE, CIGARETTE_PRICE_CENTS, "message.realworldmod.cigarette_purchased");
        return ActionResult.SUCCESS;
    }

    private void giveAndAnnounce(PlayerEntity player, Item item, long priceCents, String messageKey) {
        player.giveItemStack(new ItemStack(item));
        player.sendMessage(Text.translatable(messageKey, CurrencyFormatter.format(priceCents)), true);
    }
}
