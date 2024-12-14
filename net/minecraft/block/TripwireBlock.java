/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.block.HorizontalConnectingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.TripwireHookBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
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
import net.minecraft.world.event.GameEvent;

public class TripwireBlock
extends Block {
    public static final MapCodec<TripwireBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Registries.BLOCK.getCodec().fieldOf("hook")).forGetter(block -> block.hookBlock), TripwireBlock.createSettingsCodec()).apply((Applicative<TripwireBlock, ?>)instance, TripwireBlock::new));
    public static final BooleanProperty POWERED = Properties.POWERED;
    public static final BooleanProperty ATTACHED = Properties.ATTACHED;
    public static final BooleanProperty DISARMED = Properties.DISARMED;
    public static final BooleanProperty NORTH = ConnectingBlock.NORTH;
    public static final BooleanProperty EAST = ConnectingBlock.EAST;
    public static final BooleanProperty SOUTH = ConnectingBlock.SOUTH;
    public static final BooleanProperty WEST = ConnectingBlock.WEST;
    private static final Map<Direction, BooleanProperty> FACING_PROPERTIES = HorizontalConnectingBlock.FACING_PROPERTIES;
    protected static final VoxelShape ATTACHED_SHAPE = Block.createCuboidShape(0.0, 1.0, 0.0, 16.0, 2.5, 16.0);
    protected static final VoxelShape DETACHED_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
    private static final int SCHEDULED_TICK_DELAY = 10;
    private final Block hookBlock;

    public MapCodec<TripwireBlock> getCodec() {
        return CODEC;
    }

    public TripwireBlock(Block hookBlock, AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(POWERED, false)).with(ATTACHED, false)).with(DISARMED, false)).with(NORTH, false)).with(EAST, false)).with(SOUTH, false)).with(WEST, false));
        this.hookBlock = hookBlock;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return state.get(ATTACHED) != false ? ATTACHED_SHAPE : DETACHED_SHAPE;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        World lv = ctx.getWorld();
        BlockPos lv2 = ctx.getBlockPos();
        return (BlockState)((BlockState)((BlockState)((BlockState)this.getDefaultState().with(NORTH, this.shouldConnectTo(lv.getBlockState(lv2.north()), Direction.NORTH))).with(EAST, this.shouldConnectTo(lv.getBlockState(lv2.east()), Direction.EAST))).with(SOUTH, this.shouldConnectTo(lv.getBlockState(lv2.south()), Direction.SOUTH))).with(WEST, this.shouldConnectTo(lv.getBlockState(lv2.west()), Direction.WEST));
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction.getAxis().isHorizontal()) {
            return (BlockState)state.with(FACING_PROPERTIES.get(direction), this.shouldConnectTo(neighborState, direction));
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (oldState.isOf(state.getBlock())) {
            return;
        }
        this.update(world, pos, state);
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (moved || state.isOf(newState.getBlock())) {
            return;
        }
        this.update(world, pos, (BlockState)state.with(POWERED, true));
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient && !player.getMainHandStack().isEmpty() && player.getMainHandStack().isOf(Items.SHEARS)) {
            world.setBlockState(pos, (BlockState)state.with(DISARMED, true), Block.NO_REDRAW);
            world.emitGameEvent((Entity)player, GameEvent.SHEAR, pos);
        }
        return super.onBreak(world, pos, state, player);
    }

    private void update(World world, BlockPos pos, BlockState state) {
        block0: for (Direction lv : new Direction[]{Direction.SOUTH, Direction.WEST}) {
            for (int i = 1; i < 42; ++i) {
                BlockPos lv2 = pos.offset(lv, i);
                BlockState lv3 = world.getBlockState(lv2);
                if (lv3.isOf(this.hookBlock)) {
                    if (lv3.get(TripwireHookBlock.FACING) != lv.getOpposite()) continue block0;
                    TripwireHookBlock.update(world, lv2, lv3, false, true, i, state);
                    continue block0;
                }
                if (!lv3.isOf(this)) continue block0;
            }
        }
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (world.isClient) {
            return;
        }
        if (state.get(POWERED).booleanValue()) {
            return;
        }
        this.updatePowered(world, pos);
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!world.getBlockState(pos).get(POWERED).booleanValue()) {
            return;
        }
        this.updatePowered(world, pos);
    }

    private void updatePowered(World world, BlockPos pos) {
        BlockState lv = world.getBlockState(pos);
        boolean bl = lv.get(POWERED);
        boolean bl2 = false;
        List<Entity> list = world.getOtherEntities(null, lv.getOutlineShape(world, pos).getBoundingBox().offset(pos));
        if (!list.isEmpty()) {
            for (Entity lv2 : list) {
                if (lv2.canAvoidTraps()) continue;
                bl2 = true;
                break;
            }
        }
        if (bl2 != bl) {
            lv = (BlockState)lv.with(POWERED, bl2);
            world.setBlockState(pos, lv, Block.NOTIFY_ALL);
            this.update(world, pos, lv);
        }
        if (bl2) {
            world.scheduleBlockTick(new BlockPos(pos), this, 10);
        }
    }

    public boolean shouldConnectTo(BlockState state, Direction facing) {
        if (state.isOf(this.hookBlock)) {
            return state.get(TripwireHookBlock.FACING) == facing.getOpposite();
        }
        return state.isOf(this);
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        switch (rotation) {
            case CLOCKWISE_180: {
                return (BlockState)((BlockState)((BlockState)((BlockState)state.with(NORTH, state.get(SOUTH))).with(EAST, state.get(WEST))).with(SOUTH, state.get(NORTH))).with(WEST, state.get(EAST));
            }
            case COUNTERCLOCKWISE_90: {
                return (BlockState)((BlockState)((BlockState)((BlockState)state.with(NORTH, state.get(EAST))).with(EAST, state.get(SOUTH))).with(SOUTH, state.get(WEST))).with(WEST, state.get(NORTH));
            }
            case CLOCKWISE_90: {
                return (BlockState)((BlockState)((BlockState)((BlockState)state.with(NORTH, state.get(WEST))).with(EAST, state.get(NORTH))).with(SOUTH, state.get(EAST))).with(WEST, state.get(SOUTH));
            }
        }
        return state;
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        switch (mirror) {
            case LEFT_RIGHT: {
                return (BlockState)((BlockState)state.with(NORTH, state.get(SOUTH))).with(SOUTH, state.get(NORTH));
            }
            case FRONT_BACK: {
                return (BlockState)((BlockState)state.with(EAST, state.get(WEST))).with(WEST, state.get(EAST));
            }
        }
        return super.mirror(state, mirror);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(POWERED, ATTACHED, DISARMED, NORTH, EAST, WEST, SOUTH);
    }
}

