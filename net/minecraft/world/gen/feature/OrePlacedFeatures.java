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
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.OreConfiguredFeatures;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.PlacedFeatures;
import net.minecraft.world.gen.placementmodifier.BiomePlacementModifier;
import net.minecraft.world.gen.placementmodifier.CountPlacementModifier;
import net.minecraft.world.gen.placementmodifier.HeightRangePlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import net.minecraft.world.gen.placementmodifier.RarityFilterPlacementModifier;
import net.minecraft.world.gen.placementmodifier.SquarePlacementModifier;

public class OrePlacedFeatures {
    public static final RegistryKey<PlacedFeature> ORE_MAGMA = PlacedFeatures.of("ore_magma");
    public static final RegistryKey<PlacedFeature> ORE_SOUL_SAND = PlacedFeatures.of("ore_soul_sand");
    public static final RegistryKey<PlacedFeature> ORE_GOLD_DELTAS = PlacedFeatures.of("ore_gold_deltas");
    public static final RegistryKey<PlacedFeature> ORE_QUARTZ_DELTAS = PlacedFeatures.of("ore_quartz_deltas");
    public static final RegistryKey<PlacedFeature> ORE_GOLD_NETHER = PlacedFeatures.of("ore_gold_nether");
    public static final RegistryKey<PlacedFeature> ORE_QUARTZ_NETHER = PlacedFeatures.of("ore_quartz_nether");
    public static final RegistryKey<PlacedFeature> ORE_GRAVEL_NETHER = PlacedFeatures.of("ore_gravel_nether");
    public static final RegistryKey<PlacedFeature> ORE_BLACKSTONE = PlacedFeatures.of("ore_blackstone");
    public static final RegistryKey<PlacedFeature> ORE_DIRT = PlacedFeatures.of("ore_dirt");
    public static final RegistryKey<PlacedFeature> ORE_GRAVEL = PlacedFeatures.of("ore_gravel");
    public static final RegistryKey<PlacedFeature> ORE_GRANITE_UPPER = PlacedFeatures.of("ore_granite_upper");
    public static final RegistryKey<PlacedFeature> ORE_GRANITE_LOWER = PlacedFeatures.of("ore_granite_lower");
    public static final RegistryKey<PlacedFeature> ORE_DIORITE_UPPER = PlacedFeatures.of("ore_diorite_upper");
    public static final RegistryKey<PlacedFeature> ORE_DIORITE_LOWER = PlacedFeatures.of("ore_diorite_lower");
    public static final RegistryKey<PlacedFeature> ORE_ANDESITE_UPPER = PlacedFeatures.of("ore_andesite_upper");
    public static final RegistryKey<PlacedFeature> ORE_ANDESITE_LOWER = PlacedFeatures.of("ore_andesite_lower");
    public static final RegistryKey<PlacedFeature> ORE_TUFF = PlacedFeatures.of("ore_tuff");
    public static final RegistryKey<PlacedFeature> ORE_COAL_UPPER = PlacedFeatures.of("ore_coal_upper");
    public static final RegistryKey<PlacedFeature> ORE_COAL_LOWER = PlacedFeatures.of("ore_coal_lower");
    public static final RegistryKey<PlacedFeature> ORE_IRON_UPPER = PlacedFeatures.of("ore_iron_upper");
    public static final RegistryKey<PlacedFeature> ORE_IRON_MIDDLE = PlacedFeatures.of("ore_iron_middle");
    public static final RegistryKey<PlacedFeature> ORE_IRON_SMALL = PlacedFeatures.of("ore_iron_small");
    public static final RegistryKey<PlacedFeature> ORE_GOLD_EXTRA = PlacedFeatures.of("ore_gold_extra");
    public static final RegistryKey<PlacedFeature> ORE_GOLD = PlacedFeatures.of("ore_gold");
    public static final RegistryKey<PlacedFeature> ORE_GOLD_LOWER = PlacedFeatures.of("ore_gold_lower");
    public static final RegistryKey<PlacedFeature> ORE_REDSTONE = PlacedFeatures.of("ore_redstone");
    public static final RegistryKey<PlacedFeature> ORE_REDSTONE_LOWER = PlacedFeatures.of("ore_redstone_lower");
    public static final RegistryKey<PlacedFeature> ORE_DIAMOND = PlacedFeatures.of("ore_diamond");
    public static final RegistryKey<PlacedFeature> ORE_DIAMOND_MEDIUM = PlacedFeatures.of("ore_diamond_medium");
    public static final RegistryKey<PlacedFeature> ORE_DIAMOND_LARGE = PlacedFeatures.of("ore_diamond_large");
    public static final RegistryKey<PlacedFeature> ORE_DIAMOND_BURIED = PlacedFeatures.of("ore_diamond_buried");
    public static final RegistryKey<PlacedFeature> ORE_LAPIS = PlacedFeatures.of("ore_lapis");
    public static final RegistryKey<PlacedFeature> ORE_LAPIS_BURIED = PlacedFeatures.of("ore_lapis_buried");
    public static final RegistryKey<PlacedFeature> ORE_INFESTED = PlacedFeatures.of("ore_infested");
    public static final RegistryKey<PlacedFeature> ORE_EMERALD = PlacedFeatures.of("ore_emerald");
    public static final RegistryKey<PlacedFeature> ORE_ANCIENT_DEBRIS_LARGE = PlacedFeatures.of("ore_ancient_debris_large");
    public static final RegistryKey<PlacedFeature> ORE_DEBRIS_SMALL = PlacedFeatures.of("ore_debris_small");
    public static final RegistryKey<PlacedFeature> ORE_COPPER = PlacedFeatures.of("ore_copper");
    public static final RegistryKey<PlacedFeature> ORE_COPPER_LARGE = PlacedFeatures.of("ore_copper_large");
    public static final RegistryKey<PlacedFeature> ORE_CLAY = PlacedFeatures.of("ore_clay");

