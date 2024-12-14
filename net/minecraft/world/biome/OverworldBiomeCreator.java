/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world.biome;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.sound.BiomeMoodSound;
import net.minecraft.sound.MusicSound;
import net.minecraft.sound.MusicType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEffects;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.carver.ConfiguredCarvers;
import net.minecraft.world.gen.feature.DefaultBiomeFeatures;
import net.minecraft.world.gen.feature.MiscPlacedFeatures;
import net.minecraft.world.gen.feature.OceanPlacedFeatures;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.VegetationPlacedFeatures;
import org.jetbrains.annotations.Nullable;

public class OverworldBiomeCreator {
    protected static final int DEFAULT_WATER_COLOR = 4159204;
    protected static final int DEFAULT_WATER_FOG_COLOR = 329011;
    private static final int DEFAULT_FOG_COLOR = 12638463;
    @Nullable
    private static final MusicSound DEFAULT_MUSIC = null;
    public static final int SWAMP_SKELETON_WEIGHT = 70;

    protected static int getSkyColor(float temperature) {
        float g = temperature;
        g /= 3.0f;
        g = MathHelper.clamp(g, -1.0f, 1.0f);
        return MathHelper.hsvToRgb(0.62222224f - g * 0.05f, 0.5f + g * 0.1f, 1.0f);
    }

    private static Biome createBiome(boolean precipitation, float temperature, float downfall, SpawnSettings.Builder spawnSettings, GenerationSettings.LookupBackedBuilder generationSettings, @Nullable MusicSound music) {
        return OverworldBiomeCreator.createBiome(precipitation, temperature, downfall, 4159204, 329011, null, null, spawnSettings, generationSettings, music);
    }

    private static Biome createBiome(boolean precipitation, float temperature, float downfall, int waterColor, int waterFogColor, @Nullable Integer grassColor, @Nullable Integer foliageColor, SpawnSettings.Builder spawnSettings, GenerationSettings.LookupBackedBuilder generationSettings, @Nullable MusicSound music) {
        BiomeEffects.Builder lv = new BiomeEffects.Builder().waterColor(waterColor).waterFogColor(waterFogColor).fogColor(12638463).skyColor(OverworldBiomeCreator.getSkyColor(temperature)).moodSound(BiomeMoodSound.CAVE).music(music);
        if (grassColor != null) {
            lv.grassColor(grassColor);
        }
        if (foliageColor != null) {
            lv.foliageColor(foliageColor);
        }
        return new Biome.Builder().precipitation(precipitation).temperature(temperature).downfall(downfall).effects(lv.build()).spawnSettings(spawnSettings.build()).generationSettings(generationSettings.build()).build();
    }

    private static void addBasicFeatures(GenerationSettings.LookupBackedBuilder generationSettings) {
        DefaultBiomeFeatures.addLandCarvers(generationSettings);
        DefaultBiomeFeatures.addAmethystGeodes(generationSettings);
        DefaultBiomeFeatures.addDungeons(generationSettings);
        DefaultBiomeFeatures.addMineables(generationSettings);
        DefaultBiomeFeatures.addSprings(generationSettings);
        DefaultBiomeFeatures.addFrozenTopLayer(generationSettings);
    }

