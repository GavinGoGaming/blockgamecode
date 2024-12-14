/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.intprovider.ClampedNormalIntProvider;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.PlacedFeatures;
import net.minecraft.world.gen.feature.UndergroundConfiguredFeatures;
import net.minecraft.world.gen.feature.VegetationConfiguredFeatures;
import net.minecraft.world.gen.placementmodifier.BiomePlacementModifier;
import net.minecraft.world.gen.placementmodifier.CountPlacementModifier;
import net.minecraft.world.gen.placementmodifier.EnvironmentScanPlacementModifier;
import net.minecraft.world.gen.placementmodifier.HeightRangePlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import net.minecraft.world.gen.placementmodifier.RandomOffsetPlacementModifier;
import net.minecraft.world.gen.placementmodifier.RarityFilterPlacementModifier;
import net.minecraft.world.gen.placementmodifier.SquarePlacementModifier;
import net.minecraft.world.gen.placementmodifier.SurfaceThresholdFilterPlacementModifier;

public class UndergroundPlacedFeatures {
    public static final RegistryKey<PlacedFeature> MONSTER_ROOM = PlacedFeatures.of("monster_room");
    public static final RegistryKey<PlacedFeature> MONSTER_ROOM_DEEP = PlacedFeatures.of("monster_room_deep");
    public static final RegistryKey<PlacedFeature> FOSSIL_UPPER = PlacedFeatures.of("fossil_upper");
    public static final RegistryKey<PlacedFeature> FOSSIL_LOWER = PlacedFeatures.of("fossil_lower");
    public static final RegistryKey<PlacedFeature> DRIPSTONE_CLUSTER = PlacedFeatures.of("dripstone_cluster");
    public static final RegistryKey<PlacedFeature> LARGE_DRIPSTONE = PlacedFeatures.of("large_dripstone");
    public static final RegistryKey<PlacedFeature> POINTED_DRIPSTONE = PlacedFeatures.of("pointed_dripstone");
    public static final RegistryKey<PlacedFeature> UNDERWATER_MAGMA = PlacedFeatures.of("underwater_magma");
    public static final RegistryKey<PlacedFeature> GLOW_LICHEN = PlacedFeatures.of("glow_lichen");
    public static final RegistryKey<PlacedFeature> ROOTED_AZALEA_TREE = PlacedFeatures.of("rooted_azalea_tree");
    public static final RegistryKey<PlacedFeature> CAVE_VINES = PlacedFeatures.of("cave_vines");
    public static final RegistryKey<PlacedFeature> LUSH_CAVES_VEGETATION = PlacedFeatures.of("lush_caves_vegetation");
    public static final RegistryKey<PlacedFeature> LUSH_CAVES_CLAY = PlacedFeatures.of("lush_caves_clay");
    public static final RegistryKey<PlacedFeature> LUSH_CAVES_CEILING_VEGETATION = PlacedFeatures.of("lush_caves_ceiling_vegetation");
    public static final RegistryKey<PlacedFeature> SPORE_BLOSSOM = PlacedFeatures.of("spore_blossom");
    public static final RegistryKey<PlacedFeature> CLASSIC_VINES_CAVE_FEATURE = PlacedFeatures.of("classic_vines_cave_feature");
    public static final RegistryKey<PlacedFeature> AMETHYST_GEODE = PlacedFeatures.of("amethyst_geode");
    public static final RegistryKey<PlacedFeature> SCULK_PATCH_DEEP_DARK = PlacedFeatures.of("sculk_patch_deep_dark");
    public static final RegistryKey<PlacedFeature> SCULK_PATCH_ANCIENT_CITY = PlacedFeatures.of("sculk_patch_ancient_city");
    public static final RegistryKey<PlacedFeature> SCULK_VEIN = PlacedFeatures.of("sculk_vein");

