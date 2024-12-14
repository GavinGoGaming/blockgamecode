/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.biome;

import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.OverworldBiomeCreator;
import net.minecraft.world.biome.TheEndBiomeCreator;
import net.minecraft.world.biome.TheNetherBiomeCreator;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.feature.PlacedFeature;

public abstract class BuiltinBiomes {
    public static void bootstrap(Registerable<Biome> biomeRegisterable) {
        RegistryEntryLookup<PlacedFeature> lv = biomeRegisterable.getRegistryLookup(RegistryKeys.PLACED_FEATURE);
        RegistryEntryLookup<ConfiguredCarver<?>> lv2 = biomeRegisterable.getRegistryLookup(RegistryKeys.CONFIGURED_CARVER);
        biomeRegisterable.register(BiomeKeys.THE_VOID, OverworldBiomeCreator.createTheVoid(lv, lv2));
        biomeRegisterable.register(BiomeKeys.PLAINS, OverworldBiomeCreator.createPlains(lv, lv2, false, false, false));
        biomeRegisterable.register(BiomeKeys.SUNFLOWER_PLAINS, OverworldBiomeCreator.createPlains(lv, lv2, true, false, false));
        biomeRegisterable.register(BiomeKeys.SNOWY_PLAINS, OverworldBiomeCreator.createPlains(lv, lv2, false, true, false));
        biomeRegisterable.register(BiomeKeys.ICE_SPIKES, OverworldBiomeCreator.createPlains(lv, lv2, false, true, true));
        biomeRegisterable.register(BiomeKeys.DESERT, OverworldBiomeCreator.createDesert(lv, lv2));
        biomeRegisterable.register(BiomeKeys.SWAMP, OverworldBiomeCreator.createSwamp(lv, lv2));
        biomeRegisterable.register(BiomeKeys.MANGROVE_SWAMP, OverworldBiomeCreator.createMangroveSwamp(lv, lv2));
        biomeRegisterable.register(BiomeKeys.FOREST, OverworldBiomeCreator.createNormalForest(lv, lv2, false, false, false));
        biomeRegisterable.register(BiomeKeys.FLOWER_FOREST, OverworldBiomeCreator.createNormalForest(lv, lv2, false, false, true));
        biomeRegisterable.register(BiomeKeys.BIRCH_FOREST, OverworldBiomeCreator.createNormalForest(lv, lv2, true, false, false));
        biomeRegisterable.register(BiomeKeys.DARK_FOREST, OverworldBiomeCreator.createDarkForest(lv, lv2));
        biomeRegisterable.register(BiomeKeys.OLD_GROWTH_BIRCH_FOREST, OverworldBiomeCreator.createNormalForest(lv, lv2, true, true, false));
        biomeRegisterable.register(BiomeKeys.OLD_GROWTH_PINE_TAIGA, OverworldBiomeCreator.createOldGrowthTaiga(lv, lv2, false));
        biomeRegisterable.register(BiomeKeys.OLD_GROWTH_SPRUCE_TAIGA, OverworldBiomeCreator.createOldGrowthTaiga(lv, lv2, true));
        biomeRegisterable.register(BiomeKeys.TAIGA, OverworldBiomeCreator.createTaiga(lv, lv2, false));
        biomeRegisterable.register(BiomeKeys.SNOWY_TAIGA, OverworldBiomeCreator.createTaiga(lv, lv2, true));
        biomeRegisterable.register(BiomeKeys.SAVANNA, OverworldBiomeCreator.createSavanna(lv, lv2, false, false));
        biomeRegisterable.register(BiomeKeys.SAVANNA_PLATEAU, OverworldBiomeCreator.createSavanna(lv, lv2, false, true));
        biomeRegisterable.register(BiomeKeys.WINDSWEPT_HILLS, OverworldBiomeCreator.createWindsweptHills(lv, lv2, false));
        biomeRegisterable.register(BiomeKeys.WINDSWEPT_GRAVELLY_HILLS, OverworldBiomeCreator.createWindsweptHills(lv, lv2, false));
        biomeRegisterable.register(BiomeKeys.WINDSWEPT_FOREST, OverworldBiomeCreator.createWindsweptHills(lv, lv2, true));
        biomeRegisterable.register(BiomeKeys.WINDSWEPT_SAVANNA, OverworldBiomeCreator.createSavanna(lv, lv2, true, false));
        biomeRegisterable.register(BiomeKeys.JUNGLE, OverworldBiomeCreator.createJungle(lv, lv2));
        biomeRegisterable.register(BiomeKeys.SPARSE_JUNGLE, OverworldBiomeCreator.createSparseJungle(lv, lv2));
        biomeRegisterable.register(BiomeKeys.BAMBOO_JUNGLE, OverworldBiomeCreator.createNormalBambooJungle(lv, lv2));
        biomeRegisterable.register(BiomeKeys.BADLANDS, OverworldBiomeCreator.createBadlands(lv, lv2, false));
        biomeRegisterable.register(BiomeKeys.ERODED_BADLANDS, OverworldBiomeCreator.createBadlands(lv, lv2, false));
        biomeRegisterable.register(BiomeKeys.WOODED_BADLANDS, OverworldBiomeCreator.createBadlands(lv, lv2, true));
        biomeRegisterable.register(BiomeKeys.MEADOW, OverworldBiomeCreator.createMeadow(lv, lv2, false));
        biomeRegisterable.register(BiomeKeys.CHERRY_GROVE, OverworldBiomeCreator.createMeadow(lv, lv2, true));
        biomeRegisterable.register(BiomeKeys.GROVE, OverworldBiomeCreator.createGrove(lv, lv2));
        biomeRegisterable.register(BiomeKeys.SNOWY_SLOPES, OverworldBiomeCreator.createSnowySlopes(lv, lv2));
        biomeRegisterable.register(BiomeKeys.FROZEN_PEAKS, OverworldBiomeCreator.createFrozenPeaks(lv, lv2));
        biomeRegisterable.register(BiomeKeys.JAGGED_PEAKS, OverworldBiomeCreator.createJaggedPeaks(lv, lv2));
        biomeRegisterable.register(BiomeKeys.STONY_PEAKS, OverworldBiomeCreator.createStonyPeaks(lv, lv2));
        biomeRegisterable.register(BiomeKeys.RIVER, OverworldBiomeCreator.createRiver(lv, lv2, false));
        biomeRegisterable.register(BiomeKeys.FROZEN_RIVER, OverworldBiomeCreator.createRiver(lv, lv2, true));
        biomeRegisterable.register(BiomeKeys.BEACH, OverworldBiomeCreator.createBeach(lv, lv2, false, false));
        biomeRegisterable.register(BiomeKeys.SNOWY_BEACH, OverworldBiomeCreator.createBeach(lv, lv2, true, false));
        biomeRegisterable.register(BiomeKeys.STONY_SHORE, OverworldBiomeCreator.createBeach(lv, lv2, false, true));
        biomeRegisterable.register(BiomeKeys.WARM_OCEAN, OverworldBiomeCreator.createWarmOcean(lv, lv2));
        biomeRegisterable.register(BiomeKeys.LUKEWARM_OCEAN, OverworldBiomeCreator.createLukewarmOcean(lv, lv2, false));
        biomeRegisterable.register(BiomeKeys.DEEP_LUKEWARM_OCEAN, OverworldBiomeCreator.createLukewarmOcean(lv, lv2, true));
        biomeRegisterable.register(BiomeKeys.OCEAN, OverworldBiomeCreator.createNormalOcean(lv, lv2, false));
        biomeRegisterable.register(BiomeKeys.DEEP_OCEAN, OverworldBiomeCreator.createNormalOcean(lv, lv2, true));
        biomeRegisterable.register(BiomeKeys.COLD_OCEAN, OverworldBiomeCreator.createColdOcean(lv, lv2, false));
        biomeRegisterable.register(BiomeKeys.DEEP_COLD_OCEAN, OverworldBiomeCreator.createColdOcean(lv, lv2, true));
        biomeRegisterable.register(BiomeKeys.FROZEN_OCEAN, OverworldBiomeCreator.createFrozenOcean(lv, lv2, false));
        biomeRegisterable.register(BiomeKeys.DEEP_FROZEN_OCEAN, OverworldBiomeCreator.createFrozenOcean(lv, lv2, true));
        biomeRegisterable.register(BiomeKeys.MUSHROOM_FIELDS, OverworldBiomeCreator.createMushroomFields(lv, lv2));
        biomeRegisterable.register(BiomeKeys.DRIPSTONE_CAVES, OverworldBiomeCreator.createDripstoneCaves(lv, lv2));
        biomeRegisterable.register(BiomeKeys.LUSH_CAVES, OverworldBiomeCreator.createLushCaves(lv, lv2));
        biomeRegisterable.register(BiomeKeys.DEEP_DARK, OverworldBiomeCreator.createDeepDark(lv, lv2));
        biomeRegisterable.register(BiomeKeys.NETHER_WASTES, TheNetherBiomeCreator.createNetherWastes(lv, lv2));
        biomeRegisterable.register(BiomeKeys.WARPED_FOREST, TheNetherBiomeCreator.createWarpedForest(lv, lv2));
        biomeRegisterable.register(BiomeKeys.CRIMSON_FOREST, TheNetherBiomeCreator.createCrimsonForest(lv, lv2));
        biomeRegisterable.register(BiomeKeys.SOUL_SAND_VALLEY, TheNetherBiomeCreator.createSoulSandValley(lv, lv2));
        biomeRegisterable.register(BiomeKeys.BASALT_DELTAS, TheNetherBiomeCreator.createBasaltDeltas(lv, lv2));
        biomeRegisterable.register(BiomeKeys.THE_END, TheEndBiomeCreator.createTheEnd(lv, lv2));
        biomeRegisterable.register(BiomeKeys.END_HIGHLANDS, TheEndBiomeCreator.createEndHighlands(lv, lv2));
        biomeRegisterable.register(BiomeKeys.END_MIDLANDS, TheEndBiomeCreator.createEndMidlands(lv, lv2));
        biomeRegisterable.register(BiomeKeys.SMALL_END_ISLANDS, TheEndBiomeCreator.createSmallEndIslands(lv, lv2));
        biomeRegisterable.register(BiomeKeys.END_BARRENS, TheEndBiomeCreator.createEndBarrens(lv, lv2));
    }
}

