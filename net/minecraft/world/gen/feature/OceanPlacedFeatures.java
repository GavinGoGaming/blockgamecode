/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import java.util.List;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.OceanConfiguredFeatures;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.PlacedFeatures;
import net.minecraft.world.gen.placementmodifier.BiomePlacementModifier;
import net.minecraft.world.gen.placementmodifier.BlockFilterPlacementModifier;
import net.minecraft.world.gen.placementmodifier.CarvingMaskPlacementModifier;
import net.minecraft.world.gen.placementmodifier.CountPlacementModifier;
import net.minecraft.world.gen.placementmodifier.NoiseBasedCountPlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import net.minecraft.world.gen.placementmodifier.RarityFilterPlacementModifier;
import net.minecraft.world.gen.placementmodifier.SquarePlacementModifier;

public class OceanPlacedFeatures {
    public static final RegistryKey<PlacedFeature> SEAGRASS_WARM = PlacedFeatures.of("seagrass_warm");
    public static final RegistryKey<PlacedFeature> SEAGRASS_NORMAL = PlacedFeatures.of("seagrass_normal");
    public static final RegistryKey<PlacedFeature> SEAGRASS_COLD = PlacedFeatures.of("seagrass_cold");
    public static final RegistryKey<PlacedFeature> SEAGRASS_RIVER = PlacedFeatures.of("seagrass_river");
    public static final RegistryKey<PlacedFeature> SEAGRASS_SWAMP = PlacedFeatures.of("seagrass_swamp");
    public static final RegistryKey<PlacedFeature> SEAGRASS_DEEP_WARM = PlacedFeatures.of("seagrass_deep_warm");
    public static final RegistryKey<PlacedFeature> SEAGRASS_DEEP = PlacedFeatures.of("seagrass_deep");
    public static final RegistryKey<PlacedFeature> SEAGRASS_DEEP_COLD = PlacedFeatures.of("seagrass_deep_cold");
    public static final RegistryKey<PlacedFeature> SEAGRASS_SIMPLE = PlacedFeatures.of("seagrass_simple");
    public static final RegistryKey<PlacedFeature> SEA_PICKLE = PlacedFeatures.of("sea_pickle");
    public static final RegistryKey<PlacedFeature> KELP_COLD = PlacedFeatures.of("kelp_cold");
    public static final RegistryKey<PlacedFeature> KELP_WARM = PlacedFeatures.of("kelp_warm");
    public static final RegistryKey<PlacedFeature> WARM_OCEAN_VEGETATION = PlacedFeatures.of("warm_ocean_vegetation");

    private static List<PlacementModifier> seagrassModifiers(int count) {
        return List.of(SquarePlacementModifier.of(), PlacedFeatures.OCEAN_FLOOR_WG_HEIGHTMAP, CountPlacementModifier.of(count), BiomePlacementModifier.of());
    }

    public static void bootstrap(Registerable<PlacedFeature> featureRegisterable) {
        RegistryEntryLookup<ConfiguredFeature<?, ?>> lv = featureRegisterable.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv2 = lv.getOrThrow(OceanConfiguredFeatures.SEAGRASS_SHORT);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv3 = lv.getOrThrow(OceanConfiguredFeatures.SEAGRASS_SLIGHTLY_LESS_SHORT);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv4 = lv.getOrThrow(OceanConfiguredFeatures.SEAGRASS_MID);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv5 = lv.getOrThrow(OceanConfiguredFeatures.SEAGRASS_TALL);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv6 = lv.getOrThrow(OceanConfiguredFeatures.SEAGRASS_SIMPLE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv7 = lv.getOrThrow(OceanConfiguredFeatures.SEA_PICKLE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv8 = lv.getOrThrow(OceanConfiguredFeatures.KELP);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv9 = lv.getOrThrow(OceanConfiguredFeatures.WARM_OCEAN_VEGETATION);
        PlacedFeatures.register(featureRegisterable, SEAGRASS_WARM, lv2, OceanPlacedFeatures.seagrassModifiers(80));
        PlacedFeatures.register(featureRegisterable, SEAGRASS_NORMAL, lv2, OceanPlacedFeatures.seagrassModifiers(48));
        PlacedFeatures.register(featureRegisterable, SEAGRASS_COLD, lv2, OceanPlacedFeatures.seagrassModifiers(32));
        PlacedFeatures.register(featureRegisterable, SEAGRASS_RIVER, lv3, OceanPlacedFeatures.seagrassModifiers(48));
        PlacedFeatures.register(featureRegisterable, SEAGRASS_SWAMP, lv4, OceanPlacedFeatures.seagrassModifiers(64));
        PlacedFeatures.register(featureRegisterable, SEAGRASS_DEEP_WARM, lv5, OceanPlacedFeatures.seagrassModifiers(80));
        PlacedFeatures.register(featureRegisterable, SEAGRASS_DEEP, lv5, OceanPlacedFeatures.seagrassModifiers(48));
        PlacedFeatures.register(featureRegisterable, SEAGRASS_DEEP_COLD, lv5, OceanPlacedFeatures.seagrassModifiers(40));
        PlacedFeatures.register(featureRegisterable, SEAGRASS_SIMPLE, lv6, CarvingMaskPlacementModifier.of(GenerationStep.Carver.LIQUID), RarityFilterPlacementModifier.of(10), BlockFilterPlacementModifier.of(BlockPredicate.allOf(BlockPredicate.matchingBlocks(Direction.DOWN.getVector(), Blocks.STONE), BlockPredicate.matchingBlocks((Vec3i)BlockPos.ORIGIN, Blocks.WATER), BlockPredicate.matchingBlocks(Direction.UP.getVector(), Blocks.WATER))), BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, SEA_PICKLE, lv7, RarityFilterPlacementModifier.of(16), SquarePlacementModifier.of(), PlacedFeatures.OCEAN_FLOOR_WG_HEIGHTMAP, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, KELP_COLD, lv8, NoiseBasedCountPlacementModifier.of(120, 80.0, 0.0), SquarePlacementModifier.of(), PlacedFeatures.OCEAN_FLOOR_WG_HEIGHTMAP, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, KELP_WARM, lv8, NoiseBasedCountPlacementModifier.of(80, 80.0, 0.0), SquarePlacementModifier.of(), PlacedFeatures.OCEAN_FLOOR_WG_HEIGHTMAP, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, WARM_OCEAN_VEGETATION, lv9, NoiseBasedCountPlacementModifier.of(20, 400.0, 0.0), SquarePlacementModifier.of(), PlacedFeatures.OCEAN_FLOOR_WG_HEIGHTMAP, BiomePlacementModifier.of());
    }
}

