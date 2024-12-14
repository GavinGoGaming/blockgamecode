/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world.gen.chunk;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Suppliers;
import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.SharedConstants;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.RandomSeed;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.BiomeSupplier;
import net.minecraft.world.chunk.BelowZeroRetrogen;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.StructureWeightSampler;
import net.minecraft.world.gen.carver.CarverContext;
import net.minecraft.world.gen.carver.CarvingMask;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.chunk.AquiferSampler;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.chunk.GenerationShapeConfig;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import net.minecraft.world.gen.densityfunction.DensityFunctions;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.noise.NoiseRouter;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

public final class NoiseChunkGenerator
extends ChunkGenerator {
    public static final MapCodec<NoiseChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)BiomeSource.CODEC.fieldOf("biome_source")).forGetter(generator -> generator.biomeSource), ((MapCodec)ChunkGeneratorSettings.REGISTRY_CODEC.fieldOf("settings")).forGetter(generator -> generator.settings)).apply((Applicative<NoiseChunkGenerator, ?>)instance, instance.stable(NoiseChunkGenerator::new)));
    private static final BlockState AIR = Blocks.AIR.getDefaultState();
    private final RegistryEntry<ChunkGeneratorSettings> settings;
    private final Supplier<AquiferSampler.FluidLevelSampler> fluidLevelSampler;

    public NoiseChunkGenerator(BiomeSource biomeSource, RegistryEntry<ChunkGeneratorSettings> settings) {
        super(biomeSource);
        this.settings = settings;
        this.fluidLevelSampler = Suppliers.memoize(() -> NoiseChunkGenerator.createFluidLevelSampler((ChunkGeneratorSettings)settings.value()));
    }

    private static AquiferSampler.FluidLevelSampler createFluidLevelSampler(ChunkGeneratorSettings settings) {
        AquiferSampler.FluidLevel lv = new AquiferSampler.FluidLevel(-54, Blocks.LAVA.getDefaultState());
        int i = settings.seaLevel();
        AquiferSampler.FluidLevel lv2 = new AquiferSampler.FluidLevel(i, settings.defaultFluid());
        AquiferSampler.FluidLevel lv3 = new AquiferSampler.FluidLevel(DimensionType.MIN_HEIGHT * 2, Blocks.AIR.getDefaultState());
        return (x, y, z) -> {
            if (y < Math.min(-54, i)) {
                return lv;
            }
            return lv2;
        };
    }

    @Override
    public CompletableFuture<Chunk> populateBiomes(NoiseConfig noiseConfig, Blender blender, StructureAccessor structureAccessor, Chunk chunk) {
        return CompletableFuture.supplyAsync(Util.debugSupplier("init_biomes", () -> {
            this.populateBiomes(blender, noiseConfig, structureAccessor, chunk);
            return chunk;
        }), Util.getMainWorkerExecutor());
    }

    private void populateBiomes(Blender blender, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk) {
        ChunkNoiseSampler lv = chunk.getOrCreateChunkNoiseSampler(chunkx -> this.createChunkNoiseSampler((Chunk)chunkx, structureAccessor, blender, noiseConfig));
        BiomeSupplier lv2 = BelowZeroRetrogen.getBiomeSupplier(blender.getBiomeSupplier(this.biomeSource), chunk);
        chunk.populateBiomes(lv2, lv.createMultiNoiseSampler(noiseConfig.getNoiseRouter(), this.settings.value().spawnTarget()));
    }

    private ChunkNoiseSampler createChunkNoiseSampler(Chunk chunk, StructureAccessor world, Blender blender, NoiseConfig noiseConfig) {
        return ChunkNoiseSampler.create(chunk, noiseConfig, StructureWeightSampler.createStructureWeightSampler(world, chunk.getPos()), this.settings.value(), this.fluidLevelSampler.get(), blender);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> getCodec() {
        return CODEC;
    }

    public RegistryEntry<ChunkGeneratorSettings> getSettings() {
        return this.settings;
    }

    public boolean matchesSettings(RegistryKey<ChunkGeneratorSettings> settings) {
        return this.settings.matchesKey(settings);
    }

    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noiseConfig) {
        return this.sampleHeightmap(world, noiseConfig, x, z, null, heightmap.getBlockPredicate()).orElse(world.getBottomY());
    }

    @Override
    public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, NoiseConfig noiseConfig) {
        MutableObject<VerticalBlockSample> mutableObject = new MutableObject<VerticalBlockSample>();
        this.sampleHeightmap(world, noiseConfig, x, z, mutableObject, null);
        return mutableObject.getValue();
    }

    @Override
    public void getDebugHudText(List<String> text, NoiseConfig noiseConfig, BlockPos pos) {
        DecimalFormat decimalFormat = new DecimalFormat("0.000");
        NoiseRouter lv = noiseConfig.getNoiseRouter();
        DensityFunction.UnblendedNoisePos lv2 = new DensityFunction.UnblendedNoisePos(pos.getX(), pos.getY(), pos.getZ());
        double d = lv.ridges().sample(lv2);
        text.add("NoiseRouter T: " + decimalFormat.format(lv.temperature().sample(lv2)) + " V: " + decimalFormat.format(lv.vegetation().sample(lv2)) + " C: " + decimalFormat.format(lv.continents().sample(lv2)) + " E: " + decimalFormat.format(lv.erosion().sample(lv2)) + " D: " + decimalFormat.format(lv.depth().sample(lv2)) + " W: " + decimalFormat.format(d) + " PV: " + decimalFormat.format(DensityFunctions.getPeaksValleysNoise((float)d)) + " AS: " + decimalFormat.format(lv.initialDensityWithoutJaggedness().sample(lv2)) + " N: " + decimalFormat.format(lv.finalDensity().sample(lv2)));
    }

    private OptionalInt sampleHeightmap(HeightLimitView world, NoiseConfig noiseConfig, int x, int z, @Nullable MutableObject<VerticalBlockSample> columnSample, @Nullable Predicate<BlockState> stopPredicate) {
        BlockState[] lvs;
        GenerationShapeConfig lv = this.settings.value().generationShapeConfig().trimHeight(world);
        int k = lv.verticalCellBlockCount();
        int l = lv.minimumY();
        int m = MathHelper.floorDiv(l, k);
        int n = MathHelper.floorDiv(lv.height(), k);
        if (n <= 0) {
            return OptionalInt.empty();
        }
        if (columnSample == null) {
            lvs = null;
        } else {
            lvs = new BlockState[lv.height()];
            columnSample.setValue(new VerticalBlockSample(l, lvs));
        }
        int o = lv.horizontalCellBlockCount();
        int p = Math.floorDiv(x, o);
        int q = Math.floorDiv(z, o);
        int r = Math.floorMod(x, o);
        int s = Math.floorMod(z, o);
        int t = p * o;
        int u = q * o;
        double d = (double)r / (double)o;
        double e = (double)s / (double)o;
        ChunkNoiseSampler lv2 = new ChunkNoiseSampler(1, noiseConfig, t, u, lv, DensityFunctionTypes.Beardifier.INSTANCE, this.settings.value(), this.fluidLevelSampler.get(), Blender.getNoBlending());
        lv2.sampleStartDensity();
        lv2.sampleEndDensity(0);
        for (int v = n - 1; v >= 0; --v) {
            lv2.onSampledCellCorners(v, 0);
            for (int w = k - 1; w >= 0; --w) {
                BlockState lv4;
                int x2 = (m + v) * k + w;
                double f = (double)w / (double)k;
                lv2.interpolateY(x2, f);
                lv2.interpolateX(x, d);
                lv2.interpolateZ(z, e);
                BlockState lv3 = lv2.sampleBlockState();
                BlockState blockState = lv4 = lv3 == null ? this.settings.value().defaultBlock() : lv3;
                if (lvs != null) {
                    int y = v * k + w;
                    lvs[y] = lv4;
                }
                if (stopPredicate == null || !stopPredicate.test(lv4)) continue;
                lv2.stopInterpolation();
                return OptionalInt.of(x2 + 1);
            }
        }
        lv2.stopInterpolation();
        return OptionalInt.empty();
    }

    @Override
    public void buildSurface(ChunkRegion region, StructureAccessor structures, NoiseConfig noiseConfig, Chunk chunk) {
        if (SharedConstants.isOutsideGenerationArea(chunk.getPos())) {
            return;
        }
        HeightContext lv = new HeightContext(this, region);
        this.buildSurface(chunk, lv, noiseConfig, structures, region.getBiomeAccess(), region.getRegistryManager().get(RegistryKeys.BIOME), Blender.getBlender(region));
    }

    @VisibleForTesting
    public void buildSurface(Chunk chunk, HeightContext heightContext, NoiseConfig noiseConfig, StructureAccessor structureAccessor, BiomeAccess biomeAccess, Registry<Biome> biomeRegistry, Blender blender) {
        ChunkNoiseSampler lv = chunk.getOrCreateChunkNoiseSampler(chunkx -> this.createChunkNoiseSampler((Chunk)chunkx, structureAccessor, blender, noiseConfig));
        ChunkGeneratorSettings lv2 = this.settings.value();
        noiseConfig.getSurfaceBuilder().buildSurface(noiseConfig, biomeAccess, biomeRegistry, lv2.usesLegacyRandom(), heightContext, chunk, lv, lv2.surfaceRule());
    }

    @Override
    public void carve(ChunkRegion chunkRegion, long seed, NoiseConfig noiseConfig, BiomeAccess biomeAccess, StructureAccessor structureAccessor, Chunk chunk, GenerationStep.Carver carverStep) {
        BiomeAccess lv = biomeAccess.withSource((biomeX, biomeY, biomeZ) -> this.biomeSource.getBiome(biomeX, biomeY, biomeZ, noiseConfig.getMultiNoiseSampler()));
        ChunkRandom lv2 = new ChunkRandom(new CheckedRandom(RandomSeed.getSeed()));
        int i = 8;
        ChunkPos lv3 = chunk.getPos();
        ChunkNoiseSampler lv4 = chunk.getOrCreateChunkNoiseSampler(chunkx -> this.createChunkNoiseSampler((Chunk)chunkx, structureAccessor, Blender.getBlender(chunkRegion), noiseConfig));
        AquiferSampler lv5 = lv4.getAquiferSampler();
        CarverContext lv6 = new CarverContext(this, chunkRegion.getRegistryManager(), chunk.getHeightLimitView(), lv4, noiseConfig, this.settings.value().surfaceRule());
        CarvingMask lv7 = ((ProtoChunk)chunk).getOrCreateCarvingMask(carverStep);
        for (int j = -8; j <= 8; ++j) {
            for (int k = -8; k <= 8; ++k) {
                ChunkPos lv8 = new ChunkPos(lv3.x + j, lv3.z + k);
                Chunk lv9 = chunkRegion.getChunk(lv8.x, lv8.z);
                GenerationSettings lv10 = lv9.getOrCreateGenerationSettings(() -> this.getGenerationSettings(this.biomeSource.getBiome(BiomeCoords.fromBlock(lv8.getStartX()), 0, BiomeCoords.fromBlock(lv8.getStartZ()), noiseConfig.getMultiNoiseSampler())));
                Iterable<RegistryEntry<ConfiguredCarver<?>>> iterable = lv10.getCarversForStep(carverStep);
                int m = 0;
                for (RegistryEntry<ConfiguredCarver<?>> lv11 : iterable) {
                    ConfiguredCarver<?> lv12 = lv11.value();
                    lv2.setCarverSeed(seed + (long)m, lv8.x, lv8.z);
                    if (lv12.shouldCarve(lv2)) {
                        lv12.carve(lv6, chunk, lv::getBiome, lv2, lv5, lv8, lv7);
                    }
                    ++m;
                }
            }
        }
    }

    @Override
    public CompletableFuture<Chunk> populateNoise(Blender blender, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk) {
        GenerationShapeConfig lv = this.settings.value().generationShapeConfig().trimHeight(chunk.getHeightLimitView());
        int i = lv.minimumY();
        int j = MathHelper.floorDiv(i, lv.verticalCellBlockCount());
        int k = MathHelper.floorDiv(lv.height(), lv.verticalCellBlockCount());
        if (k <= 0) {
            return CompletableFuture.completedFuture(chunk);
        }
        return CompletableFuture.supplyAsync(Util.debugSupplier("wgen_fill_noise", () -> {
            int l = chunk.getSectionIndex(k * lv.verticalCellBlockCount() - 1 + i);
            int m = chunk.getSectionIndex(i);
            HashSet<ChunkSection> set = Sets.newHashSet();
            for (int n = l; n >= m; --n) {
                ChunkSection lv = chunk.getSection(n);
                lv.lock();
                set.add(lv);
            }
            try {
                Chunk chunk = this.populateNoise(blender, structureAccessor, noiseConfig, chunk, j, k);
                return chunk;
            } finally {
                for (ChunkSection lv2 : set) {
                    lv2.unlock();
                }
            }
        }), Util.getMainWorkerExecutor());
    }

    private Chunk populateNoise(Blender blender, StructureAccessor structureAccessor, NoiseConfig noiseConfig, Chunk chunk, int minimumCellY, int cellHeight) {
        ChunkNoiseSampler lv = chunk.getOrCreateChunkNoiseSampler(chunkx -> this.createChunkNoiseSampler((Chunk)chunkx, structureAccessor, blender, noiseConfig));
        Heightmap lv2 = chunk.getHeightmap(Heightmap.Type.OCEAN_FLOOR_WG);
        Heightmap lv3 = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE_WG);
        ChunkPos lv4 = chunk.getPos();
        int k = lv4.getStartX();
        int l = lv4.getStartZ();
        AquiferSampler lv5 = lv.getAquiferSampler();
        lv.sampleStartDensity();
        BlockPos.Mutable lv6 = new BlockPos.Mutable();
        int m = lv.getHorizontalCellBlockCount();
        int n = lv.getVerticalCellBlockCount();
        int o = 16 / m;
        int p = 16 / m;
        for (int q = 0; q < o; ++q) {
            lv.sampleEndDensity(q);
            for (int r = 0; r < p; ++r) {
                int s = chunk.countVerticalSections() - 1;
                ChunkSection lv7 = chunk.getSection(s);
                for (int t = cellHeight - 1; t >= 0; --t) {
                    lv.onSampledCellCorners(t, r);
                    for (int u = n - 1; u >= 0; --u) {
                        int v = (minimumCellY + t) * n + u;
                        int w = v & 0xF;
                        int x = chunk.getSectionIndex(v);
                        if (s != x) {
                            s = x;
                            lv7 = chunk.getSection(x);
                        }
                        double d = (double)u / (double)n;
                        lv.interpolateY(v, d);
                        for (int y = 0; y < m; ++y) {
                            int z = k + q * m + y;
                            int aa = z & 0xF;
                            double e = (double)y / (double)m;
                            lv.interpolateX(z, e);
                            for (int ab = 0; ab < m; ++ab) {
                                int ac = l + r * m + ab;
                                int ad = ac & 0xF;
                                double f = (double)ab / (double)m;
                                lv.interpolateZ(ac, f);
                                BlockState lv8 = lv.sampleBlockState();
                                if (lv8 == null) {
                                    lv8 = this.settings.value().defaultBlock();
                                }
                                if ((lv8 = this.getBlockState(lv, z, v, ac, lv8)) == AIR || SharedConstants.isOutsideGenerationArea(chunk.getPos())) continue;
                                lv7.setBlockState(aa, w, ad, lv8, false);
                                lv2.trackUpdate(aa, v, ad, lv8);
                                lv3.trackUpdate(aa, v, ad, lv8);
                                if (!lv5.needsFluidTick() || lv8.getFluidState().isEmpty()) continue;
                                lv6.set(z, v, ac);
                                chunk.markBlockForPostProcessing(lv6);
                            }
                        }
                    }
                }
            }
            lv.swapBuffers();
        }
        lv.stopInterpolation();
        return chunk;
    }

    private BlockState getBlockState(ChunkNoiseSampler chunkNoiseSampler, int x, int y, int z, BlockState state) {
        return state;
    }

    @Override
    public int getWorldHeight() {
        return this.settings.value().generationShapeConfig().height();
    }

    @Override
    public int getSeaLevel() {
        return this.settings.value().seaLevel();
    }

    @Override
    public int getMinimumY() {
        return this.settings.value().generationShapeConfig().minimumY();
    }

    @Override
    public void populateEntities(ChunkRegion region) {
        if (this.settings.value().mobGenerationDisabled()) {
            return;
        }
        ChunkPos lv = region.getCenterPos();
        RegistryEntry<Biome> lv2 = region.getBiome(lv.getStartPos().withY(region.getTopY() - 1));
        ChunkRandom lv3 = new ChunkRandom(new CheckedRandom(RandomSeed.getSeed()));
        lv3.setPopulationSeed(region.getSeed(), lv.getStartX(), lv.getStartZ());
        SpawnHelper.populateEntities(region, lv2, lv, lv3);
    }
}

