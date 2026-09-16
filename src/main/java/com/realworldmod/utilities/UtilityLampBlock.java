package com.realworldmod.utilities;

import com.realworldmod.property.ClaimRegistry;
import com.realworldmod.property.PropertyAccess;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.AbstractBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/**
 * A light source whose lit state actually reflects utility billing
 * (Section 9: "lights shutting off"): it self-schedules a periodic check
 * of whichever claim it sits in, via {@link ClaimRegistry}, and looks up
 * that claim owner's connection status via {@link UtilityAccess}.
 * Unclaimed land is always treated as powered, matching the rest of the
 * property system's "unclaimed is unrestricted" convention.
 */
public final class UtilityLampBlock extends Block {
    public static final BooleanProperty LIT = Properties.LIT;
    private static final int CHECK_INTERVAL_TICKS = 40;

    public UtilityLampBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(this.getStateManager().getDefaultState().with(LIT, true));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient) {
            world.scheduleBlockTick(pos, this, CHECK_INTERVAL_TICKS);
        }
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        boolean shouldBeLit = isPowered(pos);
        if (state.get(LIT) != shouldBeLit) {
            world.setBlockState(pos, state.with(LIT, shouldBeLit), Block.NOTIFY_ALL);
        }
        world.scheduleBlockTick(pos, this, CHECK_INTERVAL_TICKS);
    }

    private boolean isPowered(BlockPos pos) {
        ClaimRegistry registry = PropertyAccess.get();
        UtilityService utilityService = UtilityAccess.get();
        if (registry == null || utilityService == null) {
            return true;
        }
        return registry.findClaimAt(pos.getX(), pos.getZ())
                .map(claim -> utilityService.getState(claim.ownerId()).powerConnected())
                .orElse(true);
    }
}
