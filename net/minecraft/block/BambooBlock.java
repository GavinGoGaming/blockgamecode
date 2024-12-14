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
import net.minecraft.block.Fertilizable;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.SwordItem;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class BambooBlock
extends Block
implements Fertilizable {
    public static final MapCodec<BambooBlock> CODEC = BambooBlock.createCodec(BambooBlock::new);
    protected static final float field_30997 = 3.0f;
    protected static final float field_30998 = 5.0f;
    protected static final float field_30999 = 1.5f;
    protected static final VoxelShape SMALL_LEAVES_SHAPE = Block.createCuboidShape(5.0, 0.0, 5.0, 11.0, 16.0, 11.0);
    protected static final VoxelShape LARGE_LEAVES_SHAPE = Block.createCuboidShape(3.0, 0.0, 3.0, 13.0, 16.0, 13.0);
    protected static final VoxelShape NO_LEAVES_SHAPE = Block.createCuboidShape(6.5, 0.0, 6.5, 9.5, 16.0, 9.5);
    public static final IntProperty AGE = Properties.AGE_1;
    public static final EnumProperty<BambooLeaves> LEAVES = Properties.BAMBOO_LEAVES;
    public static final IntProperty STAGE = Properties.STAGE;
    public static final int field_31000 = 16;
    public static final int field_31001 = 0;
    public static final int field_31002 = 1;
    public static final int field_31003 = 0;
    public static final int field_31004 = 1;

    public MapCodec<BambooBlock> getCodec() {
        return CODEC;
    }

    public BambooBlock(AbstractBlock.Settings arg) {
        super(arg);
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(AGE, 0)).with(LEAVES, BambooLeaves.NONE)).with(STAGE, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AGE, LEAVES, STAGE);
    }

    @Override
    protected boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
        return true;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        VoxelShape lv = state.get(LEAVES) == BambooLeaves.LARGE ? LARGE_LEAVES_SHAPE : SMALL_LEAVES_SHAPE;
        Vec3d lv2 = state.getModelOffset(world, pos);
        return lv.offset(lv2.x, lv2.y, lv2.z);
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Vec3d lv = state.getModelOffset(world, pos);
        return NO_LEAVES_SHAPE.offset(lv.x, lv.y, lv.z);
    }

    @Override
    protected boolean isShapeFullCube(BlockState state, BlockView world, BlockPos pos) {
        return false;
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        FluidState lv = ctx.getWorld().getFluidState(ctx.getBlockPos());
        if (!lv.isEmpty()) {
            return null;
        }
        BlockState lv2 = ctx.getWorld().getBlockState(ctx.getBlockPos().down());
        if (lv2.isIn(BlockTags.BAMBOO_PLANTABLE_ON)) {
            if (lv2.isOf(Blocks.BAMBOO_SAPLING)) {
                return (BlockState)this.getDefaultState().with(AGE, 0);
            }
            if (lv2.isOf(Blocks.BAMBOO)) {
                int i = lv2.get(AGE) > 0 ? 1 : 0;
                return (BlockState)this.getDefaultState().with(AGE, i);
            }
            BlockState lv3 = ctx.getWorld().getBlockState(ctx.getBlockPos().up());
            if (lv3.isOf(Blocks.BAMBOO)) {
                return (BlockState)this.getDefaultState().with(AGE, lv3.get(AGE));
            }
            return Blocks.BAMBOO_SAPLING.getDefaultState();
        }
        return null;
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!state.canPlaceAt(world, pos)) {
            world.breakBlock(pos, true);
        }
    }

    @Override
    protected boolean hasRandomTicks(BlockState state) {
        return state.get(STAGE) == 0;
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        int i;
        if (state.get(STAGE) != 0) {
            return;
        }
        if (random.nextInt(3) == 0 && world.isAir(pos.up()) && world.getBaseLightLevel(pos.up(), 0) >= 9 && (i = this.countBambooBelow(world, pos) + 1) < 16) {
            this.updateLeaves(state, world, pos, random, i);
        }
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return world.getBlockState(pos.down()).isIn(BlockTags.BAMBOO_PLANTABLE_ON);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (!state.canPlaceAt(world, pos)) {
            world.scheduleBlockTick(pos, this, 1);
        }
        if (direction == Direction.UP && neighborState.isOf(Blocks.BAMBOO) && neighborState.get(AGE) > state.get(AGE)) {
            world.setBlockState(pos, (BlockState)state.cycle(AGE), Block.NOTIFY_LISTENERS);
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
        int j;
        int i = this.countBambooAbove(world, pos);
        return i + (j = this.countBambooBelow(world, pos)) + 1 < 16 && world.getBlockState(pos.up(i)).get(STAGE) != 1;
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        int i = this.countBambooAbove(world, pos);
        int j = this.countBambooBelow(world, pos);
        int k = i + j + 1;
        int l = 1 + random.nextInt(2);
        for (int m = 0; m < l; ++m) {
            BlockPos lv = pos.up(i);
            BlockState lv2 = world.getBlockState(lv);
            if (k >= 16 || lv2.get(STAGE) == 1 || !world.isAir(lv.up())) {
                return;
            }
            this.updateLeaves(lv2, world, lv, random, k);
            ++i;
            ++k;
        }
    }

    @Override
    protected float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        if (player.getMainHandStack().getItem() instanceof SwordItem) {
            return 1.0f;
        }
        return super.calcBlockBreakingDelta(state, player, world, pos);
    }

    protected void updateLeaves(BlockState state, World world, BlockPos pos, Random random, int height) {
        BlockState lv = world.getBlockState(pos.down());
        BlockPos lv2 = pos.down(2);
        BlockState lv3 = world.getBlockState(lv2);
        BambooLeaves lv4 = BambooLeaves.NONE;
        if (height >= 1) {
            if (!lv.isOf(Blocks.BAMBOO) || lv.get(LEAVES) == BambooLeaves.NONE) {
                lv4 = BambooLeaves.SMALL;
            } else if (lv.isOf(Blocks.BAMBOO) && lv.get(LEAVES) != BambooLeaves.NONE) {
                lv4 = BambooLeaves.LARGE;
                if (lv3.isOf(Blocks.BAMBOO)) {
                    world.setBlockState(pos.down(), (BlockState)lv.with(LEAVES, BambooLeaves.SMALL), Block.NOTIFY_ALL);
                    world.setBlockState(lv2, (BlockState)lv3.with(LEAVES, BambooLeaves.NONE), Block.NOTIFY_ALL);
                }
            }
        }
        int j = state.get(AGE) == 1 || lv3.isOf(Blocks.BAMBOO) ? 1 : 0;
        int k = height >= 11 && random.nextFloat() < 0.25f || height == 15 ? 1 : 0;
        world.setBlockState(pos.up(), (BlockState)((BlockState)((BlockState)this.getDefaultState().with(AGE, j)).with(LEAVES, lv4)).with(STAGE, k), Block.NOTIFY_ALL);
    }

    protected int countBambooAbove(BlockView world, BlockPos pos) {
        int i;
        for (i = 0; i < 16 && world.getBlockState(pos.up(i + 1)).isOf(Blocks.BAMBOO); ++i) {
        }
        return i;
    }

    protected int countBambooBelow(BlockView world, BlockPos pos) {
        int i;
        for (i = 0; i < 16 && world.getBlockState(pos.down(i + 1)).isOf(Blocks.BAMBOO); ++i) {
        }
        return i;
    }
}

