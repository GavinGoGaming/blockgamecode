/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.fluid;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FluidFillable;
import net.minecraft.block.IceBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public abstract class FlowableFluid
extends Fluid {
    public static final BooleanProperty FALLING = Properties.FALLING;
    public static final IntProperty LEVEL = Properties.LEVEL_1_8;
    private static final int field_31726 = 200;
    private static final ThreadLocal<Object2ByteLinkedOpenHashMap<Block.NeighborGroup>> field_15901 = ThreadLocal.withInitial(() -> {
        Object2ByteLinkedOpenHashMap<Block.NeighborGroup> object2ByteLinkedOpenHashMap = new Object2ByteLinkedOpenHashMap<Block.NeighborGroup>(200){

            @Override
            protected void rehash(int i) {
            }
        };
        object2ByteLinkedOpenHashMap.defaultReturnValue((byte)127);
        return object2ByteLinkedOpenHashMap;
    });
    private final Map<FluidState, VoxelShape> shapeCache = Maps.newIdentityHashMap();

    @Override
    protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
        builder.add(FALLING);
    }

    @Override
    public Vec3d getVelocity(BlockView world, BlockPos pos, FluidState state) {
        double d = 0.0;
        double e = 0.0;
        BlockPos.Mutable lv = new BlockPos.Mutable();
        for (Direction lv2 : Direction.Type.HORIZONTAL) {
            lv.set((Vec3i)pos, lv2);
            FluidState lv3 = world.getFluidState(lv);
            if (!this.isEmptyOrThis(lv3)) continue;
            float f = lv3.getHeight();
            float g = 0.0f;
            if (f == 0.0f) {
                Vec3i lv4;
                FluidState lv5;
                if (!world.getBlockState(lv).blocksMovement() && this.isEmptyOrThis(lv5 = world.getFluidState((BlockPos)(lv4 = lv.down()))) && (f = lv5.getHeight()) > 0.0f) {
                    g = state.getHeight() - (f - 0.8888889f);
                }
            } else if (f > 0.0f) {
                g = state.getHeight() - f;
            }
            if (g == 0.0f) continue;
            d += (double)((float)lv2.getOffsetX() * g);
            e += (double)((float)lv2.getOffsetZ() * g);
        }
        Vec3d lv6 = new Vec3d(d, 0.0, e);
        if (state.get(FALLING).booleanValue()) {
            for (Direction lv7 : Direction.Type.HORIZONTAL) {
                lv.set((Vec3i)pos, lv7);
                if (!this.isFlowBlocked(world, lv, lv7) && !this.isFlowBlocked(world, (BlockPos)lv.up(), lv7)) continue;
                lv6 = lv6.normalize().add(0.0, -6.0, 0.0);
                break;
            }
        }
        return lv6.normalize();
    }

    private boolean isEmptyOrThis(FluidState state) {
        return state.isEmpty() || state.getFluid().matchesType(this);
    }

    protected boolean isFlowBlocked(BlockView world, BlockPos pos, Direction direction) {
        BlockState lv = world.getBlockState(pos);
        FluidState lv2 = world.getFluidState(pos);
        if (lv2.getFluid().matchesType(this)) {
            return false;
        }
        if (direction == Direction.UP) {
            return true;
        }
        if (lv.getBlock() instanceof IceBlock) {
            return false;
        }
        return lv.isSideSolidFullSquare(world, pos, direction);
    }

    protected void tryFlow(World world, BlockPos fluidPos, FluidState state) {
        if (state.isEmpty()) {
            return;
        }
        BlockState lv = world.getBlockState(fluidPos);
        BlockPos lv2 = fluidPos.down();
        BlockState lv3 = world.getBlockState(lv2);
        FluidState lv4 = this.getUpdatedState(world, lv2, lv3);
        if (this.canFlow(world, fluidPos, lv, Direction.DOWN, lv2, lv3, world.getFluidState(lv2), lv4.getFluid())) {
            this.flow(world, lv2, lv3, Direction.DOWN, lv4);
            if (this.countNeighboringSources(world, fluidPos) >= 3) {
                this.flowToSides(world, fluidPos, state, lv);
            }
        } else if (state.isStill() || !this.canFlowDownTo(world, lv4.getFluid(), fluidPos, lv, lv2, lv3)) {
            this.flowToSides(world, fluidPos, state, lv);
        }
    }

    private void flowToSides(World world, BlockPos pos, FluidState fluidState, BlockState blockState) {
        int i = fluidState.getLevel() - this.getLevelDecreasePerBlock(world);
        if (fluidState.get(FALLING).booleanValue()) {
            i = 7;
        }
        if (i <= 0) {
            return;
        }
        Map<Direction, FluidState> map = this.getSpread(world, pos, blockState);
        for (Map.Entry<Direction, FluidState> entry : map.entrySet()) {
            BlockState lv4;
            Direction lv = entry.getKey();
            FluidState lv2 = entry.getValue();
            BlockPos lv3 = pos.offset(lv);
            if (!this.canFlow(world, pos, blockState, lv, lv3, lv4 = world.getBlockState(lv3), world.getFluidState(lv3), lv2.getFluid())) continue;
            this.flow(world, lv3, lv4, lv, lv2);
        }
    }

    protected FluidState getUpdatedState(World world, BlockPos pos, BlockState state) {
        BlockPos lv7;
        BlockState lv8;
        FluidState lv9;
        int i = 0;
        int j = 0;
        for (Direction lv : Direction.Type.HORIZONTAL) {
            BlockPos lv2 = pos.offset(lv);
            BlockState lv3 = world.getBlockState(lv2);
            FluidState lv4 = lv3.getFluidState();
            if (!lv4.getFluid().matchesType(this) || !this.receivesFlow(lv, world, pos, state, lv2, lv3)) continue;
            if (lv4.isStill()) {
                ++j;
            }
            i = Math.max(i, lv4.getLevel());
        }
        if (this.isInfinite(world) && j >= 2) {
            BlockState lv5 = world.getBlockState(pos.down());
            FluidState lv6 = lv5.getFluidState();
            if (lv5.isSolid() || this.isMatchingAndStill(lv6)) {
                return this.getStill(false);
            }
        }
        if (!(lv9 = (lv8 = world.getBlockState(lv7 = pos.up())).getFluidState()).isEmpty() && lv9.getFluid().matchesType(this) && this.receivesFlow(Direction.UP, world, pos, state, lv7, lv8)) {
            return this.getFlowing(8, true);
        }
        int k = i - this.getLevelDecreasePerBlock(world);
        if (k <= 0) {
            return Fluids.EMPTY.getDefaultState();
        }
        return this.getFlowing(k, false);
    }

    private boolean receivesFlow(Direction face, BlockView world, BlockPos pos, BlockState state, BlockPos fromPos, BlockState fromState) {
        VoxelShape lv3;
        VoxelShape lv2;
        boolean bl;
        Block.NeighborGroup lv;
        Object2ByteLinkedOpenHashMap<Block.NeighborGroup> object2ByteLinkedOpenHashMap = state.getBlock().hasDynamicBounds() || fromState.getBlock().hasDynamicBounds() ? null : field_15901.get();
        if (object2ByteLinkedOpenHashMap != null) {
            lv = new Block.NeighborGroup(state, fromState, face);
            byte b = object2ByteLinkedOpenHashMap.getAndMoveToFirst(lv);
            if (b != 127) {
                return b != 0;
            }
        } else {
            lv = null;
        }
        boolean bl2 = bl = !VoxelShapes.adjacentSidesCoverSquare(lv2 = state.getCollisionShape(world, pos), lv3 = fromState.getCollisionShape(world, fromPos), face);
        if (object2ByteLinkedOpenHashMap != null) {
            if (object2ByteLinkedOpenHashMap.size() == 200) {
                object2ByteLinkedOpenHashMap.removeLastByte();
            }
            object2ByteLinkedOpenHashMap.putAndMoveToFirst(lv, (byte)(bl ? 1 : 0));
        }
        return bl;
    }

    public abstract Fluid getFlowing();

    public FluidState getFlowing(int level, boolean falling) {
        return (FluidState)((FluidState)this.getFlowing().getDefaultState().with(LEVEL, level)).with(FALLING, falling);
    }

    public abstract Fluid getStill();

    public FluidState getStill(boolean falling) {
        return (FluidState)this.getStill().getDefaultState().with(FALLING, falling);
    }

    protected abstract boolean isInfinite(World var1);

    protected void flow(WorldAccess world, BlockPos pos, BlockState state, Direction direction, FluidState fluidState) {
        if (state.getBlock() instanceof FluidFillable) {
            ((FluidFillable)((Object)state.getBlock())).tryFillWithFluid(world, pos, state, fluidState);
        } else {
            if (!state.isAir()) {
                this.beforeBreakingBlock(world, pos, state);
            }
            world.setBlockState(pos, fluidState.getBlockState(), Block.NOTIFY_ALL);
        }
    }

    protected abstract void beforeBreakingBlock(WorldAccess var1, BlockPos var2, BlockState var3);

    private static short packXZOffset(BlockPos from, BlockPos to) {
        int i = to.getX() - from.getX();
        int j = to.getZ() - from.getZ();
        return (short)((i + 128 & 0xFF) << 8 | j + 128 & 0xFF);
    }

    protected int getMinFlowDownDistance(WorldView world, BlockPos pos, int i, Direction direction, BlockState state, BlockPos fromPos, Short2ObjectMap<Pair<BlockState, FluidState>> stateCache, Short2BooleanMap flowDownCache) {
        int j = 1000;
        for (Direction lv : Direction.Type.HORIZONTAL) {
            int k;
            if (lv == direction) continue;
            BlockPos lv2 = pos.offset(lv);
            short s2 = FlowableFluid.packXZOffset(fromPos, lv2);
            Pair pair = stateCache.computeIfAbsent(s2, s -> {
                BlockState lv = world.getBlockState(lv2);
                return Pair.of(lv, lv.getFluidState());
            });
            BlockState lv3 = (BlockState)pair.getFirst();
            FluidState lv4 = (FluidState)pair.getSecond();
            if (!this.canFlowThrough(world, this.getFlowing(), pos, state, lv, lv2, lv3, lv4)) continue;
            boolean bl = flowDownCache.computeIfAbsent(s2, s -> {
                BlockPos lv = lv2.down();
                BlockState lv2 = world.getBlockState(lv);
                return this.canFlowDownTo(world, this.getFlowing(), lv2, lv3, lv, lv2);
            });
            if (bl) {
                return i;
            }
            if (i >= this.getMaxFlowDistance(world) || (k = this.getMinFlowDownDistance(world, lv2, i + 1, lv.getOpposite(), lv3, fromPos, stateCache, flowDownCache)) >= j) continue;
            j = k;
        }
        return j;
    }

    private boolean canFlowDownTo(BlockView world, Fluid fluid, BlockPos pos, BlockState state, BlockPos fromPos, BlockState fromState) {
        if (!this.receivesFlow(Direction.DOWN, world, pos, state, fromPos, fromState)) {
            return false;
        }
        if (fromState.getFluidState().getFluid().matchesType(this)) {
            return true;
        }
        return this.canFill(world, fromPos, fromState, fluid);
    }

    private boolean canFlowThrough(BlockView world, Fluid fluid, BlockPos pos, BlockState state, Direction face, BlockPos fromPos, BlockState fromState, FluidState fluidState) {
        return !this.isMatchingAndStill(fluidState) && this.receivesFlow(face, world, pos, state, fromPos, fromState) && this.canFill(world, fromPos, fromState, fluid);
    }

    private boolean isMatchingAndStill(FluidState state) {
        return state.getFluid().matchesType(this) && state.isStill();
    }

    protected abstract int getMaxFlowDistance(WorldView var1);

    private int countNeighboringSources(WorldView world, BlockPos pos) {
        int i = 0;
        for (Direction lv : Direction.Type.HORIZONTAL) {
            BlockPos lv2 = pos.offset(lv);
            FluidState lv3 = world.getFluidState(lv2);
            if (!this.isMatchingAndStill(lv3)) continue;
            ++i;
        }
        return i;
    }

    protected Map<Direction, FluidState> getSpread(World world, BlockPos pos, BlockState state) {
        int i = 1000;
        EnumMap<Direction, FluidState> map = Maps.newEnumMap(Direction.class);
        Short2ObjectOpenHashMap<Pair<BlockState, FluidState>> short2ObjectMap = new Short2ObjectOpenHashMap<Pair<BlockState, FluidState>>();
        Short2BooleanOpenHashMap short2BooleanMap = new Short2BooleanOpenHashMap();
        for (Direction lv : Direction.Type.HORIZONTAL) {
            BlockPos lv2 = pos.offset(lv);
            short s2 = FlowableFluid.packXZOffset(pos, lv2);
            Pair pair = short2ObjectMap.computeIfAbsent(s2, s -> {
                BlockState lv = world.getBlockState(lv2);
                return Pair.of(lv, lv.getFluidState());
            });
            BlockState lv3 = (BlockState)pair.getFirst();
            FluidState lv4 = (FluidState)pair.getSecond();
            FluidState lv5 = this.getUpdatedState(world, lv2, lv3);
            if (!this.canFlowThrough(world, lv5.getFluid(), pos, state, lv, lv2, lv3, lv4)) continue;
            BlockPos lv6 = lv2.down();
            boolean bl = short2BooleanMap.computeIfAbsent(s2, s -> {
                BlockState lv = world.getBlockState(lv6);
                return this.canFlowDownTo(world, this.getFlowing(), lv2, lv3, lv6, lv);
            });
            int j = bl ? 0 : this.getMinFlowDownDistance(world, lv2, 1, lv.getOpposite(), lv3, pos, short2ObjectMap, short2BooleanMap);
            if (j < i) {
                map.clear();
            }
            if (j > i) continue;
            map.put(lv, lv5);
            i = j;
        }
        return map;
    }

    private boolean canFill(BlockView world, BlockPos pos, BlockState state, Fluid fluid) {
        Block lv = state.getBlock();
        if (lv instanceof FluidFillable) {
            FluidFillable lv2 = (FluidFillable)((Object)lv);
            return lv2.canFillWithFluid(null, world, pos, state, fluid);
        }
        if (lv instanceof DoorBlock || state.isIn(BlockTags.SIGNS) || state.isOf(Blocks.LADDER) || state.isOf(Blocks.SUGAR_CANE) || state.isOf(Blocks.BUBBLE_COLUMN)) {
            return false;
        }
        if (state.isOf(Blocks.NETHER_PORTAL) || state.isOf(Blocks.END_PORTAL) || state.isOf(Blocks.END_GATEWAY) || state.isOf(Blocks.STRUCTURE_VOID)) {
            return false;
        }
        return !state.blocksMovement();
    }

    protected boolean canFlow(BlockView world, BlockPos fluidPos, BlockState fluidBlockState, Direction flowDirection, BlockPos flowTo, BlockState flowToBlockState, FluidState fluidState, Fluid fluid) {
        return fluidState.canBeReplacedWith(world, flowTo, fluid, flowDirection) && this.receivesFlow(flowDirection, world, fluidPos, fluidBlockState, flowTo, flowToBlockState) && this.canFill(world, flowTo, flowToBlockState, fluid);
    }

    protected abstract int getLevelDecreasePerBlock(WorldView var1);

    protected int getNextTickDelay(World world, BlockPos pos, FluidState oldState, FluidState newState) {
        return this.getTickRate(world);
    }

    @Override
    public void onScheduledTick(World world, BlockPos pos, FluidState state) {
        if (!state.isStill()) {
            FluidState lv = this.getUpdatedState(world, pos, world.getBlockState(pos));
            int i = this.getNextTickDelay(world, pos, state, lv);
            if (lv.isEmpty()) {
                state = lv;
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
            } else if (!lv.equals(state)) {
                state = lv;
                BlockState lv2 = state.getBlockState();
                world.setBlockState(pos, lv2, Block.NOTIFY_LISTENERS);
                world.scheduleFluidTick(pos, state.getFluid(), i);
                world.updateNeighborsAlways(pos, lv2.getBlock());
            }
        }
        this.tryFlow(world, pos, state);
    }

    protected static int getBlockStateLevel(FluidState state) {
        if (state.isStill()) {
            return 0;
        }
        return 8 - Math.min(state.getLevel(), 8) + (state.get(FALLING) != false ? 8 : 0);
    }

    private static boolean isFluidAboveEqual(FluidState state, BlockView world, BlockPos pos) {
        return state.getFluid().matchesType(world.getFluidState(pos.up()).getFluid());
    }

    @Override
    public float getHeight(FluidState state, BlockView world, BlockPos pos) {
        if (FlowableFluid.isFluidAboveEqual(state, world, pos)) {
            return 1.0f;
        }
        return state.getHeight();
    }

    @Override
    public float getHeight(FluidState state) {
        return (float)state.getLevel() / 9.0f;
    }

    @Override
    public abstract int getLevel(FluidState var1);

    @Override
    public VoxelShape getShape(FluidState state, BlockView world, BlockPos pos) {
        if (state.getLevel() == 9 && FlowableFluid.isFluidAboveEqual(state, world, pos)) {
            return VoxelShapes.fullCube();
        }
        return this.shapeCache.computeIfAbsent(state, state2 -> VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, state2.getHeight(world, pos), 1.0));
    }
}

