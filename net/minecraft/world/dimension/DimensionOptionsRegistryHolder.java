/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.dimension;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryInfo;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.biome.source.TheEndBiomeSource;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.DebugChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.level.LevelProperties;

public record DimensionOptionsRegistryHolder(Map<RegistryKey<DimensionOptions>, DimensionOptions> dimensions) {
    public static final MapCodec<DimensionOptionsRegistryHolder> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.unboundedMap(RegistryKey.createCodec(RegistryKeys.DIMENSION), DimensionOptions.CODEC).fieldOf("dimensions")).forGetter(DimensionOptionsRegistryHolder::dimensions)).apply((Applicative<DimensionOptionsRegistryHolder, ?>)instance, instance.stable(DimensionOptionsRegistryHolder::new)));
    private static final Set<RegistryKey<DimensionOptions>> VANILLA_KEYS = ImmutableSet.of(DimensionOptions.OVERWORLD, DimensionOptions.NETHER, DimensionOptions.END);
    private static final int VANILLA_KEY_COUNT = VANILLA_KEYS.size();

    public DimensionOptionsRegistryHolder {
        DimensionOptions lv = map.get(DimensionOptions.OVERWORLD);
        if (lv == null) {
            throw new IllegalStateException("Overworld settings missing");
        }
    }

    public DimensionOptionsRegistryHolder(Registry<DimensionOptions> dimensionOptionsRegistry) {
        this(dimensionOptionsRegistry.streamEntries().collect(Collectors.toMap(RegistryEntry.Reference::registryKey, RegistryEntry.Reference::value)));
    }

    public static Stream<RegistryKey<DimensionOptions>> streamAll(Stream<RegistryKey<DimensionOptions>> otherKeys) {
        return Stream.concat(VANILLA_KEYS.stream(), otherKeys.filter(key -> !VANILLA_KEYS.contains(key)));
    }

    public DimensionOptionsRegistryHolder with(DynamicRegistryManager dynamicRegistryManager, ChunkGenerator chunkGenerator) {
        Registry<DimensionType> lv = dynamicRegistryManager.get(RegistryKeys.DIMENSION_TYPE);
        Map<RegistryKey<DimensionOptions>, DimensionOptions> map = DimensionOptionsRegistryHolder.createRegistry(lv, this.dimensions, chunkGenerator);
        return new DimensionOptionsRegistryHolder(map);
    }

    public static Map<RegistryKey<DimensionOptions>, DimensionOptions> createRegistry(Registry<DimensionType> dynamicRegistry, Map<RegistryKey<DimensionOptions>, DimensionOptions> dimensionOptions, ChunkGenerator chunkGenerator) {
        DimensionOptions lv = dimensionOptions.get(DimensionOptions.OVERWORLD);
        RegistryEntry<DimensionType> lv2 = lv == null ? dynamicRegistry.entryOf(DimensionTypes.OVERWORLD) : lv.dimensionTypeEntry();
        return DimensionOptionsRegistryHolder.createRegistry(dimensionOptions, lv2, chunkGenerator);
    }

    public static Map<RegistryKey<DimensionOptions>, DimensionOptions> createRegistry(Map<RegistryKey<DimensionOptions>, DimensionOptions> dimensionOptions, RegistryEntry<DimensionType> overworld, ChunkGenerator chunkGenerator) {
        ImmutableMap.Builder<RegistryKey<DimensionOptions>, DimensionOptions> builder = ImmutableMap.builder();
        builder.putAll(dimensionOptions);
        builder.put(DimensionOptions.OVERWORLD, new DimensionOptions(overworld, chunkGenerator));
        return builder.buildKeepingLast();
    }

    public ChunkGenerator getChunkGenerator() {
        DimensionOptions lv = this.dimensions.get(DimensionOptions.OVERWORLD);
        if (lv == null) {
            throw new IllegalStateException("Overworld settings missing");
        }
        return lv.chunkGenerator();
    }

    public Optional<DimensionOptions> getOrEmpty(RegistryKey<DimensionOptions> key) {
        return Optional.ofNullable(this.dimensions.get(key));
    }

    public ImmutableSet<RegistryKey<World>> getWorldKeys() {
        return this.dimensions().keySet().stream().map(RegistryKeys::toWorldKey).collect(ImmutableSet.toImmutableSet());
    }

    public boolean isDebug() {
        return this.getChunkGenerator() instanceof DebugChunkGenerator;
    }

    private static LevelProperties.SpecialProperty getSpecialProperty(Registry<DimensionOptions> dimensionOptionsRegistry) {
        return dimensionOptionsRegistry.getOrEmpty(DimensionOptions.OVERWORLD).map(overworldEntry -> {
            ChunkGenerator lv = overworldEntry.chunkGenerator();
            if (lv instanceof DebugChunkGenerator) {
                return LevelProperties.SpecialProperty.DEBUG;
            }
            if (lv instanceof FlatChunkGenerator) {
                return LevelProperties.SpecialProperty.FLAT;
            }
            return LevelProperties.SpecialProperty.NONE;
        }).orElse(LevelProperties.SpecialProperty.NONE);
    }

    static Lifecycle getLifecycle(RegistryKey<DimensionOptions> key, DimensionOptions dimensionOptions) {
        return DimensionOptionsRegistryHolder.isVanilla(key, dimensionOptions) ? Lifecycle.stable() : Lifecycle.experimental();
    }

    private static boolean isVanilla(RegistryKey<DimensionOptions> key, DimensionOptions dimensionOptions) {
        if (key == DimensionOptions.OVERWORLD) {
            return DimensionOptionsRegistryHolder.isOverworldVanilla(dimensionOptions);
        }
        if (key == DimensionOptions.NETHER) {
            return DimensionOptionsRegistryHolder.isNetherVanilla(dimensionOptions);
        }
        if (key == DimensionOptions.END) {
            return DimensionOptionsRegistryHolder.isTheEndVanilla(dimensionOptions);
        }
        return false;
    }

    private static boolean isOverworldVanilla(DimensionOptions dimensionOptions) {
        MultiNoiseBiomeSource lv2;
        RegistryEntry<DimensionType> lv = dimensionOptions.dimensionTypeEntry();
        if (!lv.matchesKey(DimensionTypes.OVERWORLD) && !lv.matchesKey(DimensionTypes.OVERWORLD_CAVES)) {
            return false;
        }
        BiomeSource biomeSource = dimensionOptions.chunkGenerator().getBiomeSource();
        return !(biomeSource instanceof MultiNoiseBiomeSource) || (lv2 = (MultiNoiseBiomeSource)biomeSource).matchesInstance(MultiNoiseBiomeSourceParameterLists.OVERWORLD);
    }

    private static boolean isNetherVanilla(DimensionOptions dimensionOptions) {
        MultiNoiseBiomeSource lv2;
        NoiseChunkGenerator lv;
        Object object;
        return dimensionOptions.dimensionTypeEntry().matchesKey(DimensionTypes.THE_NETHER) && (object = dimensionOptions.chunkGenerator()) instanceof NoiseChunkGenerator && (lv = (NoiseChunkGenerator)object).matchesSettings(ChunkGeneratorSettings.NETHER) && (object = lv.getBiomeSource()) instanceof MultiNoiseBiomeSource && (lv2 = (MultiNoiseBiomeSource)object).matchesInstance(MultiNoiseBiomeSourceParameterLists.NETHER);
    }

    private static boolean isTheEndVanilla(DimensionOptions dimensionOptions) {
        NoiseChunkGenerator lv;
        ChunkGenerator chunkGenerator;
        return dimensionOptions.dimensionTypeEntry().matchesKey(DimensionTypes.THE_END) && (chunkGenerator = dimensionOptions.chunkGenerator()) instanceof NoiseChunkGenerator && (lv = (NoiseChunkGenerator)chunkGenerator).matchesSettings(ChunkGeneratorSettings.END) && lv.getBiomeSource() instanceof TheEndBiomeSource;
    }

    public DimensionsConfig toConfig(Registry<DimensionOptions> existingRegistry) {
        record Entry(RegistryKey<DimensionOptions> key, DimensionOptions value) {
            RegistryEntryInfo toEntryInfo() {
                return new RegistryEntryInfo(Optional.empty(), DimensionOptionsRegistryHolder.getLifecycle(this.key, this.value));
            }
        }
        Stream<RegistryKey<DimensionOptions>> stream = Stream.concat(existingRegistry.getKeys().stream(), this.dimensions.keySet().stream()).distinct();
        ArrayList list = new ArrayList();
        DimensionOptionsRegistryHolder.streamAll(stream).forEach(key -> existingRegistry.getOrEmpty((RegistryKey<DimensionOptions>)key).or(() -> Optional.ofNullable(this.dimensions.get(key))).ifPresent(dimensionOptions -> list.add(new Entry((RegistryKey<DimensionOptions>)key, (DimensionOptions)dimensionOptions))));
        Lifecycle lifecycle = list.size() == VANILLA_KEY_COUNT ? Lifecycle.stable() : Lifecycle.experimental();
        SimpleRegistry<DimensionOptions> lv = new SimpleRegistry<DimensionOptions>(RegistryKeys.DIMENSION, lifecycle);
        list.forEach(entry -> lv.add(entry.key, entry.value, entry.toEntryInfo()));
        Registry<DimensionOptions> lv2 = lv.freeze();
        LevelProperties.SpecialProperty lv3 = DimensionOptionsRegistryHolder.getSpecialProperty(lv2);
        return new DimensionsConfig(lv2.freeze(), lv3);
    }

    public record DimensionsConfig(Registry<DimensionOptions> dimensions, LevelProperties.SpecialProperty specialWorldProperty) {
        public Lifecycle getLifecycle() {
            return this.dimensions.getLifecycle();
        }

        public DynamicRegistryManager.Immutable toDynamicRegistryManager() {
            return new DynamicRegistryManager.ImmutableImpl(List.of(this.dimensions)).toImmutable();
        }
    }
}

