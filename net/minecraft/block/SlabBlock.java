/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class SlabBlock
extends Block
implements Waterloggable {
    public static final MapCodec<SlabBlock> CODEC = SlabBlock.createCodec(SlabBlock::new);
    public static final EnumProperty<SlabType> TYPE = Properties.SLAB_TYPE;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    protected static final VoxelShape BOTTOM_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
    protected static final VoxelShape TOP_SHAPE = Block.createCuboidShape(0.0, 8.0, 0.0, 16.0, 16.0, 16.0);

    public MapCodec<? extends SlabBlock> getCodec() {
        return CODEC;
    }

    public SlabBlock(AbstractBlock.Settings arg) {
        super(arg);
        this.setDefaultState((BlockState)((BlockState)this.getDefaultState().with(TYPE, SlabType.BOTTOM)).with(WATERLOGGED, false));
    }

    @Override
    protected boolean hasSidedTransparency(BlockState state) {
        return state.get(TYPE) != SlabType.DOUBLE;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(TYPE, WATERLOGGED);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        SlabType lv = state.get(TYPE);
        switch (lv) {
            case DOUBLE: {
                return VoxelShapes.fullCube();
            }
            case TOP: {
                return TOP_SHAPE;
            }
        }
        return BOTTOM_SHAPE;
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos lv = ctx.getBlockPos();
        BlockState lv2 = ctx.getWorld().getBlockState(lv);
        if (lv2.isOf(this)) {
            return (BlockState)((BlockState)lv2.with(TYPE, SlabType.DOUBLE)).with(WATERLOGGED, false);
        }
        FluidState lv3 = ctx.getWorld().getFluidState(lv);
        BlockState lv4 = (BlockState)((BlockState)this.getDefaultState().with(TYPE, SlabType.BOTTOM)).with(WATERLOGGED, lv3.getFluid() == Fluids.WATER);
        Direction lv5 = ctx.getSide();
        if (lv5 == Direction.DOWN || lv5 != Direction.UP && ctx.getHitPos().y - (double)lv.getY() > 0.5) {
            return (BlockState)lv4.with(TYPE, SlabType.TOP);
        }
        return lv4;
    }

    @Override
    protected boolean canReplace(BlockState state, ItemPlacementContext context) {
        ItemStack lv = context.getStack();
        SlabType lv2 = state.get(TYPE);
        if (lv2 == SlabType.DOUBLE || !lv.isOf(this.asItem())) {
            return false;
        }
        if (context.canReplaceExisting()) {
            boolean bl = context.getHitPos().y - (double)context.getBlockPos().getY() > 0.5;
            Direction lv3 = context.getSide();
            if (lv2 == SlabType.BOTTOM) {
                return lv3 == Direction.UP || bl && lv3.getAxis().isHorizontal();
            }
            return lv3 == Direction.DOWN || !bl && lv3.getAxis().isHorizontal();
        }
        return true;
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        if (state.get(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getStill(false);
        }
        return super.getFluidState(state);
    }

    @Override
    public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
        if (state.get(TYPE) != SlabType.DOUBLE) {
            return Waterloggable.super.tryFillWithFluid(world, pos, state, fluidState);
        }
        return false;
    }

    @Override
    public boolean canFillWithFluid(@Nullable PlayerEntity player, BlockView world, BlockPos pos, BlockState state, Fluid fluid) {
        if (state.get(TYPE) != SlabType.DOUBLE) {
            return Waterloggable.super.canFillWithFluid(player, world, pos, state, fluid);
        }
        return false;
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED).booleanValue()) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        switch (type) {
            case LAND: {
                return false;
            }
            case WATER: {
                return state.getFluidState().isIn(FluidTags.WATER);
            }
            case AIR: {
                return false;
            }
        }
        return false;
    }
}