    public static Biome createOldGrowthTaiga(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup, boolean spruce) {
        SpawnSettings.Builder lv = new SpawnSettings.Builder();
        DefaultBiomeFeatures.addFarmAnimals(lv);
        lv.spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.WOLF, 8, 4, 4));
        lv.spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.RABBIT, 4, 2, 3));
        lv.spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.FOX, 8, 2, 4));
        if (spruce) {
            DefaultBiomeFeatures.addBatsAndMonsters(lv);
        } else {
            DefaultBiomeFeatures.addCaveMobs(lv);
            DefaultBiomeFeatures.addMonsters(lv, 100, 25, 100, false);
        }
        GenerationSettings.LookupBackedBuilder lv2 = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        OverworldBiomeCreator.addBasicFeatures(lv2);
        DefaultBiomeFeatures.addMossyRocks(lv2);
        DefaultBiomeFeatures.addLargeFerns(lv2);
        DefaultBiomeFeatures.addDefaultOres(lv2);
        DefaultBiomeFeatures.addDefaultDisks(lv2);
        lv2.feature(GenerationStep.Feature.VEGETAL_DECORATION, spruce ? VegetationPlacedFeatures.TREES_OLD_GROWTH_SPRUCE_TAIGA : VegetationPlacedFeatures.TREES_OLD_GROWTH_PINE_TAIGA);
        DefaultBiomeFeatures.addDefaultFlowers(lv2);
        DefaultBiomeFeatures.addGiantTaigaGrass(lv2);
        DefaultBiomeFeatures.addDefaultMushrooms(lv2);
        DefaultBiomeFeatures.addDefaultVegetation(lv2);
        DefaultBiomeFeatures.addSweetBerryBushes(lv2);
        MusicSound lv3 = MusicType.createIngameMusic(SoundEvents.MUSIC_OVERWORLD_OLD_GROWTH_TAIGA);
        return OverworldBiomeCreator.createBiome(true, spruce ? 0.25f : 0.3f, 0.8f, lv, lv2, lv3);
    }

    public static Biome createSparseJungle(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        SpawnSettings.Builder lv = new SpawnSettings.Builder();
        DefaultBiomeFeatures.addJungleMobs(lv);
        lv.spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.WOLF, 8, 2, 4));
        return OverworldBiomeCreator.createJungleFeatures(featureLookup, carverLookup, 0.8f, false, true, false, lv, MusicType.createIngameMusic(SoundEvents.MUSIC_OVERWORLD_SPARSE_JUNGLE));
    }

    public static Biome createJungle(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        SpawnSettings.Builder lv = new SpawnSettings.Builder();
        DefaultBiomeFeatures.addJungleMobs(lv);
        lv.spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.PARROT, 40, 1, 2)).spawn(SpawnGroup.MONSTER, new SpawnSettings.SpawnEntry(EntityType.OCELOT, 2, 1, 3)).spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.PANDA, 1, 1, 2));
        return OverworldBiomeCreator.createJungleFeatures(featureLookup, carverLookup, 0.9f, false, false, true, lv, MusicType.createIngameMusic(SoundEvents.MUSIC_OVERWORLD_JUNGLE));
    }

    public static Biome createNormalBambooJungle(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        SpawnSettings.Builder lv = new SpawnSettings.Builder();
        DefaultBiomeFeatures.addJungleMobs(lv);
        lv.spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.PARROT, 40, 1, 2)).spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.PANDA, 80, 1, 2)).spawn(SpawnGroup.MONSTER, new SpawnSettings.SpawnEntry(EntityType.OCELOT, 2, 1, 1));
        return OverworldBiomeCreator.createJungleFeatures(featureLookup, carverLookup, 0.9f, true, false, true, lv, MusicType.createIngameMusic(SoundEvents.MUSIC_OVERWORLD_BAMBOO_JUNGLE));
    }

    private static Biome createJungleFeatures(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup, float depth, boolean bamboo, boolean sparse, boolean unmodified, SpawnSettings.Builder spawnSettings, MusicSound music) {
        GenerationSettings.LookupBackedBuilder lv = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        OverworldBiomeCreator.addBasicFeatures(lv);
        DefaultBiomeFeatures.addDefaultOres(lv);
        DefaultBiomeFeatures.addDefaultDisks(lv);
        if (bamboo) {
            DefaultBiomeFeatures.addBambooJungleTrees(lv);
        } else {
            if (unmodified) {
                DefaultBiomeFeatures.addBamboo(lv);
            }
            if (sparse) {
                DefaultBiomeFeatures.addSparseJungleTrees(lv);
            } else {
                DefaultBiomeFeatures.addJungleTrees(lv);
            }
        }
        DefaultBiomeFeatures.addExtraDefaultFlowers(lv);
        DefaultBiomeFeatures.addJungleGrass(lv);
        DefaultBiomeFeatures.addDefaultMushrooms(lv);
        DefaultBiomeFeatures.addDefaultVegetation(lv);
        DefaultBiomeFeatures.addVines(lv);
        if (sparse) {
            DefaultBiomeFeatures.addSparseMelons(lv);
        } else {
            DefaultBiomeFeatures.addMelons(lv);
        }
        return OverworldBiomeCreator.createBiome(true, 0.95f, depth, spawnSettings, lv, music);
    }

    public static Biome createWindsweptHills(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup, boolean forest) {
        SpawnSettings.Builder lv = new SpawnSettings.Builder();
        DefaultBiomeFeatures.addFarmAnimals(lv);
        lv.spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.LLAMA, 5, 4, 6));
        DefaultBiomeFeatures.addBatsAndMonsters(lv);
        GenerationSettings.LookupBackedBuilder lv2 = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        OverworldBiomeCreator.addBasicFeatures(lv2);
        DefaultBiomeFeatures.addDefaultOres(lv2);
        DefaultBiomeFeatures.addDefaultDisks(lv2);
        if (forest) {
            DefaultBiomeFeatures.addWindsweptForestTrees(lv2);
        } else {
            DefaultBiomeFeatures.addWindsweptHillsTrees(lv2);
        }
        DefaultBiomeFeatures.addDefaultFlowers(lv2);
        DefaultBiomeFeatures.addDefaultGrass(lv2);
        DefaultBiomeFeatures.addDefaultMushrooms(lv2);
        DefaultBiomeFeatures.addDefaultVegetation(lv2);
        DefaultBiomeFeatures.addEmeraldOre(lv2);
        DefaultBiomeFeatures.addInfestedStone(lv2);
        return OverworldBiomeCreator.createBiome(true, 0.2f, 0.3f, lv, lv2, DEFAULT_MUSIC);
    }

    public static Biome createDesert(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        SpawnSettings.Builder lv = new SpawnSettings.Builder();
        DefaultBiomeFeatures.addDesertMobs(lv);
        GenerationSettings.LookupBackedBuilder lv2 = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        DefaultBiomeFeatures.addFossils(lv2);
        OverworldBiomeCreator.addBasicFeatures(lv2);
        DefaultBiomeFeatures.addDefaultOres(lv2);
        DefaultBiomeFeatures.addDefaultDisks(lv2);
        DefaultBiomeFeatures.addDefaultFlowers(lv2);
        DefaultBiomeFeatures.addDefaultGrass(lv2);
        DefaultBiomeFeatures.addDesertDeadBushes(lv2);
        DefaultBiomeFeatures.addDefaultMushrooms(lv2);
        DefaultBiomeFeatures.addDesertVegetation(lv2);
        DefaultBiomeFeatures.addDesertFeatures(lv2);
        return OverworldBiomeCreator.createBiome(false, 2.0f, 0.0f, lv, lv2, MusicType.createIngameMusic(SoundEvents.MUSIC_OVERWORLD_DESERT));
    }

    public static Biome createPlains(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup, boolean sunflower, boolean snowy, boolean iceSpikes) {
        SpawnSettings.Builder lv = new SpawnSettings.Builder();
        GenerationSettings.LookupBackedBuilder lv2 = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        OverworldBiomeCreator.addBasicFeatures(lv2);
        if (snowy) {
            lv.creatureSpawnProbability(0.07f);
            DefaultBiomeFeatures.addSnowyMobs(lv);
            if (iceSpikes) {
                lv2.feature(GenerationStep.Feature.SURFACE_STRUCTURES, MiscPlacedFeatures.ICE_SPIKE);
                lv2.feature(GenerationStep.Feature.SURFACE_STRUCTURES, MiscPlacedFeatures.ICE_PATCH);
            }
        } else {
            DefaultBiomeFeatures.addPlainsMobs(lv);
            DefaultBiomeFeatures.addPlainsTallGrass(lv2);
            if (sunflower) {
                lv2.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_SUNFLOWER);
            }
        }
        DefaultBiomeFeatures.addDefaultOres(lv2);
        DefaultBiomeFeatures.addDefaultDisks(lv2);
        if (snowy) {
            DefaultBiomeFeatures.addSnowySpruceTrees(lv2);
            DefaultBiomeFeatures.addDefaultFlowers(lv2);
            DefaultBiomeFeatures.addDefaultGrass(lv2);
        } else {
            DefaultBiomeFeatures.addPlainsFeatures(lv2);
        }
        DefaultBiomeFeatures.addDefaultMushrooms(lv2);
        if (sunflower) {
            lv2.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_SUGAR_CANE);
            lv2.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_PUMPKIN);
        } else {
            DefaultBiomeFeatures.addDefaultVegetation(lv2);
        }
        float f = snowy ? 0.0f : 0.8f;
        return OverworldBiomeCreator.createBiome(true, f, snowy ? 0.5f : 0.4f, lv, lv2, DEFAULT_MUSIC);
    }

    public static Biome createMushroomFields(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        SpawnSettings.Builder lv = new SpawnSettings.Builder();
        DefaultBiomeFeatures.addMushroomMobs(lv);
        GenerationSettings.LookupBackedBuilder lv2 = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        OverworldBiomeCreator.addBasicFeatures(lv2);
        DefaultBiomeFeatures.addDefaultOres(lv2);
        DefaultBiomeFeatures.addDefaultDisks(lv2);
        DefaultBiomeFeatures.addMushroomFieldsFeatures(lv2);
        DefaultBiomeFeatures.addDefaultVegetation(lv2);
        return OverworldBiomeCreator.createBiome(true, 0.9f, 1.0f, lv, lv2, DEFAULT_MUSIC);
    }

    public static Biome createSavanna(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup, boolean windswept, boolean plateau) {
        GenerationSettings.LookupBackedBuilder lv = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        OverworldBiomeCreator.addBasicFeatures(lv);
        if (!windswept) {
            DefaultBiomeFeatures.addSavannaTallGrass(lv);
        }
        DefaultBiomeFeatures.addDefaultOres(lv);
        DefaultBiomeFeatures.addDefaultDisks(lv);
        if (windswept) {
            DefaultBiomeFeatures.addExtraSavannaTrees(lv);
            DefaultBiomeFeatures.addDefaultFlowers(lv);
            DefaultBiomeFeatures.addWindsweptSavannaGrass(lv);
        } else {
            DefaultBiomeFeatures.addSavannaTrees(lv);
            DefaultBiomeFeatures.addExtraDefaultFlowers(lv);
            DefaultBiomeFeatures.addSavannaGrass(lv);
        }
        DefaultBiomeFeatures.addDefaultMushrooms(lv);
        DefaultBiomeFeatures.addDefaultVegetation(lv);
        SpawnSettings.Builder lv2 = new SpawnSettings.Builder();
        DefaultBiomeFeatures.addFarmAnimals(lv2);
        lv2.spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.HORSE, 1, 2, 6)).spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.DONKEY, 1, 1, 1)).spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.ARMADILLO, 10, 2, 3));
        DefaultBiomeFeatures.addBatsAndMonsters(lv2);
        if (plateau) {
            lv2.spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.LLAMA, 8, 4, 4));
            lv2.spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.WOLF, 8, 4, 8));
        }
        return OverworldBiomeCreator.createBiome(false, 2.0f, 0.0f, lv2, lv, DEFAULT_MUSIC);
    }

    public static Biome createBadlands(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup, boolean plateau) {
        SpawnSettings.Builder lv = new SpawnSettings.Builder();
        DefaultBiomeFeatures.addBatsAndMonsters(lv);
        lv.spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.ARMADILLO, 6, 1, 2));
        lv.creatureSpawnProbability(0.03f);
        if (plateau) {
            lv.spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.WOLF, 2, 4, 8));
            lv.creatureSpawnProbability(0.04f);
        }
        GenerationSettings.LookupBackedBuilder lv2 = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        OverworldBiomeCreator.addBasicFeatures(lv2);
        DefaultBiomeFeatures.addDefaultOres(lv2);
        DefaultBiomeFeatures.addExtraGoldOre(lv2);
        DefaultBiomeFeatures.addDefaultDisks(lv2);
        if (plateau) {
            DefaultBiomeFeatures.addBadlandsPlateauTrees(lv2);
        }
        DefaultBiomeFeatures.addBadlandsGrass(lv2);
        DefaultBiomeFeatures.addDefaultMushrooms(lv2);
        DefaultBiomeFeatures.addBadlandsVegetation(lv2);
        return new Biome.Builder().precipitation(false).temperature(2.0f).downfall(0.0f).effects(new BiomeEffects.Builder().waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(OverworldBiomeCreator.getSkyColor(2.0f)).foliageColor(10387789).grassColor(9470285).moodSound(BiomeMoodSound.CAVE).music(MusicType.createIngameMusic(SoundEvents.MUSIC_OVERWORLD_BADLANDS)).build()).spawnSettings(lv.build()).generationSettings(lv2.build()).build();
    }

    private static Biome createOcean(SpawnSettings.Builder spawnSettings, int waterColor, int waterFogColor, GenerationSettings.LookupBackedBuilder generationSettings) {
        return OverworldBiomeCreator.createBiome(true, 0.5f, 0.5f, waterColor, waterFogColor, null, null, spawnSettings, generationSettings, DEFAULT_MUSIC);
    }

    private static GenerationSettings.LookupBackedBuilder createOceanGenerationSettings(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        GenerationSettings.LookupBackedBuilder lv = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        OverworldBiomeCreator.addBasicFeatures(lv);
        DefaultBiomeFeatures.addDefaultOres(lv);
        DefaultBiomeFeatures.addDefaultDisks(lv);
        DefaultBiomeFeatures.addWaterBiomeOakTrees(lv);
        DefaultBiomeFeatures.addDefaultFlowers(lv);
        DefaultBiomeFeatures.addDefaultGrass(lv);
        DefaultBiomeFeatures.addDefaultMushrooms(lv);
        DefaultBiomeFeatures.addDefaultVegetation(lv);
        return lv;
    }

    public static Biome createColdOcean(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup, boolean deep) {
        SpawnSettings.Builder lv = new SpawnSettings.Builder();
        DefaultBiomeFeatures.addOceanMobs(lv, 3, 4, 15);
        lv.spawn(SpawnGroup.WATER_AMBIENT, new SpawnSettings.SpawnEntry(EntityType.SALMON, 15, 1, 5));
        GenerationSettings.LookupBackedBuilder lv2 = OverworldBiomeCreator.createOceanGenerationSettings(featureLookup, carverLookup);
        lv2.feature(GenerationStep.Feature.VEGETAL_DECORATION, deep ? OceanPlacedFeatures.SEAGRASS_DEEP_COLD : OceanPlacedFeatures.SEAGRASS_COLD);
        DefaultBiomeFeatures.addSeagrassOnStone(lv2);
        DefaultBiomeFeatures.addKelp(lv2);
        return OverworldBiomeCreator.createOcean(lv, 4020182, 329011, lv2);
    }

    public static Biome createNormalOcean(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup, boolean deep) {
        SpawnSettings.Builder lv = new SpawnSettings.Builder();
        DefaultBiomeFeatures.addOceanMobs(lv, 1, 4, 10);
        lv.spawn(SpawnGroup.WATER_CREATURE, new SpawnSettings.SpawnEntry(EntityType.DOLPHIN, 1, 1, 2));
        GenerationSettings.LookupBackedBuilder lv2 = OverworldBiomeCreator.createOceanGenerationSettings(featureLookup, carverLookup);
        lv2.feature(GenerationStep.Feature.VEGETAL_DECORATION, deep ? OceanPlacedFeatures.SEAGRASS_DEEP : OceanPlacedFeatures.SEAGRASS_NORMAL);
        DefaultBiomeFeatures.addSeagrassOnStone(lv2);
        DefaultBiomeFeatures.addKelp(lv2);
        return OverworldBiomeCreator.createOcean(lv, 4159204, 329011, lv2);
    }

    public static Biome createLukewarmOcean(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup, boolean deep) {
        SpawnSettings.Builder lv = new SpawnSettings.Builder();
        if (deep) {
            DefaultBiomeFeatures.addOceanMobs(lv, 8, 4, 8);
        } else {
            DefaultBiomeFeatures.addOceanMobs(lv, 10, 2, 15);
        }
        lv.spawn(SpawnGroup.WATER_AMBIENT, new SpawnSettings.SpawnEntry(EntityType.PUFFERFISH, 5, 1, 3)).spawn(SpawnGroup.WATER_AMBIENT, new SpawnSettings.SpawnEntry(EntityType.TROPICAL_FISH, 25, 8, 8)).spawn(SpawnGroup.WATER_CREATURE, new SpawnSettings.SpawnEntry(EntityType.DOLPHIN, 2, 1, 2));
        GenerationSettings.LookupBackedBuilder lv2 = OverworldBiomeCreator.createOceanGenerationSettings(featureLookup, carverLookup);
        lv2.feature(GenerationStep.Feature.VEGETAL_DECORATION, deep ? OceanPlacedFeatures.SEAGRASS_DEEP_WARM : OceanPlacedFeatures.SEAGRASS_WARM);
        if (deep) {
            DefaultBiomeFeatures.addSeagrassOnStone(lv2);
        }
        DefaultBiomeFeatures.addLessKelp(lv2);
        return OverworldBiomeCreator.createOcean(lv, 4566514, 267827, lv2);
    }

    public static Biome createWarmOcean(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        SpawnSettings.Builder lv = new SpawnSettings.Builder().spawn(SpawnGroup.WATER_AMBIENT, new SpawnSettings.SpawnEntry(EntityType.PUFFERFISH, 15, 1, 3));
        DefaultBiomeFeatures.addWarmOceanMobs(lv, 10, 4);
        GenerationSettings.LookupBackedBuilder lv2 = OverworldBiomeCreator.createOceanGenerationSettings(featureLookup, carverLookup).feature(GenerationStep.Feature.VEGETAL_DECORATION, OceanPlacedFeatures.WARM_OCEAN_VEGETATION).feature(GenerationStep.Feature.VEGETAL_DECORATION, OceanPlacedFeatures.SEAGRASS_WARM).feature(GenerationStep.Feature.VEGETAL_DECORATION, OceanPlacedFeatures.SEA_PICKLE);
        return OverworldBiomeCreator.createOcean(lv, 4445678, 270131, lv2);
    }

    public static Biome createFrozenOcean(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup, boolean deep) {
        SpawnSettings.Builder lv = new SpawnSettings.Builder().spawn(SpawnGroup.WATER_CREATURE, new SpawnSettings.SpawnEntry(EntityType.SQUID, 1, 1, 4)).spawn(SpawnGroup.WATER_AMBIENT, new SpawnSettings.SpawnEntry(EntityType.SALMON, 15, 1, 5)).spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.POLAR_BEAR, 1, 1, 2));
        DefaultBiomeFeatures.addBatsAndMonsters(lv);
        lv.spawn(SpawnGroup.MONSTER, new SpawnSettings.SpawnEntry(EntityType.DROWNED, 5, 1, 1));
        float f = deep ? 0.5f : 0.0f;
        GenerationSettings.LookupBackedBuilder lv2 = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        DefaultBiomeFeatures.addIcebergs(lv2);
        OverworldBiomeCreator.addBasicFeatures(lv2);
        DefaultBiomeFeatures.addBlueIce(lv2);
        DefaultBiomeFeatures.addDefaultOres(lv2);
        DefaultBiomeFeatures.addDefaultDisks(lv2);
        DefaultBiomeFeatures.addWaterBiomeOakTrees(lv2);
        DefaultBiomeFeatures.addDefaultFlowers(lv2);
        DefaultBiomeFeatures.addDefaultGrass(lv2);
        DefaultBiomeFeatures.addDefaultMushrooms(lv2);
        DefaultBiomeFeatures.addDefaultVegetation(lv2);
        return new Biome.Builder().precipitation(true).temperature(f).temperatureModifier(Biome.TemperatureModifier.FROZEN).downfall(0.5f).effects(new BiomeEffects.Builder().waterColor(3750089).waterFogColor(329011).fogColor(12638463).skyColor(OverworldBiomeCreator.getSkyColor(f)).moodSound(BiomeMoodSound.CAVE).build()).spawnSettings(lv.build()).generationSettings(lv2.build()).build();
    }

    public static Biome createNormalForest(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup, boolean birch, boolean oldGrowth, boolean flower) {
        MusicSound lv2;
        GenerationSettings.LookupBackedBuilder lv = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        OverworldBiomeCreator.addBasicFeatures(lv);
        if (flower) {
            lv2 = MusicType.createIngameMusic(SoundEvents.MUSIC_OVERWORLD_FLOWER_FOREST);
            lv.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.FLOWER_FOREST_FLOWERS);
        } else {
            lv2 = MusicType.createIngameMusic(SoundEvents.MUSIC_OVERWORLD_FOREST);
            DefaultBiomeFeatures.addForestFlowers(lv);
        }
        DefaultBiomeFeatures.addDefaultOres(lv);
        DefaultBiomeFeatures.addDefaultDisks(lv);
        if (flower) {
            lv.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.TREES_FLOWER_FOREST);
            lv.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.FLOWER_FLOWER_FOREST);
            DefaultBiomeFeatures.addDefaultGrass(lv);
        } else {
            if (birch) {
                if (oldGrowth) {
                    DefaultBiomeFeatures.addTallBirchTrees(lv);
                } else {
                    DefaultBiomeFeatures.addBirchTrees(lv);
                }
            } else {
                DefaultBiomeFeatures.addForestTrees(lv);
            }
            DefaultBiomeFeatures.addDefaultFlowers(lv);
            DefaultBiomeFeatures.addForestGrass(lv);
        }
        DefaultBiomeFeatures.addDefaultMushrooms(lv);
        DefaultBiomeFeatures.addDefaultVegetation(lv);
        SpawnSettings.Builder lv3 = new SpawnSettings.Builder();
        DefaultBiomeFeatures.addFarmAnimals(lv3);
        DefaultBiomeFeatures.addBatsAndMonsters(lv3);
        if (flower) {
            lv3.spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.RABBIT, 4, 2, 3));
        } else if (!birch) {
            lv3.spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.WOLF, 5, 4, 4));
        }
        float f = birch ? 0.6f : 0.7f;
        return OverworldBiomeCreator.createBiome(true, f, birch ? 0.6f : 0.8f, lv3, lv, lv2);
    }

    public static Biome createTaiga(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup, boolean snowy) {
        SpawnSettings.Builder lv = new SpawnSettings.Builder();
        DefaultBiomeFeatures.addFarmAnimals(lv);
        lv.spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.WOLF, 8, 4, 4)).spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.RABBIT, 4, 2, 3)).spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.FOX, 8, 2, 4));
        DefaultBiomeFeatures.addBatsAndMonsters(lv);
        float f = snowy ? -0.5f : 0.25f;
        GenerationSettings.LookupBackedBuilder lv2 = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        OverworldBiomeCreator.addBasicFeatures(lv2);
        DefaultBiomeFeatures.addLargeFerns(lv2);
        DefaultBiomeFeatures.addDefaultOres(lv2);
        DefaultBiomeFeatures.addDefaultDisks(lv2);
        DefaultBiomeFeatures.addTaigaTrees(lv2);
        DefaultBiomeFeatures.addDefaultFlowers(lv2);
        DefaultBiomeFeatures.addTaigaGrass(lv2);
        DefaultBiomeFeatures.addDefaultVegetation(lv2);
        if (snowy) {
            DefaultBiomeFeatures.addSweetBerryBushesSnowy(lv2);
        } else {
            DefaultBiomeFeatures.addSweetBerryBushes(lv2);
        }
        return OverworldBiomeCreator.createBiome(true, f, snowy ? 0.4f : 0.8f, snowy ? 4020182 : 4159204, 329011, null, null, lv, lv2, DEFAULT_MUSIC);
    }

    public static Biome createDarkForest(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        SpawnSettings.Builder lv = new SpawnSettings.Builder();
        DefaultBiomeFeatures.addFarmAnimals(lv);
        DefaultBiomeFeatures.addBatsAndMonsters(lv);
        GenerationSettings.LookupBackedBuilder lv2 = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        OverworldBiomeCreator.addBasicFeatures(lv2);
        lv2.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.DARK_FOREST_VEGETATION);
        DefaultBiomeFeatures.addForestFlowers(lv2);
        DefaultBiomeFeatures.addDefaultOres(lv2);
        DefaultBiomeFeatures.addDefaultDisks(lv2);
        DefaultBiomeFeatures.addDefaultFlowers(lv2);
        DefaultBiomeFeatures.addForestGrass(lv2);
        DefaultBiomeFeatures.addDefaultMushrooms(lv2);
        DefaultBiomeFeatures.addDefaultVegetation(lv2);
        MusicSound lv3 = MusicType.createIngameMusic(SoundEvents.MUSIC_OVERWORLD_FOREST);
        return new Biome.Builder().precipitation(true).temperature(0.7f).downfall(0.8f).effects(new BiomeEffects.Builder().waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(OverworldBiomeCreator.getSkyColor(0.7f)).grassColorModifier(BiomeEffects.GrassColorModifier.DARK_FOREST).moodSound(BiomeMoodSound.CAVE).music(lv3).build()).spawnSettings(lv.build()).generationSettings(lv2.build()).build();
    }

    public static Biome createSwamp(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        SpawnSettings.Builder lv = new SpawnSettings.Builder();
        DefaultBiomeFeatures.addFarmAnimals(lv);
        DefaultBiomeFeatures.addBatsAndMonsters(lv, 70);
        lv.spawn(SpawnGroup.MONSTER, new SpawnSettings.SpawnEntry(EntityType.SLIME, 1, 1, 1));
        lv.spawn(SpawnGroup.MONSTER, new SpawnSettings.SpawnEntry(EntityType.BOGGED, 30, 4, 4));
        lv.spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.FROG, 10, 2, 5));
        GenerationSettings.LookupBackedBuilder lv2 = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        DefaultBiomeFeatures.addFossils(lv2);
        OverworldBiomeCreator.addBasicFeatures(lv2);
        DefaultBiomeFeatures.addDefaultOres(lv2);
        DefaultBiomeFeatures.addClayDisk(lv2);
        DefaultBiomeFeatures.addSwampFeatures(lv2);
        DefaultBiomeFeatures.addDefaultMushrooms(lv2);
        DefaultBiomeFeatures.addSwampVegetation(lv2);
        lv2.feature(GenerationStep.Feature.VEGETAL_DECORATION, OceanPlacedFeatures.SEAGRASS_SWAMP);
        MusicSound lv3 = MusicType.createIngameMusic(SoundEvents.MUSIC_OVERWORLD_SWAMP);
        return new Biome.Builder().precipitation(true).temperature(0.8f).downfall(0.9f).effects(new BiomeEffects.Builder().waterColor(6388580).waterFogColor(2302743).fogColor(12638463).skyColor(OverworldBiomeCreator.getSkyColor(0.8f)).foliageColor(6975545).grassColorModifier(BiomeEffects.GrassColorModifier.SWAMP).moodSound(BiomeMoodSound.CAVE).music(lv3).build()).spawnSettings(lv.build()).generationSettings(lv2.build()).build();
    }

    public static Biome createMangroveSwamp(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        SpawnSettings.Builder lv = new SpawnSettings.Builder();
        DefaultBiomeFeatures.addBatsAndMonsters(lv, 70);
        lv.spawn(SpawnGroup.MONSTER, new SpawnSettings.SpawnEntry(EntityType.SLIME, 1, 1, 1));
        lv.spawn(SpawnGroup.MONSTER, new SpawnSettings.SpawnEntry(EntityType.BOGGED, 30, 4, 4));
        lv.spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.FROG, 10, 2, 5));
        lv.spawn(SpawnGroup.WATER_AMBIENT, new SpawnSettings.SpawnEntry(EntityType.TROPICAL_FISH, 25, 8, 8));
        GenerationSettings.LookupBackedBuilder lv2 = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        DefaultBiomeFeatures.addFossils(lv2);
        OverworldBiomeCreator.addBasicFeatures(lv2);
        DefaultBiomeFeatures.addDefaultOres(lv2);
        DefaultBiomeFeatures.addGrassAndClayDisks(lv2);
        DefaultBiomeFeatures.addMangroveSwampFeatures(lv2);
        lv2.feature(GenerationStep.Feature.VEGETAL_DECORATION, OceanPlacedFeatures.SEAGRASS_SWAMP);
        MusicSound lv3 = MusicType.createIngameMusic(SoundEvents.MUSIC_OVERWORLD_SWAMP);
        return new Biome.Builder().precipitation(true).temperature(0.8f).downfall(0.9f).effects(new BiomeEffects.Builder().waterColor(3832426).waterFogColor(5077600).fogColor(12638463).skyColor(OverworldBiomeCreator.getSkyColor(0.8f)).foliageColor(9285927).grassColorModifier(BiomeEffects.GrassColorModifier.SWAMP).moodSound(BiomeMoodSound.CAVE).music(lv3).build()).spawnSettings(lv.build()).generationSettings(lv2.build()).build();
    }

    public static Biome createRiver(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup, boolean frozen) {
        SpawnSettings.Builder lv = new SpawnSettings.Builder().spawn(SpawnGroup.WATER_CREATURE, new SpawnSettings.SpawnEntry(EntityType.SQUID, 2, 1, 4)).spawn(SpawnGroup.WATER_AMBIENT, new SpawnSettings.SpawnEntry(EntityType.SALMON, 5, 1, 5));
        DefaultBiomeFeatures.addBatsAndMonsters(lv);
        lv.spawn(SpawnGroup.MONSTER, new SpawnSettings.SpawnEntry(EntityType.DROWNED, frozen ? 1 : 100, 1, 1));
        GenerationSettings.LookupBackedBuilder lv2 = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        OverworldBiomeCreator.addBasicFeatures(lv2);
        DefaultBiomeFeatures.addDefaultOres(lv2);
        DefaultBiomeFeatures.addDefaultDisks(lv2);
        DefaultBiomeFeatures.addWaterBiomeOakTrees(lv2);
        DefaultBiomeFeatures.addDefaultFlowers(lv2);
        DefaultBiomeFeatures.addDefaultGrass(lv2);
        DefaultBiomeFeatures.addDefaultMushrooms(lv2);
        DefaultBiomeFeatures.addDefaultVegetation(lv2);
        if (!frozen) {
            lv2.feature(GenerationStep.Feature.VEGETAL_DECORATION, OceanPlacedFeatures.SEAGRASS_RIVER);
        }
        float f = frozen ? 0.0f : 0.5f;
        return OverworldBiomeCreator.createBiome(true, f, 0.5f, frozen ? 3750089 : 4159204, 329011, null, null, lv, lv2, DEFAULT_MUSIC);
    }

    public static Biome createBeach(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup, boolean snowy, boolean stony) {
        boolean bl3;
        SpawnSettings.Builder lv = new SpawnSettings.Builder();
        boolean bl = bl3 = !stony && !snowy;
        if (bl3) {
            lv.spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.TURTLE, 5, 2, 5));
        }
        DefaultBiomeFeatures.addBatsAndMonsters(lv);
        GenerationSettings.LookupBackedBuilder lv2 = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        OverworldBiomeCreator.addBasicFeatures(lv2);
        DefaultBiomeFeatures.addDefaultOres(lv2);
        DefaultBiomeFeatures.addDefaultDisks(lv2);
        DefaultBiomeFeatures.addDefaultFlowers(lv2);
        DefaultBiomeFeatures.addDefaultGrass(lv2);
        DefaultBiomeFeatures.addDefaultMushrooms(lv2);
        DefaultBiomeFeatures.addDefaultVegetation(lv2);
        float f = snowy ? 0.05f : (stony ? 0.2f : 0.8f);
        return OverworldBiomeCreator.createBiome(true, f, bl3 ? 0.4f : 0.3f, snowy ? 4020182 : 4159204, 329011, null, null, lv, lv2, DEFAULT_MUSIC);
    }

    public static Biome createTheVoid(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        GenerationSettings.LookupBackedBuilder lv = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        lv.feature(GenerationStep.Feature.TOP_LAYER_MODIFICATION, MiscPlacedFeatures.VOID_START_PLATFORM);
        return OverworldBiomeCreator.createBiome(false, 0.5f, 0.5f, new SpawnSettings.Builder(), lv, DEFAULT_MUSIC);
    }

    public static Biome createMeadow(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup, boolean cherryGrove) {
        GenerationSettings.LookupBackedBuilder lv = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        SpawnSettings.Builder lv2 = new SpawnSettings.Builder();
        lv2.spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(cherryGrove ? EntityType.PIG : EntityType.DONKEY, 1, 1, 2)).spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.RABBIT, 2, 2, 6)).spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.SHEEP, 2, 2, 4));
        DefaultBiomeFeatures.addBatsAndMonsters(lv2);
        OverworldBiomeCreator.addBasicFeatures(lv);
        DefaultBiomeFeatures.addPlainsTallGrass(lv);
        DefaultBiomeFeatures.addDefaultOres(lv);
        DefaultBiomeFeatures.addDefaultDisks(lv);
        if (cherryGrove) {
            DefaultBiomeFeatures.addCherryGroveFeatures(lv);
        } else {
            DefaultBiomeFeatures.addMeadowFlowers(lv);
        }
        DefaultBiomeFeatures.addEmeraldOre(lv);
        DefaultBiomeFeatures.addInfestedStone(lv);
        MusicSound lv3 = MusicType.createIngameMusic(cherryGrove ? SoundEvents.MUSIC_OVERWORLD_CHERRY_GROVE : SoundEvents.MUSIC_OVERWORLD_MEADOW);
        if (cherryGrove) {
            return OverworldBiomeCreator.createBiome(true, 0.5f, 0.8f, 6141935, 6141935, 11983713, 11983713, lv2, lv, lv3);
        }
        return OverworldBiomeCreator.createBiome(true, 0.5f, 0.8f, 937679, 329011, null, null, lv2, lv, lv3);
    }

    public static Biome createFrozenPeaks(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        GenerationSettings.LookupBackedBuilder lv = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        SpawnSettings.Builder lv2 = new SpawnSettings.Builder();
        lv2.spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.GOAT, 5, 1, 3));
        DefaultBiomeFeatures.addBatsAndMonsters(lv2);
        OverworldBiomeCreator.addBasicFeatures(lv);
        DefaultBiomeFeatures.addFrozenLavaSpring(lv);
        DefaultBiomeFeatures.addDefaultOres(lv);
        DefaultBiomeFeatures.addDefaultDisks(lv);
        DefaultBiomeFeatures.addEmeraldOre(lv);
        DefaultBiomeFeatures.addInfestedStone(lv);
        MusicSound lv3 = MusicType.createIngameMusic(SoundEvents.MUSIC_OVERWORLD_FROZEN_PEAKS);
        return OverworldBiomeCreator.createBiome(true, -0.7f, 0.9f, lv2, lv, lv3);
    }

    public static Biome createJaggedPeaks(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        GenerationSettings.LookupBackedBuilder lv = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        SpawnSettings.Builder lv2 = new SpawnSettings.Builder();
        lv2.spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.GOAT, 5, 1, 3));
        DefaultBiomeFeatures.addBatsAndMonsters(lv2);
        OverworldBiomeCreator.addBasicFeatures(lv);
        DefaultBiomeFeatures.addFrozenLavaSpring(lv);
        DefaultBiomeFeatures.addDefaultOres(lv);
        DefaultBiomeFeatures.addDefaultDisks(lv);
        DefaultBiomeFeatures.addEmeraldOre(lv);
        DefaultBiomeFeatures.addInfestedStone(lv);
        MusicSound lv3 = MusicType.createIngameMusic(SoundEvents.MUSIC_OVERWORLD_JAGGED_PEAKS);
        return OverworldBiomeCreator.createBiome(true, -0.7f, 0.9f, lv2, lv, lv3);
    }

    public static Biome createStonyPeaks(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        GenerationSettings.LookupBackedBuilder lv = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        SpawnSettings.Builder lv2 = new SpawnSettings.Builder();
        DefaultBiomeFeatures.addBatsAndMonsters(lv2);
        OverworldBiomeCreator.addBasicFeatures(lv);
        DefaultBiomeFeatures.addDefaultOres(lv);
        DefaultBiomeFeatures.addDefaultDisks(lv);
        DefaultBiomeFeatures.addEmeraldOre(lv);
        DefaultBiomeFeatures.addInfestedStone(lv);
        MusicSound lv3 = MusicType.createIngameMusic(SoundEvents.MUSIC_OVERWORLD_STONY_PEAKS);
        return OverworldBiomeCreator.createBiome(true, 1.0f, 0.3f, lv2, lv, lv3);
    }

    public static Biome createSnowySlopes(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        GenerationSettings.LookupBackedBuilder lv = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        SpawnSettings.Builder lv2 = new SpawnSettings.Builder();
        lv2.spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.RABBIT, 4, 2, 3)).spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.GOAT, 5, 1, 3));
        DefaultBiomeFeatures.addBatsAndMonsters(lv2);
        OverworldBiomeCreator.addBasicFeatures(lv);
        DefaultBiomeFeatures.addFrozenLavaSpring(lv);
        DefaultBiomeFeatures.addDefaultOres(lv);
        DefaultBiomeFeatures.addDefaultDisks(lv);
        DefaultBiomeFeatures.addDefaultVegetation(lv);
        DefaultBiomeFeatures.addEmeraldOre(lv);
        DefaultBiomeFeatures.addInfestedStone(lv);
        MusicSound lv3 = MusicType.createIngameMusic(SoundEvents.MUSIC_OVERWORLD_SNOWY_SLOPES);
        return OverworldBiomeCreator.createBiome(true, -0.3f, 0.9f, lv2, lv, lv3);
    }

    public static Biome createGrove(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        GenerationSettings.LookupBackedBuilder lv = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        SpawnSettings.Builder lv2 = new SpawnSettings.Builder();
        lv2.spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.WOLF, 1, 1, 1)).spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.RABBIT, 8, 2, 3)).spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.FOX, 4, 2, 4));
        DefaultBiomeFeatures.addBatsAndMonsters(lv2);
        OverworldBiomeCreator.addBasicFeatures(lv);
        DefaultBiomeFeatures.addFrozenLavaSpring(lv);
        DefaultBiomeFeatures.addDefaultOres(lv);
        DefaultBiomeFeatures.addDefaultDisks(lv);
        DefaultBiomeFeatures.addGroveTrees(lv);
        DefaultBiomeFeatures.addDefaultVegetation(lv);
        DefaultBiomeFeatures.addEmeraldOre(lv);
        DefaultBiomeFeatures.addInfestedStone(lv);
        MusicSound lv3 = MusicType.createIngameMusic(SoundEvents.MUSIC_OVERWORLD_GROVE);
        return OverworldBiomeCreator.createBiome(true, -0.2f, 0.8f, lv2, lv, lv3);
    }

    public static Biome createLushCaves(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        SpawnSettings.Builder lv = new SpawnSettings.Builder();
        lv.spawn(SpawnGroup.AXOLOTLS, new SpawnSettings.SpawnEntry(EntityType.AXOLOTL, 10, 4, 6));
        lv.spawn(SpawnGroup.WATER_AMBIENT, new SpawnSettings.SpawnEntry(EntityType.TROPICAL_FISH, 25, 8, 8));
        DefaultBiomeFeatures.addBatsAndMonsters(lv);
        GenerationSettings.LookupBackedBuilder lv2 = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        OverworldBiomeCreator.addBasicFeatures(lv2);
        DefaultBiomeFeatures.addPlainsTallGrass(lv2);
        DefaultBiomeFeatures.addDefaultOres(lv2);
        DefaultBiomeFeatures.addClayOre(lv2);
        DefaultBiomeFeatures.addDefaultDisks(lv2);
        DefaultBiomeFeatures.addLushCavesDecoration(lv2);
        MusicSound lv3 = MusicType.createIngameMusic(SoundEvents.MUSIC_OVERWORLD_LUSH_CAVES);
        return OverworldBiomeCreator.createBiome(true, 0.5f, 0.5f, lv, lv2, lv3);
    }

    public static Biome createDripstoneCaves(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        SpawnSettings.Builder lv = new SpawnSettings.Builder();
        DefaultBiomeFeatures.addDripstoneCaveMobs(lv);
        GenerationSettings.LookupBackedBuilder lv2 = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        OverworldBiomeCreator.addBasicFeatures(lv2);
        DefaultBiomeFeatures.addPlainsTallGrass(lv2);
        DefaultBiomeFeatures.addDefaultOres(lv2, true);
        DefaultBiomeFeatures.addDefaultDisks(lv2);
        DefaultBiomeFeatures.addPlainsFeatures(lv2);
        DefaultBiomeFeatures.addDefaultMushrooms(lv2);
        DefaultBiomeFeatures.addDefaultVegetation(lv2);
        DefaultBiomeFeatures.addDripstone(lv2);
        MusicSound lv3 = MusicType.createIngameMusic(SoundEvents.MUSIC_OVERWORLD_DRIPSTONE_CAVES);
        return OverworldBiomeCreator.createBiome(true, 0.8f, 0.4f, lv, lv2, lv3);
    }

    public static Biome createDeepDark(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        SpawnSettings.Builder lv = new SpawnSettings.Builder();
        GenerationSettings.LookupBackedBuilder lv2 = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        lv2.carver(GenerationStep.Carver.AIR, ConfiguredCarvers.CAVE);
        lv2.carver(GenerationStep.Carver.AIR, ConfiguredCarvers.CAVE_EXTRA_UNDERGROUND);
        lv2.carver(GenerationStep.Carver.AIR, ConfiguredCarvers.CANYON);
        DefaultBiomeFeatures.addAmethystGeodes(lv2);
        DefaultBiomeFeatures.addDungeons(lv2);
        DefaultBiomeFeatures.addMineables(lv2);
        DefaultBiomeFeatures.addFrozenTopLayer(lv2);
        DefaultBiomeFeatures.addPlainsTallGrass(lv2);
        DefaultBiomeFeatures.addDefaultOres(lv2);
        DefaultBiomeFeatures.addDefaultDisks(lv2);
        DefaultBiomeFeatures.addPlainsFeatures(lv2);
        DefaultBiomeFeatures.addDefaultMushrooms(lv2);
        DefaultBiomeFeatures.addDefaultVegetation(lv2);
        DefaultBiomeFeatures.addSculk(lv2);
        MusicSound lv3 = MusicType.createIngameMusic(SoundEvents.MUSIC_OVERWORLD_DEEP_DARK);
        return OverworldBiomeCreator.createBiome(true, 0.8f, 0.4f, lv, lv2, lv3);
    }
}

