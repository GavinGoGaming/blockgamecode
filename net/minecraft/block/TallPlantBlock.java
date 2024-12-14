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
import net.minecraft.block.PlantBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class TallPlantBlock
extends PlantBlock {
    public static final MapCodec<TallPlantBlock> CODEC = TallPlantBlock.createCodec(TallPlantBlock::new);
    public static final EnumProperty<DoubleBlockHalf> HALF = Properties.DOUBLE_BLOCK_HALF;

    public MapCodec<? extends TallPlantBlock> getCodec() {
        return CODEC;
    }

    public TallPlantBlock(AbstractBlock.Settings arg) {
        super(arg);
        this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        DoubleBlockHalf lv = state.get(HALF);
        if (!(direction.getAxis() != Direction.Axis.Y || lv == DoubleBlockHalf.LOWER != (direction == Direction.UP) || neighborState.isOf(this) && neighborState.get(HALF) != lv)) {
            return Blocks.AIR.getDefaultState();
        }
        if (lv == DoubleBlockHalf.LOWER && direction == Direction.DOWN && !state.canPlaceAt(world, pos)) {
            return Blocks.AIR.getDefaultState();
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos lv = ctx.getBlockPos();
        World lv2 = ctx.getWorld();
        if (lv.getY() < lv2.getTopY() - 1 && lv2.getBlockState(lv.up()).canReplace(ctx)) {
            return super.getPlacementState(ctx);
        }
        return null;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        BlockPos lv = pos.up();
        world.setBlockState(lv, TallPlantBlock.withWaterloggedState(world, lv, (BlockState)this.getDefaultState().with(HALF, DoubleBlockHalf.UPPER)), Block.NOTIFY_ALL);
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        if (state.get(HALF) == DoubleBlockHalf.UPPER) {
            BlockState lv = world.getBlockState(pos.down());
            return lv.isOf(this) && lv.get(HALF) == DoubleBlockHalf.LOWER;
        }
        return super.canPlaceAt(state, world, pos);
    }

    public static void placeAt(WorldAccess world, BlockState state, BlockPos pos, int flags) {
        BlockPos lv = pos.up();
        world.setBlockState(pos, TallPlantBlock.withWaterloggedState(world, pos, (BlockState)state.with(HALF, DoubleBlockHalf.LOWER)), flags);
        world.setBlockState(lv, TallPlantBlock.withWaterloggedState(world, lv, (BlockState)state.with(HALF, DoubleBlockHalf.UPPER)), flags);
    }

    public static BlockState withWaterloggedState(WorldView world, BlockPos pos, BlockState state) {
        if (state.contains(Properties.WATERLOGGED)) {
            return (BlockState)state.with(Properties.WATERLOGGED, world.isWater(pos));
        }
        return state;
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient) {
            if (player.isCreative()) {
                TallPlantBlock.onBreakInCreative(world, pos, state, player);
            } else {
                TallPlantBlock.dropStacks(state, world, pos, null, player, player.getMainHandStack());
            }
        }
        return super.onBreak(world, pos, state, player);
    }

    @Override
    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.afterBreak(world, player, pos, Blocks.AIR.getDefaultState(), blockEntity, tool);
    }

    protected static void onBreakInCreative(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        BlockPos lv2;
        BlockState lv3;
        DoubleBlockHalf lv = state.get(HALF);
        if (lv == DoubleBlockHalf.UPPER && (lv3 = world.getBlockState(lv2 = pos.down())).isOf(state.getBlock()) && lv3.get(HALF) == DoubleBlockHalf.LOWER) {
            BlockState lv4 = lv3.getFluidState().isOf(Fluids.WATER) ? Blocks.WATER.getDefaultState() : Blocks.AIR.getDefaultState();
            world.setBlockState(lv2, lv4, Block.NOTIFY_ALL | Block.SKIP_DROPS);
            world.syncWorldEvent(player, WorldEvents.BLOCK_BROKEN, lv2, Block.getRawIdFromState(lv3));
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(HALF);
    }

    @Override
    protected long getRenderingSeed(BlockState state, BlockPos pos) {
        return MathHelper.hashCode(pos.getX(), pos.down(state.get(HALF) == DoubleBlockHalf.LOWER ? 0 : 1).getY(), pos.getZ());
    }
}

