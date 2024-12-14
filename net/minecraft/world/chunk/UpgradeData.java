/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.chunk;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.StemBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.EightWayDirection;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.EmptyBlockView;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PalettedContainer;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.tick.Tick;
import org.slf4j.Logger;

public class UpgradeData {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final UpgradeData NO_UPGRADE_DATA = new UpgradeData(EmptyBlockView.INSTANCE);
    private static final String INDICES_KEY = "Indices";
    private static final EightWayDirection[] EIGHT_WAYS = EightWayDirection.values();
    private final EnumSet<EightWayDirection> sidesToUpgrade = EnumSet.noneOf(EightWayDirection.class);
    private final List<Tick<Block>> blockTicks = Lists.newArrayList();
    private final List<Tick<Fluid>> fluidTicks = Lists.newArrayList();
    private final int[][] centerIndicesToUpgrade;
    static final Map<Block, Logic> BLOCK_TO_LOGIC = new IdentityHashMap<Block, Logic>();
    static final Set<Logic> CALLBACK_LOGICS = Sets.newHashSet();

    private UpgradeData(HeightLimitView world) {
        this.centerIndicesToUpgrade = new int[world.countVerticalSections()][];
    }

    public UpgradeData(NbtCompound nbt, HeightLimitView world) {
        this(world);
        if (nbt.contains(INDICES_KEY, NbtElement.COMPOUND_TYPE)) {
            NbtCompound lv = nbt.getCompound(INDICES_KEY);
            for (int i = 0; i < this.centerIndicesToUpgrade.length; ++i) {
                String string = String.valueOf(i);
                if (!lv.contains(string, NbtElement.INT_ARRAY_TYPE)) continue;
                this.centerIndicesToUpgrade[i] = lv.getIntArray(string);
            }
        }
        int j = nbt.getInt("Sides");
        for (EightWayDirection lv2 : EightWayDirection.values()) {
            if ((j & 1 << lv2.ordinal()) == 0) continue;
            this.sidesToUpgrade.add(lv2);
        }
        UpgradeData.addNeighborTicks(nbt, "neighbor_block_ticks", id -> Registries.BLOCK.getOrEmpty(Identifier.tryParse(id)).or(() -> Optional.of(Blocks.AIR)), this.blockTicks);
        UpgradeData.addNeighborTicks(nbt, "neighbor_fluid_ticks", id -> Registries.FLUID.getOrEmpty(Identifier.tryParse(id)).or(() -> Optional.of(Fluids.EMPTY)), this.fluidTicks);
    }

    private static <T> void addNeighborTicks(NbtCompound nbt, String key, Function<String, Optional<T>> nameToType, List<Tick<T>> ticks) {
        if (nbt.contains(key, NbtElement.LIST_TYPE)) {
            NbtList lv = nbt.getList(key, NbtElement.COMPOUND_TYPE);
            for (NbtElement lv2 : lv) {
                Tick.fromNbt((NbtCompound)lv2, nameToType).ifPresent(ticks::add);
            }
        }
    }

    public void upgrade(WorldChunk chunk) {
        this.upgradeCenter(chunk);
        for (EightWayDirection lv : EIGHT_WAYS) {
            UpgradeData.upgradeSide(chunk, lv);
        }
        World lv2 = chunk.getWorld();
        this.blockTicks.forEach(tick -> {
            Block lv = tick.type() == Blocks.AIR ? lv2.getBlockState(tick.pos()).getBlock() : (Block)tick.type();
            lv2.scheduleBlockTick(tick.pos(), lv, tick.delay(), tick.priority());
        });
        this.fluidTicks.forEach(tick -> {
            Fluid lv = tick.type() == Fluids.EMPTY ? lv2.getFluidState(tick.pos()).getFluid() : (Fluid)tick.type();
            lv2.scheduleFluidTick(tick.pos(), lv, tick.delay(), tick.priority());
        });
        CALLBACK_LOGICS.forEach(logic -> logic.postUpdate(lv2));
    }

