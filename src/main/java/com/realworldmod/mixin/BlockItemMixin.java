package com.realworldmod.mixin;

import com.realworldmod.property.ClaimRegistry;
import com.realworldmod.property.PropertyAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Completes the anti-griefing pair started by {@code PropertyProtection}
 * (which covers breaking): cancels placing a block inside a claim the
 * placing player doesn't own. Fabric API has no generic "before block
 * placed" event, so this needs a mixin.
 */
@Mixin(BlockItem.class)
abstract class BlockItemMixin {
    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void realworldmod$checkClaim(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir) {
        World world = context.getWorld();
        if (world.isClient) {
            return;
        }
        PlayerEntity player = context.getPlayer();
        if (player == null) {
            return;
        }
        ClaimRegistry registry = PropertyAccess.get();
        if (registry == null) {
            return;
        }

        BlockPos pos = context.getBlockPos();
        if (!registry.canModify(player.getUuid(), pos.getX(), pos.getZ())) {
            player.sendMessage(Text.translatable("message.realworldmod.no_permit"), true);
            cir.setReturnValue(ActionResult.FAIL);
        }
    }
}