    private static List<PlacementModifier> modifiers(PlacementModifier countModifier, PlacementModifier heightModifier) {
        return List.of(countModifier, SquarePlacementModifier.of(), heightModifier, BiomePlacementModifier.of());
    }

    private static List<PlacementModifier> modifiersWithCount(int count, PlacementModifier heightModifier) {
        return OrePlacedFeatures.modifiers(CountPlacementModifier.of(count), heightModifier);
    }

    private static List<PlacementModifier> modifiersWithRarity(int chance, PlacementModifier heightModifier) {
        return OrePlacedFeatures.modifiers(RarityFilterPlacementModifier.of(chance), heightModifier);
    }

    public static void bootstrap(Registerable<PlacedFeature> featureRegisterable) {
        RegistryEntryLookup<ConfiguredFeature<?, ?>> lv = featureRegisterable.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv2 = lv.getOrThrow(OreConfiguredFeatures.ORE_MAGMA);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv3 = lv.getOrThrow(OreConfiguredFeatures.ORE_SOUL_SAND);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv4 = lv.getOrThrow(OreConfiguredFeatures.ORE_NETHER_GOLD);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv5 = lv.getOrThrow(OreConfiguredFeatures.ORE_QUARTZ);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv6 = lv.getOrThrow(OreConfiguredFeatures.ORE_GRAVEL_NETHER);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv7 = lv.getOrThrow(OreConfiguredFeatures.ORE_BLACKSTONE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv8 = lv.getOrThrow(OreConfiguredFeatures.ORE_DIRT);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv9 = lv.getOrThrow(OreConfiguredFeatures.ORE_GRAVEL);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv10 = lv.getOrThrow(OreConfiguredFeatures.ORE_GRANITE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv11 = lv.getOrThrow(OreConfiguredFeatures.ORE_DIORITE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv12 = lv.getOrThrow(OreConfiguredFeatures.ORE_ANDESITE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv13 = lv.getOrThrow(OreConfiguredFeatures.ORE_TUFF);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv14 = lv.getOrThrow(OreConfiguredFeatures.ORE_COAL);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv15 = lv.getOrThrow(OreConfiguredFeatures.ORE_COAL_BURIED);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv16 = lv.getOrThrow(OreConfiguredFeatures.ORE_IRON);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv17 = lv.getOrThrow(OreConfiguredFeatures.ORE_IRON_SMALL);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv18 = lv.getOrThrow(OreConfiguredFeatures.ORE_GOLD);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv19 = lv.getOrThrow(OreConfiguredFeatures.ORE_GOLD_BURIED);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv20 = lv.getOrThrow(OreConfiguredFeatures.ORE_REDSTONE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv21 = lv.getOrThrow(OreConfiguredFeatures.ORE_DIAMOND_SMALL);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv22 = lv.getOrThrow(OreConfiguredFeatures.ORE_DIAMOND_MEDIUM);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv23 = lv.getOrThrow(OreConfiguredFeatures.ORE_DIAMOND_LARGE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv24 = lv.getOrThrow(OreConfiguredFeatures.ORE_DIAMOND_BURIED);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv25 = lv.getOrThrow(OreConfiguredFeatures.ORE_LAPIS);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv26 = lv.getOrThrow(OreConfiguredFeatures.ORE_LAPIS_BURIED);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv27 = lv.getOrThrow(OreConfiguredFeatures.ORE_INFESTED);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv28 = lv.getOrThrow(OreConfiguredFeatures.ORE_EMERALD);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv29 = lv.getOrThrow(OreConfiguredFeatures.ORE_ANCIENT_DEBRIS_LARGE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv30 = lv.getOrThrow(OreConfiguredFeatures.ORE_ANCIENT_DEBRIS_SMALL);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv31 = lv.getOrThrow(OreConfiguredFeatures.ORE_COPPER_SMALL);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv32 = lv.getOrThrow(OreConfiguredFeatures.ORE_COPPER_LARGE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv33 = lv.getOrThrow(OreConfiguredFeatures.ORE_CLAY);
        PlacedFeatures.register(featureRegisterable, ORE_MAGMA, lv2, OrePlacedFeatures.modifiersWithCount(4, HeightRangePlacementModifier.uniform(YOffset.fixed(27), YOffset.fixed(36))));
        PlacedFeatures.register(featureRegisterable, ORE_SOUL_SAND, lv3, OrePlacedFeatures.modifiersWithCount(12, HeightRangePlacementModifier.uniform(YOffset.getBottom(), YOffset.fixed(31))));
        PlacedFeatures.register(featureRegisterable, ORE_GOLD_DELTAS, lv4, OrePlacedFeatures.modifiersWithCount(20, PlacedFeatures.TEN_ABOVE_AND_BELOW_RANGE));
        PlacedFeatures.register(featureRegisterable, ORE_QUARTZ_DELTAS, lv5, OrePlacedFeatures.modifiersWithCount(32, PlacedFeatures.TEN_ABOVE_AND_BELOW_RANGE));
        PlacedFeatures.register(featureRegisterable, ORE_GOLD_NETHER, lv4, OrePlacedFeatures.modifiersWithCount(10, PlacedFeatures.TEN_ABOVE_AND_BELOW_RANGE));
        PlacedFeatures.register(featureRegisterable, ORE_QUARTZ_NETHER, lv5, OrePlacedFeatures.modifiersWithCount(16, PlacedFeatures.TEN_ABOVE_AND_BELOW_RANGE));
        PlacedFeatures.register(featureRegisterable, ORE_GRAVEL_NETHER, lv6, OrePlacedFeatures.modifiersWithCount(2, HeightRangePlacementModifier.uniform(YOffset.fixed(5), YOffset.fixed(41))));
        PlacedFeatures.register(featureRegisterable, ORE_BLACKSTONE, lv7, OrePlacedFeatures.modifiersWithCount(2, HeightRangePlacementModifier.uniform(YOffset.fixed(5), YOffset.fixed(31))));
        PlacedFeatures.register(featureRegisterable, ORE_DIRT, lv8, OrePlacedFeatures.modifiersWithCount(7, HeightRangePlacementModifier.uniform(YOffset.fixed(0), YOffset.fixed(160))));
        PlacedFeatures.register(featureRegisterable, ORE_GRAVEL, lv9, OrePlacedFeatures.modifiersWithCount(14, HeightRangePlacementModifier.uniform(YOffset.getBottom(), YOffset.getTop())));
        PlacedFeatures.register(featureRegisterable, ORE_GRANITE_UPPER, lv10, OrePlacedFeatures.modifiersWithRarity(6, HeightRangePlacementModifier.uniform(YOffset.fixed(64), YOffset.fixed(128))));
        PlacedFeatures.register(featureRegisterable, ORE_GRANITE_LOWER, lv10, OrePlacedFeatures.modifiersWithCount(2, HeightRangePlacementModifier.uniform(YOffset.fixed(0), YOffset.fixed(60))));
        PlacedFeatures.register(featureRegisterable, ORE_DIORITE_UPPER, lv11, OrePlacedFeatures.modifiersWithRarity(6, HeightRangePlacementModifier.uniform(YOffset.fixed(64), YOffset.fixed(128))));
        PlacedFeatures.register(featureRegisterable, ORE_DIORITE_LOWER, lv11, OrePlacedFeatures.modifiersWithCount(2, HeightRangePlacementModifier.uniform(YOffset.fixed(0), YOffset.fixed(60))));
        PlacedFeatures.register(featureRegisterable, ORE_ANDESITE_UPPER, lv12, OrePlacedFeatures.modifiersWithRarity(6, HeightRangePlacementModifier.uniform(YOffset.fixed(64), YOffset.fixed(128))));
        PlacedFeatures.register(featureRegisterable, ORE_ANDESITE_LOWER, lv12, OrePlacedFeatures.modifiersWithCount(2, HeightRangePlacementModifier.uniform(YOffset.fixed(0), YOffset.fixed(60))));
        PlacedFeatures.register(featureRegisterable, ORE_TUFF, lv13, OrePlacedFeatures.modifiersWithCount(2, HeightRangePlacementModifier.uniform(YOffset.getBottom(), YOffset.fixed(0))));
        PlacedFeatures.register(featureRegisterable, ORE_COAL_UPPER, lv14, OrePlacedFeatures.modifiersWithCount(30, HeightRangePlacementModifier.uniform(YOffset.fixed(136), YOffset.getTop())));
        PlacedFeatures.register(featureRegisterable, ORE_COAL_LOWER, lv15, OrePlacedFeatures.modifiersWithCount(20, HeightRangePlacementModifier.trapezoid(YOffset.fixed(0), YOffset.fixed(192))));
        PlacedFeatures.register(featureRegisterable, ORE_IRON_UPPER, lv16, OrePlacedFeatures.modifiersWithCount(90, HeightRangePlacementModifier.trapezoid(YOffset.fixed(80), YOffset.fixed(384))));
        PlacedFeatures.register(featureRegisterable, ORE_IRON_MIDDLE, lv16, OrePlacedFeatures.modifiersWithCount(10, HeightRangePlacementModifier.trapezoid(YOffset.fixed(-24), YOffset.fixed(56))));
        PlacedFeatures.register(featureRegisterable, ORE_IRON_SMALL, lv17, OrePlacedFeatures.modifiersWithCount(10, HeightRangePlacementModifier.uniform(YOffset.getBottom(), YOffset.fixed(72))));
        PlacedFeatures.register(featureRegisterable, ORE_GOLD_EXTRA, lv18, OrePlacedFeatures.modifiersWithCount(50, HeightRangePlacementModifier.uniform(YOffset.fixed(32), YOffset.fixed(256))));
        PlacedFeatures.register(featureRegisterable, ORE_GOLD, lv19, OrePlacedFeatures.modifiersWithCount(4, HeightRangePlacementModifier.trapezoid(YOffset.fixed(-64), YOffset.fixed(32))));
        PlacedFeatures.register(featureRegisterable, ORE_GOLD_LOWER, lv19, OrePlacedFeatures.modifiers(CountPlacementModifier.of(UniformIntProvider.create(0, 1)), HeightRangePlacementModifier.uniform(YOffset.fixed(-64), YOffset.fixed(-48))));
        PlacedFeatures.register(featureRegisterable, ORE_REDSTONE, lv20, OrePlacedFeatures.modifiersWithCount(4, HeightRangePlacementModifier.uniform(YOffset.getBottom(), YOffset.fixed(15))));
        PlacedFeatures.register(featureRegisterable, ORE_REDSTONE_LOWER, lv20, OrePlacedFeatures.modifiersWithCount(8, HeightRangePlacementModifier.trapezoid(YOffset.aboveBottom(-32), YOffset.aboveBottom(32))));
        PlacedFeatures.register(featureRegisterable, ORE_DIAMOND, lv21, OrePlacedFeatures.modifiersWithCount(7, HeightRangePlacementModifier.trapezoid(YOffset.aboveBottom(-80), YOffset.aboveBottom(80))));
        PlacedFeatures.register(featureRegisterable, ORE_DIAMOND_MEDIUM, lv22, OrePlacedFeatures.modifiersWithCount(2, HeightRangePlacementModifier.uniform(YOffset.fixed(-64), YOffset.fixed(-4))));
        PlacedFeatures.register(featureRegisterable, ORE_DIAMOND_LARGE, lv23, OrePlacedFeatures.modifiersWithRarity(9, HeightRangePlacementModifier.trapezoid(YOffset.aboveBottom(-80), YOffset.aboveBottom(80))));
        PlacedFeatures.register(featureRegisterable, ORE_DIAMOND_BURIED, lv24, OrePlacedFeatures.modifiersWithCount(4, HeightRangePlacementModifier.trapezoid(YOffset.aboveBottom(-80), YOffset.aboveBottom(80))));
        PlacedFeatures.register(featureRegisterable, ORE_LAPIS, lv25, OrePlacedFeatures.modifiersWithCount(2, HeightRangePlacementModifier.trapezoid(YOffset.fixed(-32), YOffset.fixed(32))));
        PlacedFeatures.register(featureRegisterable, ORE_LAPIS_BURIED, lv26, OrePlacedFeatures.modifiersWithCount(4, HeightRangePlacementModifier.uniform(YOffset.getBottom(), YOffset.fixed(64))));
        PlacedFeatures.register(featureRegisterable, ORE_INFESTED, lv27, OrePlacedFeatures.modifiersWithCount(14, HeightRangePlacementModifier.uniform(YOffset.getBottom(), YOffset.fixed(63))));
        PlacedFeatures.register(featureRegisterable, ORE_EMERALD, lv28, OrePlacedFeatures.modifiersWithCount(100, HeightRangePlacementModifier.trapezoid(YOffset.fixed(-16), YOffset.fixed(480))));
        PlacedFeatures.register(featureRegisterable, ORE_ANCIENT_DEBRIS_LARGE, lv29, SquarePlacementModifier.of(), HeightRangePlacementModifier.trapezoid(YOffset.fixed(8), YOffset.fixed(24)), BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, ORE_DEBRIS_SMALL, lv30, SquarePlacementModifier.of(), PlacedFeatures.EIGHT_ABOVE_AND_BELOW_RANGE, BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, ORE_COPPER, lv31, OrePlacedFeatures.modifiersWithCount(16, HeightRangePlacementModifier.trapezoid(YOffset.fixed(-16), YOffset.fixed(112))));
        PlacedFeatures.register(featureRegisterable, ORE_COPPER_LARGE, lv32, OrePlacedFeatures.modifiersWithCount(16, HeightRangePlacementModifier.trapezoid(YOffset.fixed(-16), YOffset.fixed(112))));
        PlacedFeatures.register(featureRegisterable, ORE_CLAY, lv33, OrePlacedFeatures.modifiersWithCount(46, PlacedFeatures.BOTTOM_TO_120_RANGE));
    }
}

