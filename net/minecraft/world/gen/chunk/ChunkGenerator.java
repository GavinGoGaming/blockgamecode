/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world.gen.chunk;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.SharedConstants;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureSet;
import net.minecraft.structure.StructureStart;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.Util;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.RandomSeed;
import net.minecraft.util.math.random.Xoroshiro128PlusPlusRandom;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructurePresence;
import net.minecraft.world.StructureSpawns;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.chunk.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.gen.chunk.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.gen.chunk.placement.StructurePlacement;
import net.minecraft.world.gen.chunk.placement.StructurePlacementCalculator;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.util.PlacedFeatureIndexer;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.structure.Structure;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;

public abstract class ChunkGenerator {
    public static final Codec<ChunkGenerator> CODEC = Registries.CHUNK_GENERATOR.getCodec().dispatchStable(ChunkGenerator::getCodec, Function.identity());
    protected final BiomeSource biomeSource;
    private final Supplier<List<PlacedFeatureIndexer.IndexedFeatures>> indexedFeaturesListSupplier;
    private final Function<RegistryEntry<Biome>, GenerationSettings> generationSettingsGetter;

    public ChunkGenerator(BiomeSource biomeSource) {
        this(biomeSource, biomeEntry -> ((Biome)biomeEntry.value()).getGenerationSettings());
    }

    public ChunkGenerator(BiomeSource biomeSource, Function<RegistryEntry<Biome>, GenerationSettings> generationSettingsGetter) {
        this.biomeSource = biomeSource;
        this.generationSettingsGetter = generationSettingsGetter;
        this.indexedFeaturesListSupplier = Suppliers.memoize(() -> PlacedFeatureIndexer.collectIndexedFeatures(List.copyOf(biomeSource.getBiomes()), biomeEntry -> ((GenerationSettings)generationSettingsGetter.apply((RegistryEntry<Biome>)biomeEntry)).getFeatures(), true));
    }

    public void initializeIndexedFeaturesList() {
        this.indexedFeaturesListSupplier.get();
    }

    protected abstract MapCodec<? extends ChunkGenerator> getCodec();

    public StructurePlacementCalculator createStructurePlacementCalculator(RegistryWrapper<StructureSet> structureSetRegistry, NoiseConfig noiseConfig, long seed) {
        return StructurePlacementCalculator.create(noiseConfig, seed, this.biomeSource, structureSetRegistry);
    }

    public Optional<RegistryKey<MapCodec<? extends ChunkGenerator>>> getCodecKey() {
        return Registries.CHUNK_GENERATOR.getKey(this.getCodec());
    }

    public CompletableFuture<Chunk> populateBiomes(NoiseConfig noiseConfig, Blender blender, StructureAccessor structureAccessor, Chunk chunk) {
        return CompletableFuture.supplyAsync(Util.debugSupplier("init_biomes", () -> {
            chunk.populateBiomes(this.biomeSource, noiseConfig.getMultiNoiseSampler());
            return chunk;
        }), Util.getMainWorkerExecutor());
    }

    public abstract void carve(ChunkRegion var1, long var2, NoiseConfig var4, BiomeAccess var5, StructureAccessor var6, Chunk var7, GenerationStep.Carver var8);

    @Nullable
    public Pair<BlockPos, RegistryEntry<Structure>> locateStructure(ServerWorld world, RegistryEntryList<Structure> structures, BlockPos center, int radius, boolean skipReferencedStructures) {
        StructurePlacementCalculator lv = world.getChunkManager().getStructurePlacementCalculator();
        Object2ObjectArrayMap<StructurePlacement, Set> map = new Object2ObjectArrayMap<StructurePlacement, Set>();
        for (RegistryEntry registryEntry : structures) {
            for (StructurePlacement lv3 : lv.getPlacements(registryEntry)) {
                map.computeIfAbsent(lv3, placement -> new ObjectArraySet()).add(registryEntry);
            }
        }
        if (map.isEmpty()) {
            return null;
        }
        Pair<BlockPos, RegistryEntry<Structure>> pair = null;
        double d = Double.MAX_VALUE;
        StructureAccessor lv4 = world.getStructureAccessor();
        ArrayList list = new ArrayList(map.size());
        for (Map.Entry entry : map.entrySet()) {
            StructurePlacement lv5 = (StructurePlacement)entry.getKey();
            if (lv5 instanceof ConcentricRingsStructurePlacement) {
                BlockPos blockPos;
                double e;
                ConcentricRingsStructurePlacement lv6 = (ConcentricRingsStructurePlacement)lv5;
                Pair<BlockPos, RegistryEntry<Structure>> pair2 = this.locateConcentricRingsStructure((Set)entry.getValue(), world, lv4, center, skipReferencedStructures, lv6);
                if (pair2 == null || !((e = center.getSquaredDistance(blockPos = pair2.getFirst())) < d)) continue;
                d = e;
                pair = pair2;
                continue;
            }
            if (!(lv5 instanceof RandomSpreadStructurePlacement)) continue;
            list.add(entry);
        }
        if (!list.isEmpty()) {
            int j = ChunkSectionPos.getSectionCoord(center.getX());
            int k = ChunkSectionPos.getSectionCoord(center.getZ());
            for (int l = 0; l <= radius; ++l) {
                boolean bl2 = false;
                for (Map.Entry entry : list) {
                    RandomSpreadStructurePlacement lv8 = (RandomSpreadStructurePlacement)entry.getKey();
                    Pair<BlockPos, RegistryEntry<Structure>> pair3 = ChunkGenerator.locateRandomSpreadStructure((Set)entry.getValue(), world, lv4, j, k, l, skipReferencedStructures, lv.getStructureSeed(), lv8);
                    if (pair3 == null) continue;
                    bl2 = true;
                    double f = center.getSquaredDistance(pair3.getFirst());
                    if (!(f < d)) continue;
                    d = f;
                    pair = pair3;
                }
                if (!bl2) continue;
                return pair;
            }
        }
        return pair;
    }

