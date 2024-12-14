/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class SugarCaneBlock
extends Block {
    public static final MapCodec<SugarCaneBlock> CODEC = SugarCaneBlock.createCodec(SugarCaneBlock::new);
    public static final IntProperty AGE = Properties.AGE_15;
    protected static final float field_31258 = 6.0f;
    protected static final VoxelShape SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

    public MapCodec<SugarCaneBlock> getCodec() {
        return CODEC;
    }

    protected SugarCaneBlock(AbstractBlock.Settings arg) {
        super(arg);
        this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(AGE, 0));
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!state.canPlaceAt(world, pos)) {
            world.breakBlock(pos, true);
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (world.isAir(pos.up())) {
            int i = 1;
            while (world.getBlockState(pos.down(i)).isOf(this)) {
                ++i;
            }
            if (i < 3) {
                int j = state.get(AGE);
                if (j == 15) {
                    world.setBlockState(pos.up(), this.getDefaultState());
                    world.setBlockState(pos, (BlockState)state.with(AGE, 0), Block.NO_REDRAW);
                } else {
                    world.setBlockState(pos, (BlockState)state.with(AGE, j + 1), Block.NO_REDRAW);
                }
            }
        }
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (!state.canPlaceAt(world, pos)) {
            world.scheduleBlockTick(pos, this, 1);
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockState lv = world.getBlockState(pos.down());
        if (lv.isOf(this)) {
            return true;
        }
        if (lv.isIn(BlockTags.DIRT) || lv.isIn(BlockTags.SAND)) {
            BlockPos lv2 = pos.down();
            for (Direction lv3 : Direction.Type.HORIZONTAL) {
                BlockState lv4 = world.getBlockState(lv2.offset(lv3));
                FluidState lv5 = world.getFluidState(lv2.offset(lv3));
                if (!lv5.isIn(FluidTags.WATER) && !lv4.isOf(Blocks.FROSTED_ICE)) continue;
                return true;
            }
        }
        return false;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }
}

