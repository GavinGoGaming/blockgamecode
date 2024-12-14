/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block;

import com.google.common.base.MoreObjects;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.TripwireBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class TripwireHookBlock
extends Block {
    public static final MapCodec<TripwireHookBlock> CODEC = TripwireHookBlock.createCodec(TripwireHookBlock::new);
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
    public static final BooleanProperty POWERED = Properties.POWERED;
    public static final BooleanProperty ATTACHED = Properties.ATTACHED;
    protected static final int field_31268 = 1;
    protected static final int field_31269 = 42;
    private static final int SCHEDULED_TICK_DELAY = 10;
    protected static final int field_31270 = 3;
    protected static final VoxelShape SOUTH_SHAPE = Block.createCuboidShape(5.0, 0.0, 10.0, 11.0, 10.0, 16.0);
    protected static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(5.0, 0.0, 0.0, 11.0, 10.0, 6.0);
    protected static final VoxelShape EAST_SHAPE = Block.createCuboidShape(10.0, 0.0, 5.0, 16.0, 10.0, 11.0);
    protected static final VoxelShape WEST_SHAPE = Block.createCuboidShape(0.0, 0.0, 5.0, 6.0, 10.0, 11.0);

    public MapCodec<TripwireHookBlock> getCodec() {
        return CODEC;
    }

    public TripwireHookBlock(AbstractBlock.Settings arg) {
        super(arg);
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(POWERED, false)).with(ATTACHED, false));
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        switch (state.get(FACING)) {
            default: {
                return WEST_SHAPE;
            }
            case WEST: {
                return EAST_SHAPE;
            }
            case SOUTH: {
                return NORTH_SHAPE;
            }
            case NORTH: 
        }
        return SOUTH_SHAPE;
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        Direction lv = state.get(FACING);
        BlockPos lv2 = pos.offset(lv.getOpposite());
        BlockState lv3 = world.getBlockState(lv2);
        return lv.getAxis().isHorizontal() && lv3.isSideSolidFullSquare(world, lv2, lv);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction.getOpposite() == state.get(FACING) && !state.canPlaceAt(world, pos)) {
            return Blocks.AIR.getDefaultState();
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction[] lvs;
        BlockState lv = (BlockState)((BlockState)this.getDefaultState().with(POWERED, false)).with(ATTACHED, false);
        World lv2 = ctx.getWorld();
        BlockPos lv3 = ctx.getBlockPos();
        for (Direction lv4 : lvs = ctx.getPlacementDirections()) {
            Direction lv5;
            if (!lv4.getAxis().isHorizontal() || !(lv = (BlockState)lv.with(FACING, lv5 = lv4.getOpposite())).canPlaceAt(lv2, lv3)) continue;
            return lv;
        }
        return null;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        TripwireHookBlock.update(world, pos, state, false, false, -1, null);
    }

    public static void update(World world, BlockPos pos, BlockState state, boolean bl, boolean bl2, int i, @Nullable BlockState arg4) {
        BlockPos lv3;
        Optional<Direction> optional = state.getOrEmpty(FACING);
        if (!optional.isPresent()) {
            return;
        }
        Direction lv = optional.get();
        boolean bl3 = state.getOrEmpty(ATTACHED).orElse(false);
        boolean bl4 = state.getOrEmpty(POWERED).orElse(false);
        Block lv2 = state.getBlock();
        boolean bl5 = !bl;
        boolean bl6 = false;
        int j = 0;
        BlockState[] lvs = new BlockState[42];
        for (int k = 1; k < 42; ++k) {
            lv3 = pos.offset(lv, k);
            BlockState lv4 = world.getBlockState(lv3);
            if (lv4.isOf(Blocks.TRIPWIRE_HOOK)) {
                if (lv4.get(FACING) != lv.getOpposite()) break;
                j = k;
                break;
            }
            if (lv4.isOf(Blocks.TRIPWIRE) || k == i) {
                if (k == i) {
                    lv4 = MoreObjects.firstNonNull(arg4, lv4);
                }
                boolean bl7 = lv4.get(TripwireBlock.DISARMED) == false;
                boolean bl8 = lv4.get(TripwireBlock.POWERED);
                bl6 |= bl7 && bl8;
                lvs[k] = lv4;
                if (k != i) continue;
                world.scheduleBlockTick(pos, lv2, 10);
                bl5 &= bl7;
                continue;
            }
            lvs[k] = null;
            bl5 = false;
        }
        BlockState lv5 = (BlockState)((BlockState)lv2.getDefaultState().withIfExists(ATTACHED, bl5)).withIfExists(POWERED, bl6 &= (bl5 &= j > 1));
        if (j > 0) {
            lv3 = pos.offset(lv, j);
            Direction lv6 = lv.getOpposite();
            world.setBlockState(lv3, (BlockState)lv5.with(FACING, lv6), Block.NOTIFY_ALL);
            TripwireHookBlock.updateNeighborsOnAxis(lv2, world, lv3, lv6);
            TripwireHookBlock.playSound(world, lv3, bl5, bl6, bl3, bl4);
        }
        TripwireHookBlock.playSound(world, pos, bl5, bl6, bl3, bl4);
        if (!bl) {
            world.setBlockState(pos, (BlockState)lv5.with(FACING, lv), Block.NOTIFY_ALL);
            if (bl2) {
                TripwireHookBlock.updateNeighborsOnAxis(lv2, world, pos, lv);
            }
        }
        if (bl3 != bl5) {
            for (int l = 1; l < j; ++l) {
                BlockPos lv7 = pos.offset(lv, l);
                BlockState lv8 = lvs[l];
                if (lv8 == null) continue;
                world.setBlockState(lv7, (BlockState)lv8.withIfExists(ATTACHED, bl5), Block.NOTIFY_ALL);
                if (world.getBlockState(lv7).isAir()) continue;
            }
        }
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        TripwireHookBlock.update(world, pos, state, false, true, -1, null);
    }

    private static void playSound(World world, BlockPos pos, boolean attached, boolean on, boolean detached, boolean off) {
        if (on && !off) {
            world.playSound(null, pos, SoundEvents.BLOCK_TRIPWIRE_CLICK_ON, SoundCategory.BLOCKS, 0.4f, 0.6f);
            world.emitGameEvent(null, GameEvent.BLOCK_ACTIVATE, pos);
        } else if (!on && off) {
            world.playSound(null, pos, SoundEvents.BLOCK_TRIPWIRE_CLICK_OFF, SoundCategory.BLOCKS, 0.4f, 0.5f);
            world.emitGameEvent(null, GameEvent.BLOCK_DEACTIVATE, pos);
        } else if (attached && !detached) {
            world.playSound(null, pos, SoundEvents.BLOCK_TRIPWIRE_ATTACH, SoundCategory.BLOCKS, 0.4f, 0.7f);
            world.emitGameEvent(null, GameEvent.BLOCK_ATTACH, pos);
        } else if (!attached && detached) {
            world.playSound(null, pos, SoundEvents.BLOCK_TRIPWIRE_DETACH, SoundCategory.BLOCKS, 0.4f, 1.2f / (world.random.nextFloat() * 0.2f + 0.9f));
            world.emitGameEvent(null, GameEvent.BLOCK_DETACH, pos);
        }
    }

    private static void updateNeighborsOnAxis(Block block, World world, BlockPos pos, Direction direction) {
        world.updateNeighborsAlways(pos, block);
        world.updateNeighborsAlways(pos.offset(direction.getOpposite()), block);
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (moved || state.isOf(newState.getBlock())) {
            return;
        }
        boolean bl2 = state.get(ATTACHED);
        boolean bl3 = state.get(POWERED);
        if (bl2 || bl3) {
            TripwireHookBlock.update(world, pos, state, true, false, -1, null);
        }
        if (bl3) {
            world.updateNeighborsAlways(pos, this);
            world.updateNeighborsAlways(pos.offset(state.get(FACING).getOpposite()), this);
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.get(POWERED) != false ? 15 : 0;
    }

    @Override
    protected int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if (!state.get(POWERED).booleanValue()) {
            return 0;
        }
        if (state.get(FACING) == direction) {
            return 15;
        }
        return 0;
    }

    @Override
    protected boolean emitsRedstonePower(BlockState state) {
        return true;
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
        builder.add(FACING, POWERED, ATTACHED);
    }
}

