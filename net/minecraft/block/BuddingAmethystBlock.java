/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AmethystBlock;
import net.minecraft.block.AmethystClusterBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

public class BuddingAmethystBlock
extends AmethystBlock {
    public static final MapCodec<BuddingAmethystBlock> CODEC = BuddingAmethystBlock.createCodec(BuddingAmethystBlock::new);
    public static final int GROW_CHANCE = 5;
    private static final Direction[] DIRECTIONS = Direction.values();

    public MapCodec<BuddingAmethystBlock> getCodec() {
        return CODEC;
    }

    public BuddingAmethystBlock(AbstractBlock.Settings arg) {
        super(arg);
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (random.nextInt(5) != 0) {
            return;
        }
        Direction lv = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
        BlockPos lv2 = pos.offset(lv);
        BlockState lv3 = world.getBlockState(lv2);
        Block lv4 = null;
        if (BuddingAmethystBlock.canGrowIn(lv3)) {
            lv4 = Blocks.SMALL_AMETHYST_BUD;
        } else if (lv3.isOf(Blocks.SMALL_AMETHYST_BUD) && lv3.get(AmethystClusterBlock.FACING) == lv) {
            lv4 = Blocks.MEDIUM_AMETHYST_BUD;
        } else if (lv3.isOf(Blocks.MEDIUM_AMETHYST_BUD) && lv3.get(AmethystClusterBlock.FACING) == lv) {
            lv4 = Blocks.LARGE_AMETHYST_BUD;
        } else if (lv3.isOf(Blocks.LARGE_AMETHYST_BUD) && lv3.get(AmethystClusterBlock.FACING) == lv) {
            lv4 = Blocks.AMETHYST_CLUSTER;
        }
        if (lv4 != null) {
            BlockState lv5 = (BlockState)((BlockState)lv4.getDefaultState().with(AmethystClusterBlock.FACING, lv)).with(AmethystClusterBlock.WATERLOGGED, lv3.getFluidState().getFluid() == Fluids.WATER);
            world.setBlockState(lv2, lv5);
        }
    }

    public static boolean canGrowIn(BlockState state) {
        return state.isAir() || state.isOf(Blocks.WATER) && state.getFluidState().getLevel() == 8;
    }
}

