/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.GravityField;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.SpawnDensityCapper;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.structure.NetherFortressStructure;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureKeys;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public final class SpawnHelper {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MIN_SPAWN_DISTANCE = 24;
    public static final int field_30972 = 8;
    public static final int field_30973 = 128;
    static final int CHUNK_AREA = (int)Math.pow(17.0, 2.0);
    private static final SpawnGroup[] SPAWNABLE_GROUPS = (SpawnGroup[])Stream.of(SpawnGroup.values()).filter(spawnGroup -> spawnGroup != SpawnGroup.MISC).toArray(SpawnGroup[]::new);

    private SpawnHelper() {
    }

    public static Info setupSpawn(int spawningChunkCount, Iterable<Entity> entities, ChunkSource chunkSource, SpawnDensityCapper densityCapper) {
        GravityField lv = new GravityField();
        Object2IntOpenHashMap<SpawnGroup> object2IntOpenHashMap = new Object2IntOpenHashMap<SpawnGroup>();
        for (Entity lv2 : entities) {
            SpawnGroup lv4;
            MobEntity lv3;
            if (lv2 instanceof MobEntity && ((lv3 = (MobEntity)lv2).isPersistent() || lv3.cannotDespawn()) || (lv4 = lv2.getType().getSpawnGroup()) == SpawnGroup.MISC) continue;
            BlockPos lv5 = lv2.getBlockPos();
            chunkSource.query(ChunkPos.toLong(lv5), chunk -> {
                SpawnSettings.SpawnDensity lv = SpawnHelper.getBiomeDirectly(lv5, chunk).getSpawnSettings().getSpawnDensity(lv2.getType());
                if (lv != null) {
                    lv.addPoint(lv2.getBlockPos(), lv.mass());
                }
                if (lv2 instanceof MobEntity) {
                    densityCapper.increaseDensity(chunk.getPos(), lv4);
                }
                object2IntOpenHashMap.addTo(lv4, 1);
            });
        }
        return new Info(spawningChunkCount, object2IntOpenHashMap, lv, densityCapper);
    }

    static Biome getBiomeDirectly(BlockPos pos, Chunk chunk) {
        return chunk.getBiomeForNoiseGen(BiomeCoords.fromBlock(pos.getX()), BiomeCoords.fromBlock(pos.getY()), BiomeCoords.fromBlock(pos.getZ())).value();
    }

    public static void spawn(ServerWorld world, WorldChunk chunk, Info info, boolean spawnAnimals, boolean spawnMonsters, boolean rareSpawn) {
        world.getProfiler().push("spawner");
        for (SpawnGroup lv : SPAWNABLE_GROUPS) {
            if (!spawnAnimals && lv.isPeaceful() || !spawnMonsters && !lv.isPeaceful() || !rareSpawn && lv.isRare() || !info.isBelowCap(lv, chunk.getPos())) continue;
            SpawnHelper.spawnEntitiesInChunk(lv, world, chunk, info::test, info::run);
        }
        world.getProfiler().pop();
    }

    public static void spawnEntitiesInChunk(SpawnGroup group, ServerWorld world, WorldChunk chunk, Checker checker, Runner runner) {
        BlockPos lv = SpawnHelper.getRandomPosInChunkSection(world, chunk);
        if (lv.getY() < world.getBottomY() + 1) {
            return;
        }
        SpawnHelper.spawnEntitiesInChunk(group, world, chunk, lv, checker, runner);
    }

    @Debug
    public static void spawnEntitiesInChunk(SpawnGroup group, ServerWorld world, BlockPos pos) {
        SpawnHelper.spawnEntitiesInChunk(group, world, world.getChunk(pos), pos, (type, posx, chunk) -> true, (entity, chunk) -> {});
    }

    public static void spawnEntitiesInChunk(SpawnGroup group, ServerWorld world, Chunk chunk, BlockPos pos, Checker checker, Runner runner) {
        StructureAccessor lv = world.getStructureAccessor();
        ChunkGenerator lv2 = world.getChunkManager().getChunkGenerator();
        int i = pos.getY();
        BlockState lv3 = chunk.getBlockState(pos);
        if (lv3.isSolidBlock(chunk, pos)) {
            return;
        }
        BlockPos.Mutable lv4 = new BlockPos.Mutable();
        int j = 0;
        block0: for (int k = 0; k < 3; ++k) {
            int l = pos.getX();
            int m = pos.getZ();
            int n = 6;
            SpawnSettings.SpawnEntry lv5 = null;
            EntityData lv6 = null;
            int o = MathHelper.ceil(world.random.nextFloat() * 4.0f);
            int p = 0;
            for (int q = 0; q < o; ++q) {
                double f;
                lv4.set(l += world.random.nextInt(6) - world.random.nextInt(6), i, m += world.random.nextInt(6) - world.random.nextInt(6));
                double d = (double)l + 0.5;
                double e = (double)m + 0.5;
                PlayerEntity lv7 = world.getClosestPlayer(d, (double)i, e, -1.0, false);
                if (lv7 == null || !SpawnHelper.isAcceptableSpawnPosition(world, chunk, lv4, f = lv7.squaredDistanceTo(d, i, e))) continue;
                if (lv5 == null) {
                    Optional<SpawnSettings.SpawnEntry> optional = SpawnHelper.pickRandomSpawnEntry(world, lv, lv2, group, world.random, lv4);
                    if (optional.isEmpty()) continue block0;
                    lv5 = optional.get();
                    o = lv5.minGroupSize + world.random.nextInt(1 + lv5.maxGroupSize - lv5.minGroupSize);
                }
                if (!SpawnHelper.canSpawn(world, group, lv, lv2, lv5, lv4, f) || !checker.test(lv5.type, lv4, chunk)) continue;
                MobEntity lv8 = SpawnHelper.createMob(world, lv5.type);
                if (lv8 == null) {
                    return;
                }
                lv8.refreshPositionAndAngles(d, i, e, world.random.nextFloat() * 360.0f, 0.0f);
                if (!SpawnHelper.isValidSpawn(world, lv8, f)) continue;
                lv6 = lv8.initialize(world, world.getLocalDifficulty(lv8.getBlockPos()), SpawnReason.NATURAL, lv6);
                ++p;
                world.spawnEntityAndPassengers(lv8);
                runner.run(lv8, chunk);
                if (++j >= lv8.getLimitPerChunk()) {
                    return;
                }
                if (lv8.spawnsTooManyForEachTry(p)) continue block0;
            }
        }
    }

    private static boolean isAcceptableSpawnPosition(ServerWorld world, Chunk chunk, BlockPos.Mutable pos, double squaredDistance) {
        if (squaredDistance <= 576.0) {
            return false;
        }
        if (world.getSpawnPos().isWithinDistance(new Vec3d((double)pos.getX() + 0.5, pos.getY(), (double)pos.getZ() + 0.5), 24.0)) {
            return false;
        }
        return Objects.equals(new ChunkPos(pos), chunk.getPos()) || world.shouldTick(pos);
    }

    private static boolean canSpawn(ServerWorld world, SpawnGroup group, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, SpawnSettings.SpawnEntry spawnEntry, BlockPos.Mutable pos, double squaredDistance) {
        EntityType<?> lv = spawnEntry.type;
        if (lv.getSpawnGroup() == SpawnGroup.MISC) {
            return false;
        }
        if (!lv.isSpawnableFarFromPlayer() && squaredDistance > (double)(lv.getSpawnGroup().getImmediateDespawnRange() * lv.getSpawnGroup().getImmediateDespawnRange())) {
            return false;
        }
        if (!lv.isSummonable() || !SpawnHelper.containsSpawnEntry(world, structureAccessor, chunkGenerator, group, spawnEntry, pos)) {
            return false;
        }
        if (!SpawnRestriction.isSpawnPosAllowed(lv, world, pos)) {
            return false;
        }
        if (!SpawnRestriction.canSpawn(lv, world, SpawnReason.NATURAL, pos, world.random)) {
            return false;
        }
        return world.isSpaceEmpty(lv.getSpawnBox((double)pos.getX() + 0.5, pos.getY(), (double)pos.getZ() + 0.5));
    }

    @Nullable
    private static MobEntity createMob(ServerWorld world, EntityType<?> type) {
        try {
            Object obj = type.create(world);
            if (obj instanceof MobEntity) {
                MobEntity lv = (MobEntity)obj;
                return lv;
            }
            LOGGER.warn("Can't spawn entity of type: {}", (Object)Registries.ENTITY_TYPE.getId(type));
        } catch (Exception exception) {
            LOGGER.warn("Failed to create mob", exception);
        }
        return null;
    }

    private static boolean isValidSpawn(ServerWorld world, MobEntity entity, double squaredDistance) {
        if (squaredDistance > (double)(entity.getType().getSpawnGroup().getImmediateDespawnRange() * entity.getType().getSpawnGroup().getImmediateDespawnRange()) && entity.canImmediatelyDespawn(squaredDistance)) {
            return false;
        }
        return entity.canSpawn(world, SpawnReason.NATURAL) && entity.canSpawn(world);
    }

    private static Optional<SpawnSettings.SpawnEntry> pickRandomSpawnEntry(ServerWorld world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, SpawnGroup spawnGroup, Random random, BlockPos pos) {
        RegistryEntry<Biome> lv = world.getBiome(pos);
        if (spawnGroup == SpawnGroup.WATER_AMBIENT && lv.isIn(BiomeTags.REDUCE_WATER_AMBIENT_SPAWNS) && random.nextFloat() < 0.98f) {
            return Optional.empty();
        }
        return SpawnHelper.getSpawnEntries(world, structureAccessor, chunkGenerator, spawnGroup, pos, lv).getOrEmpty(random);
    }

    private static boolean containsSpawnEntry(ServerWorld world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, SpawnGroup spawnGroup, SpawnSettings.SpawnEntry spawnEntry, BlockPos pos) {
        return SpawnHelper.getSpawnEntries(world, structureAccessor, chunkGenerator, spawnGroup, pos, null).getEntries().contains(spawnEntry);
    }

    private static Pool<SpawnSettings.SpawnEntry> getSpawnEntries(ServerWorld world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, SpawnGroup spawnGroup, BlockPos pos, @Nullable RegistryEntry<Biome> biomeEntry) {
        if (SpawnHelper.shouldUseNetherFortressSpawns(pos, world, spawnGroup, structureAccessor)) {
            return NetherFortressStructure.MONSTER_SPAWNS;
        }
        return chunkGenerator.getEntitySpawnList(biomeEntry != null ? biomeEntry : world.getBiome(pos), structureAccessor, spawnGroup, pos);
    }

    public static boolean shouldUseNetherFortressSpawns(BlockPos pos, ServerWorld world, SpawnGroup spawnGroup, StructureAccessor structureAccessor) {
        if (spawnGroup != SpawnGroup.MONSTER || !world.getBlockState(pos.down()).isOf(Blocks.NETHER_BRICKS)) {
            return false;
        }
        Structure lv = structureAccessor.getRegistryManager().get(RegistryKeys.STRUCTURE).get(StructureKeys.FORTRESS);
        if (lv == null) {
            return false;
        }
        return structureAccessor.getStructureAt(pos, lv).hasChildren();
    }

    private static BlockPos getRandomPosInChunkSection(World world, WorldChunk chunk) {
        ChunkPos lv = chunk.getPos();
        int i = lv.getStartX() + world.random.nextInt(16);
        int j = lv.getStartZ() + world.random.nextInt(16);
        int k = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, i, j) + 1;
        int l = MathHelper.nextBetween(world.random, world.getBottomY(), k);
        return new BlockPos(i, l, j);
    }

    public static boolean isClearForSpawn(BlockView blockView, BlockPos pos, BlockState state, FluidState fluidState, EntityType<?> entityType) {
        if (state.isFullCube(blockView, pos)) {
            return false;
        }
        if (state.emitsRedstonePower()) {
            return false;
        }
        if (!fluidState.isEmpty()) {
            return false;
        }
        if (state.isIn(BlockTags.PREVENT_MOB_SPAWNING_INSIDE)) {
            return false;
        }
        return !entityType.isInvalidSpawn(state);
    }

    public static void populateEntities(ServerWorldAccess world, RegistryEntry<Biome> biomeEntry, ChunkPos chunkPos, Random random) {
        SpawnSettings lv = biomeEntry.value().getSpawnSettings();
        Pool<SpawnSettings.SpawnEntry> lv2 = lv.getSpawnEntries(SpawnGroup.CREATURE);
        if (lv2.isEmpty()) {
            return;
        }
        int i = chunkPos.getStartX();
        int j = chunkPos.getStartZ();
        while (random.nextFloat() < lv.getCreatureSpawnProbability()) {
            Optional<SpawnSettings.SpawnEntry> optional = lv2.getOrEmpty(random);
            if (optional.isEmpty()) continue;
            SpawnSettings.SpawnEntry lv3 = optional.get();
            int k = lv3.minGroupSize + random.nextInt(1 + lv3.maxGroupSize - lv3.minGroupSize);
            EntityData lv4 = null;
            int l = i + random.nextInt(16);
            int m = j + random.nextInt(16);
            int n = l;
            int o = m;
            for (int p = 0; p < k; ++p) {
                boolean bl = false;
                for (int q = 0; !bl && q < 4; ++q) {
                    BlockPos lv5 = SpawnHelper.getEntitySpawnPos(world, lv3.type, l, m);
                    if (lv3.type.isSummonable() && SpawnRestriction.isSpawnPosAllowed(lv3.type, world, lv5)) {
                        MobEntity lv7;
                        Object lv6;
                        float f = lv3.type.getWidth();
                        double d = MathHelper.clamp((double)l, (double)i + (double)f, (double)i + 16.0 - (double)f);
                        double e = MathHelper.clamp((double)m, (double)j + (double)f, (double)j + 16.0 - (double)f);
                        if (!world.isSpaceEmpty(lv3.type.getSpawnBox(d, lv5.getY(), e)) || !SpawnRestriction.canSpawn(lv3.type, world, SpawnReason.CHUNK_GENERATION, BlockPos.ofFloored(d, lv5.getY(), e), world.getRandom())) continue;
                        try {
                            lv6 = lv3.type.create(world.toServerWorld());
                        } catch (Exception exception) {
                            LOGGER.warn("Failed to create mob", exception);
                            continue;
                        }
                        if (lv6 == null) continue;
                        ((Entity)lv6).refreshPositionAndAngles(d, lv5.getY(), e, random.nextFloat() * 360.0f, 0.0f);
                        if (lv6 instanceof MobEntity && (lv7 = (MobEntity)lv6).canSpawn(world, SpawnReason.CHUNK_GENERATION) && lv7.canSpawn(world)) {
                            lv4 = lv7.initialize(world, world.getLocalDifficulty(lv7.getBlockPos()), SpawnReason.CHUNK_GENERATION, lv4);
                            world.spawnEntityAndPassengers(lv7);
                            bl = true;
                        }
                    }
                    l += random.nextInt(5) - random.nextInt(5);
                    m += random.nextInt(5) - random.nextInt(5);
                    while (l < i || l >= i + 16 || m < j || m >= j + 16) {
                        l = n + random.nextInt(5) - random.nextInt(5);
                        m = o + random.nextInt(5) - random.nextInt(5);
                    }
                }
            }
        }
    }

    private static BlockPos getEntitySpawnPos(WorldView world, EntityType<?> entityType, int x, int z) {
        int k = world.getTopY(SpawnRestriction.getHeightmapType(entityType), x, z);
        BlockPos.Mutable lv = new BlockPos.Mutable(x, k, z);
        if (world.getDimension().hasCeiling()) {
            do {
                lv.move(Direction.DOWN);
            } while (!world.getBlockState(lv).isAir());
            do {
                lv.move(Direction.DOWN);
            } while (world.getBlockState(lv).isAir() && lv.getY() > world.getBottomY());
        }
        return SpawnRestriction.getLocation(entityType).adjustPosition(world, lv.toImmutable());
    }

    @FunctionalInterface
    public static interface ChunkSource {
        public void query(long var1, Consumer<WorldChunk> var3);
    }

    public static class Info {
        private final int spawningChunkCount;
        private final Object2IntOpenHashMap<SpawnGroup> groupToCount;
        private final GravityField densityField;
        private final Object2IntMap<SpawnGroup> groupToCountView;
        private final SpawnDensityCapper densityCapper;
        @Nullable
        private BlockPos cachedPos;
        @Nullable
        private EntityType<?> cachedEntityType;
        private double cachedDensityMass;

        Info(int spawningChunkCount, Object2IntOpenHashMap<SpawnGroup> groupToCount, GravityField densityField, SpawnDensityCapper densityCapper) {
            this.spawningChunkCount = spawningChunkCount;
            this.groupToCount = groupToCount;
            this.densityField = densityField;
            this.densityCapper = densityCapper;
            this.groupToCountView = Object2IntMaps.unmodifiable(groupToCount);
        }

        private boolean test(EntityType<?> type, BlockPos pos, Chunk chunk) {
            double d;
            this.cachedPos = pos;
            this.cachedEntityType = type;
            SpawnSettings.SpawnDensity lv = SpawnHelper.getBiomeDirectly(pos, chunk).getSpawnSettings().getSpawnDensity(type);
            if (lv == null) {
                this.cachedDensityMass = 0.0;
                return true;
            }
            this.cachedDensityMass = d = lv.mass();
            double e = this.densityField.calculate(pos, d);
            return e <= lv.gravityLimit();
        }

        private void run(MobEntity entity, Chunk chunk) {
            SpawnSettings.SpawnDensity lv3;
            EntityType<?> lv = entity.getType();
            BlockPos lv2 = entity.getBlockPos();
            double d = lv2.equals(this.cachedPos) && lv == this.cachedEntityType ? this.cachedDensityMass : ((lv3 = SpawnHelper.getBiomeDirectly(lv2, chunk).getSpawnSettings().getSpawnDensity(lv)) != null ? lv3.mass() : 0.0);
            this.densityField.addPoint(lv2, d);
            SpawnGroup lv4 = lv.getSpawnGroup();
            this.groupToCount.addTo(lv4, 1);
            this.densityCapper.increaseDensity(new ChunkPos(lv2), lv4);
        }

        public int getSpawningChunkCount() {
            return this.spawningChunkCount;
        }

        public Object2IntMap<SpawnGroup> getGroupToCount() {
            return this.groupToCountView;
        }

        boolean isBelowCap(SpawnGroup group, ChunkPos chunkPos) {
            int i = group.getCapacity() * this.spawningChunkCount / CHUNK_AREA;
            if (this.groupToCount.getInt(group) >= i) {
                return false;
            }
            return this.densityCapper.canSpawn(group, chunkPos);
        }
    }

    @FunctionalInterface
    public static interface Checker {
        public boolean test(EntityType<?> var1, BlockPos var2, Chunk var3);
    }

    @FunctionalInterface
    public static interface Runner {
        public void run(MobEntity var1, Chunk var2);
    }
}

