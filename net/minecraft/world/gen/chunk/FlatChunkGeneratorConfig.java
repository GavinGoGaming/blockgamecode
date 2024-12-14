/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.chunk;

import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.structure.StructureSet;
import net.minecraft.structure.StructureSetKeys;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorLayer;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FillLayerFeatureConfig;
import net.minecraft.world.gen.feature.MiscPlacedFeatures;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.PlacedFeatures;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import org.slf4j.Logger;

public class FlatChunkGeneratorConfig {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Codec<FlatChunkGeneratorConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(RegistryCodecs.entryList(RegistryKeys.STRUCTURE_SET).lenientOptionalFieldOf("structure_overrides").forGetter(config -> config.structureOverrides), ((MapCodec)FlatChunkGeneratorLayer.CODEC.listOf().fieldOf("layers")).forGetter(FlatChunkGeneratorConfig::getLayers), ((MapCodec)Codec.BOOL.fieldOf("lakes")).orElse(false).forGetter(config -> config.hasLakes), ((MapCodec)Codec.BOOL.fieldOf("features")).orElse(false).forGetter(config -> config.hasFeatures), Biome.REGISTRY_CODEC.lenientOptionalFieldOf("biome").orElseGet(Optional::empty).forGetter(config -> Optional.of(config.biome)), RegistryOps.getEntryCodec(BiomeKeys.PLAINS), RegistryOps.getEntryCodec(MiscPlacedFeatures.LAKE_LAVA_UNDERGROUND), RegistryOps.getEntryCodec(MiscPlacedFeatures.LAKE_LAVA_SURFACE)).apply((Applicative<FlatChunkGeneratorConfig, ?>)instance, FlatChunkGeneratorConfig::new)).comapFlatMap(FlatChunkGeneratorConfig::checkHeight, Function.identity()).stable();
    private final Optional<RegistryEntryList<StructureSet>> structureOverrides;
    private final List<FlatChunkGeneratorLayer> layers = Lists.newArrayList();
    private final RegistryEntry<Biome> biome;
    private final List<BlockState> layerBlocks;
    private boolean hasNoTerrain;
    private boolean hasFeatures;
    private boolean hasLakes;
    private final List<RegistryEntry<PlacedFeature>> features;

    private static DataResult<FlatChunkGeneratorConfig> checkHeight(FlatChunkGeneratorConfig config) {
        int i = config.layers.stream().mapToInt(FlatChunkGeneratorLayer::getThickness).sum();
        if (i > DimensionType.MAX_HEIGHT) {
            return DataResult.error(() -> "Sum of layer heights is > " + DimensionType.MAX_HEIGHT, config);
        }
        return DataResult.success(config);
    }

    private FlatChunkGeneratorConfig(Optional<RegistryEntryList<StructureSet>> structureOverrides, List<FlatChunkGeneratorLayer> layers, boolean lakes, boolean features, Optional<RegistryEntry<Biome>> biome, RegistryEntry.Reference<Biome> fallback, RegistryEntry<PlacedFeature> undergroundLavaLakeFeature, RegistryEntry<PlacedFeature> surfaceLavaLakeFeature) {
        this(structureOverrides, FlatChunkGeneratorConfig.getBiome(biome, fallback), List.of(undergroundLavaLakeFeature, surfaceLavaLakeFeature));
        if (lakes) {
            this.enableLakes();
        }
        if (features) {
            this.enableFeatures();
        }
        this.layers.addAll(layers);
        this.updateLayerBlocks();
    }

    private static RegistryEntry<Biome> getBiome(Optional<? extends RegistryEntry<Biome>> biome, RegistryEntry<Biome> fallback) {
        if (biome.isEmpty()) {
            LOGGER.error("Unknown biome, defaulting to plains");
            return fallback;
        }
        return biome.get();
    }

    public FlatChunkGeneratorConfig(Optional<RegistryEntryList<StructureSet>> structureOverrides, RegistryEntry<Biome> biome, List<RegistryEntry<PlacedFeature>> features) {
        this.structureOverrides = structureOverrides;
        this.biome = biome;
        this.layerBlocks = Lists.newArrayList();
        this.features = features;
    }

    public FlatChunkGeneratorConfig with(List<FlatChunkGeneratorLayer> layers, Optional<RegistryEntryList<StructureSet>> structureOverrides, RegistryEntry<Biome> biome) {
        FlatChunkGeneratorConfig lv = new FlatChunkGeneratorConfig(structureOverrides, biome, this.features);
        for (FlatChunkGeneratorLayer lv2 : layers) {
            lv.layers.add(new FlatChunkGeneratorLayer(lv2.getThickness(), lv2.getBlockState().getBlock()));
            lv.updateLayerBlocks();
        }
        if (this.hasFeatures) {
            lv.enableFeatures();
        }
        if (this.hasLakes) {
            lv.enableLakes();
        }
        return lv;
    }

    public void enableFeatures() {
        this.hasFeatures = true;
    }

    public void enableLakes() {
        this.hasLakes = true;
    }

    public GenerationSettings createGenerationSettings(RegistryEntry<Biome> biomeEntry) {
        int i;
        List<Object> list;
        boolean bl;
        if (!biomeEntry.equals(this.biome)) {
            return biomeEntry.value().getGenerationSettings();
        }
        GenerationSettings lv = this.getBiome().value().getGenerationSettings();
        GenerationSettings.Builder lv2 = new GenerationSettings.Builder();
        if (this.hasLakes) {
            for (RegistryEntry<PlacedFeature> lv3 : this.features) {
                lv2.feature(GenerationStep.Feature.LAKES, lv3);
            }
        }
        boolean bl2 = bl = (!this.hasNoTerrain || biomeEntry.matchesKey(BiomeKeys.THE_VOID)) && this.hasFeatures;
        if (bl) {
            list = lv.getFeatures();
            for (i = 0; i < list.size(); ++i) {
                if (i == GenerationStep.Feature.UNDERGROUND_STRUCTURES.ordinal() || i == GenerationStep.Feature.SURFACE_STRUCTURES.ordinal() || this.hasLakes && i == GenerationStep.Feature.LAKES.ordinal()) continue;
                RegistryEntryList lv4 = (RegistryEntryList)list.get(i);
                for (RegistryEntry lv5 : lv4) {
                    lv2.addFeature(i, lv5);
                }
            }
        }
        list = this.getLayerBlocks();
        for (i = 0; i < list.size(); ++i) {
            BlockState lv6 = (BlockState)list.get(i);
            if (Heightmap.Type.MOTION_BLOCKING.getBlockPredicate().test(lv6)) continue;
            list.set(i, null);
            lv2.feature(GenerationStep.Feature.TOP_LAYER_MODIFICATION, PlacedFeatures.createEntry(Feature.FILL_LAYER, new FillLayerFeatureConfig(i, lv6), new PlacementModifier[0]));
        }
        return lv2.build();
    }

    public Optional<RegistryEntryList<StructureSet>> getStructureOverrides() {
        return this.structureOverrides;
    }

    public RegistryEntry<Biome> getBiome() {
        return this.biome;
    }

    public List<FlatChunkGeneratorLayer> getLayers() {
        return this.layers;
    }

    public List<BlockState> getLayerBlocks() {
        return this.layerBlocks;
    }

    public void updateLayerBlocks() {
        this.layerBlocks.clear();
        for (FlatChunkGeneratorLayer lv : this.layers) {
            for (int i = 0; i < lv.getThickness(); ++i) {
                this.layerBlocks.add(lv.getBlockState());
            }
        }
        this.hasNoTerrain = this.layerBlocks.stream().allMatch(state -> state.isOf(Blocks.AIR));
    }

    public static FlatChunkGeneratorConfig getDefaultConfig(RegistryEntryLookup<Biome> biomeLookup, RegistryEntryLookup<StructureSet> structureSetLookup, RegistryEntryLookup<PlacedFeature> featureLookup) {
        RegistryEntryList.Direct lv = RegistryEntryList.of(structureSetLookup.getOrThrow(StructureSetKeys.STRONGHOLDS), structureSetLookup.getOrThrow(StructureSetKeys.VILLAGES));
        FlatChunkGeneratorConfig lv2 = new FlatChunkGeneratorConfig(Optional.of(lv), FlatChunkGeneratorConfig.getPlains(biomeLookup), FlatChunkGeneratorConfig.getLavaLakes(featureLookup));
        lv2.getLayers().add(new FlatChunkGeneratorLayer(1, Blocks.BEDROCK));
        lv2.getLayers().add(new FlatChunkGeneratorLayer(2, Blocks.DIRT));
        lv2.getLayers().add(new FlatChunkGeneratorLayer(1, Blocks.GRASS_BLOCK));
        lv2.updateLayerBlocks();
        return lv2;
    }

    public static RegistryEntry<Biome> getPlains(RegistryEntryLookup<Biome> biomeLookup) {
        return biomeLookup.getOrThrow(BiomeKeys.PLAINS);
    }

    public static List<RegistryEntry<PlacedFeature>> getLavaLakes(RegistryEntryLookup<PlacedFeature> featureLookup) {
        return List.of(featureLookup.getOrThrow(MiscPlacedFeatures.LAKE_LAVA_UNDERGROUND), featureLookup.getOrThrow(MiscPlacedFeatures.LAKE_LAVA_SURFACE));
    }
}

