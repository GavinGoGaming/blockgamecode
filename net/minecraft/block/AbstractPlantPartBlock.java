/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractPlantStemBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractPlantPartBlock
extends Block {
    protected final Direction growthDirection;
    protected final boolean tickWater;
    protected final VoxelShape outlineShape;

    protected AbstractPlantPartBlock(AbstractBlock.Settings settings, Direction growthDirection, VoxelShape outlineShape, boolean tickWater) {
        super(settings);
        this.growthDirection = growthDirection;
        this.outlineShape = outlineShape;
        this.tickWater = tickWater;
    }

    protected abstract MapCodec<? extends AbstractPlantPartBlock> getCodec();

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState lv = ctx.getWorld().getBlockState(ctx.getBlockPos().offset(this.growthDirection));
        if (lv.isOf(this.getStem()) || lv.isOf(this.getPlant())) {
            return this.getPlant().getDefaultState();
        }
        return this.getRandomGrowthState(ctx.getWorld());
    }

    public BlockState getRandomGrowthState(WorldAccess world) {
        return this.getDefaultState();
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockPos lv = pos.offset(this.growthDirection.getOpposite());
        BlockState lv2 = world.getBlockState(lv);
        if (!this.canAttachTo(lv2)) {
            return false;
        }
        return lv2.isOf(this.getStem()) || lv2.isOf(this.getPlant()) || lv2.isSideSolidFullSquare(world, lv, this.growthDirection);
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!state.canPlaceAt(world, pos)) {
            world.breakBlock(pos, true);
        }
    }

    protected boolean canAttachTo(BlockState state) {
        return true;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.outlineShape;
    }

    protected abstract AbstractPlantStemBlock getStem();

    protected abstract Block getPlant();
}

