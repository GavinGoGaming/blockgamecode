/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import java.util.function.BiConsumer;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.WallMountedBlock;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.Attachment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
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
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

public class BellBlock
extends BlockWithEntity {
    public static final MapCodec<BellBlock> CODEC = BellBlock.createCodec(BellBlock::new);
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
    public static final EnumProperty<Attachment> ATTACHMENT = Properties.ATTACHMENT;
    public static final BooleanProperty POWERED = Properties.POWERED;
    private static final VoxelShape NORTH_SOUTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 4.0, 16.0, 16.0, 12.0);
    private static final VoxelShape EAST_WEST_SHAPE = Block.createCuboidShape(4.0, 0.0, 0.0, 12.0, 16.0, 16.0);
    private static final VoxelShape BELL_WAIST_SHAPE = Block.createCuboidShape(5.0, 6.0, 5.0, 11.0, 13.0, 11.0);
    private static final VoxelShape BELL_LIP_SHAPE = Block.createCuboidShape(4.0, 4.0, 4.0, 12.0, 6.0, 12.0);
    private static final VoxelShape BELL_SHAPE = VoxelShapes.union(BELL_LIP_SHAPE, BELL_WAIST_SHAPE);
    private static final VoxelShape NORTH_SOUTH_WALLS_SHAPE = VoxelShapes.union(BELL_SHAPE, Block.createCuboidShape(7.0, 13.0, 0.0, 9.0, 15.0, 16.0));
    private static final VoxelShape EAST_WEST_WALLS_SHAPE = VoxelShapes.union(BELL_SHAPE, Block.createCuboidShape(0.0, 13.0, 7.0, 16.0, 15.0, 9.0));
    private static final VoxelShape WEST_WALL_SHAPE = VoxelShapes.union(BELL_SHAPE, Block.createCuboidShape(0.0, 13.0, 7.0, 13.0, 15.0, 9.0));
    private static final VoxelShape EAST_WALL_SHAPE = VoxelShapes.union(BELL_SHAPE, Block.createCuboidShape(3.0, 13.0, 7.0, 16.0, 15.0, 9.0));
    private static final VoxelShape NORTH_WALL_SHAPE = VoxelShapes.union(BELL_SHAPE, Block.createCuboidShape(7.0, 13.0, 0.0, 9.0, 15.0, 13.0));
    private static final VoxelShape SOUTH_WALL_SHAPE = VoxelShapes.union(BELL_SHAPE, Block.createCuboidShape(7.0, 13.0, 3.0, 9.0, 15.0, 16.0));
    private static final VoxelShape HANGING_SHAPE = VoxelShapes.union(BELL_SHAPE, Block.createCuboidShape(7.0, 13.0, 7.0, 9.0, 16.0, 9.0));
    public static final int field_31014 = 1;

    public MapCodec<BellBlock> getCodec() {
        return CODEC;
    }

    public BellBlock(AbstractBlock.Settings arg) {
        super(arg);
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(ATTACHMENT, Attachment.FLOOR)).with(POWERED, false));
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        boolean bl2 = world.isReceivingRedstonePower(pos);
        if (bl2 != state.get(POWERED)) {
            if (bl2) {
                this.ring(world, pos, null);
            }
            world.setBlockState(pos, (BlockState)state.with(POWERED, bl2), Block.NOTIFY_ALL);
        }
    }

    @Override
    protected void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
        Entity lv = projectile.getOwner();
        PlayerEntity lv2 = lv instanceof PlayerEntity ? (PlayerEntity)lv : null;
        this.ring(world, state, hit, lv2, true);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        return this.ring(world, state, hit, player, true) ? ActionResult.success(world.isClient) : ActionResult.PASS;
    }

    public boolean ring(World world, BlockState state, BlockHitResult hitResult, @Nullable PlayerEntity player, boolean checkHitPos) {
        boolean bl2;
        Direction lv = hitResult.getSide();
        BlockPos lv2 = hitResult.getBlockPos();
        boolean bl = bl2 = !checkHitPos || this.isPointOnBell(state, lv, hitResult.getPos().y - (double)lv2.getY());
        if (bl2) {
            boolean bl3 = this.ring(player, world, lv2, lv);
            if (bl3 && player != null) {
                player.incrementStat(Stats.BELL_RING);
            }
            return true;
        }
        return false;
    }

    private boolean isPointOnBell(BlockState state, Direction side, double y) {
        if (side.getAxis() == Direction.Axis.Y || y > (double)0.8124f) {
            return false;
        }
        Direction lv = state.get(FACING);
        Attachment lv2 = state.get(ATTACHMENT);
        switch (lv2) {
            case FLOOR: {
                return lv.getAxis() == side.getAxis();
            }
            case SINGLE_WALL: 
            case DOUBLE_WALL: {
                return lv.getAxis() != side.getAxis();
            }
            case CEILING: {
                return true;
            }
        }
        return false;
    }

    public boolean ring(World world, BlockPos pos, @Nullable Direction direction) {
        return this.ring(null, world, pos, direction);
    }

    public boolean ring(@Nullable Entity entity, World world, BlockPos pos, @Nullable Direction direction) {
        BlockEntity lv = world.getBlockEntity(pos);
        if (!world.isClient && lv instanceof BellBlockEntity) {
            if (direction == null) {
                direction = world.getBlockState(pos).get(FACING);
            }
            ((BellBlockEntity)lv).activate(direction);
            world.playSound(null, pos, SoundEvents.BLOCK_BELL_USE, SoundCategory.BLOCKS, 2.0f, 1.0f);
            world.emitGameEvent(entity, GameEvent.BLOCK_CHANGE, pos);
            return true;
        }
        return false;
    }

    private VoxelShape getShape(BlockState state) {
        Direction lv = state.get(FACING);
        Attachment lv2 = state.get(ATTACHMENT);
        if (lv2 == Attachment.FLOOR) {
            if (lv == Direction.NORTH || lv == Direction.SOUTH) {
                return NORTH_SOUTH_SHAPE;
            }
            return EAST_WEST_SHAPE;
        }
        if (lv2 == Attachment.CEILING) {
            return HANGING_SHAPE;
        }
        if (lv2 == Attachment.DOUBLE_WALL) {
            if (lv == Direction.NORTH || lv == Direction.SOUTH) {
                return NORTH_SOUTH_WALLS_SHAPE;
            }
            return EAST_WEST_WALLS_SHAPE;
        }
        if (lv == Direction.NORTH) {
            return NORTH_WALL_SHAPE;
        }
        if (lv == Direction.SOUTH) {
            return SOUTH_WALL_SHAPE;
        }
        if (lv == Direction.EAST) {
            return EAST_WALL_SHAPE;
        }
        return WEST_WALL_SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.getShape(state);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.getShape(state);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction lv = ctx.getSide();
        BlockPos lv2 = ctx.getBlockPos();
        World lv3 = ctx.getWorld();
        Direction.Axis lv4 = lv.getAxis();
        if (lv4 == Direction.Axis.Y) {
            BlockState lv5 = (BlockState)((BlockState)this.getDefaultState().with(ATTACHMENT, lv == Direction.DOWN ? Attachment.CEILING : Attachment.FLOOR)).with(FACING, ctx.getHorizontalPlayerFacing());
            if (lv5.canPlaceAt(ctx.getWorld(), lv2)) {
                return lv5;
            }
        } else {
            boolean bl = lv4 == Direction.Axis.X && lv3.getBlockState(lv2.west()).isSideSolidFullSquare(lv3, lv2.west(), Direction.EAST) && lv3.getBlockState(lv2.east()).isSideSolidFullSquare(lv3, lv2.east(), Direction.WEST) || lv4 == Direction.Axis.Z && lv3.getBlockState(lv2.north()).isSideSolidFullSquare(lv3, lv2.north(), Direction.SOUTH) && lv3.getBlockState(lv2.south()).isSideSolidFullSquare(lv3, lv2.south(), Direction.NORTH);
            BlockState lv5 = (BlockState)((BlockState)this.getDefaultState().with(FACING, lv.getOpposite())).with(ATTACHMENT, bl ? Attachment.DOUBLE_WALL : Attachment.SINGLE_WALL);
            if (lv5.canPlaceAt(ctx.getWorld(), ctx.getBlockPos())) {
                return lv5;
            }
            boolean bl2 = lv3.getBlockState(lv2.down()).isSideSolidFullSquare(lv3, lv2.down(), Direction.UP);
            if ((lv5 = (BlockState)lv5.with(ATTACHMENT, bl2 ? Attachment.FLOOR : Attachment.CEILING)).canPlaceAt(ctx.getWorld(), ctx.getBlockPos())) {
                return lv5;
            }
        }
        return null;
    }

    @Override
    protected void onExploded(BlockState state, World world, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> stackMerger) {
        if (explosion.canTriggerBlocks()) {
            this.ring(world, pos, null);
        }
        super.onExploded(state, world, pos, explosion, stackMerger);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        Attachment lv = state.get(ATTACHMENT);
        Direction lv2 = BellBlock.getPlacementSide(state).getOpposite();
        if (lv2 == direction && !state.canPlaceAt(world, pos) && lv != Attachment.DOUBLE_WALL) {
            return Blocks.AIR.getDefaultState();
        }
        if (direction.getAxis() == state.get(FACING).getAxis()) {
            if (lv == Attachment.DOUBLE_WALL && !neighborState.isSideSolidFullSquare(world, neighborPos, direction)) {
                return (BlockState)((BlockState)state.with(ATTACHMENT, Attachment.SINGLE_WALL)).with(FACING, direction.getOpposite());
            }
            if (lv == Attachment.SINGLE_WALL && lv2.getOpposite() == direction && neighborState.isSideSolidFullSquare(world, neighborPos, state.get(FACING))) {
                return (BlockState)state.with(ATTACHMENT, Attachment.DOUBLE_WALL);
            }
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        Direction lv = BellBlock.getPlacementSide(state).getOpposite();
        if (lv == Direction.UP) {
            return Block.sideCoversSmallSquare(world, pos.up(), Direction.DOWN);
        }
        return WallMountedBlock.canPlaceAt(world, pos, lv);
    }

    private static Direction getPlacementSide(BlockState state) {
        switch (state.get(ATTACHMENT)) {
            case CEILING: {
                return Direction.DOWN;
            }
            case FLOOR: {
                return Direction.UP;
            }
        }
        return state.get(FACING).getOpposite();
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, ATTACHMENT, POWERED);
    }

    @Override
    @Nullable
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BellBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return BellBlock.validateTicker(type, BlockEntityType.BELL, world.isClient ? BellBlockEntity::clientTick : BellBlockEntity::serverTick);
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return (BlockState)state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }
}

