/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world.gen.chunk.placement;

import com.google.common.base.Stopwatch;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.structure.StructureSet;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.chunk.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.gen.chunk.placement.StructurePlacement;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.structure.Structure;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class StructurePlacementCalculator {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final NoiseConfig noiseConfig;
    private final BiomeSource biomeSource;
    private final long structureSeed;
    private final long concentricRingSeed;
    private final Map<Structure, List<StructurePlacement>> structuresToPlacements = new Object2ObjectOpenHashMap<Structure, List<StructurePlacement>>();
    private final Map<ConcentricRingsStructurePlacement, CompletableFuture<List<ChunkPos>>> concentricPlacementsToPositions = new Object2ObjectArrayMap<ConcentricRingsStructurePlacement, CompletableFuture<List<ChunkPos>>>();
    private boolean calculated;
    private final List<RegistryEntry<StructureSet>> structureSets;

    public static StructurePlacementCalculator create(NoiseConfig noiseConfig, long seed, BiomeSource biomeSource, Stream<RegistryEntry<StructureSet>> structureSets) {
        List<RegistryEntry<StructureSet>> list = structureSets.filter(structureSet -> StructurePlacementCalculator.hasValidBiome((StructureSet)structureSet.value(), biomeSource)).toList();
        return new StructurePlacementCalculator(noiseConfig, biomeSource, seed, 0L, list);
    }

    public static StructurePlacementCalculator create(NoiseConfig noiseConfig, long seed, BiomeSource biomeSource, RegistryWrapper<StructureSet> structureSetRegistry) {
        List<RegistryEntry<StructureSet>> list = structureSetRegistry.streamEntries().filter(structureSet -> StructurePlacementCalculator.hasValidBiome((StructureSet)structureSet.value(), biomeSource)).collect(Collectors.toUnmodifiableList());
        return new StructurePlacementCalculator(noiseConfig, biomeSource, seed, seed, list);
    }

    private static boolean hasValidBiome(StructureSet structureSet, BiomeSource biomeSource) {
        Stream stream = structureSet.structures().stream().flatMap(structure -> {
            Structure lv = structure.structure().value();
            return lv.getValidBiomes().stream();
        });
        return stream.anyMatch(biomeSource.getBiomes()::contains);
    }

    private StructurePlacementCalculator(NoiseConfig noiseConfig, BiomeSource biomeSource, long structureSeed, long concentricRingSeed, List<RegistryEntry<StructureSet>> structureSets) {
        this.noiseConfig = noiseConfig;
        this.structureSeed = structureSeed;
        this.biomeSource = biomeSource;
        this.concentricRingSeed = concentricRingSeed;
        this.structureSets = structureSets;
    }

    public List<RegistryEntry<StructureSet>> getStructureSets() {
        return this.structureSets;
    }

    private void calculate() {
        Set<RegistryEntry<Biome>> set = this.biomeSource.getBiomes();
        this.getStructureSets().forEach(structureSet -> {
            StructurePlacement lv4;
            StructureSet lv = (StructureSet)structureSet.value();
            boolean bl = false;
            for (StructureSet.WeightedEntry lv2 : lv.structures()) {
                Structure lv3 = lv2.structure().value();
                if (!lv3.getValidBiomes().stream().anyMatch(set::contains)) continue;
                this.structuresToPlacements.computeIfAbsent(lv3, structure -> new ArrayList()).add(lv.placement());
                bl = true;
            }
            if (bl && (lv4 = lv.placement()) instanceof ConcentricRingsStructurePlacement) {
                ConcentricRingsStructurePlacement lv5 = (ConcentricRingsStructurePlacement)lv4;
                this.concentricPlacementsToPositions.put(lv5, this.calculateConcentricsRingPlacementPos((RegistryEntry<StructureSet>)structureSet, lv5));
            }
        });
    }

    private CompletableFuture<List<ChunkPos>> calculateConcentricsRingPlacementPos(RegistryEntry<StructureSet> structureSetEntry, ConcentricRingsStructurePlacement placement) {
        if (placement.getCount() == 0) {
            return CompletableFuture.completedFuture(List.of());
        }
        Stopwatch stopwatch = Stopwatch.createStarted(Util.TICKER);
        int i = placement.getDistance();
        int j = placement.getCount();
        ArrayList<CompletableFuture<ChunkPos>> list = new ArrayList<CompletableFuture<ChunkPos>>(j);
        int k = placement.getSpread();
        RegistryEntryList<Biome> lv = placement.getPreferredBiomes();
        Random lv2 = Random.create();
        lv2.setSeed(this.concentricRingSeed);
        double d = lv2.nextDouble() * Math.PI * 2.0;
        int l = 0;
        int m = 0;
        for (int n = 0; n < j; ++n) {
            double e = (double)(4 * i + i * m * 6) + (lv2.nextDouble() - 0.5) * ((double)i * 2.5);
            int o = (int)Math.round(Math.cos(d) * e);
            int p = (int)Math.round(Math.sin(d) * e);
            Random lv3 = lv2.split();
            list.add(CompletableFuture.supplyAsync(() -> {
                Pair<BlockPos, RegistryEntry<Biome>> pair = this.biomeSource.locateBiome(ChunkSectionPos.getOffsetPos(o, 8), 0, ChunkSectionPos.getOffsetPos(p, 8), 112, lv::contains, lv3, this.noiseConfig.getMultiNoiseSampler());
                if (pair != null) {
                    BlockPos lv = pair.getFirst();
                    return new ChunkPos(ChunkSectionPos.getSectionCoord(lv.getX()), ChunkSectionPos.getSectionCoord(lv.getZ()));
                }
                return new ChunkPos(o, p);
            }, Util.getMainWorkerExecutor()));
            d += Math.PI * 2 / (double)k;
            if (++l != k) continue;
            l = 0;
            k += 2 * k / (++m + 1);
            k = Math.min(k, j - n);
            d += lv2.nextDouble() * Math.PI * 2.0;
        }
        return Util.combineSafe(list).thenApply(positions -> {
            double d = (double)stopwatch.stop().elapsed(TimeUnit.MILLISECONDS) / 1000.0;
            LOGGER.debug("Calculation for {} took {}s", (Object)structureSetEntry, (Object)d);
            return positions;
        });
    }

    public void tryCalculate() {
        if (!this.calculated) {
            this.calculate();
            this.calculated = true;
        }
    }

    @Nullable
    public List<ChunkPos> getPlacementPositions(ConcentricRingsStructurePlacement placement) {
        this.tryCalculate();
        CompletableFuture<List<ChunkPos>> completableFuture = this.concentricPlacementsToPositions.get(placement);
        return completableFuture != null ? completableFuture.join() : null;
    }

    public List<StructurePlacement> getPlacements(RegistryEntry<Structure> structureEntry) {
        this.tryCalculate();
        return this.structuresToPlacements.getOrDefault(structureEntry.value(), List.of());
    }

    public NoiseConfig getNoiseConfig() {
        return this.noiseConfig;
    }

    public boolean canGenerate(RegistryEntry<StructureSet> structureSetEntry, int centerChunkX, int centerChunkZ, int chunkCount) {
        StructurePlacement lv = structureSetEntry.value().placement();
        for (int l = centerChunkX - chunkCount; l <= centerChunkX + chunkCount; ++l) {
            for (int m = centerChunkZ - chunkCount; m <= centerChunkZ + chunkCount; ++m) {
                if (!lv.shouldGenerate(this, l, m)) continue;
                return true;
            }
        }
        return false;
    }

    public long getStructureSeed() {
        return this.structureSeed;
    }
}

