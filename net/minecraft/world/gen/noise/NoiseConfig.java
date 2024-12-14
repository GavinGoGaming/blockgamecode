/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.noise;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.noise.InterpolatedNoiseSampler;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.RandomSplitter;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import net.minecraft.world.gen.noise.NoiseParametersKeys;
import net.minecraft.world.gen.noise.NoiseRouter;
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilder;

public final class NoiseConfig {
    final RandomSplitter randomDeriver;
    private final RegistryEntryLookup<DoublePerlinNoiseSampler.NoiseParameters> noiseParametersRegistry;
    private final NoiseRouter noiseRouter;
    private final MultiNoiseUtil.MultiNoiseSampler multiNoiseSampler;
    private final SurfaceBuilder surfaceBuilder;
    private final RandomSplitter aquiferRandomDeriver;
    private final RandomSplitter oreRandomDeriver;
    private final Map<RegistryKey<DoublePerlinNoiseSampler.NoiseParameters>, DoublePerlinNoiseSampler> noises;
    private final Map<Identifier, RandomSplitter> randomDerivers;

    public static NoiseConfig create(RegistryEntryLookup.RegistryLookup registryLookup, RegistryKey<ChunkGeneratorSettings> chunkGeneratorSettingsKey, long legacyWorldSeed) {
        return NoiseConfig.create(registryLookup.getOrThrow(RegistryKeys.CHUNK_GENERATOR_SETTINGS).getOrThrow(chunkGeneratorSettingsKey).value(), registryLookup.getOrThrow(RegistryKeys.NOISE_PARAMETERS), legacyWorldSeed);
    }

    public static NoiseConfig create(ChunkGeneratorSettings chunkGeneratorSettings, RegistryEntryLookup<DoublePerlinNoiseSampler.NoiseParameters> noiseParametersLookup, long legacyWorldSeed) {
        return new NoiseConfig(chunkGeneratorSettings, noiseParametersLookup, legacyWorldSeed);
    }

