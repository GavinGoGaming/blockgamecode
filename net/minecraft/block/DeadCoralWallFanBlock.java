/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DeadCoralFanBlock;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class DeadCoralWallFanBlock
extends DeadCoralFanBlock {
    public static final MapCodec<DeadCoralWallFanBlock> CODEC = DeadCoralWallFanBlock.createCodec(DeadCoralWallFanBlock::new);
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
    private static final Map<Direction, VoxelShape> FACING_TO_SHAPE = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, Block.createCuboidShape(0.0, 4.0, 5.0, 16.0, 12.0, 16.0), Direction.SOUTH, Block.createCuboidShape(0.0, 4.0, 0.0, 16.0, 12.0, 11.0), Direction.WEST, Block.createCuboidShape(5.0, 4.0, 0.0, 16.0, 12.0, 16.0), Direction.EAST, Block.createCuboidShape(0.0, 4.0, 0.0, 11.0, 12.0, 16.0)));

    public MapCodec<? extends DeadCoralWallFanBlock> getCodec() {
        return CODEC;
    }

    protected DeadCoralWallFanBlock(AbstractBlock.Settings arg) {
        super(arg);
        this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(WATERLOGGED, true));
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return FACING_TO_SHAPE.get(state.get(FACING));
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return (BlockState)state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED).booleanValue()) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        if (direction.getOpposite() == state.get(FACING) && !state.canPlaceAt(world, pos)) {
            return Blocks.AIR.getDefaultState();
        }
        return state;
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        Direction lv = state.get(FACING);
        BlockPos lv2 = pos.offset(lv.getOpposite());
        BlockState lv3 = world.getBlockState(lv2);
        return lv3.isSideSolidFullSquare(world, lv2, lv);
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction[] lvs;
        BlockState lv = super.getPlacementState(ctx);
        World lv2 = ctx.getWorld();
        BlockPos lv3 = ctx.getBlockPos();
        for (Direction lv4 : lvs = ctx.getPlacementDirections()) {
            if (!lv4.getAxis().isHorizontal() || !(lv = (BlockState)lv.with(FACING, lv4.getOpposite())).canPlaceAt(lv2, lv3)) continue;
            return lv;
        }
        return null;
    }
}

