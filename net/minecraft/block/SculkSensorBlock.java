/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SculkSensorBlockEntity;
import net.minecraft.block.enums.SculkSensorPhase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustColorTransitionParticleEffect;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.Vibrations;
import org.jetbrains.annotations.Nullable;

public class SculkSensorBlock
extends BlockWithEntity
implements Waterloggable {
    public static final MapCodec<SculkSensorBlock> CODEC = SculkSensorBlock.createCodec(SculkSensorBlock::new);
    public static final int field_31239 = 30;
    public static final int field_44607 = 10;
    public static final EnumProperty<SculkSensorPhase> SCULK_SENSOR_PHASE = Properties.SCULK_SENSOR_PHASE;
    public static final IntProperty POWER = Properties.POWER;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    protected static final VoxelShape OUTLINE_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
    private static final float[] RESONATION_NOTE_PITCHES = Util.make(new float[16], frequency -> {
        int[] is = new int[]{0, 0, 2, 4, 6, 7, 9, 10, 12, 14, 15, 18, 19, 21, 22, 24};
        for (int i = 0; i < 16; ++i) {
            frequency[i] = NoteBlock.getNotePitch(is[i]);
        }
    });

    public MapCodec<? extends SculkSensorBlock> getCodec() {
        return CODEC;
    }

    public SculkSensorBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(SCULK_SENSOR_PHASE, SculkSensorPhase.INACTIVE)).with(POWER, 0)).with(WATERLOGGED, false));
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos lv = ctx.getBlockPos();
        FluidState lv2 = ctx.getWorld().getFluidState(lv);
        return (BlockState)this.getDefaultState().with(WATERLOGGED, lv2.getFluid() == Fluids.WATER);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        if (state.get(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getStill(false);
        }
        return super.getFluidState(state);
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (SculkSensorBlock.getPhase(state) != SculkSensorPhase.ACTIVE) {
            if (SculkSensorBlock.getPhase(state) == SculkSensorPhase.COOLDOWN) {
                world.setBlockState(pos, (BlockState)state.with(SCULK_SENSOR_PHASE, SculkSensorPhase.INACTIVE), Block.NOTIFY_ALL);
                if (!state.get(WATERLOGGED).booleanValue()) {
                    world.playSound(null, pos, SoundEvents.BLOCK_SCULK_SENSOR_CLICKING_STOP, SoundCategory.BLOCKS, 1.0f, world.random.nextFloat() * 0.2f + 0.8f);
                }
            }
            return;
        }
        SculkSensorBlock.setCooldown(world, pos, state);
    }

    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        BlockEntity lv;
        if (!world.isClient() && SculkSensorBlock.isInactive(state) && entity.getType() != EntityType.WARDEN && (lv = world.getBlockEntity(pos)) instanceof SculkSensorBlockEntity) {
            SculkSensorBlockEntity lv2 = (SculkSensorBlockEntity)lv;
            if (world instanceof ServerWorld) {
                ServerWorld lv3 = (ServerWorld)world;
                if (lv2.getVibrationCallback().accepts(lv3, pos, GameEvent.STEP, GameEvent.Emitter.of(state))) {
                    lv2.getEventListener().forceListen(lv3, GameEvent.STEP, GameEvent.Emitter.of(entity), entity.getPos());
                }
            }
        }
        super.onSteppedOn(world, pos, state, entity);
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (world.isClient() || state.isOf(oldState.getBlock())) {
            return;
        }
        if (state.get(POWER) > 0 && !world.getBlockTickScheduler().isQueued(pos, this)) {
            world.setBlockState(pos, (BlockState)state.with(POWER, 0), Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
        }
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.isOf(newState.getBlock())) {
            return;
        }
        if (SculkSensorBlock.getPhase(state) == SculkSensorPhase.ACTIVE) {
            SculkSensorBlock.updateNeighbors(world, pos, state);
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED).booleanValue()) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    private static void updateNeighbors(World world, BlockPos pos, BlockState state) {
        Block lv = state.getBlock();
        world.updateNeighborsAlways(pos, lv);
        world.updateNeighborsAlways(pos.down(), lv);
    }

    @Override
    @Nullable
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SculkSensorBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (!world.isClient) {
            return SculkSensorBlock.validateTicker(type, BlockEntityType.SCULK_SENSOR, (worldx, pos, statex, blockEntity) -> Vibrations.Ticker.tick(worldx, blockEntity.getVibrationListenerData(), blockEntity.getVibrationCallback()));
        }
        return null;
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return OUTLINE_SHAPE;
    }

    @Override
    protected boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.get(POWER);
    }

    @Override
    public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if (direction == Direction.UP) {
            return state.getWeakRedstonePower(world, pos, direction);
        }
        return 0;
    }

    public static SculkSensorPhase getPhase(BlockState state) {
        return state.get(SCULK_SENSOR_PHASE);
    }

    public static boolean isInactive(BlockState state) {
        return SculkSensorBlock.getPhase(state) == SculkSensorPhase.INACTIVE;
    }

    public static void setCooldown(World world, BlockPos pos, BlockState state) {
        world.setBlockState(pos, (BlockState)((BlockState)state.with(SCULK_SENSOR_PHASE, SculkSensorPhase.COOLDOWN)).with(POWER, 0), Block.NOTIFY_ALL);
        world.scheduleBlockTick(pos, state.getBlock(), 10);
        SculkSensorBlock.updateNeighbors(world, pos, state);
    }

    @VisibleForTesting
    public int getCooldownTime() {
        return 30;
    }

    public void setActive(@Nullable Entity sourceEntity, World world, BlockPos pos, BlockState state, int power, int frequency) {
        world.setBlockState(pos, (BlockState)((BlockState)state.with(SCULK_SENSOR_PHASE, SculkSensorPhase.ACTIVE)).with(POWER, power), Block.NOTIFY_ALL);
        world.scheduleBlockTick(pos, state.getBlock(), this.getCooldownTime());
        SculkSensorBlock.updateNeighbors(world, pos, state);
        SculkSensorBlock.tryResonate(sourceEntity, world, pos, frequency);
        world.emitGameEvent(sourceEntity, GameEvent.SCULK_SENSOR_TENDRILS_CLICKING, pos);
        if (!state.get(WATERLOGGED).booleanValue()) {
            world.playSound(null, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, SoundEvents.BLOCK_SCULK_SENSOR_CLICKING, SoundCategory.BLOCKS, 1.0f, world.random.nextFloat() * 0.2f + 0.8f);
        }
    }

    public static void tryResonate(@Nullable Entity sourceEntity, World world, BlockPos pos, int frequency) {
        for (Direction lv : Direction.values()) {
            BlockPos lv2 = pos.offset(lv);
            BlockState lv3 = world.getBlockState(lv2);
            if (!lv3.isIn(BlockTags.VIBRATION_RESONATORS)) continue;
            world.emitGameEvent(Vibrations.getResonation(frequency), lv2, GameEvent.Emitter.of(sourceEntity, lv3));
            float f = RESONATION_NOTE_PITCHES[frequency];
            world.playSound(null, lv2, SoundEvents.BLOCK_AMETHYST_BLOCK_RESONATE, SoundCategory.BLOCKS, 1.0f, f);
        }
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (SculkSensorBlock.getPhase(state) != SculkSensorPhase.ACTIVE) {
            return;
        }
        Direction lv = Direction.random(random);
        if (lv == Direction.UP || lv == Direction.DOWN) {
            return;
        }
        double d = (double)pos.getX() + 0.5 + (lv.getOffsetX() == 0 ? 0.5 - random.nextDouble() : (double)lv.getOffsetX() * 0.6);
        double e = (double)pos.getY() + 0.25;
        double f = (double)pos.getZ() + 0.5 + (lv.getOffsetZ() == 0 ? 0.5 - random.nextDouble() : (double)lv.getOffsetZ() * 0.6);
        double g = (double)random.nextFloat() * 0.04;
        world.addParticle(DustColorTransitionParticleEffect.DEFAULT, d, e, f, 0.0, g, 0.0);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(SCULK_SENSOR_PHASE, POWER, WATERLOGGED);
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        BlockEntity lv = world.getBlockEntity(pos);
        if (lv instanceof SculkSensorBlockEntity) {
            SculkSensorBlockEntity lv2 = (SculkSensorBlockEntity)lv;
            return SculkSensorBlock.getPhase(state) == SculkSensorPhase.ACTIVE ? lv2.getLastVibrationFrequency() : 0;
        }
        return 0;
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }

    @Override
    protected boolean hasSidedTransparency(BlockState state) {
        return true;
    }

    @Override
    protected void onStacksDropped(BlockState state, ServerWorld world, BlockPos pos, ItemStack tool, boolean dropExperience) {
        super.onStacksDropped(state, world, pos, tool, dropExperience);
        if (dropExperience) {
            this.dropExperienceWhenMined(world, pos, tool, ConstantIntProvider.create(5));
        }
    }
}

