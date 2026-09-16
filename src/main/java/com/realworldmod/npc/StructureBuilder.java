package com.realworldmod.npc;

import com.realworldmod.init.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

/**
 * Places a small, functional building at a citizen's home or workplace
 * coordinate, instead of {@link CommuteGoal} walking them to a bare point
 * in the world — the known gap called out since slice 21. Deliberately
 * simple: a single rectangular room with a doorway and a flat roof, built
 * block-by-block rather than from a hand-designed structure template or
 * NBT schematic — every citizen's home/workplace looks identical, and
 * this doesn't check for terrain, water, or overlapping claims/structures
 * first (see ROADMAP.md).
 */
public final class StructureBuilder {
    private static final int RADIUS = 2;
    private static final int WALL_HEIGHT = 3;

    private StructureBuilder() {
    }

    public static void buildHouse(ServerWorld world, BlockPos origin) {
        build(world, origin, Blocks.OAK_PLANKS.getDefaultState(), Blocks.OAK_SLAB.getDefaultState());
    }

    public static void buildWorkplace(ServerWorld world, BlockPos origin) {
        build(world, origin, Blocks.STONE_BRICKS.getDefaultState(), Blocks.STONE_BRICK_SLAB.getDefaultState());
        world.setBlockState(origin.up(), ModBlocks.CASH_REGISTER.getDefaultState());
    }

    private static void build(ServerWorld world, BlockPos origin, BlockState wallState, BlockState roofState) {
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int z = -RADIUS; z <= RADIUS; z++) {
                boolean onWall = x == -RADIUS || x == RADIUS || z == -RADIUS || z == RADIUS;
                boolean isDoorway = x == 0 && z == RADIUS;
                for (int y = 0; y < WALL_HEIGHT; y++) {
                    BlockPos pos = origin.add(x, y, z);
                    if (onWall && !(isDoorway && y < 2)) {
                        world.setBlockState(pos, wallState);
                    } else {
                        world.setBlockState(pos, Blocks.AIR.getDefaultState());
                    }
                }
                world.setBlockState(origin.add(x, WALL_HEIGHT, z), roofState);
            }
        }
    }
}