    @Nullable
    private Pair<BlockPos, RegistryEntry<Structure>> locateConcentricRingsStructure(Set<RegistryEntry<Structure>> structures, ServerWorld world, StructureAccessor structureAccessor, BlockPos center, boolean skipReferencedStructures, ConcentricRingsStructurePlacement placement) {
        List<ChunkPos> list = world.getChunkManager().getStructurePlacementCalculator().getPlacementPositions(placement);
        if (list == null) {
            throw new IllegalStateException("Somehow tried to find structures for a placement that doesn't exist");
        }
        Pair<BlockPos, RegistryEntry<Structure>> pair = null;
        double d = Double.MAX_VALUE;
        BlockPos.Mutable lv = new BlockPos.Mutable();
        for (ChunkPos lv2 : list) {
            Pair<BlockPos, RegistryEntry<Structure>> pair2;
            lv.set(ChunkSectionPos.getOffsetPos(lv2.x, 8), 32, ChunkSectionPos.getOffsetPos(lv2.z, 8));
            double e = lv.getSquaredDistance(center);
            boolean bl2 = pair == null || e < d;
            if (!bl2 || (pair2 = ChunkGenerator.locateStructure(structures, world, structureAccessor, skipReferencedStructures, placement, lv2)) == null) continue;
            pair = pair2;
            d = e;
        }
        return pair;
    }

    @Nullable
    private static Pair<BlockPos, RegistryEntry<Structure>> locateRandomSpreadStructure(Set<RegistryEntry<Structure>> structures, WorldView world, StructureAccessor structureAccessor, int centerChunkX, int centerChunkZ, int radius, boolean skipReferencedStructures, long seed, RandomSpreadStructurePlacement placement) {
        int m = placement.getSpacing();
        for (int n = -radius; n <= radius; ++n) {
            boolean bl2 = n == -radius || n == radius;
            for (int o = -radius; o <= radius; ++o) {
                int q;
                int p;
                ChunkPos lv;
                Pair<BlockPos, RegistryEntry<Structure>> pair;
                boolean bl3;
                boolean bl = bl3 = o == -radius || o == radius;
                if (!bl2 && !bl3 || (pair = ChunkGenerator.locateStructure(structures, world, structureAccessor, skipReferencedStructures, placement, lv = placement.getStartChunk(seed, p = centerChunkX + m * n, q = centerChunkZ + m * o))) == null) continue;
                return pair;
            }
        }
        return null;
    }

    @Nullable
    private static Pair<BlockPos, RegistryEntry<Structure>> locateStructure(Set<RegistryEntry<Structure>> structures, WorldView world, StructureAccessor structureAccessor, boolean skipReferencedStructures, StructurePlacement placement, ChunkPos pos) {
        for (RegistryEntry<Structure> lv : structures) {
            StructurePresence lv2 = structureAccessor.getStructurePresence(pos, lv.value(), placement, skipReferencedStructures);
            if (lv2 == StructurePresence.START_NOT_PRESENT) continue;
            if (!skipReferencedStructures && lv2 == StructurePresence.START_PRESENT) {
                return Pair.of(placement.getLocatePos(pos), lv);
            }
            Chunk lv3 = world.getChunk(pos.x, pos.z, ChunkStatus.STRUCTURE_STARTS);
            StructureStart lv4 = structureAccessor.getStructureStart(ChunkSectionPos.from(lv3), lv.value(), lv3);
            if (lv4 == null || !lv4.hasChildren() || skipReferencedStructures && !ChunkGenerator.checkNotReferenced(structureAccessor, lv4)) continue;
            return Pair.of(placement.getLocatePos(lv4.getPos()), lv);
        }
        return null;
    }

