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
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidFillable;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class TallSeagrassBlock
extends TallPlantBlock
implements FluidFillable {
    public static final MapCodec<TallSeagrassBlock> CODEC = TallSeagrassBlock.createCodec(TallSeagrassBlock::new);
    public static final EnumProperty<DoubleBlockHalf> HALF = TallPlantBlock.HALF;
    protected static final float field_31262 = 6.0f;
    protected static final VoxelShape SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

    public MapCodec<TallSeagrassBlock> getCodec() {
        return CODEC;
    }

    public TallSeagrassBlock(AbstractBlock.Settings arg) {
        super(arg);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
        return floor.isSideSolidFullSquare(world, pos, Direction.UP) && !floor.isOf(Blocks.MAGMA_BLOCK);
    }

    @Override
    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
        return new ItemStack(Blocks.SEAGRASS);
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        FluidState lv2;
        BlockState lv = super.getPlacementState(ctx);
        if (lv != null && (lv2 = ctx.getWorld().getFluidState(ctx.getBlockPos().up())).isIn(FluidTags.WATER) && lv2.getLevel() == 8) {
            return lv;
        }
        return null;
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        if (state.get(HALF) == DoubleBlockHalf.UPPER) {
            BlockState lv = world.getBlockState(pos.down());
            return lv.isOf(this) && lv.get(HALF) == DoubleBlockHalf.LOWER;
        }
        FluidState lv2 = world.getFluidState(pos);
        return super.canPlaceAt(state, world, pos) && lv2.isIn(FluidTags.WATER) && lv2.getLevel() == 8;
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return Fluids.WATER.getStill(false);
    }

    @Override
    public boolean canFillWithFluid(@Nullable PlayerEntity player, BlockView world, BlockPos pos, BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
        return false;
    }
}

