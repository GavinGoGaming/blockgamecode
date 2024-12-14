/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalConnectingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class PaneBlock
extends HorizontalConnectingBlock {
    public static final MapCodec<PaneBlock> CODEC = PaneBlock.createCodec(PaneBlock::new);

    public MapCodec<? extends PaneBlock> getCodec() {
        return CODEC;
    }

    protected PaneBlock(AbstractBlock.Settings arg) {
        super(1.0f, 1.0f, 16.0f, 16.0f, 16.0f, arg);
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(NORTH, false)).with(EAST, false)).with(SOUTH, false)).with(WEST, false)).with(WATERLOGGED, false));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        World lv = ctx.getWorld();
        BlockPos lv2 = ctx.getBlockPos();
        FluidState lv3 = ctx.getWorld().getFluidState(ctx.getBlockPos());
        BlockPos lv4 = lv2.north();
        BlockPos lv5 = lv2.south();
        BlockPos lv6 = lv2.west();
        BlockPos lv7 = lv2.east();
        BlockState lv8 = lv.getBlockState(lv4);
        BlockState lv9 = lv.getBlockState(lv5);
        BlockState lv10 = lv.getBlockState(lv6);
        BlockState lv11 = lv.getBlockState(lv7);
        return (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.getDefaultState().with(NORTH, this.connectsTo(lv8, lv8.isSideSolidFullSquare(lv, lv4, Direction.SOUTH)))).with(SOUTH, this.connectsTo(lv9, lv9.isSideSolidFullSquare(lv, lv5, Direction.NORTH)))).with(WEST, this.connectsTo(lv10, lv10.isSideSolidFullSquare(lv, lv6, Direction.EAST)))).with(EAST, this.connectsTo(lv11, lv11.isSideSolidFullSquare(lv, lv7, Direction.WEST)))).with(WATERLOGGED, lv3.getFluid() == Fluids.WATER);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED).booleanValue()) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        if (direction.getAxis().isHorizontal()) {
            return (BlockState)state.with((Property)FACING_PROPERTIES.get(direction), this.connectsTo(neighborState, neighborState.isSideSolidFullSquare(world, neighborPos, direction.getOpposite())));
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    protected VoxelShape getCameraCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.empty();
    }

    @Override
    protected boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
        if (stateFrom.isOf(this)) {
            if (!direction.getAxis().isHorizontal()) {
                return true;
            }
            if (((Boolean)state.get((Property)FACING_PROPERTIES.get(direction))).booleanValue() && ((Boolean)stateFrom.get((Property)FACING_PROPERTIES.get(direction.getOpposite()))).booleanValue()) {
                return true;
            }
        }
        return super.isSideInvisible(state, stateFrom, direction);
    }

    public final boolean connectsTo(BlockState state, boolean sideSolidFullSquare) {
        return !PaneBlock.cannotConnect(state) && sideSolidFullSquare || state.getBlock() instanceof PaneBlock || state.isIn(BlockTags.WALLS);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, WEST, SOUTH, WATERLOGGED);
    }
}

