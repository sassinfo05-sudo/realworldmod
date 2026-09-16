package com.realworldmod.property;

import com.realworldmod.economy.BankService;
import com.realworldmod.economy.CurrencyFormatter;
import com.realworldmod.init.ModItems;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

/**
 * Lets a player turn a {@code LAND_DEED} item into an actual claim: right
 * click a block with a deed in hand to claim a square plot centered on it,
 * provided it doesn't overlap land someone else already owns and the
 * player can afford the purchase price. Remits a municipal sales-tax cut
 * of that price to the treasury (see
 * {@link com.realworldmod.economy.SalesTax}).
 */
public final class DeedUseHandler {
    /** Half-width of a newly claimed plot, in blocks (a 33x33 square). */
    public static final int PLOT_RADIUS = 16;
    public static final long PRICE_CENTS = 5000;

    private final PropertyService propertyService;
    private final BankService bankService;

    public DeedUseHandler(PropertyService propertyService, BankService bankService) {
        this.propertyService = propertyService;
        this.bankService = bankService;
    }

    public void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient || hand != Hand.MAIN_HAND) {
                return ActionResult.PASS;
            }
            ItemStack stack = player.getStackInHand(hand);
            if (!stack.isOf(ModItems.LAND_DEED)) {
                return ActionResult.PASS;
            }

            BlockPos pos = hitResult.getBlockPos();
            int minX = pos.getX() - PLOT_RADIUS;
            int maxX = pos.getX() + PLOT_RADIUS;
            int minZ = pos.getZ() - PLOT_RADIUS;
            int maxZ = pos.getZ() + PLOT_RADIUS;

            if (propertyService.registry().overlapsAny(minX, minZ, maxX, maxZ)) {
                player.sendMessage(Text.translatable("message.realworldmod.plot_already_claimed"), true);
                return ActionResult.FAIL;
            }

            if (bankService.withdraw(player.getUuid(), PRICE_CENTS).isEmpty()) {
                player.sendMessage(Text.translatable("message.realworldmod.plot_cannot_afford",
                        CurrencyFormatter.format(PRICE_CENTS)), true);
                return ActionResult.FAIL;
            }

            bankService.remitSalesTax(PRICE_CENTS);
            propertyService.claimPlot(player.getUuid(), pos.getX(), pos.getZ(), PLOT_RADIUS);
            stack.decrement(1);
            player.sendMessage(Text.translatable("message.realworldmod.plot_claimed",
                    CurrencyFormatter.format(PRICE_CENTS)), true);
            return ActionResult.SUCCESS;
        });
    }
}