    private static boolean checkNotReferenced(StructureAccessor structureAccessor, StructureStart start) {
        if (start.isNeverReferenced()) {
            structureAccessor.incrementReferences(start);
            return true;
        }
        return false;
    }

    public void generateFeatures(StructureWorldAccess world, Chunk chunk, StructureAccessor structureAccessor) {
        ChunkPos lv = chunk.getPos();
        if (SharedConstants.isOutsideGenerationArea(lv)) {
            return;
        }
        ChunkSectionPos lv2 = ChunkSectionPos.from(lv, world.getBottomSectionCoord());
        BlockPos lv3 = lv2.getMinPos();
        Registry<Structure> lv4 = world.getRegistryManager().get(RegistryKeys.STRUCTURE);
        Map<Integer, List<Structure>> map = lv4.stream().collect(Collectors.groupingBy(structureType -> structureType.getFeatureGenerationStep().ordinal()));
        List<PlacedFeatureIndexer.IndexedFeatures> list = this.indexedFeaturesListSupplier.get();
        ChunkRandom lv5 = new ChunkRandom(new Xoroshiro128PlusPlusRandom(RandomSeed.getSeed()));
        long l = lv5.setPopulationSeed(world.getSeed(), lv3.getX(), lv3.getZ());
        ObjectArraySet set = new ObjectArraySet();
        ChunkPos.stream(lv2.toChunkPos(), 1).forEach(pos -> {
            Chunk lv = world.getChunk(pos.x, pos.z);
            for (ChunkSection lv2 : lv.getSectionArray()) {
                lv2.getBiomeContainer().forEachValue(set::add);
            }
        });
        set.retainAll(this.biomeSource.getBiomes());
        int i = list.size();
        try {
            Registry<PlacedFeature> lv6 = world.getRegistryManager().get(RegistryKeys.PLACED_FEATURE);
            int j = Math.max(GenerationStep.Feature.values().length, i);
            for (int k = 0; k < j; ++k) {
                int m = 0;
                if (structureAccessor.shouldGenerateStructures()) {
                    List list2 = map.getOrDefault(k, Collections.emptyList());
                    for (Structure lv7 : list2) {
                        lv5.setDecoratorSeed(l, m, k);
                        Supplier<String> supplier = () -> lv4.getKey(lv7).map(Object::toString).orElseGet(lv7::toString);
                        try {
                            world.setCurrentlyGeneratingStructureName(supplier);
                            structureAccessor.getStructureStarts(lv2, lv7).forEach(start -> start.place(world, structureAccessor, this, lv5, ChunkGenerator.getBlockBoxForChunk(chunk), lv));
                        } catch (Exception exception) {
                            CrashReport lv8 = CrashReport.create(exception, "Feature placement");
                            lv8.addElement("Feature").add("Description", supplier::get);
                            throw new CrashException(lv8);
                        }
                        ++m;
                    }
                }
                if (k >= i) continue;
                IntArraySet intSet = new IntArraySet();
                for (RegistryEntry lv9 : set) {
                    List<RegistryEntryList<PlacedFeature>> list3 = this.generationSettingsGetter.apply(lv9).getFeatures();
                    if (k >= list3.size()) continue;
                    RegistryEntryList<PlacedFeature> lv10 = list3.get(k);
                    PlacedFeatureIndexer.IndexedFeatures lv11 = list.get(k);
                    lv10.stream().map(RegistryEntry::value).forEach(feature -> intSet.add(lv11.indexMapping().applyAsInt((PlacedFeature)feature)));
                }
                int n = intSet.size();
                int[] is = intSet.toIntArray();
                Arrays.sort(is);
                PlacedFeatureIndexer.IndexedFeatures lv12 = list.get(k);
                for (int o = 0; o < n; ++o) {
                    int p = is[o];
                    PlacedFeature lv13 = lv12.features().get(p);
                    Supplier<String> supplier2 = () -> lv6.getKey(lv13).map(Object::toString).orElseGet(lv13::toString);
                    lv5.setDecoratorSeed(l, p, k);
                    try {
                        world.setCurrentlyGeneratingStructureName(supplier2);
                        lv13.generate(world, this, lv5, lv3);
                        continue;
                    } catch (Exception exception2) {
                        CrashReport lv14 = CrashReport.create(exception2, "Feature placement");
                        lv14.addElement("Feature").add("Description", supplier2::get);
                        throw new CrashException(lv14);
                    }
                }
            }
            world.setCurrentlyGeneratingStructureName(null);
        } catch (Exception exception3) {
            CrashReport lv15 = CrashReport.create(exception3, "Biome decoration");
            lv15.addElement("Generation").add("CenterX", lv.x).add("CenterZ", lv.z).add("Decoration Seed", l);
            throw new CrashException(lv15);
        }
    }

