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
import net.minecraft.util.math.Direction;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.PlacedFeatures;
import net.minecraft.world.gen.feature.TreeConfiguredFeatures;
import net.minecraft.world.gen.placementmodifier.BiomePlacementModifier;
import net.minecraft.world.gen.placementmodifier.BlockFilterPlacementModifier;
import net.minecraft.world.gen.placementmodifier.CountMultilayerPlacementModifier;
import net.minecraft.world.gen.placementmodifier.EnvironmentScanPlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;

public class TreePlacedFeatures {
    public static final RegistryKey<PlacedFeature> CRIMSON_FUNGI = PlacedFeatures.of("crimson_fungi");
    public static final RegistryKey<PlacedFeature> WARPED_FUNGI = PlacedFeatures.of("warped_fungi");
    public static final RegistryKey<PlacedFeature> OAK_CHECKED = PlacedFeatures.of("oak_checked");
    public static final RegistryKey<PlacedFeature> DARK_OAK_CHECKED = PlacedFeatures.of("dark_oak_checked");
    public static final RegistryKey<PlacedFeature> BIRCH_CHECKED = PlacedFeatures.of("birch_checked");
    public static final RegistryKey<PlacedFeature> ACACIA_CHECKED = PlacedFeatures.of("acacia_checked");
    public static final RegistryKey<PlacedFeature> SPRUCE_CHECKED = PlacedFeatures.of("spruce_checked");
    public static final RegistryKey<PlacedFeature> MANGROVE_CHECKED = PlacedFeatures.of("mangrove_checked");
    public static final RegistryKey<PlacedFeature> CHERRY_CHECKED = PlacedFeatures.of("cherry_checked");
    public static final RegistryKey<PlacedFeature> PINE_ON_SNOW = PlacedFeatures.of("pine_on_snow");
    public static final RegistryKey<PlacedFeature> SPRUCE_ON_SNOW = PlacedFeatures.of("spruce_on_snow");
    public static final RegistryKey<PlacedFeature> PINE_CHECKED = PlacedFeatures.of("pine_checked");
    public static final RegistryKey<PlacedFeature> JUNGLE_TREE = PlacedFeatures.of("jungle_tree");
    public static final RegistryKey<PlacedFeature> FANCY_OAK_CHECKED = PlacedFeatures.of("fancy_oak_checked");
    public static final RegistryKey<PlacedFeature> MEGA_JUNGLE_TREE_CHECKED = PlacedFeatures.of("mega_jungle_tree_checked");
    public static final RegistryKey<PlacedFeature> MEGA_SPRUCE_CHECKED = PlacedFeatures.of("mega_spruce_checked");
    public static final RegistryKey<PlacedFeature> MEGA_PINE_CHECKED = PlacedFeatures.of("mega_pine_checked");
    public static final RegistryKey<PlacedFeature> TALL_MANGROVE_CHECKED = PlacedFeatures.of("tall_mangrove_checked");
    public static final RegistryKey<PlacedFeature> JUNGLE_BUSH = PlacedFeatures.of("jungle_bush");
    public static final RegistryKey<PlacedFeature> SUPER_BIRCH_BEES_0002 = PlacedFeatures.of("super_birch_bees_0002");
    public static final RegistryKey<PlacedFeature> SUPER_BIRCH_BEES = PlacedFeatures.of("super_birch_bees");
    public static final RegistryKey<PlacedFeature> OAK_BEES_0002 = PlacedFeatures.of("oak_bees_0002");
    public static final RegistryKey<PlacedFeature> OAK_BEES_002 = PlacedFeatures.of("oak_bees_002");
    public static final RegistryKey<PlacedFeature> BIRCH_BEES_0002 = PlacedFeatures.of("birch_bees_0002");
    public static final RegistryKey<PlacedFeature> BIRCH_BEES_002 = PlacedFeatures.of("birch_bees_002");
    public static final RegistryKey<PlacedFeature> FANCY_OAK_BEES_0002 = PlacedFeatures.of("fancy_oak_bees_0002");
    public static final RegistryKey<PlacedFeature> FANCY_OAK_BEES_002 = PlacedFeatures.of("fancy_oak_bees_002");
    public static final RegistryKey<PlacedFeature> FANCY_OAK_BEES = PlacedFeatures.of("fancy_oak_bees");
    public static final RegistryKey<PlacedFeature> CHERRY_BEES_005 = PlacedFeatures.of("cherry_bees_005");

