/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screen.world;

import java.util.Map;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.CustomizeBuffetLevelScreen;
import net.minecraft.client.gui.screen.world.CustomizeFlatLevelScreen;
import net.minecraft.client.world.GeneratorOptionsHolder;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.StructureSet;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.gen.WorldPreset;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.feature.PlacedFeature;

@Environment(value=EnvType.CLIENT)
public interface LevelScreenProvider {
    public static final Map<Optional<RegistryKey<WorldPreset>>, LevelScreenProvider> WORLD_PRESET_TO_SCREEN_PROVIDER = Map.of(Optional.of(WorldPresets.FLAT), (parent, generatorOptionsHolder) -> {
        ChunkGenerator lv = generatorOptionsHolder.selectedDimensions().getChunkGenerator();
        DynamicRegistryManager.Immutable lv2 = generatorOptionsHolder.getCombinedRegistryManager();
        RegistryWrapper.Impl<Biome> lv3 = lv2.getWrapperOrThrow(RegistryKeys.BIOME);
        RegistryWrapper.Impl<StructureSet> lv4 = lv2.getWrapperOrThrow(RegistryKeys.STRUCTURE_SET);
        RegistryWrapper.Impl<PlacedFeature> lv5 = lv2.getWrapperOrThrow(RegistryKeys.PLACED_FEATURE);
        return new CustomizeFlatLevelScreen(parent, config -> parent.getWorldCreator().applyModifier(LevelScreenProvider.createModifier(config)), lv instanceof FlatChunkGenerator ? ((FlatChunkGenerator)lv).getConfig() : FlatChunkGeneratorConfig.getDefaultConfig(lv3, lv4, lv5));
    }, Optional.of(WorldPresets.SINGLE_BIOME_SURFACE), (parent, generatorOptionsHolder) -> new CustomizeBuffetLevelScreen(parent, generatorOptionsHolder, biomeEntry -> parent.getWorldCreator().applyModifier(LevelScreenProvider.createModifier(biomeEntry))));

    public Screen createEditScreen(CreateWorldScreen var1, GeneratorOptionsHolder var2);

    private static GeneratorOptionsHolder.RegistryAwareModifier createModifier(FlatChunkGeneratorConfig config) {
        return (dynamicRegistryManager, dimensionsRegistryHolder) -> {
            FlatChunkGenerator lv = new FlatChunkGenerator(config);
            return dimensionsRegistryHolder.with((DynamicRegistryManager)dynamicRegistryManager, lv);
        };
    }

    private static GeneratorOptionsHolder.RegistryAwareModifier createModifier(RegistryEntry<Biome> biomeEntry) {
        return (dynamicRegistryManager, dimensionsRegistryHolder) -> {
            Registry<ChunkGeneratorSettings> lv = dynamicRegistryManager.get(RegistryKeys.CHUNK_GENERATOR_SETTINGS);
            RegistryEntry.Reference<ChunkGeneratorSettings> lv2 = lv.entryOf(ChunkGeneratorSettings.OVERWORLD);
            FixedBiomeSource lv3 = new FixedBiomeSource(biomeEntry);
            NoiseChunkGenerator lv4 = new NoiseChunkGenerator((BiomeSource)lv3, lv2);
            return dimensionsRegistryHolder.with((DynamicRegistryManager)dynamicRegistryManager, lv4);
        };
    }
}