    private static BlockBox getBlockBoxForChunk(Chunk chunk) {
        ChunkPos lv = chunk.getPos();
        int i = lv.getStartX();
        int j = lv.getStartZ();
        HeightLimitView lv2 = chunk.getHeightLimitView();
        int k = lv2.getBottomY() + 1;
        int l = lv2.getTopY() - 1;
        return new BlockBox(i, k, j, i + 15, l, j + 15);
    }

    public abstract void buildSurface(ChunkRegion var1, StructureAccessor var2, NoiseConfig var3, Chunk var4);

    public abstract void populateEntities(ChunkRegion var1);

    public int getSpawnHeight(HeightLimitView world) {
        return 64;
    }

    public BiomeSource getBiomeSource() {
        return this.biomeSource;
    }

    public abstract int getWorldHeight();

    public Pool<SpawnSettings.SpawnEntry> getEntitySpawnList(RegistryEntry<Biome> biome, StructureAccessor accessor, SpawnGroup group, BlockPos pos) {
        Map<Structure, LongSet> map = accessor.getStructureReferences(pos);
        for (Map.Entry<Structure, LongSet> entry : map.entrySet()) {
            Structure lv = entry.getKey();
            StructureSpawns lv2 = lv.getStructureSpawns().get(group);
            if (lv2 == null) continue;
            MutableBoolean mutableBoolean = new MutableBoolean(false);
            Predicate<StructureStart> predicate = lv2.boundingBox() == StructureSpawns.BoundingBox.PIECE ? start -> accessor.structureContains(pos, (StructureStart)start) : start -> start.getBoundingBox().contains(pos);
            accessor.acceptStructureStarts(lv, entry.getValue(), start -> {
                if (mutableBoolean.isFalse() && predicate.test((StructureStart)start)) {
                    mutableBoolean.setTrue();
                }
            });
            if (!mutableBoolean.isTrue()) continue;
            return lv2.spawns();
        }
        return biome.value().getSpawnSettings().getSpawnEntries(group);
    }

    public void setStructureStarts(DynamicRegistryManager registryManager, StructurePlacementCalculator placementCalculator, StructureAccessor structureAccessor, Chunk chunk, StructureTemplateManager structureTemplateManager) {
        ChunkPos lv = chunk.getPos();
        ChunkSectionPos lv2 = ChunkSectionPos.from(chunk);
        NoiseConfig lv3 = placementCalculator.getNoiseConfig();
        placementCalculator.getStructureSets().forEach(structureSet -> {
            StructurePlacement lv = ((StructureSet)structureSet.value()).placement();
            List<StructureSet.WeightedEntry> list = ((StructureSet)structureSet.value()).structures();
            for (StructureSet.WeightedEntry lv2 : list) {
                StructureStart lv3 = structureAccessor.getStructureStart(lv2, lv2.structure().value(), chunk);
                if (lv3 == null || !lv3.hasChildren()) continue;
                return;
            }
            if (!lv.shouldGenerate(placementCalculator, arg5.x, arg5.z)) {
                return;
            }
            if (list.size() == 1) {
                this.trySetStructureStart(list.get(0), structureAccessor, registryManager, lv3, structureTemplateManager, placementCalculator.getStructureSeed(), chunk, lv, lv2);
                return;
            }
            ArrayList<StructureSet.WeightedEntry> arrayList = new ArrayList<StructureSet.WeightedEntry>(list.size());
            arrayList.addAll(list);
            ChunkRandom lv4 = new ChunkRandom(new CheckedRandom(0L));
            lv4.setCarverSeed(placementCalculator.getStructureSeed(), arg5.x, arg5.z);
            int i = 0;
            for (StructureSet.WeightedEntry lv5 : arrayList) {
                i += lv5.weight();
            }
            while (!arrayList.isEmpty()) {
                StructureSet.WeightedEntry lv6;
                int j = lv4.nextInt(i);
                int k = 0;
                Iterator iterator = arrayList.iterator();
                while (iterator.hasNext() && (j -= (lv6 = (StructureSet.WeightedEntry)iterator.next()).weight()) >= 0) {
                    ++k;
                }
                StructureSet.WeightedEntry lv7 = (StructureSet.WeightedEntry)arrayList.get(k);
                if (this.trySetStructureStart(lv7, structureAccessor, registryManager, lv3, structureTemplateManager, placementCalculator.getStructureSeed(), chunk, lv, lv2)) {
                    return;
                }
                arrayList.remove(k);
                i -= lv7.weight();
            }
        });
    }

