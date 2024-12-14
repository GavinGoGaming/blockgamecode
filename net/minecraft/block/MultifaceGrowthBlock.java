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
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.block.LichenGrower;
import net.minecraft.block.ShapeContext;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public abstract class MultifaceGrowthBlock
extends Block {
    private static final float field_31194 = 1.0f;
    private static final VoxelShape UP_SHAPE = Block.createCuboidShape(0.0, 15.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape DOWN_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
    private static final VoxelShape EAST_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 1.0, 16.0, 16.0);
    private static final VoxelShape WEST_SHAPE = Block.createCuboidShape(15.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape SOUTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 1.0);
    private static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 15.0, 16.0, 16.0, 16.0);
    private static final Map<Direction, BooleanProperty> FACING_PROPERTIES = ConnectingBlock.FACING_PROPERTIES;
    private static final Map<Direction, VoxelShape> SHAPES_FOR_DIRECTIONS = Util.make(Maps.newEnumMap(Direction.class), shapes -> {
        shapes.put(Direction.NORTH, SOUTH_SHAPE);
        shapes.put(Direction.EAST, WEST_SHAPE);
        shapes.put(Direction.SOUTH, NORTH_SHAPE);
        shapes.put(Direction.WEST, EAST_SHAPE);
        shapes.put(Direction.UP, UP_SHAPE);
        shapes.put(Direction.DOWN, DOWN_SHAPE);
    });
    protected static final Direction[] DIRECTIONS = Direction.values();
    private final ImmutableMap<BlockState, VoxelShape> SHAPES;
    private final boolean hasAllHorizontalDirections;
    private final boolean canMirrorX;
    private final boolean canMirrorZ;

    public MultifaceGrowthBlock(AbstractBlock.Settings arg) {
        super(arg);
        this.setDefaultState(MultifaceGrowthBlock.withAllDirections(this.stateManager));
        this.SHAPES = this.getShapesForStates(MultifaceGrowthBlock::getShapeForState);
        this.hasAllHorizontalDirections = Direction.Type.HORIZONTAL.stream().allMatch(this::canHaveDirection);
        this.canMirrorX = Direction.Type.HORIZONTAL.stream().filter(Direction.Axis.X).filter(this::canHaveDirection).count() % 2L == 0L;
        this.canMirrorZ = Direction.Type.HORIZONTAL.stream().filter(Direction.Axis.Z).filter(this::canHaveDirection).count() % 2L == 0L;
    }

    protected abstract MapCodec<? extends MultifaceGrowthBlock> getCodec();

    public static Set<Direction> collectDirections(BlockState state) {
        if (!(state.getBlock() instanceof MultifaceGrowthBlock)) {
            return Set.of();
        }
        EnumSet<Direction> set = EnumSet.noneOf(Direction.class);
        for (Direction lv : Direction.values()) {
            if (!MultifaceGrowthBlock.hasDirection(state, lv)) continue;
            set.add(lv);
        }
        return set;
    }

    public static Set<Direction> flagToDirections(byte flag) {
        EnumSet<Direction> set = EnumSet.noneOf(Direction.class);
        for (Direction lv : Direction.values()) {
            if ((flag & (byte)(1 << lv.ordinal())) <= 0) continue;
            set.add(lv);
        }
        return set;
    }

    public static byte directionsToFlag(Collection<Direction> directions) {
        byte b = 0;
        for (Direction lv : directions) {
            b = (byte)(b | 1 << lv.ordinal());
        }
        return b;
    }

    protected boolean canHaveDirection(Direction direction) {
        return true;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        for (Direction lv : DIRECTIONS) {
            if (!this.canHaveDirection(lv)) continue;
            builder.add(MultifaceGrowthBlock.getProperty(lv));
        }
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (!MultifaceGrowthBlock.hasAnyDirection(state)) {
            return Blocks.AIR.getDefaultState();
        }
        if (!MultifaceGrowthBlock.hasDirection(state, direction) || MultifaceGrowthBlock.canGrowOn(world, direction, neighborPos, neighborState)) {
            return state;
        }
        return MultifaceGrowthBlock.disableDirection(state, MultifaceGrowthBlock.getProperty(direction));
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.SHAPES.get(state);
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        boolean bl = false;
        for (Direction lv : DIRECTIONS) {
            if (!MultifaceGrowthBlock.hasDirection(state, lv)) continue;
            BlockPos lv2 = pos.offset(lv);
            if (!MultifaceGrowthBlock.canGrowOn(world, lv, lv2, world.getBlockState(lv2))) {
                return false;
            }
            bl = true;
        }
        return bl;
    }

    @Override
    protected boolean canReplace(BlockState state, ItemPlacementContext context) {
        return MultifaceGrowthBlock.isNotFullBlock(state);
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        World lv = ctx.getWorld();
        BlockPos lv2 = ctx.getBlockPos();
        BlockState lv3 = lv.getBlockState(lv2);
        return Arrays.stream(ctx.getPlacementDirections()).map(direction -> this.withDirection(lv3, lv, lv2, (Direction)direction)).filter(Objects::nonNull).findFirst().orElse(null);
    }

    public boolean canGrowWithDirection(BlockView world, BlockState state, BlockPos pos, Direction direction) {
        if (!this.canHaveDirection(direction) || state.isOf(this) && MultifaceGrowthBlock.hasDirection(state, direction)) {
            return false;
        }
        BlockPos lv = pos.offset(direction);
        return MultifaceGrowthBlock.canGrowOn(world, direction, lv, world.getBlockState(lv));
    }

    @Nullable
    public BlockState withDirection(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if (!this.canGrowWithDirection(world, state, pos, direction)) {
            return null;
        }
        BlockState lv = state.isOf(this) ? state : (this.isWaterlogged() && state.getFluidState().isEqualAndStill(Fluids.WATER) ? (BlockState)this.getDefaultState().with(Properties.WATERLOGGED, true) : this.getDefaultState());
        return (BlockState)lv.with(MultifaceGrowthBlock.getProperty(direction), true);
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        if (!this.hasAllHorizontalDirections) {
            return state;
        }
        return this.mirror(state, rotation::rotate);
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        if (mirror == BlockMirror.FRONT_BACK && !this.canMirrorX) {
            return state;
        }
        if (mirror == BlockMirror.LEFT_RIGHT && !this.canMirrorZ) {
            return state;
        }
        return this.mirror(state, mirror::apply);
    }

    private BlockState mirror(BlockState state, Function<Direction, Direction> mirror) {
        BlockState lv = state;
        for (Direction lv2 : DIRECTIONS) {
            if (!this.canHaveDirection(lv2)) continue;
            lv = (BlockState)lv.with(MultifaceGrowthBlock.getProperty(mirror.apply(lv2)), state.get(MultifaceGrowthBlock.getProperty(lv2)));
        }
        return lv;
    }

    public static boolean hasDirection(BlockState state, Direction direction) {
        BooleanProperty lv = MultifaceGrowthBlock.getProperty(direction);
        return state.contains(lv) && state.get(lv) != false;
    }

    public static boolean canGrowOn(BlockView world, Direction direction, BlockPos pos, BlockState state) {
        return Block.isFaceFullSquare(state.getSidesShape(world, pos), direction.getOpposite()) || Block.isFaceFullSquare(state.getCollisionShape(world, pos), direction.getOpposite());
    }

    private boolean isWaterlogged() {
        return this.stateManager.getProperties().contains(Properties.WATERLOGGED);
    }

    private static BlockState disableDirection(BlockState state, BooleanProperty direction) {
        BlockState lv = (BlockState)state.with(direction, false);
        if (MultifaceGrowthBlock.hasAnyDirection(lv)) {
            return lv;
        }
        return Blocks.AIR.getDefaultState();
    }

    public static BooleanProperty getProperty(Direction direction) {
        return FACING_PROPERTIES.get(direction);
    }

    private static BlockState withAllDirections(StateManager<Block, BlockState> stateManager) {
        BlockState lv = stateManager.getDefaultState();
        for (BooleanProperty lv2 : FACING_PROPERTIES.values()) {
            if (!lv.contains(lv2)) continue;
            lv = (BlockState)lv.with(lv2, false);
        }
        return lv;
    }

    private static VoxelShape getShapeForState(BlockState state) {
        VoxelShape lv = VoxelShapes.empty();
        for (Direction lv2 : DIRECTIONS) {
            if (!MultifaceGrowthBlock.hasDirection(state, lv2)) continue;
            lv = VoxelShapes.union(lv, SHAPES_FOR_DIRECTIONS.get(lv2));
        }
        return lv.isEmpty() ? VoxelShapes.fullCube() : lv;
    }

    protected static boolean hasAnyDirection(BlockState state) {
        return Arrays.stream(DIRECTIONS).anyMatch(direction -> MultifaceGrowthBlock.hasDirection(state, direction));
    }

    private static boolean isNotFullBlock(BlockState state) {
        return Arrays.stream(DIRECTIONS).anyMatch(direction -> !MultifaceGrowthBlock.hasDirection(state, direction));
    }

    public abstract LichenGrower getGrower();
}

