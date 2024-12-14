/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.MiscConfiguredFeatures;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.PlacedFeatures;
import net.minecraft.world.gen.heightprovider.UniformHeightProvider;
import net.minecraft.world.gen.heightprovider.VeryBiasedToBottomHeightProvider;
import net.minecraft.world.gen.placementmodifier.BiomePlacementModifier;
import net.minecraft.world.gen.placementmodifier.BlockFilterPlacementModifier;
import net.minecraft.world.gen.placementmodifier.CountPlacementModifier;
import net.minecraft.world.gen.placementmodifier.EnvironmentScanPlacementModifier;
import net.minecraft.world.gen.placementmodifier.HeightRangePlacementModifier;
import net.minecraft.world.gen.placementmodifier.RandomOffsetPlacementModifier;
import net.minecraft.world.gen.placementmodifier.RarityFilterPlacementModifier;
import net.minecraft.world.gen.placementmodifier.SquarePlacementModifier;
import net.minecraft.world.gen.placementmodifier.SurfaceThresholdFilterPlacementModifier;

public class MiscPlacedFeatures {
    public static final RegistryKey<PlacedFeature> ICE_SPIKE = PlacedFeatures.of("ice_spike");
    public static final RegistryKey<PlacedFeature> ICE_PATCH = PlacedFeatures.of("ice_patch");
    public static final RegistryKey<PlacedFeature> FOREST_ROCK = PlacedFeatures.of("forest_rock");
    public static final RegistryKey<PlacedFeature> ICEBERG_PACKED = PlacedFeatures.of("iceberg_packed");
    public static final RegistryKey<PlacedFeature> ICEBERG_BLUE = PlacedFeatures.of("iceberg_blue");
    public static final RegistryKey<PlacedFeature> BLUE_ICE = PlacedFeatures.of("blue_ice");
    public static final RegistryKey<PlacedFeature> LAKE_LAVA_UNDERGROUND = PlacedFeatures.of("lake_lava_underground");
    public static final RegistryKey<PlacedFeature> LAKE_LAVA_SURFACE = PlacedFeatures.of("lake_lava_surface");
    public static final RegistryKey<PlacedFeature> DISK_CLAY = PlacedFeatures.of("disk_clay");
    public static final RegistryKey<PlacedFeature> DISK_GRAVEL = PlacedFeatures.of("disk_gravel");
    public static final RegistryKey<PlacedFeature> DISK_SAND = PlacedFeatures.of("disk_sand");
    public static final RegistryKey<PlacedFeature> DISK_GRASS = PlacedFeatures.of("disk_grass");
    public static final RegistryKey<PlacedFeature> FREEZE_TOP_LAYER = PlacedFeatures.of("freeze_top_layer");
    public static final RegistryKey<PlacedFeature> VOID_START_PLATFORM = PlacedFeatures.of("void_start_platform");
    public static final RegistryKey<PlacedFeature> DESERT_WELL = PlacedFeatures.of("desert_well");
    public static final RegistryKey<PlacedFeature> SPRING_LAVA = PlacedFeatures.of("spring_lava");
    public static final RegistryKey<PlacedFeature> SPRING_LAVA_FROZEN = PlacedFeatures.of("spring_lava_frozen");
    public static final RegistryKey<PlacedFeature> SPRING_WATER = PlacedFeatures.of("spring_water");

