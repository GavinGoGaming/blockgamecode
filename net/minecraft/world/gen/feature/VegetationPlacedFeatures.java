/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world.gen.feature;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.ClampedIntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.PlacedFeatures;
import net.minecraft.world.gen.feature.TreeConfiguredFeatures;
import net.minecraft.world.gen.feature.VegetationConfiguredFeatures;
import net.minecraft.world.gen.placementmodifier.BiomePlacementModifier;
import net.minecraft.world.gen.placementmodifier.BlockFilterPlacementModifier;
import net.minecraft.world.gen.placementmodifier.CountPlacementModifier;
import net.minecraft.world.gen.placementmodifier.HeightRangePlacementModifier;
import net.minecraft.world.gen.placementmodifier.NoiseBasedCountPlacementModifier;
import net.minecraft.world.gen.placementmodifier.NoiseThresholdCountPlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import net.minecraft.world.gen.placementmodifier.RarityFilterPlacementModifier;
import net.minecraft.world.gen.placementmodifier.SquarePlacementModifier;
import net.minecraft.world.gen.placementmodifier.SurfaceWaterDepthFilterPlacementModifier;
import org.jetbrains.annotations.Nullable;

public class VegetationPlacedFeatures {
    public static final RegistryKey<PlacedFeature> BAMBOO_LIGHT = PlacedFeatures.of("bamboo_light");
    public static final RegistryKey<PlacedFeature> BAMBOO = PlacedFeatures.of("bamboo");
    public static final RegistryKey<PlacedFeature> VINES = PlacedFeatures.of("vines");
    public static final RegistryKey<PlacedFeature> PATCH_SUNFLOWER = PlacedFeatures.of("patch_sunflower");
    public static final RegistryKey<PlacedFeature> PATCH_PUMPKIN = PlacedFeatures.of("patch_pumpkin");
    public static final RegistryKey<PlacedFeature> PATCH_GRASS_PLAIN = PlacedFeatures.of("patch_grass_plain");
    public static final RegistryKey<PlacedFeature> PATCH_GRASS_FOREST = PlacedFeatures.of("patch_grass_forest");
    public static final RegistryKey<PlacedFeature> PATCH_GRASS_BADLANDS = PlacedFeatures.of("patch_grass_badlands");
    public static final RegistryKey<PlacedFeature> PATCH_GRASS_SAVANNA = PlacedFeatures.of("patch_grass_savanna");
    public static final RegistryKey<PlacedFeature> PATCH_GRASS_NORMAL = PlacedFeatures.of("patch_grass_normal");
    public static final RegistryKey<PlacedFeature> PATCH_GRASS_TAIGA_2 = PlacedFeatures.of("patch_grass_taiga_2");
    public static final RegistryKey<PlacedFeature> PATCH_GRASS_TAIGA = PlacedFeatures.of("patch_grass_taiga");
    public static final RegistryKey<PlacedFeature> PATCH_GRASS_JUNGLE = PlacedFeatures.of("patch_grass_jungle");
    public static final RegistryKey<PlacedFeature> GRASS_BONEMEAL = PlacedFeatures.of("grass_bonemeal");
    public static final RegistryKey<PlacedFeature> PATCH_DEAD_BUSH_2 = PlacedFeatures.of("patch_dead_bush_2");
    public static final RegistryKey<PlacedFeature> PATCH_DEAD_BUSH = PlacedFeatures.of("patch_dead_bush");
    public static final RegistryKey<PlacedFeature> PATCH_DEAD_BUSH_BADLANDS = PlacedFeatures.of("patch_dead_bush_badlands");
    public static final RegistryKey<PlacedFeature> PATCH_MELON = PlacedFeatures.of("patch_melon");
    public static final RegistryKey<PlacedFeature> PATCH_MELON_SPARSE = PlacedFeatures.of("patch_melon_sparse");
    public static final RegistryKey<PlacedFeature> PATCH_BERRY_COMMON = PlacedFeatures.of("patch_berry_common");
    public static final RegistryKey<PlacedFeature> PATCH_BERRY_RARE = PlacedFeatures.of("patch_berry_rare");
    public static final RegistryKey<PlacedFeature> PATCH_WATERLILY = PlacedFeatures.of("patch_waterlily");
    public static final RegistryKey<PlacedFeature> PATCH_TALL_GRASS_2 = PlacedFeatures.of("patch_tall_grass_2");
    public static final RegistryKey<PlacedFeature> PATCH_TALL_GRASS = PlacedFeatures.of("patch_tall_grass");
    public static final RegistryKey<PlacedFeature> PATCH_LARGE_FERN = PlacedFeatures.of("patch_large_fern");
    public static final RegistryKey<PlacedFeature> PATCH_CACTUS_DESERT = PlacedFeatures.of("patch_cactus_desert");
    public static final RegistryKey<PlacedFeature> PATCH_CACTUS_DECORATED = PlacedFeatures.of("patch_cactus_decorated");
    public static final RegistryKey<PlacedFeature> PATCH_SUGAR_CANE_SWAMP = PlacedFeatures.of("patch_sugar_cane_swamp");
    public static final RegistryKey<PlacedFeature> PATCH_SUGAR_CANE_DESERT = PlacedFeatures.of("patch_sugar_cane_desert");
    public static final RegistryKey<PlacedFeature> PATCH_SUGAR_CANE_BADLANDS = PlacedFeatures.of("patch_sugar_cane_badlands");
    public static final RegistryKey<PlacedFeature> PATCH_SUGAR_CANE = PlacedFeatures.of("patch_sugar_cane");
    public static final RegistryKey<PlacedFeature> BROWN_MUSHROOM_NETHER = PlacedFeatures.of("brown_mushroom_nether");
    public static final RegistryKey<PlacedFeature> RED_MUSHROOM_NETHER = PlacedFeatures.of("red_mushroom_nether");
    public static final RegistryKey<PlacedFeature> BROWN_MUSHROOM_NORMAL = PlacedFeatures.of("brown_mushroom_normal");
    public static final RegistryKey<PlacedFeature> RED_MUSHROOM_NORMAL = PlacedFeatures.of("red_mushroom_normal");
    public static final RegistryKey<PlacedFeature> BROWN_MUSHROOM_TAIGA = PlacedFeatures.of("brown_mushroom_taiga");
    public static final RegistryKey<PlacedFeature> RED_MUSHROOM_TAIGA = PlacedFeatures.of("red_mushroom_taiga");
    public static final RegistryKey<PlacedFeature> BROWN_MUSHROOM_OLD_GROWTH = PlacedFeatures.of("brown_mushroom_old_growth");
    public static final RegistryKey<PlacedFeature> RED_MUSHROOM_OLD_GROWTH = PlacedFeatures.of("red_mushroom_old_growth");
    public static final RegistryKey<PlacedFeature> BROWN_MUSHROOM_SWAMP = PlacedFeatures.of("brown_mushroom_swamp");
    public static final RegistryKey<PlacedFeature> RED_MUSHROOM_SWAMP = PlacedFeatures.of("red_mushroom_swamp");
    public static final RegistryKey<PlacedFeature> FLOWER_WARM = PlacedFeatures.of("flower_warm");
    public static final RegistryKey<PlacedFeature> FLOWER_DEFAULT = PlacedFeatures.of("flower_default");
    public static final RegistryKey<PlacedFeature> FLOWER_FLOWER_FOREST = PlacedFeatures.of("flower_flower_forest");
    public static final RegistryKey<PlacedFeature> FLOWER_SWAMP = PlacedFeatures.of("flower_swamp");
    public static final RegistryKey<PlacedFeature> FLOWER_PLAIN = PlacedFeatures.of("flower_plains");
    public static final RegistryKey<PlacedFeature> FLOWER_MEADOW = PlacedFeatures.of("flower_meadow");
    public static final RegistryKey<PlacedFeature> FLOWER_CHERRY = PlacedFeatures.of("flower_cherry");
    public static final RegistryKey<PlacedFeature> TREES_PLAINS = PlacedFeatures.of("trees_plains");
    public static final RegistryKey<PlacedFeature> DARK_FOREST_VEGETATION = PlacedFeatures.of("dark_forest_vegetation");
    public static final RegistryKey<PlacedFeature> FLOWER_FOREST_FLOWERS = PlacedFeatures.of("flower_forest_flowers");
    public static final RegistryKey<PlacedFeature> FOREST_FLOWERS = PlacedFeatures.of("forest_flowers");
    public static final RegistryKey<PlacedFeature> TREES_FLOWER_FOREST = PlacedFeatures.of("trees_flower_forest");
    public static final RegistryKey<PlacedFeature> TREES_MEADOW = PlacedFeatures.of("trees_meadow");
    public static final RegistryKey<PlacedFeature> TREES_CHERRY = PlacedFeatures.of("trees_cherry");
    public static final RegistryKey<PlacedFeature> TREES_TAIGA = PlacedFeatures.of("trees_taiga");
    public static final RegistryKey<PlacedFeature> TREES_GROVE = PlacedFeatures.of("trees_grove");
    public static final RegistryKey<PlacedFeature> TREES_BADLANDS = PlacedFeatures.of("trees_badlands");
    public static final RegistryKey<PlacedFeature> TREES_SNOWY = PlacedFeatures.of("trees_snowy");
    public static final RegistryKey<PlacedFeature> TREES_SWAMP = PlacedFeatures.of("trees_swamp");
    public static final RegistryKey<PlacedFeature> TREES_WINDSWEPT_SAVANNA = PlacedFeatures.of("trees_windswept_savanna");
    public static final RegistryKey<PlacedFeature> TREES_SAVANNA = PlacedFeatures.of("trees_savanna");
    public static final RegistryKey<PlacedFeature> BIRCH_TALL = PlacedFeatures.of("birch_tall");
    public static final RegistryKey<PlacedFeature> TREES_BIRCH = PlacedFeatures.of("trees_birch");
    public static final RegistryKey<PlacedFeature> TREES_WINDSWEPT_FOREST = PlacedFeatures.of("trees_windswept_forest");
    public static final RegistryKey<PlacedFeature> TREES_WINDSWEPT_HILLS = PlacedFeatures.of("trees_windswept_hills");
    public static final RegistryKey<PlacedFeature> TREES_WATER = PlacedFeatures.of("trees_water");
    public static final RegistryKey<PlacedFeature> TREES_BIRCH_AND_OAK = PlacedFeatures.of("trees_birch_and_oak");
    public static final RegistryKey<PlacedFeature> TREES_SPARSE_JUNGLE = PlacedFeatures.of("trees_sparse_jungle");
    public static final RegistryKey<PlacedFeature> TREES_OLD_GROWTH_SPRUCE_TAIGA = PlacedFeatures.of("trees_old_growth_spruce_taiga");
    public static final RegistryKey<PlacedFeature> TREES_OLD_GROWTH_PINE_TAIGA = PlacedFeatures.of("trees_old_growth_pine_taiga");
    public static final RegistryKey<PlacedFeature> TREES_JUNGLE = PlacedFeatures.of("trees_jungle");
    public static final RegistryKey<PlacedFeature> BAMBOO_VEGETATION = PlacedFeatures.of("bamboo_vegetation");
    public static final RegistryKey<PlacedFeature> MUSHROOM_ISLAND_VEGETATION = PlacedFeatures.of("mushroom_island_vegetation");
    public static final RegistryKey<PlacedFeature> TREES_MANGROVE = PlacedFeatures.of("trees_mangrove");
    private static final PlacementModifier NOT_IN_SURFACE_WATER_MODIFIER = SurfaceWaterDepthFilterPlacementModifier.of(0);

