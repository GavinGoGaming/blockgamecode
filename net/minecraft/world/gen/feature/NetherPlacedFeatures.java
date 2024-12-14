/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import java.util.List;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.intprovider.BiasedToBottomIntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.NetherConfiguredFeatures;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.PlacedFeatures;
import net.minecraft.world.gen.placementmodifier.BiomePlacementModifier;
import net.minecraft.world.gen.placementmodifier.CountMultilayerPlacementModifier;
import net.minecraft.world.gen.placementmodifier.CountPlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import net.minecraft.world.gen.placementmodifier.SquarePlacementModifier;

public class NetherPlacedFeatures {
    public static final RegistryKey<PlacedFeature> DELTA = PlacedFeatures.of("delta");
    public static final RegistryKey<PlacedFeature> SMALL_BASALT_COLUMNS = PlacedFeatures.of("small_basalt_columns");
    public static final RegistryKey<PlacedFeature> LARGE_BASALT_COLUMNS = PlacedFeatures.of("large_basalt_columns");
    public static final RegistryKey<PlacedFeature> BASALT_BLOBS = PlacedFeatures.of("basalt_blobs");
    public static final RegistryKey<PlacedFeature> BLACKSTONE_BLOBS = PlacedFeatures.of("blackstone_blobs");
    public static final RegistryKey<PlacedFeature> GLOWSTONE_EXTRA = PlacedFeatures.of("glowstone_extra");
    public static final RegistryKey<PlacedFeature> GLOWSTONE = PlacedFeatures.of("glowstone");
    public static final RegistryKey<PlacedFeature> CRIMSON_FOREST_VEGETATION = PlacedFeatures.of("crimson_forest_vegetation");
    public static final RegistryKey<PlacedFeature> WARPED_FOREST_VEGETATION = PlacedFeatures.of("warped_forest_vegetation");
    public static final RegistryKey<PlacedFeature> NETHER_SPROUTS = PlacedFeatures.of("nether_sprouts");
    public static final RegistryKey<PlacedFeature> TWISTING_VINES = PlacedFeatures.of("twisting_vines");
    public static final RegistryKey<PlacedFeature> WEEPING_VINES = PlacedFeatures.of("weeping_vines");
    public static final RegistryKey<PlacedFeature> PATCH_CRIMSON_ROOTS = PlacedFeatures.of("patch_crimson_roots");
    public static final RegistryKey<PlacedFeature> BASALT_PILLAR = PlacedFeatures.of("basalt_pillar");
    public static final RegistryKey<PlacedFeature> SPRING_DELTA = PlacedFeatures.of("spring_delta");
    public static final RegistryKey<PlacedFeature> SPRING_CLOSED = PlacedFeatures.of("spring_closed");
    public static final RegistryKey<PlacedFeature> SPRING_CLOSED_DOUBLE = PlacedFeatures.of("spring_closed_double");
    public static final RegistryKey<PlacedFeature> SPRING_OPEN = PlacedFeatures.of("spring_open");
    public static final RegistryKey<PlacedFeature> PATCH_SOUL_FIRE = PlacedFeatures.of("patch_soul_fire");
    public static final RegistryKey<PlacedFeature> PATCH_FIRE = PlacedFeatures.of("patch_fire");