    private static void upgradeSide(WorldChunk chunk, EightWayDirection side) {
        World lv = chunk.getWorld();
        if (!chunk.getUpgradeData().sidesToUpgrade.remove((Object)side)) {
            return;
        }
        Set<Direction> set = side.getDirections();
        boolean i = false;
        int j = 15;
        boolean bl = set.contains(Direction.EAST);
        boolean bl2 = set.contains(Direction.WEST);
        boolean bl3 = set.contains(Direction.SOUTH);
        boolean bl4 = set.contains(Direction.NORTH);
        boolean bl5 = set.size() == 1;
        ChunkPos lv2 = chunk.getPos();
        int k = lv2.getStartX() + (bl5 && (bl4 || bl3) ? 1 : (bl2 ? 0 : 15));
        int l = lv2.getStartX() + (bl5 && (bl4 || bl3) ? 14 : (bl2 ? 0 : 15));
        int m = lv2.getStartZ() + (bl5 && (bl || bl2) ? 1 : (bl4 ? 0 : 15));
        int n = lv2.getStartZ() + (bl5 && (bl || bl2) ? 14 : (bl4 ? 0 : 15));
        Direction[] lvs = Direction.values();
        BlockPos.Mutable lv3 = new BlockPos.Mutable();
        for (BlockPos lv4 : BlockPos.iterate(k, lv.getBottomY(), m, l, lv.getTopY() - 1, n)) {
            BlockState lv5;
            BlockState lv6 = lv5 = lv.getBlockState(lv4);
            for (Direction lv7 : lvs) {
                lv3.set((Vec3i)lv4, lv7);
                lv6 = UpgradeData.applyAdjacentBlock(lv6, lv7, lv, lv4, lv3);
            }
            Block.replace(lv5, lv6, lv, lv4, Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
        }
    }

    private static BlockState applyAdjacentBlock(BlockState oldState, Direction dir, WorldAccess world, BlockPos currentPos, BlockPos otherPos) {
        return BLOCK_TO_LOGIC.getOrDefault(oldState.getBlock(), BuiltinLogic.DEFAULT).getUpdatedState(oldState, dir, world.getBlockState(otherPos), world, currentPos, otherPos);
    }

    private void upgradeCenter(WorldChunk chunk) {
        int i;
        BlockPos.Mutable lv = new BlockPos.Mutable();
        BlockPos.Mutable lv2 = new BlockPos.Mutable();
        ChunkPos lv3 = chunk.getPos();
        World lv4 = chunk.getWorld();
        for (i = 0; i < this.centerIndicesToUpgrade.length; ++i) {
            ChunkSection lv5 = chunk.getSection(i);
            int[] is = this.centerIndicesToUpgrade[i];
            this.centerIndicesToUpgrade[i] = null;
            if (is == null || is.length <= 0) continue;
            Direction[] lvs = Direction.values();
            PalettedContainer<BlockState> lv6 = lv5.getBlockStateContainer();
            int j = chunk.sectionIndexToCoord(i);
            int k = ChunkSectionPos.getBlockCoord(j);
            for (int l : is) {
                BlockState lv7;
                int m = l & 0xF;
                int n = l >> 8 & 0xF;
                int o = l >> 4 & 0xF;
                lv.set(lv3.getStartX() + m, k + n, lv3.getStartZ() + o);
                BlockState lv8 = lv7 = lv6.get(l);
                for (Direction lv9 : lvs) {
                    lv2.set((Vec3i)lv, lv9);
                    if (ChunkSectionPos.getSectionCoord(lv.getX()) != lv3.x || ChunkSectionPos.getSectionCoord(lv.getZ()) != lv3.z) continue;
                    lv8 = UpgradeData.applyAdjacentBlock(lv8, lv9, lv4, lv, lv2);
                }
                Block.replace(lv7, lv8, lv4, lv, Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
            }
        }
        for (i = 0; i < this.centerIndicesToUpgrade.length; ++i) {
            if (this.centerIndicesToUpgrade[i] != null) {
                LOGGER.warn("Discarding update data for section {} for chunk ({} {})", lv4.sectionIndexToCoord(i), lv3.x, lv3.z);
            }
            this.centerIndicesToUpgrade[i] = null;
        }
    }

    public boolean isDone() {
        for (int[] is : this.centerIndicesToUpgrade) {
            if (is == null) continue;
            return false;
        }
        return this.sidesToUpgrade.isEmpty();
    }

    public NbtCompound toNbt() {
        NbtList lv4;
        int i;
        NbtCompound lv = new NbtCompound();
        NbtCompound lv2 = new NbtCompound();
        for (i = 0; i < this.centerIndicesToUpgrade.length; ++i) {
            String string = String.valueOf(i);
            if (this.centerIndicesToUpgrade[i] == null || this.centerIndicesToUpgrade[i].length == 0) continue;
            lv2.putIntArray(string, this.centerIndicesToUpgrade[i]);
        }
        if (!lv2.isEmpty()) {
            lv.put(INDICES_KEY, lv2);
        }
        i = 0;
        for (EightWayDirection lv3 : this.sidesToUpgrade) {
            i |= 1 << lv3.ordinal();
        }
        lv.putByte("Sides", (byte)i);
        if (!this.blockTicks.isEmpty()) {
            lv4 = new NbtList();
            this.blockTicks.forEach(blockTick -> lv4.add(blockTick.toNbt(block -> Registries.BLOCK.getId((Block)block).toString())));
            lv.put("neighbor_block_ticks", lv4);
        }
        if (!this.fluidTicks.isEmpty()) {
            lv4 = new NbtList();
            this.fluidTicks.forEach(fluidTick -> lv4.add(fluidTick.toNbt(fluid -> Registries.FLUID.getId((Fluid)fluid).toString())));
            lv.put("neighbor_fluid_ticks", lv4);
        }
        return lv;
    }

    static enum BuiltinLogic implements Logic
    {
        BLACKLIST(new Block[]{Blocks.OBSERVER, Blocks.NETHER_PORTAL, Blocks.WHITE_CONCRETE_POWDER, Blocks.ORANGE_CONCRETE_POWDER, Blocks.MAGENTA_CONCRETE_POWDER, Blocks.LIGHT_BLUE_CONCRETE_POWDER, Blocks.YELLOW_CONCRETE_POWDER, Blocks.LIME_CONCRETE_POWDER, Blocks.PINK_CONCRETE_POWDER, Blocks.GRAY_CONCRETE_POWDER, Blocks.LIGHT_GRAY_CONCRETE_POWDER, Blocks.CYAN_CONCRETE_POWDER, Blocks.PURPLE_CONCRETE_POWDER, Blocks.BLUE_CONCRETE_POWDER, Blocks.BROWN_CONCRETE_POWDER, Blocks.GREEN_CONCRETE_POWDER, Blocks.RED_CONCRETE_POWDER, Blocks.BLACK_CONCRETE_POWDER, Blocks.ANVIL, Blocks.CHIPPED_ANVIL, Blocks.DAMAGED_ANVIL, Blocks.DRAGON_EGG, Blocks.GRAVEL, Blocks.SAND, Blocks.RED_SAND, Blocks.OAK_SIGN, Blocks.SPRUCE_SIGN, Blocks.BIRCH_SIGN, Blocks.ACACIA_SIGN, Blocks.CHERRY_SIGN, Blocks.JUNGLE_SIGN, Blocks.DARK_OAK_SIGN, Blocks.OAK_WALL_SIGN, Blocks.SPRUCE_WALL_SIGN, Blocks.BIRCH_WALL_SIGN, Blocks.ACACIA_WALL_SIGN, Blocks.JUNGLE_WALL_SIGN, Blocks.DARK_OAK_WALL_SIGN, Blocks.OAK_HANGING_SIGN, Blocks.SPRUCE_HANGING_SIGN, Blocks.BIRCH_HANGING_SIGN, Blocks.ACACIA_HANGING_SIGN, Blocks.JUNGLE_HANGING_SIGN, Blocks.DARK_OAK_HANGING_SIGN, Blocks.OAK_WALL_HANGING_SIGN, Blocks.SPRUCE_WALL_HANGING_SIGN, Blocks.BIRCH_WALL_HANGING_SIGN, Blocks.ACACIA_WALL_HANGING_SIGN, Blocks.JUNGLE_WALL_HANGING_SIGN, Blocks.DARK_OAK_WALL_HANGING_SIGN}){

            @Override
            public BlockState getUpdatedState(BlockState oldState, Direction direction, BlockState otherState, WorldAccess world, BlockPos currentPos, BlockPos otherPos) {
                return oldState;
            }
        }
        ,
        DEFAULT(new Block[0]){

            @Override
            public BlockState getUpdatedState(BlockState oldState, Direction direction, BlockState otherState, WorldAccess world, BlockPos currentPos, BlockPos otherPos) {
                return oldState.getStateForNeighborUpdate(direction, world.getBlockState(otherPos), world, currentPos, otherPos);
            }
        }
        ,
        CHEST(new Block[]{Blocks.CHEST, Blocks.TRAPPED_CHEST}){

            @Override
            public BlockState getUpdatedState(BlockState oldState, Direction direction, BlockState otherState, WorldAccess world, BlockPos currentPos, BlockPos otherPos) {
                if (otherState.isOf(oldState.getBlock()) && direction.getAxis().isHorizontal() && oldState.get(ChestBlock.CHEST_TYPE) == ChestType.SINGLE && otherState.get(ChestBlock.CHEST_TYPE) == ChestType.SINGLE) {
                    Direction lv = oldState.get(ChestBlock.FACING);
                    if (direction.getAxis() != lv.getAxis() && lv == otherState.get(ChestBlock.FACING)) {
                        ChestType lv2 = direction == lv.rotateYClockwise() ? ChestType.LEFT : ChestType.RIGHT;
                        world.setBlockState(otherPos, (BlockState)otherState.with(ChestBlock.CHEST_TYPE, lv2.getOpposite()), Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
                        if (lv == Direction.NORTH || lv == Direction.EAST) {
                            BlockEntity lv3 = world.getBlockEntity(currentPos);
                            BlockEntity lv4 = world.getBlockEntity(otherPos);
                            if (lv3 instanceof ChestBlockEntity && lv4 instanceof ChestBlockEntity) {
                                ChestBlockEntity.copyInventory((ChestBlockEntity)lv3, (ChestBlockEntity)lv4);
                            }
                        }
                        return (BlockState)oldState.with(ChestBlock.CHEST_TYPE, lv2);
                    }
                }
                return oldState;
            }
        }
        ,
        LEAVES(true, new Block[]{Blocks.ACACIA_LEAVES, Blocks.CHERRY_LEAVES, Blocks.BIRCH_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES}){
            private final ThreadLocal<List<ObjectSet<BlockPos>>> distanceToPositions = ThreadLocal.withInitial(() -> Lists.newArrayListWithCapacity(7));

            @Override
            public BlockState getUpdatedState(BlockState oldState, Direction direction, BlockState otherState, WorldAccess world, BlockPos currentPos, BlockPos otherPos) {
                BlockState lv = oldState.getStateForNeighborUpdate(direction, world.getBlockState(otherPos), world, currentPos, otherPos);
                if (oldState != lv) {
                    int i = lv.get(Properties.DISTANCE_1_7);
                    List<ObjectSet<BlockPos>> list = this.distanceToPositions.get();
                    if (list.isEmpty()) {
                        for (int j = 0; j < 7; ++j) {
                            list.add(new ObjectOpenHashSet());
                        }
                    }
                    list.get(i).add(currentPos.toImmutable());
                }
                return oldState;
            }

            @Override
            public void postUpdate(WorldAccess world) {
                BlockPos.Mutable lv = new BlockPos.Mutable();
                List<ObjectSet<BlockPos>> list = this.distanceToPositions.get();
                for (int i = 2; i < list.size(); ++i) {
                    int j = i - 1;
                    ObjectSet<BlockPos> objectSet = list.get(j);
                    ObjectSet<BlockPos> objectSet2 = list.get(i);
                    for (BlockPos lv2 : objectSet) {
                        BlockState lv3 = world.getBlockState(lv2);
                        if (lv3.get(Properties.DISTANCE_1_7) < j) continue;
                        world.setBlockState(lv2, (BlockState)lv3.with(Properties.DISTANCE_1_7, j), Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
                        if (i == 7) continue;
                        for (Direction lv4 : DIRECTIONS) {
                            lv.set((Vec3i)lv2, lv4);
                            BlockState lv5 = world.getBlockState(lv);
                            if (!lv5.contains(Properties.DISTANCE_1_7) || lv3.get(Properties.DISTANCE_1_7) <= i) continue;
                            objectSet2.add(lv.toImmutable());
                        }
                    }
                }
                list.clear();
            }
        }
        ,
        STEM_BLOCK(new Block[]{Blocks.MELON_STEM, Blocks.PUMPKIN_STEM}){

            @Override
            public BlockState getUpdatedState(BlockState oldState, Direction direction, BlockState otherState, WorldAccess world, BlockPos currentPos, BlockPos otherPos) {
                if (oldState.get(StemBlock.AGE) == 7) {
                    Block lv;
                    Block block = lv = oldState.isOf(Blocks.PUMPKIN_STEM) ? Blocks.PUMPKIN : Blocks.MELON;
                    if (otherState.isOf(lv)) {
                        return (BlockState)(oldState.isOf(Blocks.PUMPKIN_STEM) ? Blocks.ATTACHED_PUMPKIN_STEM : Blocks.ATTACHED_MELON_STEM).getDefaultState().with(HorizontalFacingBlock.FACING, direction);
                    }
                }
                return oldState;
            }
        };

        public static final Direction[] DIRECTIONS;

        BuiltinLogic(Block ... blocks) {
            this(false, blocks);
        }

        BuiltinLogic(boolean addCallback, Block ... blocks) {
            for (Block lv : blocks) {
                BLOCK_TO_LOGIC.put(lv, this);
            }
            if (addCallback) {
                CALLBACK_LOGICS.add(this);
            }
        }

        static {
            DIRECTIONS = Direction.values();
        }
    }

    public static interface Logic {
        public BlockState getUpdatedState(BlockState var1, Direction var2, BlockState var3, WorldAccess var4, BlockPos var5, BlockPos var6);

        default public void postUpdate(WorldAccess world) {
        }
    }
}

