package com.realworldmod.property;

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
 * provided it doesn't overlap land someone else already owns.
 */
public final class DeedUseHandler {
    /** Half-width of a newly claimed plot, in blocks (a 33x33 square). */
    public static final int PLOT_RADIUS = 16;

    private final PropertyService propertyService;

    public DeedUseHandler(PropertyService propertyService) {
        this.propertyService = propertyService;
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
            var claim = propertyService.claimPlot(player.getUuid(), pos.getX(), pos.getZ(), PLOT_RADIUS);
            if (claim.isPresent()) {
                stack.decrement(1);
                player.sendMessage(Text.translatable("message.realworldmod.plot_claimed"), true);
                return ActionResult.SUCCESS;
            }

            player.sendMessage(Text.translatable("message.realworldmod.plot_already_claimed"), true);
            return ActionResult.FAIL;
        });
    }
}
