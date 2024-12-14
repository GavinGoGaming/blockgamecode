/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractChestBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoubleBlockProperties;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.LidOpenable;
import net.minecraft.block.enums.ChestType;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class ChestBlock
extends AbstractChestBlock<ChestBlockEntity>
implements Waterloggable {
    public static final MapCodec<ChestBlock> CODEC = ChestBlock.createCodec(settings -> new ChestBlock((AbstractBlock.Settings)settings, () -> BlockEntityType.CHEST));
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
    public static final EnumProperty<ChestType> CHEST_TYPE = Properties.CHEST_TYPE;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final int field_31057 = 1;
    protected static final int field_31058 = 1;
    protected static final int field_31059 = 14;
    protected static final VoxelShape DOUBLE_NORTH_SHAPE = Block.createCuboidShape(1.0, 0.0, 0.0, 15.0, 14.0, 15.0);
    protected static final VoxelShape DOUBLE_SOUTH_SHAPE = Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 14.0, 16.0);
    protected static final VoxelShape DOUBLE_WEST_SHAPE = Block.createCuboidShape(0.0, 0.0, 1.0, 15.0, 14.0, 15.0);
    protected static final VoxelShape DOUBLE_EAST_SHAPE = Block.createCuboidShape(1.0, 0.0, 1.0, 16.0, 14.0, 15.0);
    protected static final VoxelShape SINGLE_SHAPE = Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 14.0, 15.0);
    private static final DoubleBlockProperties.PropertyRetriever<ChestBlockEntity, Optional<Inventory>> INVENTORY_RETRIEVER = new DoubleBlockProperties.PropertyRetriever<ChestBlockEntity, Optional<Inventory>>(){

        @Override
        public Optional<Inventory> getFromBoth(ChestBlockEntity arg, ChestBlockEntity arg2) {
            return Optional.of(new DoubleInventory(arg, arg2));
        }

        @Override
        public Optional<Inventory> getFrom(ChestBlockEntity arg) {
            return Optional.of(arg);
        }

        @Override
        public Optional<Inventory> getFallback() {
            return Optional.empty();
        }

        @Override
        public /* synthetic */ Object getFallback() {
            return this.getFallback();
        }
    };
    private static final DoubleBlockProperties.PropertyRetriever<ChestBlockEntity, Optional<NamedScreenHandlerFactory>> NAME_RETRIEVER = new DoubleBlockProperties.PropertyRetriever<ChestBlockEntity, Optional<NamedScreenHandlerFactory>>(){

        @Override
        public Optional<NamedScreenHandlerFactory> getFromBoth(final ChestBlockEntity arg, final ChestBlockEntity arg2) {
            final DoubleInventory lv = new DoubleInventory(arg, arg2);
            return Optional.of(new NamedScreenHandlerFactory(){

                @Override
                @Nullable
                public ScreenHandler createMenu(int i, PlayerInventory arg3, PlayerEntity arg22) {
                    if (arg.checkUnlocked(arg22) && arg2.checkUnlocked(arg22)) {
                        arg.generateLoot(arg3.player);
                        arg2.generateLoot(arg3.player);
                        return GenericContainerScreenHandler.createGeneric9x6(i, arg3, lv);
                    }
                    return null;
                }

                @Override
                public Text getDisplayName() {
                    if (arg.hasCustomName()) {
                        return arg.getDisplayName();
                    }
                    if (arg2.hasCustomName()) {
                        return arg2.getDisplayName();
                    }
                    return Text.translatable("container.chestDouble");
                }
            });
        }

        @Override
        public Optional<NamedScreenHandlerFactory> getFrom(ChestBlockEntity arg) {
            return Optional.of(arg);
        }

        @Override
        public Optional<NamedScreenHandlerFactory> getFallback() {
            return Optional.empty();
        }

        @Override
        public /* synthetic */ Object getFallback() {
            return this.getFallback();
        }
    };

    @Override
    public MapCodec<? extends ChestBlock> getCodec() {
        return CODEC;
    }

    protected ChestBlock(AbstractBlock.Settings arg, Supplier<BlockEntityType<? extends ChestBlockEntity>> supplier) {
        super(arg, supplier);
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(CHEST_TYPE, ChestType.SINGLE)).with(WATERLOGGED, false));
    }

    public static DoubleBlockProperties.Type getDoubleBlockType(BlockState state) {
        ChestType lv = state.get(CHEST_TYPE);
        if (lv == ChestType.SINGLE) {
            return DoubleBlockProperties.Type.SINGLE;
        }
        if (lv == ChestType.RIGHT) {
            return DoubleBlockProperties.Type.FIRST;
        }
        return DoubleBlockProperties.Type.SECOND;
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED).booleanValue()) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        if (neighborState.isOf(this) && direction.getAxis().isHorizontal()) {
            ChestType lv = neighborState.get(CHEST_TYPE);
            if (state.get(CHEST_TYPE) == ChestType.SINGLE && lv != ChestType.SINGLE && state.get(FACING) == neighborState.get(FACING) && ChestBlock.getFacing(neighborState) == direction.getOpposite()) {
                return (BlockState)state.with(CHEST_TYPE, lv.getOpposite());
            }
        } else if (ChestBlock.getFacing(state) == direction) {
            return (BlockState)state.with(CHEST_TYPE, ChestType.SINGLE);
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (state.get(CHEST_TYPE) == ChestType.SINGLE) {
            return SINGLE_SHAPE;
        }
        switch (ChestBlock.getFacing(state)) {
            default: {
                return DOUBLE_NORTH_SHAPE;
            }
            case SOUTH: {
                return DOUBLE_SOUTH_SHAPE;
            }
            case WEST: {
                return DOUBLE_WEST_SHAPE;
            }
            case EAST: 
        }
        return DOUBLE_EAST_SHAPE;
    }

    public static Direction getFacing(BlockState state) {
        Direction lv = state.get(FACING);
        return state.get(CHEST_TYPE) == ChestType.LEFT ? lv.rotateYClockwise() : lv.rotateYCounterclockwise();
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction lv5;
        ChestType lv = ChestType.SINGLE;
        Direction lv2 = ctx.getHorizontalPlayerFacing().getOpposite();
        FluidState lv3 = ctx.getWorld().getFluidState(ctx.getBlockPos());
        boolean bl = ctx.shouldCancelInteraction();
        Direction lv4 = ctx.getSide();
        if (lv4.getAxis().isHorizontal() && bl && (lv5 = this.getNeighborChestDirection(ctx, lv4.getOpposite())) != null && lv5.getAxis() != lv4.getAxis()) {
            lv2 = lv5;
            ChestType chestType = lv = lv2.rotateYCounterclockwise() == lv4.getOpposite() ? ChestType.RIGHT : ChestType.LEFT;
        }
        if (lv == ChestType.SINGLE && !bl) {
            if (lv2 == this.getNeighborChestDirection(ctx, lv2.rotateYClockwise())) {
                lv = ChestType.LEFT;
            } else if (lv2 == this.getNeighborChestDirection(ctx, lv2.rotateYCounterclockwise())) {
                lv = ChestType.RIGHT;
            }
        }
        return (BlockState)((BlockState)((BlockState)this.getDefaultState().with(FACING, lv2)).with(CHEST_TYPE, lv)).with(WATERLOGGED, lv3.getFluid() == Fluids.WATER);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        if (state.get(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getStill(false);
        }
        return super.getFluidState(state);
    }

    @Nullable
    private Direction getNeighborChestDirection(ItemPlacementContext ctx, Direction dir) {
        BlockState lv = ctx.getWorld().getBlockState(ctx.getBlockPos().offset(dir));
        return lv.isOf(this) && lv.get(CHEST_TYPE) == ChestType.SINGLE ? lv.get(FACING) : null;
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        ItemScatterer.onStateReplaced(state, newState, world, pos);
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }
        NamedScreenHandlerFactory lv = this.createScreenHandlerFactory(state, world, pos);
        if (lv != null) {
            player.openHandledScreen(lv);
            player.incrementStat(this.getOpenStat());
            PiglinBrain.onGuardedBlockInteracted(player, true);
        }
        return ActionResult.CONSUME;
    }

    protected Stat<Identifier> getOpenStat() {
        return Stats.CUSTOM.getOrCreateStat(Stats.OPEN_CHEST);
    }

    public BlockEntityType<? extends ChestBlockEntity> getExpectedEntityType() {
        return (BlockEntityType)this.entityTypeRetriever.get();
    }

    @Nullable
    public static Inventory getInventory(ChestBlock block, BlockState state, World world, BlockPos pos, boolean ignoreBlocked) {
        return block.getBlockEntitySource(state, world, pos, ignoreBlocked).apply(INVENTORY_RETRIEVER).orElse(null);
    }

    @Override
    public DoubleBlockProperties.PropertySource<? extends ChestBlockEntity> getBlockEntitySource(BlockState state, World world, BlockPos pos, boolean ignoreBlocked) {
        BiPredicate<WorldAccess, BlockPos> biPredicate = ignoreBlocked ? (worldx, posx) -> false : ChestBlock::isChestBlocked;
        return DoubleBlockProperties.toPropertySource((BlockEntityType)this.entityTypeRetriever.get(), ChestBlock::getDoubleBlockType, ChestBlock::getFacing, FACING, state, world, pos, biPredicate);
    }

    @Override
    @Nullable
    protected NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        return this.getBlockEntitySource(state, world, pos, false).apply(NAME_RETRIEVER).orElse(null);
    }

    public static DoubleBlockProperties.PropertyRetriever<ChestBlockEntity, Float2FloatFunction> getAnimationProgressRetriever(final LidOpenable progress) {
        return new DoubleBlockProperties.PropertyRetriever<ChestBlockEntity, Float2FloatFunction>(){

            @Override
            public Float2FloatFunction getFromBoth(ChestBlockEntity arg, ChestBlockEntity arg2) {
                return tickDelta -> Math.max(arg.getAnimationProgress(tickDelta), arg2.getAnimationProgress(tickDelta));
            }

            @Override
            public Float2FloatFunction getFrom(ChestBlockEntity arg) {
                return arg::getAnimationProgress;
            }

            @Override
            public Float2FloatFunction getFallback() {
                return progress::getAnimationProgress;
            }

            @Override
            public /* synthetic */ Object getFallback() {
                return this.getFallback();
            }
        };
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ChestBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? ChestBlock.validateTicker(type, this.getExpectedEntityType(), ChestBlockEntity::clientTick) : null;
    }

    public static boolean isChestBlocked(WorldAccess world, BlockPos pos) {
        return ChestBlock.hasBlockOnTop(world, pos) || ChestBlock.hasCatOnTop(world, pos);
    }

    private static boolean hasBlockOnTop(BlockView world, BlockPos pos) {
        BlockPos lv = pos.up();
        return world.getBlockState(lv).isSolidBlock(world, lv);
    }

    private static boolean hasCatOnTop(WorldAccess world, BlockPos pos) {
        List<CatEntity> list = world.getNonSpectatingEntities(CatEntity.class, new Box(pos.getX(), pos.getY() + 1, pos.getZ(), pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1));
        if (!list.isEmpty()) {
            for (CatEntity lv : list) {
                if (!lv.isInSittingPose()) continue;
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return ScreenHandler.calculateComparatorOutput(ChestBlock.getInventory(this, state, world, pos, false));
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
        builder.add(FACING, CHEST_TYPE, WATERLOGGED);
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        BlockEntity lv = world.getBlockEntity(pos);
        if (lv instanceof ChestBlockEntity) {
            ((ChestBlockEntity)lv).onScheduledTick();
        }
    }
}

