/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen;

import java.lang.runtime.SwitchBootstraps;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.StructureSet;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.biome.source.TheEndBiomeSource;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.gen.WorldPreset;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.DebugChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.feature.PlacedFeature;

public class WorldPresets {
    public static final RegistryKey<WorldPreset> DEFAULT = WorldPresets.of("normal");
    public static final RegistryKey<WorldPreset> FLAT = WorldPresets.of("flat");
    public static final RegistryKey<WorldPreset> LARGE_BIOMES = WorldPresets.of("large_biomes");
    public static final RegistryKey<WorldPreset> AMPLIFIED = WorldPresets.of("amplified");
    public static final RegistryKey<WorldPreset> SINGLE_BIOME_SURFACE = WorldPresets.of("single_biome_surface");
    public static final RegistryKey<WorldPreset> DEBUG_ALL_BLOCK_STATES = WorldPresets.of("debug_all_block_states");

    public static void bootstrap(Registerable<WorldPreset> presetRegisterable) {
        new Registrar(presetRegisterable).bootstrap();
    }

    private static RegistryKey<WorldPreset> of(String id) {
        return RegistryKey.of(RegistryKeys.WORLD_PRESET, Identifier.ofVanilla(id));
    }

    public static Optional<RegistryKey<WorldPreset>> getWorldPreset(DimensionOptionsRegistryHolder registry) {
        return registry.getOrEmpty(DimensionOptions.OVERWORLD).flatMap(overworld -> {
            ChunkGenerator chunkGenerator = overworld.chunkGenerator();
            Objects.requireNonNull(chunkGenerator);
            ChunkGenerator lv = chunkGenerator;
            int i = 0;
            return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{FlatChunkGenerator.class, DebugChunkGenerator.class, NoiseChunkGenerator.class}, (Object)lv, i)) {
                case 0 -> {
                    FlatChunkGenerator lv2 = (FlatChunkGenerator)lv;
                    yield Optional.of(FLAT);
                }
                case 1 -> {
                    DebugChunkGenerator lv3 = (DebugChunkGenerator)lv;
                    yield Optional.of(DEBUG_ALL_BLOCK_STATES);
                }
                case 2 -> {
                    NoiseChunkGenerator lv4 = (NoiseChunkGenerator)lv;
                    yield Optional.of(DEFAULT);
                }
                default -> Optional.empty();
            };
        });
    }

    public static DimensionOptionsRegistryHolder createDemoOptions(DynamicRegistryManager dynamicRegistryManager) {
        return dynamicRegistryManager.get(RegistryKeys.WORLD_PRESET).entryOf(DEFAULT).value().createDimensionsRegistryHolder();
    }

    public static DimensionOptions getDefaultOverworldOptions(DynamicRegistryManager dynamicRegistryManager) {
        return dynamicRegistryManager.get(RegistryKeys.WORLD_PRESET).entryOf(DEFAULT).value().getOverworld().orElseThrow();
    }

    static class Registrar {
        private final Registerable<WorldPreset> presetRegisterable;
        private final RegistryEntryLookup<ChunkGeneratorSettings> chunkGeneratorSettingsLookup;
        private final RegistryEntryLookup<Biome> biomeLookup;
        private final RegistryEntryLookup<PlacedFeature> featureLookup;
        private final RegistryEntryLookup<StructureSet> structureSetLookup;
        private final RegistryEntryLookup<MultiNoiseBiomeSourceParameterList> multiNoisePresetLookup;
        private final RegistryEntry<DimensionType> overworldDimensionType;
        private final DimensionOptions netherDimensionOptions;
        private final DimensionOptions endDimensionOptions;

        Registrar(Registerable<WorldPreset> presetRegisterable) {
            this.presetRegisterable = presetRegisterable;
            RegistryEntryLookup<DimensionType> lv = presetRegisterable.getRegistryLookup(RegistryKeys.DIMENSION_TYPE);
            this.chunkGeneratorSettingsLookup = presetRegisterable.getRegistryLookup(RegistryKeys.CHUNK_GENERATOR_SETTINGS);
            this.biomeLookup = presetRegisterable.getRegistryLookup(RegistryKeys.BIOME);
            this.featureLookup = presetRegisterable.getRegistryLookup(RegistryKeys.PLACED_FEATURE);
            this.structureSetLookup = presetRegisterable.getRegistryLookup(RegistryKeys.STRUCTURE_SET);
            this.multiNoisePresetLookup = presetRegisterable.getRegistryLookup(RegistryKeys.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST);
            this.overworldDimensionType = lv.getOrThrow(DimensionTypes.OVERWORLD);
            RegistryEntry.Reference<DimensionType> lv2 = lv.getOrThrow(DimensionTypes.THE_NETHER);
            RegistryEntry.Reference<ChunkGeneratorSettings> lv3 = this.chunkGeneratorSettingsLookup.getOrThrow(ChunkGeneratorSettings.NETHER);
            RegistryEntry.Reference<MultiNoiseBiomeSourceParameterList> lv4 = this.multiNoisePresetLookup.getOrThrow(MultiNoiseBiomeSourceParameterLists.NETHER);
            this.netherDimensionOptions = new DimensionOptions(lv2, new NoiseChunkGenerator((BiomeSource)MultiNoiseBiomeSource.create(lv4), lv3));
            RegistryEntry.Reference<DimensionType> lv5 = lv.getOrThrow(DimensionTypes.THE_END);
            RegistryEntry.Reference<ChunkGeneratorSettings> lv6 = this.chunkGeneratorSettingsLookup.getOrThrow(ChunkGeneratorSettings.END);
            this.endDimensionOptions = new DimensionOptions(lv5, new NoiseChunkGenerator((BiomeSource)TheEndBiomeSource.createVanilla(this.biomeLookup), lv6));
        }

        private DimensionOptions createOverworldOptions(ChunkGenerator chunkGenerator) {
            return new DimensionOptions(this.overworldDimensionType, chunkGenerator);
        }

        private DimensionOptions createOverworldOptions(BiomeSource biomeSource, RegistryEntry<ChunkGeneratorSettings> chunkGeneratorSettings) {
            return this.createOverworldOptions(new NoiseChunkGenerator(biomeSource, chunkGeneratorSettings));
        }

        private WorldPreset createPreset(DimensionOptions dimensionOptions) {
            return new WorldPreset(Map.of(DimensionOptions.OVERWORLD, dimensionOptions, DimensionOptions.NETHER, this.netherDimensionOptions, DimensionOptions.END, this.endDimensionOptions));
        }

        private void register(RegistryKey<WorldPreset> key, DimensionOptions dimensionOptions) {
            this.presetRegisterable.register(key, this.createPreset(dimensionOptions));
        }

        private void bootstrap(BiomeSource biomeSource) {
            RegistryEntry.Reference<ChunkGeneratorSettings> lv = this.chunkGeneratorSettingsLookup.getOrThrow(ChunkGeneratorSettings.OVERWORLD);
            this.register(DEFAULT, this.createOverworldOptions(biomeSource, lv));
            RegistryEntry.Reference<ChunkGeneratorSettings> lv2 = this.chunkGeneratorSettingsLookup.getOrThrow(ChunkGeneratorSettings.LARGE_BIOMES);
            this.register(LARGE_BIOMES, this.createOverworldOptions(biomeSource, lv2));
            RegistryEntry.Reference<ChunkGeneratorSettings> lv3 = this.chunkGeneratorSettingsLookup.getOrThrow(ChunkGeneratorSettings.AMPLIFIED);
            this.register(AMPLIFIED, this.createOverworldOptions(biomeSource, lv3));
        }

        public void bootstrap() {
            RegistryEntry.Reference<MultiNoiseBiomeSourceParameterList> lv = this.multiNoisePresetLookup.getOrThrow(MultiNoiseBiomeSourceParameterLists.OVERWORLD);
            this.bootstrap(MultiNoiseBiomeSource.create(lv));
            RegistryEntry.Reference<ChunkGeneratorSettings> lv2 = this.chunkGeneratorSettingsLookup.getOrThrow(ChunkGeneratorSettings.OVERWORLD);
            RegistryEntry.Reference<Biome> lv3 = this.biomeLookup.getOrThrow(BiomeKeys.PLAINS);
            this.register(SINGLE_BIOME_SURFACE, this.createOverworldOptions(new FixedBiomeSource(lv3), lv2));
            this.register(FLAT, this.createOverworldOptions(new FlatChunkGenerator(FlatChunkGeneratorConfig.getDefaultConfig(this.biomeLookup, this.structureSetLookup, this.featureLookup))));
            this.register(DEBUG_ALL_BLOCK_STATES, this.createOverworldOptions(new DebugChunkGenerator(lv3)));
        }
    }
}

