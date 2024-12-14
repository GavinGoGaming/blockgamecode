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
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.NavigationType;
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
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class CactusBlock
extends Block {
    public static final MapCodec<CactusBlock> CODEC = CactusBlock.createCodec(CactusBlock::new);
    public static final IntProperty AGE = Properties.AGE_15;
    public static final int MAX_AGE = 15;
    protected static final int field_31045 = 1;
    protected static final VoxelShape COLLISION_SHAPE = Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 15.0, 15.0);
    protected static final VoxelShape OUTLINE_SHAPE = Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);

    public MapCodec<CactusBlock> getCodec() {
        return CODEC;
    }

    protected CactusBlock(AbstractBlock.Settings arg) {
        super(arg);
        this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(AGE, 0));
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!state.canPlaceAt(world, pos)) {
            world.breakBlock(pos, true);
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        BlockPos lv = pos.up();
        if (!world.isAir(lv)) {
            return;
        }
        int i = 1;
        while (world.getBlockState(pos.down(i)).isOf(this)) {
            ++i;
        }
        if (i >= 3) {
            return;
        }
        int j = state.get(AGE);
        if (j == 15) {
            world.setBlockState(lv, this.getDefaultState());
            BlockState lv2 = (BlockState)state.with(AGE, 0);
            world.setBlockState(pos, lv2, Block.NO_REDRAW);
            world.updateNeighbor(lv2, lv, this, pos, false);
        } else {
            world.setBlockState(pos, (BlockState)state.with(AGE, j + 1), Block.NO_REDRAW);
        }
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return COLLISION_SHAPE;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return OUTLINE_SHAPE;
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
        for (Direction lv : Direction.Type.HORIZONTAL) {
            BlockState lv2 = world.getBlockState(pos.offset(lv));
            if (!lv2.isSolid() && !world.getFluidState(pos.offset(lv)).isIn(FluidTags.LAVA)) continue;
            return false;
        }
        BlockState lv3 = world.getBlockState(pos.down());
        return (lv3.isOf(Blocks.CACTUS) || lv3.isIn(BlockTags.SAND)) && !world.getBlockState(pos.up()).isLiquid();
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        entity.damage(world.getDamageSources().cactus(), 1.0f);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }
}