    public static List<PlacementModifier> modifiers(int count) {
        return List.of(CountPlacementModifier.of(count), SquarePlacementModifier.of(), PlacedFeatures.WORLD_SURFACE_WG_HEIGHTMAP, BiomePlacementModifier.of());
    }

    private static List<PlacementModifier> mushroomModifiers(int chance, @Nullable PlacementModifier modifier) {
        ImmutableList.Builder builder = ImmutableList.builder();
        if (modifier != null) {
            builder.add(modifier);
        }
        if (chance != 0) {
            builder.add(RarityFilterPlacementModifier.of(chance));
        }
        builder.add(SquarePlacementModifier.of());
        builder.add(PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP);
        builder.add(BiomePlacementModifier.of());
        return builder.build();
    }

    private static ImmutableList.Builder<PlacementModifier> treeModifiersBuilder(PlacementModifier countModifier) {
        return ((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)ImmutableList.builder().add(countModifier)).add(SquarePlacementModifier.of())).add(NOT_IN_SURFACE_WATER_MODIFIER)).add(PlacedFeatures.OCEAN_FLOOR_HEIGHTMAP)).add(BiomePlacementModifier.of());
    }

    public static List<PlacementModifier> treeModifiers(PlacementModifier modifier) {
        return VegetationPlacedFeatures.treeModifiersBuilder(modifier).build();
    }

    public static List<PlacementModifier> treeModifiersWithWouldSurvive(PlacementModifier modifier, Block block) {
        return ((ImmutableList.Builder)VegetationPlacedFeatures.treeModifiersBuilder(modifier).add((Object)BlockFilterPlacementModifier.of(BlockPredicate.wouldSurvive(block.getDefaultState(), BlockPos.ORIGIN)))).build();
    }

    public static void bootstrap(Registerable<PlacedFeature> featureRegisterable) {
        RegistryEntryLookup<ConfiguredFeature<?, ?>> lv = featureRegisterable.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv2 = lv.getOrThrow(VegetationConfiguredFeatures.BAMBOO_NO_PODZOL);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv3 = lv.getOrThrow(VegetationConfiguredFeatures.BAMBOO_SOME_PODZOL);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv4 = lv.getOrThrow(VegetationConfiguredFeatures.VINES);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv5 = lv.getOrThrow(VegetationConfiguredFeatures.PATCH_SUNFLOWER);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv6 = lv.getOrThrow(VegetationConfiguredFeatures.PATCH_PUMPKIN);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv7 = lv.getOrThrow(VegetationConfiguredFeatures.PATCH_GRASS);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv8 = lv.getOrThrow(VegetationConfiguredFeatures.PATCH_TAIGA_GRASS);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv9 = lv.getOrThrow(VegetationConfiguredFeatures.PATCH_GRASS_JUNGLE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv10 = lv.getOrThrow(VegetationConfiguredFeatures.SINGLE_PIECE_OF_GRASS);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv11 = lv.getOrThrow(VegetationConfiguredFeatures.PATCH_DEAD_BUSH);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv12 = lv.getOrThrow(VegetationConfiguredFeatures.PATCH_MELON);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv13 = lv.getOrThrow(VegetationConfiguredFeatures.PATCH_BERRY_BUSH);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv14 = lv.getOrThrow(VegetationConfiguredFeatures.PATCH_WATERLILY);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv15 = lv.getOrThrow(VegetationConfiguredFeatures.PATCH_TALL_GRASS);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv16 = lv.getOrThrow(VegetationConfiguredFeatures.PATCH_LARGE_FERN);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv17 = lv.getOrThrow(VegetationConfiguredFeatures.PATCH_CACTUS);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv18 = lv.getOrThrow(VegetationConfiguredFeatures.PATCH_SUGAR_CANE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv19 = lv.getOrThrow(VegetationConfiguredFeatures.PATCH_BROWN_MUSHROOM);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv20 = lv.getOrThrow(VegetationConfiguredFeatures.PATCH_RED_MUSHROOM);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv21 = lv.getOrThrow(VegetationConfiguredFeatures.FLOWER_DEFAULT);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv22 = lv.getOrThrow(VegetationConfiguredFeatures.FLOWER_FLOWER_FOREST);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv23 = lv.getOrThrow(VegetationConfiguredFeatures.FLOWER_SWAMP);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv24 = lv.getOrThrow(VegetationConfiguredFeatures.FLOWER_PLAIN);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv25 = lv.getOrThrow(VegetationConfiguredFeatures.FLOWER_MEADOW);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv26 = lv.getOrThrow(VegetationConfiguredFeatures.FLOWER_CHERRY);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv27 = lv.getOrThrow(VegetationConfiguredFeatures.TREES_PLAINS);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv28 = lv.getOrThrow(VegetationConfiguredFeatures.DARK_FOREST_VEGETATION);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv29 = lv.getOrThrow(VegetationConfiguredFeatures.FOREST_FLOWERS);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv30 = lv.getOrThrow(VegetationConfiguredFeatures.TREES_FLOWER_FOREST);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv31 = lv.getOrThrow(VegetationConfiguredFeatures.MEADOW_TREES);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv32 = lv.getOrThrow(VegetationConfiguredFeatures.TREES_TAIGA);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv33 = lv.getOrThrow(VegetationConfiguredFeatures.TREES_GROVE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv34 = lv.getOrThrow(TreeConfiguredFeatures.OAK);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv35 = lv.getOrThrow(TreeConfiguredFeatures.SPRUCE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv36 = lv.getOrThrow(TreeConfiguredFeatures.CHERRY_BEES_005);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv37 = lv.getOrThrow(TreeConfiguredFeatures.SWAMP_OAK);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv38 = lv.getOrThrow(VegetationConfiguredFeatures.TREES_SAVANNA);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv39 = lv.getOrThrow(VegetationConfiguredFeatures.BIRCH_TALL);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv40 = lv.getOrThrow(TreeConfiguredFeatures.BIRCH_BEES_0002);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv41 = lv.getOrThrow(VegetationConfiguredFeatures.TREES_WINDSWEPT_HILLS);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv42 = lv.getOrThrow(VegetationConfiguredFeatures.TREES_WATER);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv43 = lv.getOrThrow(VegetationConfiguredFeatures.TREES_BIRCH_AND_OAK);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv44 = lv.getOrThrow(VegetationConfiguredFeatures.TREES_SPARSE_JUNGLE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv45 = lv.getOrThrow(VegetationConfiguredFeatures.TREES_OLD_GROWTH_SPRUCE_TAIGA);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv46 = lv.getOrThrow(VegetationConfiguredFeatures.TREES_OLD_GROWTH_PINE_TAIGA);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv47 = lv.getOrThrow(VegetationConfiguredFeatures.TREES_JUNGLE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv48 = lv.getOrThrow(VegetationConfiguredFeatures.BAMBOO_VEGETATION);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv49 = lv.getOrThrow(VegetationConfiguredFeatures.MUSHROOM_ISLAND_VEGETATION);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv50 = lv.getOrThrow(VegetationConfiguredFeatures.MANGROVE_VEGETATION);
        PlacedFeatures.register(featureRegisterable, BAMBOO_LIGHT, lv2, RarityFilterPlacementModifier.of(4), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, BAMBOO, lv3, NoiseBasedCountPlacementModifier.of(160, 80.0, 0.3), SquarePlacementModifier.of(), PlacedFeatures.WORLD_SURFACE_WG_HEIGHTMAP, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, VINES, lv4, CountPlacementModifier.of(127), SquarePlacementModifier.of(), HeightRangePlacementModifier.uniform(YOffset.fixed(64), YOffset.fixed(100)), BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, PATCH_SUNFLOWER, lv5, RarityFilterPlacementModifier.of(3), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, PATCH_PUMPKIN, lv6, RarityFilterPlacementModifier.of(300), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, PATCH_GRASS_PLAIN, lv7, NoiseThresholdCountPlacementModifier.of(-0.8, 5, 10), SquarePlacementModifier.of(), PlacedFeatures.WORLD_SURFACE_WG_HEIGHTMAP, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, PATCH_GRASS_FOREST, lv7, VegetationPlacedFeatures.modifiers(2));
        PlacedFeatures.register(featureRegisterable, PATCH_GRASS_BADLANDS, lv7, SquarePlacementModifier.of(), PlacedFeatures.WORLD_SURFACE_WG_HEIGHTMAP, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, PATCH_GRASS_SAVANNA, lv7, VegetationPlacedFeatures.modifiers(20));
        PlacedFeatures.register(featureRegisterable, PATCH_GRASS_NORMAL, lv7, VegetationPlacedFeatures.modifiers(5));
        PlacedFeatures.register(featureRegisterable, PATCH_GRASS_TAIGA_2, lv8, SquarePlacementModifier.of(), PlacedFeatures.WORLD_SURFACE_WG_HEIGHTMAP, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, PATCH_GRASS_TAIGA, lv8, VegetationPlacedFeatures.modifiers(7));
        PlacedFeatures.register(featureRegisterable, PATCH_GRASS_JUNGLE, lv9, VegetationPlacedFeatures.modifiers(25));
        PlacedFeatures.register(featureRegisterable, GRASS_BONEMEAL, lv10, PlacedFeatures.isAir());
        PlacedFeatures.register(featureRegisterable, PATCH_DEAD_BUSH_2, lv11, VegetationPlacedFeatures.modifiers(2));
        PlacedFeatures.register(featureRegisterable, PATCH_DEAD_BUSH, lv11, SquarePlacementModifier.of(), PlacedFeatures.WORLD_SURFACE_WG_HEIGHTMAP, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, PATCH_DEAD_BUSH_BADLANDS, lv11, VegetationPlacedFeatures.modifiers(20));
        PlacedFeatures.register(featureRegisterable, PATCH_MELON, lv12, RarityFilterPlacementModifier.of(6), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, PATCH_MELON_SPARSE, lv12, RarityFilterPlacementModifier.of(64), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, PATCH_BERRY_COMMON, lv13, RarityFilterPlacementModifier.of(32), SquarePlacementModifier.of(), PlacedFeatures.WORLD_SURFACE_WG_HEIGHTMAP, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, PATCH_BERRY_RARE, lv13, RarityFilterPlacementModifier.of(384), SquarePlacementModifier.of(), PlacedFeatures.WORLD_SURFACE_WG_HEIGHTMAP, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, PATCH_WATERLILY, lv14, VegetationPlacedFeatures.modifiers(4));
        PlacedFeatures.register(featureRegisterable, PATCH_TALL_GRASS_2, lv15, NoiseThresholdCountPlacementModifier.of(-0.8, 0, 7), RarityFilterPlacementModifier.of(32), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, PATCH_TALL_GRASS, lv15, RarityFilterPlacementModifier.of(5), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, PATCH_LARGE_FERN, lv16, RarityFilterPlacementModifier.of(5), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, PATCH_CACTUS_DESERT, lv17, RarityFilterPlacementModifier.of(6), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, PATCH_CACTUS_DECORATED, lv17, RarityFilterPlacementModifier.of(13), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, PATCH_SUGAR_CANE_SWAMP, lv18, RarityFilterPlacementModifier.of(3), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, PATCH_SUGAR_CANE_DESERT, lv18, SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, PATCH_SUGAR_CANE_BADLANDS, lv18, RarityFilterPlacementModifier.of(5), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, PATCH_SUGAR_CANE, lv18, RarityFilterPlacementModifier.of(6), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, BROWN_MUSHROOM_NETHER, lv19, RarityFilterPlacementModifier.of(2), SquarePlacementModifier.of(), PlacedFeatures.BOTTOM_TO_TOP_RANGE, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, RED_MUSHROOM_NETHER, lv20, RarityFilterPlacementModifier.of(2), SquarePlacementModifier.of(), PlacedFeatures.BOTTOM_TO_TOP_RANGE, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, BROWN_MUSHROOM_NORMAL, lv19, VegetationPlacedFeatures.mushroomModifiers(256, null));
        PlacedFeatures.register(featureRegisterable, RED_MUSHROOM_NORMAL, lv20, VegetationPlacedFeatures.mushroomModifiers(512, null));
        PlacedFeatures.register(featureRegisterable, BROWN_MUSHROOM_TAIGA, lv19, VegetationPlacedFeatures.mushroomModifiers(4, null));
        PlacedFeatures.register(featureRegisterable, RED_MUSHROOM_TAIGA, lv20, VegetationPlacedFeatures.mushroomModifiers(256, null));
        PlacedFeatures.register(featureRegisterable, BROWN_MUSHROOM_OLD_GROWTH, lv19, VegetationPlacedFeatures.mushroomModifiers(4, CountPlacementModifier.of(3)));
        PlacedFeatures.register(featureRegisterable, RED_MUSHROOM_OLD_GROWTH, lv20, VegetationPlacedFeatures.mushroomModifiers(171, null));
        PlacedFeatures.register(featureRegisterable, BROWN_MUSHROOM_SWAMP, lv19, VegetationPlacedFeatures.mushroomModifiers(0, CountPlacementModifier.of(2)));
        PlacedFeatures.register(featureRegisterable, RED_MUSHROOM_SWAMP, lv20, VegetationPlacedFeatures.mushroomModifiers(64, null));
        PlacedFeatures.register(featureRegisterable, FLOWER_WARM, lv21, RarityFilterPlacementModifier.of(16), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, FLOWER_DEFAULT, lv21, RarityFilterPlacementModifier.of(32), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, FLOWER_FLOWER_FOREST, lv22, CountPlacementModifier.of(3), RarityFilterPlacementModifier.of(2), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, FLOWER_SWAMP, lv23, RarityFilterPlacementModifier.of(32), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, FLOWER_PLAIN, lv24, NoiseThresholdCountPlacementModifier.of(-0.8, 15, 4), RarityFilterPlacementModifier.of(32), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, FLOWER_CHERRY, lv26, NoiseThresholdCountPlacementModifier.of(-0.8, 5, 10), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, FLOWER_MEADOW, lv25, SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of());
        SurfaceWaterDepthFilterPlacementModifier lv51 = SurfaceWaterDepthFilterPlacementModifier.of(0);
        PlacedFeatures.register(featureRegisterable, TREES_PLAINS, lv27, PlacedFeatures.createCountExtraModifier(0, 0.05f, 1), SquarePlacementModifier.of(), lv51, PlacedFeatures.OCEAN_FLOOR_HEIGHTMAP, BlockFilterPlacementModifier.of(BlockPredicate.wouldSurvive(Blocks.OAK_SAPLING.getDefaultState(), BlockPos.ORIGIN)), BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, DARK_FOREST_VEGETATION, lv28, CountPlacementModifier.of(16), SquarePlacementModifier.of(), lv51, PlacedFeatures.OCEAN_FLOOR_HEIGHTMAP, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, FLOWER_FOREST_FLOWERS, lv29, RarityFilterPlacementModifier.of(7), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, CountPlacementModifier.of(ClampedIntProvider.create(UniformIntProvider.create(-1, 3), 0, 3)), BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, FOREST_FLOWERS, lv29, RarityFilterPlacementModifier.of(7), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, CountPlacementModifier.of(ClampedIntProvider.create(UniformIntProvider.create(-3, 1), 0, 1)), BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, TREES_FLOWER_FOREST, lv30, VegetationPlacedFeatures.treeModifiers(PlacedFeatures.createCountExtraModifier(6, 0.1f, 1)));
        PlacedFeatures.register(featureRegisterable, TREES_MEADOW, lv31, VegetationPlacedFeatures.treeModifiers(RarityFilterPlacementModifier.of(100)));
        PlacedFeatures.register(featureRegisterable, TREES_CHERRY, lv36, VegetationPlacedFeatures.treeModifiersWithWouldSurvive(PlacedFeatures.createCountExtraModifier(10, 0.1f, 1), Blocks.CHERRY_SAPLING));
        PlacedFeatures.register(featureRegisterable, TREES_TAIGA, lv32, VegetationPlacedFeatures.treeModifiers(PlacedFeatures.createCountExtraModifier(10, 0.1f, 1)));
        PlacedFeatures.register(featureRegisterable, TREES_GROVE, lv33, VegetationPlacedFeatures.treeModifiers(PlacedFeatures.createCountExtraModifier(10, 0.1f, 1)));
        PlacedFeatures.register(featureRegisterable, TREES_BADLANDS, lv34, VegetationPlacedFeatures.treeModifiersWithWouldSurvive(PlacedFeatures.createCountExtraModifier(5, 0.1f, 1), Blocks.OAK_SAPLING));
        PlacedFeatures.register(featureRegisterable, TREES_SNOWY, lv35, VegetationPlacedFeatures.treeModifiersWithWouldSurvive(PlacedFeatures.createCountExtraModifier(0, 0.1f, 1), Blocks.SPRUCE_SAPLING));
        PlacedFeatures.register(featureRegisterable, TREES_SWAMP, lv37, PlacedFeatures.createCountExtraModifier(2, 0.1f, 1), SquarePlacementModifier.of(), SurfaceWaterDepthFilterPlacementModifier.of(2), PlacedFeatures.OCEAN_FLOOR_HEIGHTMAP, BiomePlacementModifier.of(), BlockFilterPlacementModifier.of(BlockPredicate.wouldSurvive(Blocks.OAK_SAPLING.getDefaultState(), BlockPos.ORIGIN)));
        PlacedFeatures.register(featureRegisterable, TREES_WINDSWEPT_SAVANNA, lv38, VegetationPlacedFeatures.treeModifiers(PlacedFeatures.createCountExtraModifier(2, 0.1f, 1)));
        PlacedFeatures.register(featureRegisterable, TREES_SAVANNA, lv38, VegetationPlacedFeatures.treeModifiers(PlacedFeatures.createCountExtraModifier(1, 0.1f, 1)));
        PlacedFeatures.register(featureRegisterable, BIRCH_TALL, lv39, VegetationPlacedFeatures.treeModifiers(PlacedFeatures.createCountExtraModifier(10, 0.1f, 1)));
        PlacedFeatures.register(featureRegisterable, TREES_BIRCH, lv40, VegetationPlacedFeatures.treeModifiersWithWouldSurvive(PlacedFeatures.createCountExtraModifier(10, 0.1f, 1), Blocks.BIRCH_SAPLING));
        PlacedFeatures.register(featureRegisterable, TREES_WINDSWEPT_FOREST, lv41, VegetationPlacedFeatures.treeModifiers(PlacedFeatures.createCountExtraModifier(3, 0.1f, 1)));
        PlacedFeatures.register(featureRegisterable, TREES_WINDSWEPT_HILLS, lv41, VegetationPlacedFeatures.treeModifiers(PlacedFeatures.createCountExtraModifier(0, 0.1f, 1)));
        PlacedFeatures.register(featureRegisterable, TREES_WATER, lv42, VegetationPlacedFeatures.treeModifiers(PlacedFeatures.createCountExtraModifier(0, 0.1f, 1)));
        PlacedFeatures.register(featureRegisterable, TREES_BIRCH_AND_OAK, lv43, VegetationPlacedFeatures.treeModifiers(PlacedFeatures.createCountExtraModifier(10, 0.1f, 1)));
        PlacedFeatures.register(featureRegisterable, TREES_SPARSE_JUNGLE, lv44, VegetationPlacedFeatures.treeModifiers(PlacedFeatures.createCountExtraModifier(2, 0.1f, 1)));
        PlacedFeatures.register(featureRegisterable, TREES_OLD_GROWTH_SPRUCE_TAIGA, lv45, VegetationPlacedFeatures.treeModifiers(PlacedFeatures.createCountExtraModifier(10, 0.1f, 1)));
        PlacedFeatures.register(featureRegisterable, TREES_OLD_GROWTH_PINE_TAIGA, lv46, VegetationPlacedFeatures.treeModifiers(PlacedFeatures.createCountExtraModifier(10, 0.1f, 1)));
        PlacedFeatures.register(featureRegisterable, TREES_JUNGLE, lv47, VegetationPlacedFeatures.treeModifiers(PlacedFeatures.createCountExtraModifier(50, 0.1f, 1)));
        PlacedFeatures.register(featureRegisterable, BAMBOO_VEGETATION, lv48, VegetationPlacedFeatures.treeModifiers(PlacedFeatures.createCountExtraModifier(30, 0.1f, 1)));
        PlacedFeatures.register(featureRegisterable, MUSHROOM_ISLAND_VEGETATION, lv49, SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, TREES_MANGROVE, lv50, CountPlacementModifier.of(25), SquarePlacementModifier.of(), SurfaceWaterDepthFilterPlacementModifier.of(5), PlacedFeatures.OCEAN_FLOOR_HEIGHTMAP, BiomePlacementModifier.of(), BlockFilterPlacementModifier.of(BlockPredicate.wouldSurvive(Blocks.MANGROVE_PROPAGULE.getDefaultState(), BlockPos.ORIGIN)));
    }
}