    public static void bootstrap(Registerable<PlacedFeature> featureRegisterable) {
        RegistryEntryLookup<ConfiguredFeature<?, ?>> lv = featureRegisterable.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv2 = lv.getOrThrow(MiscConfiguredFeatures.ICE_SPIKE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv3 = lv.getOrThrow(MiscConfiguredFeatures.ICE_PATCH);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv4 = lv.getOrThrow(MiscConfiguredFeatures.FOREST_ROCK);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv5 = lv.getOrThrow(MiscConfiguredFeatures.ICEBERG_PACKED);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv6 = lv.getOrThrow(MiscConfiguredFeatures.ICEBERG_BLUE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv7 = lv.getOrThrow(MiscConfiguredFeatures.BLUE_ICE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv8 = lv.getOrThrow(MiscConfiguredFeatures.LAKE_LAVA);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv9 = lv.getOrThrow(MiscConfiguredFeatures.DISK_CLAY);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv10 = lv.getOrThrow(MiscConfiguredFeatures.DISK_GRAVEL);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv11 = lv.getOrThrow(MiscConfiguredFeatures.DISK_SAND);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv12 = lv.getOrThrow(MiscConfiguredFeatures.DISK_GRASS);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv13 = lv.getOrThrow(MiscConfiguredFeatures.FREEZE_TOP_LAYER);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv14 = lv.getOrThrow(MiscConfiguredFeatures.VOID_START_PLATFORM);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv15 = lv.getOrThrow(MiscConfiguredFeatures.DESERT_WELL);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv16 = lv.getOrThrow(MiscConfiguredFeatures.SPRING_LAVA_OVERWORLD);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv17 = lv.getOrThrow(MiscConfiguredFeatures.SPRING_LAVA_FROZEN);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv18 = lv.getOrThrow(MiscConfiguredFeatures.SPRING_WATER);
        PlacedFeatures.register(featureRegisterable, ICE_SPIKE, lv2, CountPlacementModifier.of(3), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, ICE_PATCH, lv3, CountPlacementModifier.of(2), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, RandomOffsetPlacementModifier.vertically(ConstantIntProvider.create(-1)), BlockFilterPlacementModifier.of(BlockPredicate.matchingBlocks(Blocks.SNOW_BLOCK)), BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, FOREST_ROCK, lv4, CountPlacementModifier.of(2), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, ICEBERG_BLUE, lv6, RarityFilterPlacementModifier.of(200), SquarePlacementModifier.of(), BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, ICEBERG_PACKED, lv5, RarityFilterPlacementModifier.of(16), SquarePlacementModifier.of(), BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, BLUE_ICE, lv7, CountPlacementModifier.of(UniformIntProvider.create(0, 19)), SquarePlacementModifier.of(), HeightRangePlacementModifier.uniform(YOffset.fixed(30), YOffset.fixed(61)), BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, LAKE_LAVA_UNDERGROUND, lv8, RarityFilterPlacementModifier.of(9), SquarePlacementModifier.of(), HeightRangePlacementModifier.of(UniformHeightProvider.create(YOffset.fixed(0), YOffset.getTop())), EnvironmentScanPlacementModifier.of(Direction.DOWN, BlockPredicate.bothOf(BlockPredicate.not(BlockPredicate.IS_AIR), BlockPredicate.insideWorldBounds(new BlockPos(0, -5, 0))), 32), SurfaceThresholdFilterPlacementModifier.of(Heightmap.Type.OCEAN_FLOOR_WG, Integer.MIN_VALUE, -5), BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, LAKE_LAVA_SURFACE, lv8, RarityFilterPlacementModifier.of(200), SquarePlacementModifier.of(), PlacedFeatures.WORLD_SURFACE_WG_HEIGHTMAP, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, DISK_CLAY, lv9, SquarePlacementModifier.of(), PlacedFeatures.OCEAN_FLOOR_WG_HEIGHTMAP, BlockFilterPlacementModifier.of(BlockPredicate.matchingFluids(Fluids.WATER)), BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, DISK_GRAVEL, lv10, SquarePlacementModifier.of(), PlacedFeatures.OCEAN_FLOOR_WG_HEIGHTMAP, BlockFilterPlacementModifier.of(BlockPredicate.matchingFluids(Fluids.WATER)), BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, DISK_SAND, lv11, CountPlacementModifier.of(3), SquarePlacementModifier.of(), PlacedFeatures.OCEAN_FLOOR_WG_HEIGHTMAP, BlockFilterPlacementModifier.of(BlockPredicate.matchingFluids(Fluids.WATER)), BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, DISK_GRASS, lv12, CountPlacementModifier.of(1), SquarePlacementModifier.of(), PlacedFeatures.OCEAN_FLOOR_WG_HEIGHTMAP, RandomOffsetPlacementModifier.vertically(ConstantIntProvider.create(-1)), BlockFilterPlacementModifier.of(BlockPredicate.matchingBlocks(Blocks.MUD)), BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, FREEZE_TOP_LAYER, lv13, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, VOID_START_PLATFORM, lv14, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, DESERT_WELL, lv15, RarityFilterPlacementModifier.of(1000), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, SPRING_LAVA, lv16, CountPlacementModifier.of(20), SquarePlacementModifier.of(), HeightRangePlacementModifier.of(VeryBiasedToBottomHeightProvider.create(YOffset.getBottom(), YOffset.belowTop(8), 8)), BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, SPRING_LAVA_FROZEN, lv17, CountPlacementModifier.of(20), SquarePlacementModifier.of(), HeightRangePlacementModifier.of(VeryBiasedToBottomHeightProvider.create(YOffset.getBottom(), YOffset.belowTop(8), 8)), BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, SPRING_WATER, lv18, CountPlacementModifier.of(25), SquarePlacementModifier.of(), HeightRangePlacementModifier.uniform(YOffset.getBottom(), YOffset.fixed(192)), BiomePlacementModifier.of());
    }
}