    private NoiseConfig(ChunkGeneratorSettings chunkGeneratorSettings, RegistryEntryLookup<DoublePerlinNoiseSampler.NoiseParameters> noiseParametersLookup, final long seed) {
        this.randomDeriver = chunkGeneratorSettings.getRandomProvider().create(seed).nextSplitter();
        this.noiseParametersRegistry = noiseParametersLookup;
        this.aquiferRandomDeriver = this.randomDeriver.split(Identifier.ofVanilla("aquifer")).nextSplitter();
        this.oreRandomDeriver = this.randomDeriver.split(Identifier.ofVanilla("ore")).nextSplitter();
        this.noises = new ConcurrentHashMap<RegistryKey<DoublePerlinNoiseSampler.NoiseParameters>, DoublePerlinNoiseSampler>();
        this.randomDerivers = new ConcurrentHashMap<Identifier, RandomSplitter>();
        this.surfaceBuilder = new SurfaceBuilder(this, chunkGeneratorSettings.defaultBlock(), chunkGeneratorSettings.seaLevel(), this.randomDeriver);
        final boolean bl = chunkGeneratorSettings.usesLegacyRandom();
        class LegacyNoiseDensityFunctionVisitor
        implements DensityFunction.DensityFunctionVisitor {
            private final Map<DensityFunction, DensityFunction> cache = new HashMap<DensityFunction, DensityFunction>();

            LegacyNoiseDensityFunctionVisitor() {
            }

            private Random createRandom(long seed2) {
                return new CheckedRandom(seed + seed2);
            }

            @Override
            public DensityFunction.Noise apply(DensityFunction.Noise noiseDensityFunction) {
                RegistryEntry<DoublePerlinNoiseSampler.NoiseParameters> lv = noiseDensityFunction.noiseData();
                if (bl) {
                    if (lv.matchesKey(NoiseParametersKeys.TEMPERATURE)) {
                        DoublePerlinNoiseSampler lv2 = DoublePerlinNoiseSampler.createLegacy(this.createRandom(0L), new DoublePerlinNoiseSampler.NoiseParameters(-7, 1.0, 1.0));
                        return new DensityFunction.Noise(lv, lv2);
                    }
                    if (lv.matchesKey(NoiseParametersKeys.VEGETATION)) {
                        DoublePerlinNoiseSampler lv2 = DoublePerlinNoiseSampler.createLegacy(this.createRandom(1L), new DoublePerlinNoiseSampler.NoiseParameters(-7, 1.0, 1.0));
                        return new DensityFunction.Noise(lv, lv2);
                    }
                    if (lv.matchesKey(NoiseParametersKeys.OFFSET)) {
                        DoublePerlinNoiseSampler lv2 = DoublePerlinNoiseSampler.create(NoiseConfig.this.randomDeriver.split(NoiseParametersKeys.OFFSET.getValue()), new DoublePerlinNoiseSampler.NoiseParameters(0, 0.0, new double[0]));
                        return new DensityFunction.Noise(lv, lv2);
                    }
                }
                DoublePerlinNoiseSampler lv2 = NoiseConfig.this.getOrCreateSampler(lv.getKey().orElseThrow());
                return new DensityFunction.Noise(lv, lv2);
            }

            private DensityFunction applyNotCached(DensityFunction densityFunction) {
                if (densityFunction instanceof InterpolatedNoiseSampler) {
                    InterpolatedNoiseSampler lv = (InterpolatedNoiseSampler)densityFunction;
                    Random lv2 = bl ? this.createRandom(0L) : NoiseConfig.this.randomDeriver.split(Identifier.ofVanilla("terrain"));
                    return lv.copyWithRandom(lv2);
                }
                if (densityFunction instanceof DensityFunctionTypes.EndIslands) {
                    return new DensityFunctionTypes.EndIslands(seed);
                }
                return densityFunction;
            }

            @Override
            public DensityFunction apply(DensityFunction arg) {
                return this.cache.computeIfAbsent(arg, this::applyNotCached);
            }
        }
        this.noiseRouter = chunkGeneratorSettings.noiseRouter().apply(new LegacyNoiseDensityFunctionVisitor());
        DensityFunction.DensityFunctionVisitor lv = new DensityFunction.DensityFunctionVisitor(this){
            private final Map<DensityFunction, DensityFunction> unwrapped = new HashMap<DensityFunction, DensityFunction>();

            private DensityFunction unwrap(DensityFunction densityFunction) {
                if (densityFunction instanceof DensityFunctionTypes.RegistryEntryHolder) {
                    DensityFunctionTypes.RegistryEntryHolder lv = (DensityFunctionTypes.RegistryEntryHolder)densityFunction;
                    return lv.function().value();
                }
                if (densityFunction instanceof DensityFunctionTypes.Wrapping) {
                    DensityFunctionTypes.Wrapping lv2 = (DensityFunctionTypes.Wrapping)densityFunction;
                    return lv2.wrapped();
                }
                return densityFunction;
            }

            @Override
            public DensityFunction apply(DensityFunction arg) {
                return this.unwrapped.computeIfAbsent(arg, this::unwrap);
            }
        };
        this.multiNoiseSampler = new MultiNoiseUtil.MultiNoiseSampler(this.noiseRouter.temperature().apply(lv), this.noiseRouter.vegetation().apply(lv), this.noiseRouter.continents().apply(lv), this.noiseRouter.erosion().apply(lv), this.noiseRouter.depth().apply(lv), this.noiseRouter.ridges().apply(lv), chunkGeneratorSettings.spawnTarget());
    }

    public DoublePerlinNoiseSampler getOrCreateSampler(RegistryKey<DoublePerlinNoiseSampler.NoiseParameters> noiseParametersKey) {
        return this.noises.computeIfAbsent(noiseParametersKey, key -> NoiseParametersKeys.createNoiseSampler(this.noiseParametersRegistry, this.randomDeriver, noiseParametersKey));
    }

    public RandomSplitter getOrCreateRandomDeriver(Identifier id) {
        return this.randomDerivers.computeIfAbsent(id, id2 -> this.randomDeriver.split(id).nextSplitter());
    }

    public NoiseRouter getNoiseRouter() {
        return this.noiseRouter;
    }

    public MultiNoiseUtil.MultiNoiseSampler getMultiNoiseSampler() {
        return this.multiNoiseSampler;
    }

    public SurfaceBuilder getSurfaceBuilder() {
        return this.surfaceBuilder;
    }

    public RandomSplitter getAquiferRandomDeriver() {
        return this.aquiferRandomDeriver;
    }

    public RandomSplitter getOreRandomDeriver() {
        return this.oreRandomDeriver;
    }
}

