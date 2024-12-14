/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerbedBlock;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.collection.DataPool;
import net.minecraft.util.dynamic.Range;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.intprovider.BiasedToBottomIntProvider;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.world.gen.ProbabilityConfig;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.feature.BlockColumnFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.ConfiguredFeatures;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.PlacedFeatures;
import net.minecraft.world.gen.feature.RandomBooleanFeatureConfig;
import net.minecraft.world.gen.feature.RandomFeatureConfig;
import net.minecraft.world.gen.feature.RandomFeatureEntry;
import net.minecraft.world.gen.feature.RandomPatchFeatureConfig;
import net.minecraft.world.gen.feature.SimpleBlockFeatureConfig;
import net.minecraft.world.gen.feature.SimpleRandomFeatureConfig;
import net.minecraft.world.gen.feature.TreeConfiguredFeatures;
import net.minecraft.world.gen.feature.TreePlacedFeatures;
import net.minecraft.world.gen.placementmodifier.BlockFilterPlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.stateprovider.DualNoiseBlockStateProvider;
import net.minecraft.world.gen.stateprovider.NoiseBlockStateProvider;
import net.minecraft.world.gen.stateprovider.NoiseThresholdBlockStateProvider;
import net.minecraft.world.gen.stateprovider.WeightedBlockStateProvider;

