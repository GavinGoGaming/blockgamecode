/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.block.MultifaceGrowthBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class VineBlock
extends Block {
    public static final MapCodec<VineBlock> CODEC = VineBlock.createCodec(VineBlock::new);
    public static final BooleanProperty UP = ConnectingBlock.UP;
    public static final BooleanProperty NORTH = ConnectingBlock.NORTH;
    public static final BooleanProperty EAST = ConnectingBlock.EAST;
    public static final BooleanProperty SOUTH = ConnectingBlock.SOUTH;
    public static final BooleanProperty WEST = ConnectingBlock.WEST;
    public static final Map<Direction, BooleanProperty> FACING_PROPERTIES = ConnectingBlock.FACING_PROPERTIES.entrySet().stream().filter(entry -> entry.getKey() != Direction.DOWN).collect(Util.toMap());
    protected static final float field_31275 = 1.0f;
    private static final VoxelShape UP_SHAPE = Block.createCuboidShape(0.0, 15.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape EAST_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 1.0, 16.0, 16.0);
    private static final VoxelShape WEST_SHAPE = Block.createCuboidShape(15.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape SOUTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 1.0);
    private static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 15.0, 16.0, 16.0, 16.0);
    private final Map<BlockState, VoxelShape> shapesByState;

    public MapCodec<VineBlock> getCodec() {
        return CODEC;
    }

    public VineBlock(AbstractBlock.Settings arg) {
        super(arg);
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(UP, false)).with(NORTH, false)).with(EAST, false)).with(SOUTH, false)).with(WEST, false));
        this.shapesByState = ImmutableMap.copyOf(this.stateManager.getStates().stream().collect(Collectors.toMap(Function.identity(), VineBlock::getShapeForState)));
    }

    private static VoxelShape getShapeForState(BlockState state) {
        VoxelShape lv = VoxelShapes.empty();
        if (state.get(UP).booleanValue()) {
            lv = UP_SHAPE;
        }
        if (state.get(NORTH).booleanValue()) {
            lv = VoxelShapes.union(lv, SOUTH_SHAPE);
        }
        if (state.get(SOUTH).booleanValue()) {
            lv = VoxelShapes.union(lv, NORTH_SHAPE);
        }
        if (state.get(EAST).booleanValue()) {
            lv = VoxelShapes.union(lv, WEST_SHAPE);
        }
        if (state.get(WEST).booleanValue()) {
            lv = VoxelShapes.union(lv, EAST_SHAPE);
        }
        return lv.isEmpty() ? VoxelShapes.fullCube() : lv;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.shapesByState.get(state);
    }

    @Override
    protected boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
        return true;
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return this.hasAdjacentBlocks(this.getPlacementShape(state, world, pos));
    }

    private boolean hasAdjacentBlocks(BlockState state) {
        return this.getAdjacentBlockCount(state) > 0;
    }

    private int getAdjacentBlockCount(BlockState state) {
        int i = 0;
        for (BooleanProperty lv : FACING_PROPERTIES.values()) {
            if (!state.get(lv).booleanValue()) continue;
            ++i;
        }
        return i;
    }

    private boolean shouldHaveSide(BlockView world, BlockPos pos, Direction side) {
        if (side == Direction.DOWN) {
            return false;
        }
        BlockPos lv = pos.offset(side);
        if (VineBlock.shouldConnectTo(world, lv, side)) {
            return true;
        }
        if (side.getAxis() != Direction.Axis.Y) {
            BooleanProperty lv2 = FACING_PROPERTIES.get(side);
            BlockState lv3 = world.getBlockState(pos.up());
            return lv3.isOf(this) && lv3.get(lv2) != false;
        }
        return false;
    }

    public static boolean shouldConnectTo(BlockView world, BlockPos pos, Direction direction) {
        return MultifaceGrowthBlock.canGrowOn(world, direction, pos, world.getBlockState(pos));
    }

    private BlockState getPlacementShape(BlockState state, BlockView world, BlockPos pos) {
        BlockPos lv = pos.up();
        if (state.get(UP).booleanValue()) {
            state = (BlockState)state.with(UP, VineBlock.shouldConnectTo(world, lv, Direction.DOWN));
        }
        AbstractBlock.AbstractBlockState lv2 = null;
        for (Direction lv3 : Direction.Type.HORIZONTAL) {
            BooleanProperty lv4 = VineBlock.getFacingProperty(lv3);
            if (!state.get(lv4).booleanValue()) continue;
            boolean bl = this.shouldHaveSide(world, pos, lv3);
            if (!bl) {
                if (lv2 == null) {
                    lv2 = world.getBlockState(lv);
                }
                bl = lv2.isOf(this) && lv2.get(lv4) != false;
            }
            state = (BlockState)state.with(lv4, bl);
        }
        return state;
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.DOWN) {
            return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
        }
        BlockState lv = this.getPlacementShape(state, world, pos);
        if (!this.hasAdjacentBlocks(lv)) {
            return Blocks.AIR.getDefaultState();
        }
        return lv;
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        BlockState lv12;
        BlockState lv11;
        BlockPos lv3;
        BlockState lv4;
        if (!world.getGameRules().getBoolean(GameRules.DO_VINES_SPREAD)) {
            return;
        }
        if (random.nextInt(4) != 0) {
            return;
        }
        Direction lv = Direction.random(random);
        BlockPos lv2 = pos.up();
        if (lv.getAxis().isHorizontal() && !state.get(VineBlock.getFacingProperty(lv)).booleanValue()) {
            if (!this.canGrowAt(world, pos)) {
                return;
            }
            BlockPos lv32 = pos.offset(lv);
            BlockState lv42 = world.getBlockState(lv32);
            if (lv42.isAir()) {
                Direction lv5 = lv.rotateYClockwise();
                Direction lv6 = lv.rotateYCounterclockwise();
                boolean bl = state.get(VineBlock.getFacingProperty(lv5));
                boolean bl2 = state.get(VineBlock.getFacingProperty(lv6));
                BlockPos lv7 = lv32.offset(lv5);
                BlockPos lv8 = lv32.offset(lv6);
                if (bl && VineBlock.shouldConnectTo(world, lv7, lv5)) {
                    world.setBlockState(lv32, (BlockState)this.getDefaultState().with(VineBlock.getFacingProperty(lv5), true), Block.NOTIFY_LISTENERS);
                } else if (bl2 && VineBlock.shouldConnectTo(world, lv8, lv6)) {
                    world.setBlockState(lv32, (BlockState)this.getDefaultState().with(VineBlock.getFacingProperty(lv6), true), Block.NOTIFY_LISTENERS);
                } else {
                    Direction lv9 = lv.getOpposite();
                    if (bl && world.isAir(lv7) && VineBlock.shouldConnectTo(world, pos.offset(lv5), lv9)) {
                        world.setBlockState(lv7, (BlockState)this.getDefaultState().with(VineBlock.getFacingProperty(lv9), true), Block.NOTIFY_LISTENERS);
                    } else if (bl2 && world.isAir(lv8) && VineBlock.shouldConnectTo(world, pos.offset(lv6), lv9)) {
                        world.setBlockState(lv8, (BlockState)this.getDefaultState().with(VineBlock.getFacingProperty(lv9), true), Block.NOTIFY_LISTENERS);
                    } else if ((double)random.nextFloat() < 0.05 && VineBlock.shouldConnectTo(world, lv32.up(), Direction.UP)) {
                        world.setBlockState(lv32, (BlockState)this.getDefaultState().with(UP, true), Block.NOTIFY_LISTENERS);
                    }
                }
            } else if (VineBlock.shouldConnectTo(world, lv32, lv)) {
                world.setBlockState(pos, (BlockState)state.with(VineBlock.getFacingProperty(lv), true), Block.NOTIFY_LISTENERS);
            }
            return;
        }
        if (lv == Direction.UP && pos.getY() < world.getTopY() - 1) {
            if (this.shouldHaveSide(world, pos, lv)) {
                world.setBlockState(pos, (BlockState)state.with(UP, true), Block.NOTIFY_LISTENERS);
                return;
            }
            if (world.isAir(lv2)) {
                if (!this.canGrowAt(world, pos)) {
                    return;
                }
                BlockState lv10 = state;
                for (Direction lv5 : Direction.Type.HORIZONTAL) {
                    if (!random.nextBoolean() && VineBlock.shouldConnectTo(world, lv2.offset(lv5), lv5)) continue;
                    lv10 = (BlockState)lv10.with(VineBlock.getFacingProperty(lv5), false);
                }
                if (this.hasHorizontalSide(lv10)) {
                    world.setBlockState(lv2, lv10, Block.NOTIFY_LISTENERS);
                }
                return;
            }
        }
        if (pos.getY() > world.getBottomY() && ((lv4 = world.getBlockState(lv3 = pos.down())).isAir() || lv4.isOf(this)) && (lv11 = lv4.isAir() ? this.getDefaultState() : lv4) != (lv12 = this.getGrownState(state, lv11, random)) && this.hasHorizontalSide(lv12)) {
            world.setBlockState(lv3, lv12, Block.NOTIFY_LISTENERS);
        }
    }

    private BlockState getGrownState(BlockState above, BlockState state, Random random) {
        for (Direction lv : Direction.Type.HORIZONTAL) {
            BooleanProperty lv2;
            if (!random.nextBoolean() || !above.get(lv2 = VineBlock.getFacingProperty(lv)).booleanValue()) continue;
            state = (BlockState)state.with(lv2, true);
        }
        return state;
    }

    private boolean hasHorizontalSide(BlockState state) {
        return state.get(NORTH) != false || state.get(EAST) != false || state.get(SOUTH) != false || state.get(WEST) != false;
    }

    private boolean canGrowAt(BlockView world, BlockPos pos) {
        int i = 4;
        Iterable<BlockPos> iterable = BlockPos.iterate(pos.getX() - 4, pos.getY() - 1, pos.getZ() - 4, pos.getX() + 4, pos.getY() + 1, pos.getZ() + 4);
        int j = 5;
        for (BlockPos lv : iterable) {
            if (!world.getBlockState(lv).isOf(this) || --j > 0) continue;
            return false;
        }
        return true;
    }

    @Override
    protected boolean canReplace(BlockState state, ItemPlacementContext context) {
        BlockState lv = context.getWorld().getBlockState(context.getBlockPos());
        if (lv.isOf(this)) {
            return this.getAdjacentBlockCount(lv) < FACING_PROPERTIES.size();
        }
        return super.canReplace(state, context);
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState lv = ctx.getWorld().getBlockState(ctx.getBlockPos());
        boolean bl = lv.isOf(this);
        BlockState lv2 = bl ? lv : this.getDefaultState();
        for (Direction lv3 : ctx.getPlacementDirections()) {
            boolean bl2;
            if (lv3 == Direction.DOWN) continue;
            BooleanProperty lv4 = VineBlock.getFacingProperty(lv3);
            boolean bl3 = bl2 = bl && lv.get(lv4) != false;
            if (bl2 || !this.shouldHaveSide(ctx.getWorld(), ctx.getBlockPos(), lv3)) continue;
            return (BlockState)lv2.with(lv4, true);
        }
        return bl ? lv2 : null;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(UP, NORTH, EAST, SOUTH, WEST);
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

    public static BooleanProperty getFacingProperty(Direction direction) {
        return FACING_PROPERTIES.get(direction);
    }
}

