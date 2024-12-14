/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.BiConsumer;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSetType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.block.enums.DoorHinge;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

public class DoorBlock
extends Block {
    public static final MapCodec<DoorBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)BlockSetType.CODEC.fieldOf("block_set_type")).forGetter(DoorBlock::getBlockSetType), DoorBlock.createSettingsCodec()).apply((Applicative<DoorBlock, ?>)instance, DoorBlock::new));
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
    public static final BooleanProperty OPEN = Properties.OPEN;
    public static final EnumProperty<DoorHinge> HINGE = Properties.DOOR_HINGE;
    public static final BooleanProperty POWERED = Properties.POWERED;
    public static final EnumProperty<DoubleBlockHalf> HALF = Properties.DOUBLE_BLOCK_HALF;
    protected static final float field_31083 = 3.0f;
    protected static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 3.0);
    protected static final VoxelShape SOUTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 13.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape EAST_SHAPE = Block.createCuboidShape(13.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape WEST_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 3.0, 16.0, 16.0);
    private final BlockSetType blockSetType;

    public MapCodec<? extends DoorBlock> getCodec() {
        return CODEC;
    }

    protected DoorBlock(BlockSetType type, AbstractBlock.Settings settings) {
        super(settings.sounds(type.soundType()));
        this.blockSetType = type;
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(OPEN, false)).with(HINGE, DoorHinge.LEFT)).with(POWERED, false)).with(HALF, DoubleBlockHalf.LOWER));
    }

    public BlockSetType getBlockSetType() {
        return this.blockSetType;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Direction lv = state.get(FACING);
        boolean bl = state.get(OPEN) == false;
        boolean bl2 = state.get(HINGE) == DoorHinge.RIGHT;
        return switch (lv) {
            default -> {
                if (bl) {
                    yield WEST_SHAPE;
                }
                if (bl2) {
                    yield SOUTH_SHAPE;
                }
                yield NORTH_SHAPE;
            }
            case Direction.SOUTH -> {
                if (bl) {
                    yield NORTH_SHAPE;
                }
                if (bl2) {
                    yield WEST_SHAPE;
                }
                yield EAST_SHAPE;
            }
            case Direction.WEST -> {
                if (bl) {
                    yield EAST_SHAPE;
                }
                if (bl2) {
                    yield NORTH_SHAPE;
                }
                yield SOUTH_SHAPE;
            }
            case Direction.NORTH -> bl ? SOUTH_SHAPE : (bl2 ? EAST_SHAPE : WEST_SHAPE);
        };
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        DoubleBlockHalf lv = state.get(HALF);
        if (direction.getAxis() == Direction.Axis.Y && lv == DoubleBlockHalf.LOWER == (direction == Direction.UP)) {
            if (neighborState.getBlock() instanceof DoorBlock && neighborState.get(HALF) != lv) {
                return (BlockState)neighborState.with(HALF, lv);
            }
            return Blocks.AIR.getDefaultState();
        }
        if (lv == DoubleBlockHalf.LOWER && direction == Direction.DOWN && !state.canPlaceAt(world, pos)) {
            return Blocks.AIR.getDefaultState();
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    protected void onExploded(BlockState state, World world, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> stackMerger) {
        if (explosion.canTriggerBlocks() && state.get(HALF) == DoubleBlockHalf.LOWER && this.blockSetType.canOpenByWindCharge() && !state.get(POWERED).booleanValue()) {
            this.setOpen(null, world, state, pos, !this.isOpen(state));
        }
        super.onExploded(state, world, pos, explosion, stackMerger);
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!(world.isClient || !player.isCreative() && player.canHarvest(state))) {
            TallPlantBlock.onBreakInCreative(world, pos, state, player);
        }
        return super.onBreak(world, pos, state, player);
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return switch (type) {
            default -> throw new MatchException(null, null);
            case NavigationType.LAND, NavigationType.AIR -> state.get(OPEN);
            case NavigationType.WATER -> false;
        };
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos lv = ctx.getBlockPos();
        World lv2 = ctx.getWorld();
        if (lv.getY() < lv2.getTopY() - 1 && lv2.getBlockState(lv.up()).canReplace(ctx)) {
            boolean bl = lv2.isReceivingRedstonePower(lv) || lv2.isReceivingRedstonePower(lv.up());
            return (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing())).with(HINGE, this.getHinge(ctx))).with(POWERED, bl)).with(OPEN, bl)).with(HALF, DoubleBlockHalf.LOWER);
        }
        return null;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        world.setBlockState(pos.up(), (BlockState)state.with(HALF, DoubleBlockHalf.UPPER), Block.NOTIFY_ALL);
    }

    private DoorHinge getHinge(ItemPlacementContext ctx) {
        boolean bl2;
        World lv = ctx.getWorld();
        BlockPos lv2 = ctx.getBlockPos();
        Direction lv3 = ctx.getHorizontalPlayerFacing();
        BlockPos lv4 = lv2.up();
        Direction lv5 = lv3.rotateYCounterclockwise();
        BlockPos lv6 = lv2.offset(lv5);
        BlockState lv7 = lv.getBlockState(lv6);
        BlockPos lv8 = lv4.offset(lv5);
        BlockState lv9 = lv.getBlockState(lv8);
        Direction lv10 = lv3.rotateYClockwise();
        BlockPos lv11 = lv2.offset(lv10);
        BlockState lv12 = lv.getBlockState(lv11);
        BlockPos lv13 = lv4.offset(lv10);
        BlockState lv14 = lv.getBlockState(lv13);
        int i = (lv7.isFullCube(lv, lv6) ? -1 : 0) + (lv9.isFullCube(lv, lv8) ? -1 : 0) + (lv12.isFullCube(lv, lv11) ? 1 : 0) + (lv14.isFullCube(lv, lv13) ? 1 : 0);
        boolean bl = lv7.getBlock() instanceof DoorBlock && lv7.get(HALF) == DoubleBlockHalf.LOWER;
        boolean bl3 = bl2 = lv12.getBlock() instanceof DoorBlock && lv12.get(HALF) == DoubleBlockHalf.LOWER;
        if (bl && !bl2 || i > 0) {
            return DoorHinge.RIGHT;
        }
        if (bl2 && !bl || i < 0) {
            return DoorHinge.LEFT;
        }
        int j = lv3.getOffsetX();
        int k = lv3.getOffsetZ();
        Vec3d lv15 = ctx.getHitPos();
        double d = lv15.x - (double)lv2.getX();
        double e = lv15.z - (double)lv2.getZ();
        return j < 0 && e < 0.5 || j > 0 && e > 0.5 || k < 0 && d > 0.5 || k > 0 && d < 0.5 ? DoorHinge.RIGHT : DoorHinge.LEFT;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!this.blockSetType.canOpenByHand()) {
            return ActionResult.PASS;
        }
        state = (BlockState)state.cycle(OPEN);
        world.setBlockState(pos, state, Block.NOTIFY_LISTENERS | Block.REDRAW_ON_MAIN_THREAD);
        this.playOpenCloseSound(player, world, pos, state.get(OPEN));
        world.emitGameEvent((Entity)player, this.isOpen(state) ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
        return ActionResult.success(world.isClient);
    }

    public boolean isOpen(BlockState state) {
        return state.get(OPEN);
    }

    public void setOpen(@Nullable Entity entity, World world, BlockState state, BlockPos pos, boolean open) {
        if (!state.isOf(this) || state.get(OPEN) == open) {
            return;
        }
        world.setBlockState(pos, (BlockState)state.with(OPEN, open), Block.NOTIFY_LISTENERS | Block.REDRAW_ON_MAIN_THREAD);
        this.playOpenCloseSound(entity, world, pos, open);
        world.emitGameEvent(entity, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        boolean bl2;
        boolean bl = world.isReceivingRedstonePower(pos) || world.isReceivingRedstonePower(pos.offset(state.get(HALF) == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN)) ? true : (bl2 = false);
        if (!this.getDefaultState().isOf(sourceBlock) && bl2 != state.get(POWERED)) {
            if (bl2 != state.get(OPEN)) {
                this.playOpenCloseSound(null, world, pos, bl2);
                world.emitGameEvent(null, bl2 ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
            }
            world.setBlockState(pos, (BlockState)((BlockState)state.with(POWERED, bl2)).with(OPEN, bl2), Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockPos lv = pos.down();
        BlockState lv2 = world.getBlockState(lv);
        if (state.get(HALF) == DoubleBlockHalf.LOWER) {
            return lv2.isSideSolidFullSquare(world, lv, Direction.UP);
        }
        return lv2.isOf(this);
    }

    private void playOpenCloseSound(@Nullable Entity entity, World world, BlockPos pos, boolean open) {
        world.playSound(entity, pos, open ? this.blockSetType.doorOpen() : this.blockSetType.doorClose(), SoundCategory.BLOCKS, 1.0f, world.getRandom().nextFloat() * 0.1f + 0.9f);
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return (BlockState)state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        if (mirror == BlockMirror.NONE) {
            return state;
        }
        return (BlockState)state.rotate(mirror.getRotation(state.get(FACING))).cycle(HINGE);
    }

    @Override
    protected long getRenderingSeed(BlockState state, BlockPos pos) {
        return MathHelper.hashCode(pos.getX(), pos.down(state.get(HALF) == DoubleBlockHalf.LOWER ? 0 : 1).getY(), pos.getZ());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(HALF, FACING, OPEN, HINGE, POWERED);
    }

    public static boolean canOpenByHand(World world, BlockPos pos) {
        return DoorBlock.canOpenByHand(world.getBlockState(pos));
    }

    public static boolean canOpenByHand(BlockState state) {
        DoorBlock lv;
        Block block = state.getBlock();
        return block instanceof DoorBlock && (lv = (DoorBlock)block).getBlockSetType().canOpenByHand();
    }
}