public class VegetationConfiguredFeatures {
    public static final RegistryKey<ConfiguredFeature<?, ?>> BAMBOO_NO_PODZOL = ConfiguredFeatures.of("bamboo_no_podzol");
    public static final RegistryKey<ConfiguredFeature<?, ?>> BAMBOO_SOME_PODZOL = ConfiguredFeatures.of("bamboo_some_podzol");
    public static final RegistryKey<ConfiguredFeature<?, ?>> VINES = ConfiguredFeatures.of("vines");
    public static final RegistryKey<ConfiguredFeature<?, ?>> PATCH_BROWN_MUSHROOM = ConfiguredFeatures.of("patch_brown_mushroom");
    public static final RegistryKey<ConfiguredFeature<?, ?>> PATCH_RED_MUSHROOM = ConfiguredFeatures.of("patch_red_mushroom");
    public static final RegistryKey<ConfiguredFeature<?, ?>> PATCH_SUNFLOWER = ConfiguredFeatures.of("patch_sunflower");
    public static final RegistryKey<ConfiguredFeature<?, ?>> PATCH_PUMPKIN = ConfiguredFeatures.of("patch_pumpkin");
    public static final RegistryKey<ConfiguredFeature<?, ?>> PATCH_BERRY_BUSH = ConfiguredFeatures.of("patch_berry_bush");
    public static final RegistryKey<ConfiguredFeature<?, ?>> PATCH_TAIGA_GRASS = ConfiguredFeatures.of("patch_taiga_grass");
    public static final RegistryKey<ConfiguredFeature<?, ?>> PATCH_GRASS = ConfiguredFeatures.of("patch_grass");
    public static final RegistryKey<ConfiguredFeature<?, ?>> PATCH_GRASS_JUNGLE = ConfiguredFeatures.of("patch_grass_jungle");
    public static final RegistryKey<ConfiguredFeature<?, ?>> SINGLE_PIECE_OF_GRASS = ConfiguredFeatures.of("single_piece_of_grass");
    public static final RegistryKey<ConfiguredFeature<?, ?>> PATCH_DEAD_BUSH = ConfiguredFeatures.of("patch_dead_bush");
    public static final RegistryKey<ConfiguredFeature<?, ?>> PATCH_MELON = ConfiguredFeatures.of("patch_melon");
    public static final RegistryKey<ConfiguredFeature<?, ?>> PATCH_WATERLILY = ConfiguredFeatures.of("patch_waterlily");
    public static final RegistryKey<ConfiguredFeature<?, ?>> PATCH_TALL_GRASS = ConfiguredFeatures.of("patch_tall_grass");
    public static final RegistryKey<ConfiguredFeature<?, ?>> PATCH_LARGE_FERN = ConfiguredFeatures.of("patch_large_fern");
    public static final RegistryKey<ConfiguredFeature<?, ?>> PATCH_CACTUS = ConfiguredFeatures.of("patch_cactus");
    public static final RegistryKey<ConfiguredFeature<?, ?>> PATCH_SUGAR_CANE = ConfiguredFeatures.of("patch_sugar_cane");
    public static final RegistryKey<ConfiguredFeature<?, ?>> FLOWER_DEFAULT = ConfiguredFeatures.of("flower_default");
    public static final RegistryKey<ConfiguredFeature<?, ?>> FLOWER_FLOWER_FOREST = ConfiguredFeatures.of("flower_flower_forest");
    public static final RegistryKey<ConfiguredFeature<?, ?>> FLOWER_SWAMP = ConfiguredFeatures.of("flower_swamp");
    public static final RegistryKey<ConfiguredFeature<?, ?>> FLOWER_PLAIN = ConfiguredFeatures.of("flower_plain");
    public static final RegistryKey<ConfiguredFeature<?, ?>> FLOWER_MEADOW = ConfiguredFeatures.of("flower_meadow");
    public static final RegistryKey<ConfiguredFeature<?, ?>> FLOWER_CHERRY = ConfiguredFeatures.of("flower_cherry");
    public static final RegistryKey<ConfiguredFeature<?, ?>> FOREST_FLOWERS = ConfiguredFeatures.of("forest_flowers");
    public static final RegistryKey<ConfiguredFeature<?, ?>> DARK_FOREST_VEGETATION = ConfiguredFeatures.of("dark_forest_vegetation");
    public static final RegistryKey<ConfiguredFeature<?, ?>> TREES_FLOWER_FOREST = ConfiguredFeatures.of("trees_flower_forest");
    public static final RegistryKey<ConfiguredFeature<?, ?>> MEADOW_TREES = ConfiguredFeatures.of("meadow_trees");
    public static final RegistryKey<ConfiguredFeature<?, ?>> TREES_TAIGA = ConfiguredFeatures.of("trees_taiga");
    public static final RegistryKey<ConfiguredFeature<?, ?>> TREES_GROVE = ConfiguredFeatures.of("trees_grove");
    public static final RegistryKey<ConfiguredFeature<?, ?>> TREES_SAVANNA = ConfiguredFeatures.of("trees_savanna");
    public static final RegistryKey<ConfiguredFeature<?, ?>> BIRCH_TALL = ConfiguredFeatures.of("birch_tall");
    public static final RegistryKey<ConfiguredFeature<?, ?>> TREES_WINDSWEPT_HILLS = ConfiguredFeatures.of("trees_windswept_hills");
    public static final RegistryKey<ConfiguredFeature<?, ?>> TREES_WATER = ConfiguredFeatures.of("trees_water");
    public static final RegistryKey<ConfiguredFeature<?, ?>> TREES_BIRCH_AND_OAK = ConfiguredFeatures.of("trees_birch_and_oak");
    public static final RegistryKey<ConfiguredFeature<?, ?>> TREES_PLAINS = ConfiguredFeatures.of("trees_plains");
    public static final RegistryKey<ConfiguredFeature<?, ?>> TREES_SPARSE_JUNGLE = ConfiguredFeatures.of("trees_sparse_jungle");
    public static final RegistryKey<ConfiguredFeature<?, ?>> TREES_OLD_GROWTH_SPRUCE_TAIGA = ConfiguredFeatures.of("trees_old_growth_spruce_taiga");
    public static final RegistryKey<ConfiguredFeature<?, ?>> TREES_OLD_GROWTH_PINE_TAIGA = ConfiguredFeatures.of("trees_old_growth_pine_taiga");
    public static final RegistryKey<ConfiguredFeature<?, ?>> TREES_JUNGLE = ConfiguredFeatures.of("trees_jungle");
    public static final RegistryKey<ConfiguredFeature<?, ?>> BAMBOO_VEGETATION = ConfiguredFeatures.of("bamboo_vegetation");
    public static final RegistryKey<ConfiguredFeature<?, ?>> MUSHROOM_ISLAND_VEGETATION = ConfiguredFeatures.of("mushroom_island_vegetation");
    public static final RegistryKey<ConfiguredFeature<?, ?>> MANGROVE_VEGETATION = ConfiguredFeatures.of("mangrove_vegetation");

