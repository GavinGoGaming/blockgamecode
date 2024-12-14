/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BigDripleafBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class BigDripleafStemBlock
extends HorizontalFacingBlock
implements Fertilizable,
Waterloggable {
    public static final MapCodec<BigDripleafStemBlock> CODEC = BigDripleafStemBlock.createCodec(BigDripleafStemBlock::new);
    private static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    private static final int field_31021 = 6;
    protected static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(5.0, 0.0, 9.0, 11.0, 16.0, 15.0);
    protected static final VoxelShape SOUTH_SHAPE = Block.createCuboidShape(5.0, 0.0, 1.0, 11.0, 16.0, 7.0);
    protected static final VoxelShape EAST_SHAPE = Block.createCuboidShape(1.0, 0.0, 5.0, 7.0, 16.0, 11.0);
    protected static final VoxelShape WEST_SHAPE = Block.createCuboidShape(9.0, 0.0, 5.0, 15.0, 16.0, 11.0);

    public MapCodec<BigDripleafStemBlock> getCodec() {
        return CODEC;
    }

    protected BigDripleafStemBlock(AbstractBlock.Settings arg) {
        super(arg);
        this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(WATERLOGGED, false)).with(FACING, Direction.NORTH));
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        switch (state.get(FACING)) {
            case SOUTH: {
                return SOUTH_SHAPE;
            }
            default: {
                return NORTH_SHAPE;
            }
            case WEST: {
                return WEST_SHAPE;
            }
            case EAST: 
        }
        return EAST_SHAPE;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, FACING);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        if (state.get(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getStill(false);
        }
        return super.getFluidState(state);
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockPos lv = pos.down();
        BlockState lv2 = world.getBlockState(lv);
        BlockState lv3 = world.getBlockState(pos.up());
        return !(!lv2.isOf(this) && !lv2.isIn(BlockTags.BIG_DRIPLEAF_PLACEABLE) || !lv3.isOf(this) && !lv3.isOf(Blocks.BIG_DRIPLEAF));
    }

    protected static boolean placeStemAt(WorldAccess world, BlockPos pos, FluidState fluidState, Direction direction) {
        BlockState lv = (BlockState)((BlockState)Blocks.BIG_DRIPLEAF_STEM.getDefaultState().with(WATERLOGGED, fluidState.isEqualAndStill(Fluids.WATER))).with(FACING, direction);
        return world.setBlockState(pos, lv, Block.NOTIFY_ALL);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (!(direction != Direction.DOWN && direction != Direction.UP || state.canPlaceAt(world, pos))) {
            world.scheduleBlockTick(pos, this, 1);
        }
        if (state.get(WATERLOGGED).booleanValue()) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!state.canPlaceAt(world, pos)) {
            world.breakBlock(pos, true);
        }
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
        Optional<BlockPos> optional = BlockLocating.findColumnEnd(world, pos, state.getBlock(), Direction.UP, Blocks.BIG_DRIPLEAF);
        if (optional.isEmpty()) {
            return false;
        }
        BlockPos lv = optional.get().up();
        BlockState lv2 = world.getBlockState(lv);
        return BigDripleafBlock.canGrowInto(world, lv, lv2);
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        Optional<BlockPos> optional = BlockLocating.findColumnEnd(world, pos, state.getBlock(), Direction.UP, Blocks.BIG_DRIPLEAF);
        if (optional.isEmpty()) {
            return;
        }
        BlockPos lv = optional.get();
        BlockPos lv2 = lv.up();
        Direction lv3 = state.get(FACING);
        BigDripleafStemBlock.placeStemAt(world, lv, world.getFluidState(lv), lv3);
        BigDripleafBlock.placeDripleafAt(world, lv2, world.getFluidState(lv2), lv3);
    }

    @Override
    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
        return new ItemStack(Blocks.BIG_DRIPLEAF);
    }
}

