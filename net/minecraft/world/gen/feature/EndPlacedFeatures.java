/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.EndConfiguredFeatures;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.PlacedFeatures;
import net.minecraft.world.gen.placementmodifier.BiomePlacementModifier;
import net.minecraft.world.gen.placementmodifier.CountPlacementModifier;
import net.minecraft.world.gen.placementmodifier.FixedPlacementModifier;
import net.minecraft.world.gen.placementmodifier.HeightRangePlacementModifier;
import net.minecraft.world.gen.placementmodifier.RandomOffsetPlacementModifier;
import net.minecraft.world.gen.placementmodifier.RarityFilterPlacementModifier;
import net.minecraft.world.gen.placementmodifier.SquarePlacementModifier;

public class EndPlacedFeatures {
    public static final RegistryKey<PlacedFeature> END_PLATFORM = PlacedFeatures.of("end_platform");
    public static final RegistryKey<PlacedFeature> END_SPIKE = PlacedFeatures.of("end_spike");
    public static final RegistryKey<PlacedFeature> END_GATEWAY_RETURN = PlacedFeatures.of("end_gateway_return");
    public static final RegistryKey<PlacedFeature> CHORUS_PLANT = PlacedFeatures.of("chorus_plant");
    public static final RegistryKey<PlacedFeature> END_ISLAND_DECORATED = PlacedFeatures.of("end_island_decorated");

    public static void bootstrap(Registerable<PlacedFeature> featureRegisterable) {
        RegistryEntryLookup<ConfiguredFeature<?, ?>> lv = featureRegisterable.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv2 = lv.getOrThrow(EndConfiguredFeatures.END_PLATFORM);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv3 = lv.getOrThrow(EndConfiguredFeatures.END_SPIKE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv4 = lv.getOrThrow(EndConfiguredFeatures.END_GATEWAY_RETURN);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv5 = lv.getOrThrow(EndConfiguredFeatures.CHORUS_PLANT);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv6 = lv.getOrThrow(EndConfiguredFeatures.END_ISLAND);
        PlacedFeatures.register(featureRegisterable, END_PLATFORM, lv2, FixedPlacementModifier.of(ServerWorld.END_SPAWN_POS.down()), BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, END_SPIKE, lv3, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, END_GATEWAY_RETURN, lv4, RarityFilterPlacementModifier.of(700), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, RandomOffsetPlacementModifier.vertically(UniformIntProvider.create(3, 9)), BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, CHORUS_PLANT, lv5, CountPlacementModifier.of(UniformIntProvider.create(0, 4)), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, END_ISLAND_DECORATED, lv6, RarityFilterPlacementModifier.of(14), PlacedFeatures.createCountExtraModifier(1, 0.25f, 1), SquarePlacementModifier.of(), HeightRangePlacementModifier.uniform(YOffset.fixed(55), YOffset.fixed(70)), BiomePlacementModifier.of());
    }
}