    public static void bootstrap(Registerable<PlacedFeature> featureRegisterable) {
        RegistryEntryLookup<ConfiguredFeature<?, ?>> lv = featureRegisterable.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv2 = lv.getOrThrow(NetherConfiguredFeatures.DELTA);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv3 = lv.getOrThrow(NetherConfiguredFeatures.SMALL_BASALT_COLUMNS);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv4 = lv.getOrThrow(NetherConfiguredFeatures.SMALL_BASALT_COLUMNS_TEMP);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv5 = lv.getOrThrow(NetherConfiguredFeatures.BASALT_BLOBS);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv6 = lv.getOrThrow(NetherConfiguredFeatures.BLACKSTONE_BLOBS);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv7 = lv.getOrThrow(NetherConfiguredFeatures.GLOWSTONE_EXTRA);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv8 = lv.getOrThrow(NetherConfiguredFeatures.CRIMSON_FOREST_VEGETATION);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv9 = lv.getOrThrow(NetherConfiguredFeatures.WARPED_FOREST_VEGETATION);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv10 = lv.getOrThrow(NetherConfiguredFeatures.NETHER_SPROUTS);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv11 = lv.getOrThrow(NetherConfiguredFeatures.TWISTING_VINES);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv12 = lv.getOrThrow(NetherConfiguredFeatures.WEEPING_VINES);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv13 = lv.getOrThrow(NetherConfiguredFeatures.PATCH_CRIMSON_ROOTS);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv14 = lv.getOrThrow(NetherConfiguredFeatures.BASALT_PILLAR);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv15 = lv.getOrThrow(NetherConfiguredFeatures.SPRING_LAVA_NETHER);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv16 = lv.getOrThrow(NetherConfiguredFeatures.SPRING_NETHER_CLOSED);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv17 = lv.getOrThrow(NetherConfiguredFeatures.SPRING_NETHER_OPEN);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv18 = lv.getOrThrow(NetherConfiguredFeatures.PATCH_SOUL_FIRE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv19 = lv.getOrThrow(NetherConfiguredFeatures.PATCH_FIRE);
        PlacedFeatures.register(featureRegisterable, DELTA, lv2, CountMultilayerPlacementModifier.of(40), BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, SMALL_BASALT_COLUMNS, lv3, CountMultilayerPlacementModifier.of(4), BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, LARGE_BASALT_COLUMNS, lv4, CountMultilayerPlacementModifier.of(2), BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, BASALT_BLOBS, lv5, CountPlacementModifier.of(75), SquarePlacementModifier.of(), PlacedFeatures.BOTTOM_TO_TOP_RANGE, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, BLACKSTONE_BLOBS, lv6, CountPlacementModifier.of(25), SquarePlacementModifier.of(), PlacedFeatures.BOTTOM_TO_TOP_RANGE, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, GLOWSTONE_EXTRA, lv7, CountPlacementModifier.of(BiasedToBottomIntProvider.create(0, 9)), SquarePlacementModifier.of(), PlacedFeatures.FOUR_ABOVE_AND_BELOW_RANGE, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, GLOWSTONE, lv7, CountPlacementModifier.of(10), SquarePlacementModifier.of(), PlacedFeatures.BOTTOM_TO_TOP_RANGE, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, CRIMSON_FOREST_VEGETATION, lv8, CountMultilayerPlacementModifier.of(6), BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, WARPED_FOREST_VEGETATION, lv9, CountMultilayerPlacementModifier.of(5), BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, NETHER_SPROUTS, lv10, CountMultilayerPlacementModifier.of(4), BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, TWISTING_VINES, lv11, CountPlacementModifier.of(10), SquarePlacementModifier.of(), PlacedFeatures.BOTTOM_TO_TOP_RANGE, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, WEEPING_VINES, lv12, CountPlacementModifier.of(10), SquarePlacementModifier.of(), PlacedFeatures.BOTTOM_TO_TOP_RANGE, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, PATCH_CRIMSON_ROOTS, lv13, PlacedFeatures.BOTTOM_TO_TOP_RANGE, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, BASALT_PILLAR, lv14, CountPlacementModifier.of(10), SquarePlacementModifier.of(), PlacedFeatures.BOTTOM_TO_TOP_RANGE, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, SPRING_DELTA, lv15, CountPlacementModifier.of(16), SquarePlacementModifier.of(), PlacedFeatures.FOUR_ABOVE_AND_BELOW_RANGE, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, SPRING_CLOSED, lv16, CountPlacementModifier.of(16), SquarePlacementModifier.of(), PlacedFeatures.TEN_ABOVE_AND_BELOW_RANGE, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, SPRING_CLOSED_DOUBLE, lv16, CountPlacementModifier.of(32), SquarePlacementModifier.of(), PlacedFeatures.TEN_ABOVE_AND_BELOW_RANGE, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, SPRING_OPEN, lv17, CountPlacementModifier.of(8), SquarePlacementModifier.of(), PlacedFeatures.FOUR_ABOVE_AND_BELOW_RANGE, BiomePlacementModifier.of());
        List<PlacementModifier> list = List.of(CountPlacementModifier.of(UniformIntProvider.create(0, 5)), SquarePlacementModifier.of(), PlacedFeatures.FOUR_ABOVE_AND_BELOW_RANGE, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, PATCH_SOUL_FIRE, lv18, list);
        PlacedFeatures.register(featureRegisterable, PATCH_FIRE, lv19, list);
    }
}