    private boolean trySetStructureStart(StructureSet.WeightedEntry weightedEntry, StructureAccessor structureAccessor, DynamicRegistryManager dynamicRegistryManager, NoiseConfig noiseConfig, StructureTemplateManager structureManager, long seed, Chunk chunk, ChunkPos pos, ChunkSectionPos sectionPos) {
        Structure lv = weightedEntry.structure().value();
        int i = ChunkGenerator.getStructureReferences(structureAccessor, chunk, sectionPos, lv);
        RegistryEntryList<Biome> lv2 = lv.getValidBiomes();
        Predicate<RegistryEntry<Biome>> predicate = lv2::contains;
        StructureStart lv3 = lv.createStructureStart(dynamicRegistryManager, this, this.biomeSource, noiseConfig, structureManager, seed, pos, i, chunk, predicate);
        if (lv3.hasChildren()) {
            structureAccessor.setStructureStart(sectionPos, lv, lv3, chunk);
            return true;
        }
        return false;
    }

    private static int getStructureReferences(StructureAccessor structureAccessor, Chunk chunk, ChunkSectionPos sectionPos, Structure structure) {
        StructureStart lv = structureAccessor.getStructureStart(sectionPos, structure, chunk);
        return lv != null ? lv.getReferences() : 0;
    }

    public void addStructureReferences(StructureWorldAccess world, StructureAccessor structureAccessor, Chunk chunk) {
        int i = 8;
        ChunkPos lv = chunk.getPos();
        int j = lv.x;
        int k = lv.z;
        int l = lv.getStartX();
        int m = lv.getStartZ();
        ChunkSectionPos lv2 = ChunkSectionPos.from(chunk);
        for (int n = j - 8; n <= j + 8; ++n) {
            for (int o = k - 8; o <= k + 8; ++o) {
                long p = ChunkPos.toLong(n, o);
                for (StructureStart lv3 : world.getChunk(n, o).getStructureStarts().values()) {
                    try {
                        if (!lv3.hasChildren() || !lv3.getBoundingBox().intersectsXZ(l, m, l + 15, m + 15)) continue;
                        structureAccessor.addStructureReference(lv2, lv3.getStructure(), p, chunk);
                        DebugInfoSender.sendStructureStart(world, lv3);
                    } catch (Exception exception) {
                        CrashReport lv4 = CrashReport.create(exception, "Generating structure reference");
                        CrashReportSection lv5 = lv4.addElement("Structure");
                        Optional<Registry<Structure>> optional = world.getRegistryManager().getOptional(RegistryKeys.STRUCTURE);
                        lv5.add("Id", () -> optional.map(structureTypeRegistry -> structureTypeRegistry.getId(lv3.getStructure()).toString()).orElse("UNKNOWN"));
                        lv5.add("Name", () -> Registries.STRUCTURE_TYPE.getId(lv3.getStructure().getType()).toString());
                        lv5.add("Class", () -> lv3.getStructure().getClass().getCanonicalName());
                        throw new CrashException(lv4);
                    }
                }
            }
        }
    }

    public abstract CompletableFuture<Chunk> populateNoise(Blender var1, NoiseConfig var2, StructureAccessor var3, Chunk var4);

    public abstract int getSeaLevel();

    public abstract int getMinimumY();

    public abstract int getHeight(int var1, int var2, Heightmap.Type var3, HeightLimitView var4, NoiseConfig var5);

    public abstract VerticalBlockSample getColumnSample(int var1, int var2, HeightLimitView var3, NoiseConfig var4);

    public int getHeightOnGround(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noiseConfig) {
        return this.getHeight(x, z, heightmap, world, noiseConfig);
    }

    public int getHeightInGround(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noiseConfig) {
        return this.getHeight(x, z, heightmap, world, noiseConfig) - 1;
    }

    public abstract void getDebugHudText(List<String> var1, NoiseConfig var2, BlockPos var3);

    @Deprecated
    public GenerationSettings getGenerationSettings(RegistryEntry<Biome> biomeEntry) {
        return this.generationSettingsGetter.apply(biomeEntry);
    }
}