    public static void bootstrap(Registerable<PlacedFeature> featureRegisterable) {
        RegistryEntryLookup<ConfiguredFeature<?, ?>> lv = featureRegisterable.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv2 = lv.getOrThrow(UndergroundConfiguredFeatures.MONSTER_ROOM);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv3 = lv.getOrThrow(UndergroundConfiguredFeatures.FOSSIL_COAL);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv4 = lv.getOrThrow(UndergroundConfiguredFeatures.FOSSIL_DIAMONDS);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv5 = lv.getOrThrow(UndergroundConfiguredFeatures.DRIPSTONE_CLUSTER);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv6 = lv.getOrThrow(UndergroundConfiguredFeatures.LARGE_DRIPSTONE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv7 = lv.getOrThrow(UndergroundConfiguredFeatures.POINTED_DRIPSTONE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv8 = lv.getOrThrow(UndergroundConfiguredFeatures.UNDERWATER_MAGMA);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv9 = lv.getOrThrow(UndergroundConfiguredFeatures.GLOW_LICHEN);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv10 = lv.getOrThrow(UndergroundConfiguredFeatures.ROOTED_AZALEA_TREE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv11 = lv.getOrThrow(UndergroundConfiguredFeatures.CAVE_VINE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv12 = lv.getOrThrow(UndergroundConfiguredFeatures.MOSS_PATCH);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv13 = lv.getOrThrow(UndergroundConfiguredFeatures.LUSH_CAVES_CLAY);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv14 = lv.getOrThrow(UndergroundConfiguredFeatures.MOSS_PATCH_CEILING);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv15 = lv.getOrThrow(UndergroundConfiguredFeatures.SPORE_BLOSSOM);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv16 = lv.getOrThrow(VegetationConfiguredFeatures.VINES);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv17 = lv.getOrThrow(UndergroundConfiguredFeatures.AMETHYST_GEODE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv18 = lv.getOrThrow(UndergroundConfiguredFeatures.SCULK_PATCH_DEEP_DARK);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv19 = lv.getOrThrow(UndergroundConfiguredFeatures.SCULK_PATCH_ANCIENT_CITY);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv20 = lv.getOrThrow(UndergroundConfiguredFeatures.SCULK_VEIN);
        PlacedFeatures.register(featureRegisterable, MONSTER_ROOM, lv2, CountPlacementModifier.of(10), SquarePlacementModifier.of(), HeightRangePlacementModifier.uniform(YOffset.fixed(0), YOffset.getTop()), BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, MONSTER_ROOM_DEEP, lv2, CountPlacementModifier.of(4), SquarePlacementModifier.of(), HeightRangePlacementModifier.uniform(YOffset.aboveBottom(6), YOffset.fixed(-1)), BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, FOSSIL_UPPER, lv3, RarityFilterPlacementModifier.of(64), SquarePlacementModifier.of(), HeightRangePlacementModifier.uniform(YOffset.fixed(0), YOffset.getTop()), BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, FOSSIL_LOWER, lv4, RarityFilterPlacementModifier.of(64), SquarePlacementModifier.of(), HeightRangePlacementModifier.uniform(YOffset.getBottom(), YOffset.fixed(-8)), BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, DRIPSTONE_CLUSTER, lv5, CountPlacementModifier.of(UniformIntProvider.create(48, 96)), SquarePlacementModifier.of(), PlacedFeatures.BOTTOM_TO_120_RANGE, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, LARGE_DRIPSTONE, lv6, CountPlacementModifier.of(UniformIntProvider.create(10, 48)), SquarePlacementModifier.of(), PlacedFeatures.BOTTOM_TO_120_RANGE, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, POINTED_DRIPSTONE, lv7, CountPlacementModifier.of(UniformIntProvider.create(192, 256)), SquarePlacementModifier.of(), PlacedFeatures.BOTTOM_TO_120_RANGE, CountPlacementModifier.of(UniformIntProvider.create(1, 5)), RandomOffsetPlacementModifier.of(ClampedNormalIntProvider.of(0.0f, 3.0f, -10, 10), ClampedNormalIntProvider.of(0.0f, 0.6f, -2, 2)), BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, UNDERWATER_MAGMA, lv8, CountPlacementModifier.of(UniformIntProvider.create(44, 52)), SquarePlacementModifier.of(), PlacedFeatures.BOTTOM_TO_120_RANGE, SurfaceThresholdFilterPlacementModifier.of(Heightmap.Type.OCEAN_FLOOR_WG, Integer.MIN_VALUE, -2), BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, GLOW_LICHEN, lv9, CountPlacementModifier.of(UniformIntProvider.create(104, 157)), PlacedFeatures.BOTTOM_TO_120_RANGE, SquarePlacementModifier.of(), SurfaceThresholdFilterPlacementModifier.of(Heightmap.Type.OCEAN_FLOOR_WG, Integer.MIN_VALUE, -13), BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, ROOTED_AZALEA_TREE, lv10, CountPlacementModifier.of(UniformIntProvider.create(1, 2)), SquarePlacementModifier.of(), PlacedFeatures.BOTTOM_TO_120_RANGE, EnvironmentScanPlacementModifier.of(Direction.UP, BlockPredicate.solid(), BlockPredicate.IS_AIR, 12), RandomOffsetPlacementModifier.vertically(ConstantIntProvider.create(-1)), BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, CAVE_VINES, lv11, CountPlacementModifier.of(188), SquarePlacementModifier.of(), PlacedFeatures.BOTTOM_TO_120_RANGE, EnvironmentScanPlacementModifier.of(Direction.UP, BlockPredicate.hasSturdyFace(Direction.DOWN), BlockPredicate.IS_AIR, 12), RandomOffsetPlacementModifier.vertically(ConstantIntProvider.create(-1)), BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, LUSH_CAVES_VEGETATION, lv12, CountPlacementModifier.of(125), SquarePlacementModifier.of(), PlacedFeatures.BOTTOM_TO_120_RANGE, EnvironmentScanPlacementModifier.of(Direction.DOWN, BlockPredicate.solid(), BlockPredicate.IS_AIR, 12), RandomOffsetPlacementModifier.vertically(ConstantIntProvider.create(1)), BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, LUSH_CAVES_CLAY, lv13, CountPlacementModifier.of(62), SquarePlacementModifier.of(), PlacedFeatures.BOTTOM_TO_120_RANGE, EnvironmentScanPlacementModifier.of(Direction.DOWN, BlockPredicate.solid(), BlockPredicate.IS_AIR, 12), RandomOffsetPlacementModifier.vertically(ConstantIntProvider.create(1)), BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, LUSH_CAVES_CEILING_VEGETATION, lv14, CountPlacementModifier.of(125), SquarePlacementModifier.of(), PlacedFeatures.BOTTOM_TO_120_RANGE, EnvironmentScanPlacementModifier.of(Direction.UP, BlockPredicate.solid(), BlockPredicate.IS_AIR, 12), RandomOffsetPlacementModifier.vertically(ConstantIntProvider.create(-1)), BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, SPORE_BLOSSOM, lv15, CountPlacementModifier.of(25), SquarePlacementModifier.of(), PlacedFeatures.BOTTOM_TO_120_RANGE, EnvironmentScanPlacementModifier.of(Direction.UP, BlockPredicate.solid(), BlockPredicate.IS_AIR, 12), RandomOffsetPlacementModifier.vertically(ConstantIntProvider.create(-1)), BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, CLASSIC_VINES_CAVE_FEATURE, lv16, CountPlacementModifier.of(256), SquarePlacementModifier.of(), PlacedFeatures.BOTTOM_TO_120_RANGE, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, AMETHYST_GEODE, lv17, RarityFilterPlacementModifier.of(24), SquarePlacementModifier.of(), HeightRangePlacementModifier.uniform(YOffset.aboveBottom(6), YOffset.fixed(30)), BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, SCULK_PATCH_DEEP_DARK, lv18, CountPlacementModifier.of(ConstantIntProvider.create(256)), SquarePlacementModifier.of(), PlacedFeatures.BOTTOM_TO_120_RANGE, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, SCULK_PATCH_ANCIENT_CITY, lv19, new PlacementModifier[0]);
        PlacedFeatures.register(featureRegisterable, SCULK_VEIN, lv20, CountPlacementModifier.of(UniformIntProvider.create(204, 250)), SquarePlacementModifier.of(), PlacedFeatures.BOTTOM_TO_120_RANGE, BiomePlacementModifier.of());
    }
}