    public static void bootstrap(Registerable<PlacedFeature> featureRegisterable) {
        RegistryEntryLookup<ConfiguredFeature<?, ?>> lv = featureRegisterable.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv2 = lv.getOrThrow(TreeConfiguredFeatures.CRIMSON_FUNGUS);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv3 = lv.getOrThrow(TreeConfiguredFeatures.WARPED_FUNGUS);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv4 = lv.getOrThrow(TreeConfiguredFeatures.OAK);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv5 = lv.getOrThrow(TreeConfiguredFeatures.DARK_OAK);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv6 = lv.getOrThrow(TreeConfiguredFeatures.BIRCH);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv7 = lv.getOrThrow(TreeConfiguredFeatures.ACACIA);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv8 = lv.getOrThrow(TreeConfiguredFeatures.SPRUCE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv9 = lv.getOrThrow(TreeConfiguredFeatures.MANGROVE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv10 = lv.getOrThrow(TreeConfiguredFeatures.CHERRY);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv11 = lv.getOrThrow(TreeConfiguredFeatures.PINE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv12 = lv.getOrThrow(TreeConfiguredFeatures.JUNGLE_TREE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv13 = lv.getOrThrow(TreeConfiguredFeatures.FANCY_OAK);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv14 = lv.getOrThrow(TreeConfiguredFeatures.MEGA_JUNGLE_TREE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv15 = lv.getOrThrow(TreeConfiguredFeatures.MEGA_SPRUCE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv16 = lv.getOrThrow(TreeConfiguredFeatures.MEGA_PINE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv17 = lv.getOrThrow(TreeConfiguredFeatures.TALL_MANGROVE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv18 = lv.getOrThrow(TreeConfiguredFeatures.JUNGLE_BUSH);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv19 = lv.getOrThrow(TreeConfiguredFeatures.SUPER_BIRCH_BEES_0002);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv20 = lv.getOrThrow(TreeConfiguredFeatures.SUPER_BIRCH_BEES);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv21 = lv.getOrThrow(TreeConfiguredFeatures.OAK_BEES_0002);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv22 = lv.getOrThrow(TreeConfiguredFeatures.OAK_BEES_002);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv23 = lv.getOrThrow(TreeConfiguredFeatures.BIRCH_BEES_0002);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv24 = lv.getOrThrow(TreeConfiguredFeatures.BIRCH_BEES_002);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv25 = lv.getOrThrow(TreeConfiguredFeatures.FANCY_OAK_BEES_0002);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv26 = lv.getOrThrow(TreeConfiguredFeatures.FANCY_OAK_BEES_002);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv27 = lv.getOrThrow(TreeConfiguredFeatures.FANCY_OAK_BEES);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv28 = lv.getOrThrow(TreeConfiguredFeatures.CHERRY_BEES_005);
        PlacedFeatures.register(featureRegisterable, CRIMSON_FUNGI, lv2, CountMultilayerPlacementModifier.of(8), BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, WARPED_FUNGI, lv3, CountMultilayerPlacementModifier.of(8), BiomePlacementModifier.of());
        PlacedFeatures.register(featureRegisterable, OAK_CHECKED, lv4, PlacedFeatures.wouldSurvive(Blocks.OAK_SAPLING));
        PlacedFeatures.register(featureRegisterable, DARK_OAK_CHECKED, lv5, PlacedFeatures.wouldSurvive(Blocks.DARK_OAK_SAPLING));
        PlacedFeatures.register(featureRegisterable, BIRCH_CHECKED, lv6, PlacedFeatures.wouldSurvive(Blocks.BIRCH_SAPLING));
        PlacedFeatures.register(featureRegisterable, ACACIA_CHECKED, lv7, PlacedFeatures.wouldSurvive(Blocks.ACACIA_SAPLING));
        PlacedFeatures.register(featureRegisterable, SPRUCE_CHECKED, lv8, PlacedFeatures.wouldSurvive(Blocks.SPRUCE_SAPLING));
        PlacedFeatures.register(featureRegisterable, MANGROVE_CHECKED, lv9, PlacedFeatures.wouldSurvive(Blocks.MANGROVE_PROPAGULE));
        PlacedFeatures.register(featureRegisterable, CHERRY_CHECKED, lv10, PlacedFeatures.wouldSurvive(Blocks.CHERRY_SAPLING));
        BlockPredicate lv29 = BlockPredicate.matchingBlocks(Direction.DOWN.getVector(), Blocks.SNOW_BLOCK, Blocks.POWDER_SNOW);
        List<PlacementModifier> list = List.of(EnvironmentScanPlacementModifier.of(Direction.UP, BlockPredicate.not(BlockPredicate.matchingBlocks(Blocks.POWDER_SNOW)), 8), BlockFilterPlacementModifier.of(lv29));
        PlacedFeatures.register(featureRegisterable, PINE_ON_SNOW, lv11, list);
        PlacedFeatures.register(featureRegisterable, SPRUCE_ON_SNOW, lv8, list);
        PlacedFeatures.register(featureRegisterable, PINE_CHECKED, lv11, PlacedFeatures.wouldSurvive(Blocks.SPRUCE_SAPLING));
        PlacedFeatures.register(featureRegisterable, JUNGLE_TREE, lv12, PlacedFeatures.wouldSurvive(Blocks.JUNGLE_SAPLING));
        PlacedFeatures.register(featureRegisterable, FANCY_OAK_CHECKED, lv13, PlacedFeatures.wouldSurvive(Blocks.OAK_SAPLING));
        PlacedFeatures.register(featureRegisterable, MEGA_JUNGLE_TREE_CHECKED, lv14, PlacedFeatures.wouldSurvive(Blocks.JUNGLE_SAPLING));
        PlacedFeatures.register(featureRegisterable, MEGA_SPRUCE_CHECKED, lv15, PlacedFeatures.wouldSurvive(Blocks.SPRUCE_SAPLING));
        PlacedFeatures.register(featureRegisterable, MEGA_PINE_CHECKED, lv16, PlacedFeatures.wouldSurvive(Blocks.SPRUCE_SAPLING));
        PlacedFeatures.register(featureRegisterable, TALL_MANGROVE_CHECKED, lv17, PlacedFeatures.wouldSurvive(Blocks.MANGROVE_PROPAGULE));
        PlacedFeatures.register(featureRegisterable, JUNGLE_BUSH, lv18, PlacedFeatures.wouldSurvive(Blocks.OAK_SAPLING));
        PlacedFeatures.register(featureRegisterable, SUPER_BIRCH_BEES_0002, lv19, PlacedFeatures.wouldSurvive(Blocks.BIRCH_SAPLING));
        PlacedFeatures.register(featureRegisterable, SUPER_BIRCH_BEES, lv20, PlacedFeatures.wouldSurvive(Blocks.BIRCH_SAPLING));
        PlacedFeatures.register(featureRegisterable, OAK_BEES_0002, lv21, PlacedFeatures.wouldSurvive(Blocks.OAK_SAPLING));
        PlacedFeatures.register(featureRegisterable, OAK_BEES_002, lv22, PlacedFeatures.wouldSurvive(Blocks.OAK_SAPLING));
        PlacedFeatures.register(featureRegisterable, BIRCH_BEES_0002, lv23, PlacedFeatures.wouldSurvive(Blocks.BIRCH_SAPLING));
        PlacedFeatures.register(featureRegisterable, BIRCH_BEES_002, lv24, PlacedFeatures.wouldSurvive(Blocks.BIRCH_SAPLING));
        PlacedFeatures.register(featureRegisterable, FANCY_OAK_BEES_0002, lv25, PlacedFeatures.wouldSurvive(Blocks.OAK_SAPLING));
        PlacedFeatures.register(featureRegisterable, FANCY_OAK_BEES_002, lv26, PlacedFeatures.wouldSurvive(Blocks.OAK_SAPLING));
        PlacedFeatures.register(featureRegisterable, FANCY_OAK_BEES, lv27, PlacedFeatures.wouldSurvive(Blocks.OAK_SAPLING));
        PlacedFeatures.register(featureRegisterable, CHERRY_BEES_005, lv28, PlacedFeatures.wouldSurvive(Blocks.CHERRY_SAPLING));
    }
}