    private static RandomPatchFeatureConfig createRandomPatchFeatureConfig(BlockStateProvider block, int tries) {
        return ConfiguredFeatures.createRandomPatchFeatureConfig(tries, PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(block)));
    }

    public static void bootstrap(Registerable<ConfiguredFeature<?, ?>> featureRegisterable) {
        RegistryEntryLookup<ConfiguredFeature<?, ?>> lv = featureRegisterable.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv2 = lv.getOrThrow(TreeConfiguredFeatures.HUGE_BROWN_MUSHROOM);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv3 = lv.getOrThrow(TreeConfiguredFeatures.HUGE_RED_MUSHROOM);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv4 = lv.getOrThrow(TreeConfiguredFeatures.FANCY_OAK_BEES_005);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv5 = lv.getOrThrow(TreeConfiguredFeatures.OAK_BEES_005);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv6 = lv.getOrThrow(PATCH_GRASS_JUNGLE);
        RegistryEntryLookup<PlacedFeature> lv7 = featureRegisterable.getRegistryLookup(RegistryKeys.PLACED_FEATURE);
        RegistryEntry.Reference<PlacedFeature> lv8 = lv7.getOrThrow(TreePlacedFeatures.DARK_OAK_CHECKED);
        RegistryEntry.Reference<PlacedFeature> lv9 = lv7.getOrThrow(TreePlacedFeatures.BIRCH_CHECKED);
        RegistryEntry.Reference<PlacedFeature> lv10 = lv7.getOrThrow(TreePlacedFeatures.FANCY_OAK_CHECKED);
        RegistryEntry.Reference<PlacedFeature> lv11 = lv7.getOrThrow(TreePlacedFeatures.BIRCH_BEES_002);
        RegistryEntry.Reference<PlacedFeature> lv12 = lv7.getOrThrow(TreePlacedFeatures.FANCY_OAK_BEES_002);
        RegistryEntry.Reference<PlacedFeature> lv13 = lv7.getOrThrow(TreePlacedFeatures.FANCY_OAK_BEES);
        RegistryEntry.Reference<PlacedFeature> lv14 = lv7.getOrThrow(TreePlacedFeatures.PINE_CHECKED);
        RegistryEntry.Reference<PlacedFeature> lv15 = lv7.getOrThrow(TreePlacedFeatures.SPRUCE_CHECKED);
        RegistryEntry.Reference<PlacedFeature> lv16 = lv7.getOrThrow(TreePlacedFeatures.PINE_ON_SNOW);
        RegistryEntry.Reference<PlacedFeature> lv17 = lv7.getOrThrow(TreePlacedFeatures.ACACIA_CHECKED);
        RegistryEntry.Reference<PlacedFeature> lv18 = lv7.getOrThrow(TreePlacedFeatures.SUPER_BIRCH_BEES_0002);
        RegistryEntry.Reference<PlacedFeature> lv19 = lv7.getOrThrow(TreePlacedFeatures.BIRCH_BEES_0002);
        RegistryEntry.Reference<PlacedFeature> lv20 = lv7.getOrThrow(TreePlacedFeatures.FANCY_OAK_BEES_0002);
        RegistryEntry.Reference<PlacedFeature> lv21 = lv7.getOrThrow(TreePlacedFeatures.JUNGLE_BUSH);
        RegistryEntry.Reference<PlacedFeature> lv22 = lv7.getOrThrow(TreePlacedFeatures.MEGA_SPRUCE_CHECKED);
        RegistryEntry.Reference<PlacedFeature> lv23 = lv7.getOrThrow(TreePlacedFeatures.MEGA_PINE_CHECKED);
        RegistryEntry.Reference<PlacedFeature> lv24 = lv7.getOrThrow(TreePlacedFeatures.MEGA_JUNGLE_TREE_CHECKED);
        RegistryEntry.Reference<PlacedFeature> lv25 = lv7.getOrThrow(TreePlacedFeatures.TALL_MANGROVE_CHECKED);
        RegistryEntry.Reference<PlacedFeature> lv26 = lv7.getOrThrow(TreePlacedFeatures.OAK_CHECKED);
        RegistryEntry.Reference<PlacedFeature> lv27 = lv7.getOrThrow(TreePlacedFeatures.OAK_BEES_002);
        RegistryEntry.Reference<PlacedFeature> lv28 = lv7.getOrThrow(TreePlacedFeatures.SUPER_BIRCH_BEES);
        RegistryEntry.Reference<PlacedFeature> lv29 = lv7.getOrThrow(TreePlacedFeatures.SPRUCE_ON_SNOW);
        RegistryEntry.Reference<PlacedFeature> lv30 = lv7.getOrThrow(TreePlacedFeatures.OAK_BEES_0002);
        RegistryEntry.Reference<PlacedFeature> lv31 = lv7.getOrThrow(TreePlacedFeatures.JUNGLE_TREE);
        RegistryEntry.Reference<PlacedFeature> lv32 = lv7.getOrThrow(TreePlacedFeatures.MANGROVE_CHECKED);
        ConfiguredFeatures.register(featureRegisterable, BAMBOO_NO_PODZOL, Feature.BAMBOO, new ProbabilityConfig(0.0f));
        ConfiguredFeatures.register(featureRegisterable, BAMBOO_SOME_PODZOL, Feature.BAMBOO, new ProbabilityConfig(0.2f));
        ConfiguredFeatures.register(featureRegisterable, VINES, Feature.VINES);
        ConfiguredFeatures.register(featureRegisterable, PATCH_BROWN_MUSHROOM, Feature.RANDOM_PATCH, ConfiguredFeatures.createRandomPatchFeatureConfig(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.BROWN_MUSHROOM))));
        ConfiguredFeatures.register(featureRegisterable, PATCH_RED_MUSHROOM, Feature.RANDOM_PATCH, ConfiguredFeatures.createRandomPatchFeatureConfig(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.RED_MUSHROOM))));
        ConfiguredFeatures.register(featureRegisterable, PATCH_SUNFLOWER, Feature.RANDOM_PATCH, ConfiguredFeatures.createRandomPatchFeatureConfig(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.SUNFLOWER))));
        ConfiguredFeatures.register(featureRegisterable, PATCH_PUMPKIN, Feature.RANDOM_PATCH, ConfiguredFeatures.createRandomPatchFeatureConfig(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.PUMPKIN)), List.of(Blocks.GRASS_BLOCK)));
        ConfiguredFeatures.register(featureRegisterable, PATCH_BERRY_BUSH, Feature.RANDOM_PATCH, ConfiguredFeatures.createRandomPatchFeatureConfig(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of((BlockState)Blocks.SWEET_BERRY_BUSH.getDefaultState().with(SweetBerryBushBlock.AGE, 3))), List.of(Blocks.GRASS_BLOCK)));
        ConfiguredFeatures.register(featureRegisterable, PATCH_TAIGA_GRASS, Feature.RANDOM_PATCH, VegetationConfiguredFeatures.createRandomPatchFeatureConfig(new WeightedBlockStateProvider(DataPool.builder().add(Blocks.SHORT_GRASS.getDefaultState(), 1).add(Blocks.FERN.getDefaultState(), 4)), 32));
        ConfiguredFeatures.register(featureRegisterable, PATCH_GRASS, Feature.RANDOM_PATCH, VegetationConfiguredFeatures.createRandomPatchFeatureConfig(BlockStateProvider.of(Blocks.SHORT_GRASS), 32));
        ConfiguredFeatures.register(featureRegisterable, PATCH_GRASS_JUNGLE, Feature.RANDOM_PATCH, new RandomPatchFeatureConfig(32, 7, 3, PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(new WeightedBlockStateProvider(DataPool.builder().add(Blocks.SHORT_GRASS.getDefaultState(), 3).add(Blocks.FERN.getDefaultState(), 1))), BlockPredicate.bothOf(BlockPredicate.IS_AIR, BlockPredicate.not(BlockPredicate.matchingBlocks(Direction.DOWN.getVector(), Blocks.PODZOL))))));
        ConfiguredFeatures.register(featureRegisterable, SINGLE_PIECE_OF_GRASS, Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.SHORT_GRASS.getDefaultState())));
        ConfiguredFeatures.register(featureRegisterable, PATCH_DEAD_BUSH, Feature.RANDOM_PATCH, VegetationConfiguredFeatures.createRandomPatchFeatureConfig(BlockStateProvider.of(Blocks.DEAD_BUSH), 4));
        ConfiguredFeatures.register(featureRegisterable, PATCH_MELON, Feature.RANDOM_PATCH, new RandomPatchFeatureConfig(64, 7, 3, PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.MELON)), BlockPredicate.allOf(BlockPredicate.replaceable(), BlockPredicate.noFluid(), BlockPredicate.matchingBlocks(Direction.DOWN.getVector(), Blocks.GRASS_BLOCK)))));
        ConfiguredFeatures.register(featureRegisterable, PATCH_WATERLILY, Feature.RANDOM_PATCH, new RandomPatchFeatureConfig(10, 7, 3, PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.LILY_PAD)))));
        ConfiguredFeatures.register(featureRegisterable, PATCH_TALL_GRASS, Feature.RANDOM_PATCH, ConfiguredFeatures.createRandomPatchFeatureConfig(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.TALL_GRASS))));
        ConfiguredFeatures.register(featureRegisterable, PATCH_LARGE_FERN, Feature.RANDOM_PATCH, ConfiguredFeatures.createRandomPatchFeatureConfig(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.LARGE_FERN))));
        ConfiguredFeatures.register(featureRegisterable, PATCH_CACTUS, Feature.RANDOM_PATCH, ConfiguredFeatures.createRandomPatchFeatureConfig(10, PlacedFeatures.createEntry(Feature.BLOCK_COLUMN, BlockColumnFeatureConfig.create(BiasedToBottomIntProvider.create(1, 3), BlockStateProvider.of(Blocks.CACTUS)), BlockFilterPlacementModifier.of(BlockPredicate.bothOf(BlockPredicate.IS_AIR, BlockPredicate.wouldSurvive(Blocks.CACTUS.getDefaultState(), BlockPos.ORIGIN))))));
        ConfiguredFeatures.register(featureRegisterable, PATCH_SUGAR_CANE, Feature.RANDOM_PATCH, new RandomPatchFeatureConfig(20, 4, 0, PlacedFeatures.createEntry(Feature.BLOCK_COLUMN, BlockColumnFeatureConfig.create(BiasedToBottomIntProvider.create(2, 4), BlockStateProvider.of(Blocks.SUGAR_CANE)), BlockFilterPlacementModifier.of(BlockPredicate.allOf(BlockPredicate.IS_AIR, BlockPredicate.wouldSurvive(Blocks.SUGAR_CANE.getDefaultState(), BlockPos.ORIGIN), BlockPredicate.anyOf(BlockPredicate.matchingFluids((Vec3i)new BlockPos(1, -1, 0), Fluids.WATER, Fluids.FLOWING_WATER), BlockPredicate.matchingFluids((Vec3i)new BlockPos(-1, -1, 0), Fluids.WATER, Fluids.FLOWING_WATER), BlockPredicate.matchingFluids((Vec3i)new BlockPos(0, -1, 1), Fluids.WATER, Fluids.FLOWING_WATER), BlockPredicate.matchingFluids((Vec3i)new BlockPos(0, -1, -1), Fluids.WATER, Fluids.FLOWING_WATER)))))));
        ConfiguredFeatures.register(featureRegisterable, FLOWER_DEFAULT, Feature.FLOWER, VegetationConfiguredFeatures.createRandomPatchFeatureConfig(new WeightedBlockStateProvider(DataPool.builder().add(Blocks.POPPY.getDefaultState(), 2).add(Blocks.DANDELION.getDefaultState(), 1)), 64));
        ConfiguredFeatures.register(featureRegisterable, FLOWER_FLOWER_FOREST, Feature.FLOWER, new RandomPatchFeatureConfig(96, 6, 2, PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(new NoiseBlockStateProvider(2345L, new DoublePerlinNoiseSampler.NoiseParameters(0, 1.0, new double[0]), 0.020833334f, List.of(Blocks.DANDELION.getDefaultState(), Blocks.POPPY.getDefaultState(), Blocks.ALLIUM.getDefaultState(), Blocks.AZURE_BLUET.getDefaultState(), Blocks.RED_TULIP.getDefaultState(), Blocks.ORANGE_TULIP.getDefaultState(), Blocks.WHITE_TULIP.getDefaultState(), Blocks.PINK_TULIP.getDefaultState(), Blocks.OXEYE_DAISY.getDefaultState(), Blocks.CORNFLOWER.getDefaultState(), Blocks.LILY_OF_THE_VALLEY.getDefaultState()))))));
        ConfiguredFeatures.register(featureRegisterable, FLOWER_SWAMP, Feature.FLOWER, new RandomPatchFeatureConfig(64, 6, 2, PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.BLUE_ORCHID)))));
        ConfiguredFeatures.register(featureRegisterable, FLOWER_PLAIN, Feature.FLOWER, new RandomPatchFeatureConfig(64, 6, 2, PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(new NoiseThresholdBlockStateProvider(2345L, new DoublePerlinNoiseSampler.NoiseParameters(0, 1.0, new double[0]), 0.005f, -0.8f, 0.33333334f, Blocks.DANDELION.getDefaultState(), List.of(Blocks.ORANGE_TULIP.getDefaultState(), Blocks.RED_TULIP.getDefaultState(), Blocks.PINK_TULIP.getDefaultState(), Blocks.WHITE_TULIP.getDefaultState()), List.of(Blocks.POPPY.getDefaultState(), Blocks.AZURE_BLUET.getDefaultState(), Blocks.OXEYE_DAISY.getDefaultState(), Blocks.CORNFLOWER.getDefaultState()))))));
        ConfiguredFeatures.register(featureRegisterable, FLOWER_MEADOW, Feature.FLOWER, new RandomPatchFeatureConfig(96, 6, 2, PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(new DualNoiseBlockStateProvider(new Range<Integer>(1, 3), new DoublePerlinNoiseSampler.NoiseParameters(-10, 1.0, new double[0]), 1.0f, 2345L, new DoublePerlinNoiseSampler.NoiseParameters(-3, 1.0, new double[0]), 1.0f, List.of(Blocks.TALL_GRASS.getDefaultState(), Blocks.ALLIUM.getDefaultState(), Blocks.POPPY.getDefaultState(), Blocks.AZURE_BLUET.getDefaultState(), Blocks.DANDELION.getDefaultState(), Blocks.CORNFLOWER.getDefaultState(), Blocks.OXEYE_DAISY.getDefaultState(), Blocks.SHORT_GRASS.getDefaultState()))))));
        DataPool.Builder<BlockState> lv33 = DataPool.builder();
        for (int i = 1; i <= 4; ++i) {
            for (Direction lv34 : Direction.Type.HORIZONTAL) {
                lv33.add((BlockState)((BlockState)Blocks.PINK_PETALS.getDefaultState().with(FlowerbedBlock.FLOWER_AMOUNT, i)).with(FlowerbedBlock.FACING, lv34), 1);
            }
        }
        ConfiguredFeatures.register(featureRegisterable, FLOWER_CHERRY, Feature.FLOWER, new RandomPatchFeatureConfig(96, 6, 2, PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(new WeightedBlockStateProvider(lv33)))));
        ConfiguredFeatures.register(featureRegisterable, FOREST_FLOWERS, Feature.SIMPLE_RANDOM_SELECTOR, new SimpleRandomFeatureConfig(RegistryEntryList.of(PlacedFeatures.createEntry(Feature.RANDOM_PATCH, ConfiguredFeatures.createRandomPatchFeatureConfig(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.LILAC))), new PlacementModifier[0]), PlacedFeatures.createEntry(Feature.RANDOM_PATCH, ConfiguredFeatures.createRandomPatchFeatureConfig(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.ROSE_BUSH))), new PlacementModifier[0]), PlacedFeatures.createEntry(Feature.RANDOM_PATCH, ConfiguredFeatures.createRandomPatchFeatureConfig(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.PEONY))), new PlacementModifier[0]), PlacedFeatures.createEntry(Feature.NO_BONEMEAL_FLOWER, ConfiguredFeatures.createRandomPatchFeatureConfig(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.LILY_OF_THE_VALLEY))), new PlacementModifier[0]))));
        ConfiguredFeatures.register(featureRegisterable, DARK_FOREST_VEGETATION, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(PlacedFeatures.createEntry(lv2, new PlacementModifier[0]), 0.025f), new RandomFeatureEntry(PlacedFeatures.createEntry(lv3, new PlacementModifier[0]), 0.05f), new RandomFeatureEntry(lv8, 0.6666667f), new RandomFeatureEntry(lv9, 0.2f), new RandomFeatureEntry(lv10, 0.1f)), lv26));
        ConfiguredFeatures.register(featureRegisterable, TREES_FLOWER_FOREST, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(lv11, 0.2f), new RandomFeatureEntry(lv12, 0.1f)), lv27));
        ConfiguredFeatures.register(featureRegisterable, MEADOW_TREES, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(lv13, 0.5f)), lv28));
        ConfiguredFeatures.register(featureRegisterable, TREES_TAIGA, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(lv14, 0.33333334f)), lv15));
        ConfiguredFeatures.register(featureRegisterable, TREES_GROVE, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(lv16, 0.33333334f)), lv29));
        ConfiguredFeatures.register(featureRegisterable, TREES_SAVANNA, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(lv17, 0.8f)), lv26));
        ConfiguredFeatures.register(featureRegisterable, BIRCH_TALL, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(lv18, 0.5f)), lv19));
        ConfiguredFeatures.register(featureRegisterable, TREES_WINDSWEPT_HILLS, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(lv15, 0.666f), new RandomFeatureEntry(lv10, 0.1f)), lv26));
        ConfiguredFeatures.register(featureRegisterable, TREES_WATER, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(lv10, 0.1f)), lv26));
        ConfiguredFeatures.register(featureRegisterable, TREES_BIRCH_AND_OAK, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(lv19, 0.2f), new RandomFeatureEntry(lv20, 0.1f)), lv30));
        ConfiguredFeatures.register(featureRegisterable, TREES_PLAINS, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(PlacedFeatures.createEntry(lv4, new PlacementModifier[0]), 0.33333334f)), PlacedFeatures.createEntry(lv5, new PlacementModifier[0])));
        ConfiguredFeatures.register(featureRegisterable, TREES_SPARSE_JUNGLE, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(lv10, 0.1f), new RandomFeatureEntry(lv21, 0.5f)), lv31));
        ConfiguredFeatures.register(featureRegisterable, TREES_OLD_GROWTH_SPRUCE_TAIGA, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(lv22, 0.33333334f), new RandomFeatureEntry(lv14, 0.33333334f)), lv15));
        ConfiguredFeatures.register(featureRegisterable, TREES_OLD_GROWTH_PINE_TAIGA, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(lv22, 0.025641026f), new RandomFeatureEntry(lv23, 0.30769232f), new RandomFeatureEntry(lv14, 0.33333334f)), lv15));
        ConfiguredFeatures.register(featureRegisterable, TREES_JUNGLE, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(lv10, 0.1f), new RandomFeatureEntry(lv21, 0.5f), new RandomFeatureEntry(lv24, 0.33333334f)), lv31));
        ConfiguredFeatures.register(featureRegisterable, BAMBOO_VEGETATION, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(lv10, 0.05f), new RandomFeatureEntry(lv21, 0.15f), new RandomFeatureEntry(lv24, 0.7f)), PlacedFeatures.createEntry(lv6, new PlacementModifier[0])));
        ConfiguredFeatures.register(featureRegisterable, MUSHROOM_ISLAND_VEGETATION, Feature.RANDOM_BOOLEAN_SELECTOR, new RandomBooleanFeatureConfig(PlacedFeatures.createEntry(lv3, new PlacementModifier[0]), PlacedFeatures.createEntry(lv2, new PlacementModifier[0])));
        ConfiguredFeatures.register(featureRegisterable, MANGROVE_VEGETATION, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(lv25, 0.85f)), lv32));
    }
}

