/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import net.minecraft.block.Blocks;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.PileConfiguredFeatures;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.PlacedFeatures;
import net.minecraft.world.gen.feature.TreeConfiguredFeatures;
import net.minecraft.world.gen.feature.VegetationConfiguredFeatures;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;

public class VillagePlacedFeatures {
    public static final RegistryKey<PlacedFeature> PILE_HAY = PlacedFeatures.of("pile_hay");
    public static final RegistryKey<PlacedFeature> PILE_MELON = PlacedFeatures.of("pile_melon");
    public static final RegistryKey<PlacedFeature> PILE_SNOW = PlacedFeatures.of("pile_snow");
    public static final RegistryKey<PlacedFeature> PILE_ICE = PlacedFeatures.of("pile_ice");
    public static final RegistryKey<PlacedFeature> PILE_PUMPKIN = PlacedFeatures.of("pile_pumpkin");
    public static final RegistryKey<PlacedFeature> OAK = PlacedFeatures.of("oak");
    public static final RegistryKey<PlacedFeature> ACACIA = PlacedFeatures.of("acacia");
    public static final RegistryKey<PlacedFeature> SPRUCE = PlacedFeatures.of("spruce");
    public static final RegistryKey<PlacedFeature> PINE = PlacedFeatures.of("pine");
    public static final RegistryKey<PlacedFeature> PATCH_CACTUS = PlacedFeatures.of("patch_cactus");
    public static final RegistryKey<PlacedFeature> FLOWER_PLAIN = PlacedFeatures.of("flower_plain");
    public static final RegistryKey<PlacedFeature> PATCH_TAIGA_GRASS = PlacedFeatures.of("patch_taiga_grass");
    public static final RegistryKey<PlacedFeature> PATCH_BERRY_BUSH = PlacedFeatures.of("patch_berry_bush");

    public static void bootstrap(Registerable<PlacedFeature> featureRegisterable) {
        RegistryEntryLookup<ConfiguredFeature<?, ?>> lv = featureRegisterable.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv2 = lv.getOrThrow(PileConfiguredFeatures.PILE_HAY);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv3 = lv.getOrThrow(PileConfiguredFeatures.PILE_MELON);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv4 = lv.getOrThrow(PileConfiguredFeatures.PILE_SNOW);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv5 = lv.getOrThrow(PileConfiguredFeatures.PILE_ICE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv6 = lv.getOrThrow(PileConfiguredFeatures.PILE_PUMPKIN);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv7 = lv.getOrThrow(TreeConfiguredFeatures.OAK);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv8 = lv.getOrThrow(TreeConfiguredFeatures.ACACIA);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv9 = lv.getOrThrow(TreeConfiguredFeatures.SPRUCE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv10 = lv.getOrThrow(TreeConfiguredFeatures.PINE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv11 = lv.getOrThrow(VegetationConfiguredFeatures.PATCH_CACTUS);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv12 = lv.getOrThrow(VegetationConfiguredFeatures.FLOWER_PLAIN);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv13 = lv.getOrThrow(VegetationConfiguredFeatures.PATCH_TAIGA_GRASS);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> lv14 = lv.getOrThrow(VegetationConfiguredFeatures.PATCH_BERRY_BUSH);
        PlacedFeatures.register(featureRegisterable, PILE_HAY, lv2, new PlacementModifier[0]);
        PlacedFeatures.register(featureRegisterable, PILE_MELON, lv3, new PlacementModifier[0]);
        PlacedFeatures.register(featureRegisterable, PILE_SNOW, lv4, new PlacementModifier[0]);
        PlacedFeatures.register(featureRegisterable, PILE_ICE, lv5, new PlacementModifier[0]);
        PlacedFeatures.register(featureRegisterable, PILE_PUMPKIN, lv6, new PlacementModifier[0]);
        PlacedFeatures.register(featureRegisterable, OAK, lv7, PlacedFeatures.wouldSurvive(Blocks.OAK_SAPLING));
        PlacedFeatures.register(featureRegisterable, ACACIA, lv8, PlacedFeatures.wouldSurvive(Blocks.ACACIA_SAPLING));
        PlacedFeatures.register(featureRegisterable, SPRUCE, lv9, PlacedFeatures.wouldSurvive(Blocks.SPRUCE_SAPLING));
        PlacedFeatures.register(featureRegisterable, PINE, lv10, PlacedFeatures.wouldSurvive(Blocks.SPRUCE_SAPLING));
        PlacedFeatures.register(featureRegisterable, PATCH_CACTUS, lv11, new PlacementModifier[0]);
        PlacedFeatures.register(featureRegisterable, FLOWER_PLAIN, lv12, new PlacementModifier[0]);
        PlacedFeatures.register(featureRegisterable, PATCH_TAIGA_GRASS, lv13, new PlacementModifier[0]);
        PlacedFeatures.register(featureRegisterable, PATCH_BERRY_BUSH, lv14, new PlacementModifier[0]);
    }
}

