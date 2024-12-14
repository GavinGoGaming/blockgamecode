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
import java.util.List;
import java.util.Optional;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoubleBlockProperties;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.BedPart;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.CollisionView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

public class BedBlock
extends HorizontalFacingBlock
implements BlockEntityProvider {
    public static final MapCodec<BedBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)DyeColor.CODEC.fieldOf("color")).forGetter(BedBlock::getColor), BedBlock.createSettingsCodec()).apply((Applicative<BedBlock, ?>)instance, BedBlock::new));
    public static final EnumProperty<BedPart> PART = Properties.BED_PART;
    public static final BooleanProperty OCCUPIED = Properties.OCCUPIED;
    protected static final int field_31009 = 9;
    protected static final VoxelShape TOP_SHAPE = Block.createCuboidShape(0.0, 3.0, 0.0, 16.0, 9.0, 16.0);
    private static final int field_31010 = 3;
    protected static final VoxelShape LEG_1_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 3.0, 3.0, 3.0);
    protected static final VoxelShape LEG_2_SHAPE = Block.createCuboidShape(0.0, 0.0, 13.0, 3.0, 3.0, 16.0);
    protected static final VoxelShape LEG_3_SHAPE = Block.createCuboidShape(13.0, 0.0, 0.0, 16.0, 3.0, 3.0);
    protected static final VoxelShape LEG_4_SHAPE = Block.createCuboidShape(13.0, 0.0, 13.0, 16.0, 3.0, 16.0);
    protected static final VoxelShape NORTH_SHAPE = VoxelShapes.union(TOP_SHAPE, LEG_1_SHAPE, LEG_3_SHAPE);
    protected static final VoxelShape SOUTH_SHAPE = VoxelShapes.union(TOP_SHAPE, LEG_2_SHAPE, LEG_4_SHAPE);
    protected static final VoxelShape WEST_SHAPE = VoxelShapes.union(TOP_SHAPE, LEG_1_SHAPE, LEG_2_SHAPE);
    protected static final VoxelShape EAST_SHAPE = VoxelShapes.union(TOP_SHAPE, LEG_3_SHAPE, LEG_4_SHAPE);
    private final DyeColor color;

    public MapCodec<BedBlock> getCodec() {
        return CODEC;
    }

    public BedBlock(DyeColor color, AbstractBlock.Settings settings) {
        super(settings);
        this.color = color;
        this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(PART, BedPart.FOOT)).with(OCCUPIED, false));
    }

    @Nullable
    public static Direction getDirection(BlockView world, BlockPos pos) {
        BlockState lv = world.getBlockState(pos);
        return lv.getBlock() instanceof BedBlock ? lv.get(FACING) : null;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.CONSUME;
        }
        if (state.get(PART) != BedPart.HEAD && !(state = world.getBlockState(pos = pos.offset(state.get(FACING)))).isOf(this)) {
            return ActionResult.CONSUME;
        }
        if (!BedBlock.isBedWorking(world)) {
            world.removeBlock(pos, false);
            BlockPos lv = pos.offset(state.get(FACING).getOpposite());
            if (world.getBlockState(lv).isOf(this)) {
                world.removeBlock(lv, false);
            }
            Vec3d lv2 = pos.toCenterPos();
            world.createExplosion(null, world.getDamageSources().badRespawnPoint(lv2), null, lv2, 5.0f, true, World.ExplosionSourceType.BLOCK);
            return ActionResult.SUCCESS;
        }
        if (state.get(OCCUPIED).booleanValue()) {
            if (!this.wakeVillager(world, pos)) {
                player.sendMessage(Text.translatable("block.minecraft.bed.occupied"), true);
            }
            return ActionResult.SUCCESS;
        }
        player.trySleep(pos).ifLeft(reason -> {
            if (reason.getMessage() != null) {
                player.sendMessage(reason.getMessage(), true);
            }
        });
        return ActionResult.SUCCESS;
    }

    public static boolean isBedWorking(World world) {
        return world.getDimension().bedWorks();
    }

    private boolean wakeVillager(World world, BlockPos pos) {
        List<VillagerEntity> list = world.getEntitiesByClass(VillagerEntity.class, new Box(pos), LivingEntity::isSleeping);
        if (list.isEmpty()) {
            return false;
        }
        list.get(0).wakeUp();
        return true;
    }

    @Override
    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        super.onLandedUpon(world, state, pos, entity, fallDistance * 0.5f);
    }

    @Override
    public void onEntityLand(BlockView world, Entity entity) {
        if (entity.bypassesLandingEffects()) {
            super.onEntityLand(world, entity);
        } else {
            this.bounceEntity(entity);
        }
    }

    private void bounceEntity(Entity entity) {
        Vec3d lv = entity.getVelocity();
        if (lv.y < 0.0) {
            double d = entity instanceof LivingEntity ? 1.0 : 0.8;
            entity.setVelocity(lv.x, -lv.y * (double)0.66f * d, lv.z);
        }
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction == BedBlock.getDirectionTowardsOtherPart(state.get(PART), state.get(FACING))) {
            if (neighborState.isOf(this) && neighborState.get(PART) != state.get(PART)) {
                return (BlockState)state.with(OCCUPIED, neighborState.get(OCCUPIED));
            }
            return Blocks.AIR.getDefaultState();
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    private static Direction getDirectionTowardsOtherPart(BedPart part, Direction direction) {
        return part == BedPart.FOOT ? direction : direction.getOpposite();
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        BlockPos lv2;
        BlockState lv3;
        BedPart lv;
        if (!world.isClient && player.isCreative() && (lv = state.get(PART)) == BedPart.FOOT && (lv3 = world.getBlockState(lv2 = pos.offset(BedBlock.getDirectionTowardsOtherPart(lv, state.get(FACING))))).isOf(this) && lv3.get(PART) == BedPart.HEAD) {
            world.setBlockState(lv2, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL | Block.SKIP_DROPS);
            world.syncWorldEvent(player, WorldEvents.BLOCK_BROKEN, lv2, Block.getRawIdFromState(lv3));
        }
        return super.onBreak(world, pos, state, player);
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction lv = ctx.getHorizontalPlayerFacing();
        BlockPos lv2 = ctx.getBlockPos();
        BlockPos lv3 = lv2.offset(lv);
        World lv4 = ctx.getWorld();
        if (lv4.getBlockState(lv3).canReplace(ctx) && lv4.getWorldBorder().contains(lv3)) {
            return (BlockState)this.getDefaultState().with(FACING, lv);
        }
        return null;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Direction lv = BedBlock.getOppositePartDirection(state).getOpposite();
        switch (lv) {
            case NORTH: {
                return NORTH_SHAPE;
            }
            case SOUTH: {
                return SOUTH_SHAPE;
            }
            case WEST: {
                return WEST_SHAPE;
            }
        }
        return EAST_SHAPE;
    }

    public static Direction getOppositePartDirection(BlockState state) {
        Direction lv = state.get(FACING);
        return state.get(PART) == BedPart.HEAD ? lv.getOpposite() : lv;
    }

    public static DoubleBlockProperties.Type getBedPart(BlockState state) {
        BedPart lv = state.get(PART);
        if (lv == BedPart.HEAD) {
            return DoubleBlockProperties.Type.FIRST;
        }
        return DoubleBlockProperties.Type.SECOND;
    }

    private static boolean isBedBelow(BlockView world, BlockPos pos) {
        return world.getBlockState(pos.down()).getBlock() instanceof BedBlock;
    }

    public static Optional<Vec3d> findWakeUpPosition(EntityType<?> type, CollisionView world, BlockPos pos, Direction bedDirection, float spawnAngle) {
        Direction lv2;
        Direction lv = bedDirection.rotateYClockwise();
        Direction direction = lv2 = lv.pointsTo(spawnAngle) ? lv.getOpposite() : lv;
        if (BedBlock.isBedBelow(world, pos)) {
            return BedBlock.findWakeUpPosition(type, world, pos, bedDirection, lv2);
        }
        int[][] is = BedBlock.getAroundAndOnBedOffsets(bedDirection, lv2);
        Optional<Vec3d> optional = BedBlock.findWakeUpPosition(type, world, pos, is, true);
        if (optional.isPresent()) {
            return optional;
        }
        return BedBlock.findWakeUpPosition(type, world, pos, is, false);
    }

    private static Optional<Vec3d> findWakeUpPosition(EntityType<?> type, CollisionView world, BlockPos pos, Direction bedDirection, Direction respawnDirection) {
        int[][] is = BedBlock.getAroundBedOffsets(bedDirection, respawnDirection);
        Optional<Vec3d> optional = BedBlock.findWakeUpPosition(type, world, pos, is, true);
        if (optional.isPresent()) {
            return optional;
        }
        BlockPos lv = pos.down();
        Optional<Vec3d> optional2 = BedBlock.findWakeUpPosition(type, world, lv, is, true);
        if (optional2.isPresent()) {
            return optional2;
        }
        int[][] js = BedBlock.getOnBedOffsets(bedDirection);
        Optional<Vec3d> optional3 = BedBlock.findWakeUpPosition(type, world, pos, js, true);
        if (optional3.isPresent()) {
            return optional3;
        }
        Optional<Vec3d> optional4 = BedBlock.findWakeUpPosition(type, world, pos, is, false);
        if (optional4.isPresent()) {
            return optional4;
        }
        Optional<Vec3d> optional5 = BedBlock.findWakeUpPosition(type, world, lv, is, false);
        if (optional5.isPresent()) {
            return optional5;
        }
        return BedBlock.findWakeUpPosition(type, world, pos, js, false);
    }

    private static Optional<Vec3d> findWakeUpPosition(EntityType<?> type, CollisionView world, BlockPos pos, int[][] possibleOffsets, boolean ignoreInvalidPos) {
        BlockPos.Mutable lv = new BlockPos.Mutable();
        for (int[] js : possibleOffsets) {
            lv.set(pos.getX() + js[0], pos.getY(), pos.getZ() + js[1]);
            Vec3d lv2 = Dismounting.findRespawnPos(type, world, lv, ignoreInvalidPos);
            if (lv2 == null) continue;
            return Optional.of(lv2);
        }
        return Optional.empty();
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART, OCCUPIED);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BedBlockEntity(pos, state, this.color);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient) {
            BlockPos lv = pos.offset(state.get(FACING));
            world.setBlockState(lv, (BlockState)state.with(PART, BedPart.HEAD), Block.NOTIFY_ALL);
            world.updateNeighbors(pos, Blocks.AIR);
            state.updateNeighbors(world, pos, Block.NOTIFY_ALL);
        }
    }

    public DyeColor getColor() {
        return this.color;
    }

    @Override
    protected long getRenderingSeed(BlockState state, BlockPos pos) {
        BlockPos lv = pos.offset(state.get(FACING), state.get(PART) == BedPart.HEAD ? 0 : 1);
        return MathHelper.hashCode(lv.getX(), pos.getY(), lv.getZ());
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }

    private static int[][] getAroundAndOnBedOffsets(Direction bedDirection, Direction respawnDirection) {
        return (int[][])ArrayUtils.addAll(BedBlock.getAroundBedOffsets(bedDirection, respawnDirection), BedBlock.getOnBedOffsets(bedDirection));
    }

    private static int[][] getAroundBedOffsets(Direction bedDirection, Direction respawnDirection) {
        return new int[][]{{respawnDirection.getOffsetX(), respawnDirection.getOffsetZ()}, {respawnDirection.getOffsetX() - bedDirection.getOffsetX(), respawnDirection.getOffsetZ() - bedDirection.getOffsetZ()}, {respawnDirection.getOffsetX() - bedDirection.getOffsetX() * 2, respawnDirection.getOffsetZ() - bedDirection.getOffsetZ() * 2}, {-bedDirection.getOffsetX() * 2, -bedDirection.getOffsetZ() * 2}, {-respawnDirection.getOffsetX() - bedDirection.getOffsetX() * 2, -respawnDirection.getOffsetZ() - bedDirection.getOffsetZ() * 2}, {-respawnDirection.getOffsetX() - bedDirection.getOffsetX(), -respawnDirection.getOffsetZ() - bedDirection.getOffsetZ()}, {-respawnDirection.getOffsetX(), -respawnDirection.getOffsetZ()}, {-respawnDirection.getOffsetX() + bedDirection.getOffsetX(), -respawnDirection.getOffsetZ() + bedDirection.getOffsetZ()}, {bedDirection.getOffsetX(), bedDirection.getOffsetZ()}, {respawnDirection.getOffsetX() + bedDirection.getOffsetX(), respawnDirection.getOffsetZ() + bedDirection.getOffsetZ()}};
    }

    private static int[][] getOnBedOffsets(Direction bedDirection) {
        return new int[][]{{0, 0}, {-bedDirection.getOffsetX(), -bedDirection.getOffsetZ()}};
    }
}

