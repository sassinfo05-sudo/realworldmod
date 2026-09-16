package com.realworldmod.utilities;

import com.realworldmod.property.ClaimRegistry;
import com.realworldmod.property.PropertyAccess;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/**
 * A sink/outlet block whose flowing state actually reflects water billing
 * (Section 9: "no sinks that dry up") — the water counterpart to
 * {@link UtilityLampBlock}, self-scheduling a periodic check of whichever
 * claim it sits in via {@link ClaimRegistry} and looking up that claim
 * owner's connection status via {@link WaterAccess}. Unclaimed land is
 * always treated as connected, matching the rest of the property system's
 * "unclaimed is unrestricted" convention.
 */
public final class WaterOutletBlock extends Block {
    public static final BooleanProperty FLOWING = BooleanProperty.of("flowing");
    private static final int CHECK_INTERVAL_TICKS = 40;

    public WaterOutletBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(this.getStateManager().getDefaultState().with(FLOWING, true));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FLOWING);
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
        boolean shouldFlow = isConnected(pos);
        if (state.get(FLOWING) != shouldFlow) {
            world.setBlockState(pos, state.with(FLOWING, shouldFlow), Block.NOTIFY_ALL);
        }
        world.scheduleBlockTick(pos, this, CHECK_INTERVAL_TICKS);
    }

    private boolean isConnected(BlockPos pos) {
        ClaimRegistry registry = PropertyAccess.get();
        WaterService waterService = WaterAccess.get();
        if (registry == null || waterService == null) {
            return true;
        }
        return registry.findClaimAt(pos.getX(), pos.getZ())
                .map(claim -> waterService.getState(claim.ownerId()).connected())
                .orElse(true);
    }
}
