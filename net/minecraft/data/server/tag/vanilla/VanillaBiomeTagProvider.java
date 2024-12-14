/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.data.server.tag.vanilla;

import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.minecraft.data.DataOutput;
import net.minecraft.data.server.tag.TagProvider;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterList;

public class VanillaBiomeTagProvider
extends TagProvider<Biome> {
    public VanillaBiomeTagProvider(DataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookupFuture) {
        super(output, RegistryKeys.BIOME, registryLookupFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup lookup) {
        this.getOrCreateTagBuilder(BiomeTags.IS_DEEP_OCEAN).add(BiomeKeys.DEEP_FROZEN_OCEAN).add(BiomeKeys.DEEP_COLD_OCEAN).add(BiomeKeys.DEEP_OCEAN).add(BiomeKeys.DEEP_LUKEWARM_OCEAN);
        this.getOrCreateTagBuilder(BiomeTags.IS_OCEAN).addTag(BiomeTags.IS_DEEP_OCEAN).add(BiomeKeys.FROZEN_OCEAN).add(BiomeKeys.OCEAN).add(BiomeKeys.COLD_OCEAN).add(BiomeKeys.LUKEWARM_OCEAN).add(BiomeKeys.WARM_OCEAN);
        this.getOrCreateTagBuilder(BiomeTags.IS_BEACH).add(BiomeKeys.BEACH).add(BiomeKeys.SNOWY_BEACH);
        this.getOrCreateTagBuilder(BiomeTags.IS_RIVER).add(BiomeKeys.RIVER).add(BiomeKeys.FROZEN_RIVER);
        this.getOrCreateTagBuilder(BiomeTags.IS_MOUNTAIN).add(BiomeKeys.MEADOW).add(BiomeKeys.FROZEN_PEAKS).add(BiomeKeys.JAGGED_PEAKS).add(BiomeKeys.STONY_PEAKS).add(BiomeKeys.SNOWY_SLOPES).add(BiomeKeys.CHERRY_GROVE);
        this.getOrCreateTagBuilder(BiomeTags.IS_BADLANDS).add(BiomeKeys.BADLANDS).add(BiomeKeys.ERODED_BADLANDS).add(BiomeKeys.WOODED_BADLANDS);
        this.getOrCreateTagBuilder(BiomeTags.IS_HILL).add(BiomeKeys.WINDSWEPT_HILLS).add(BiomeKeys.WINDSWEPT_FOREST).add(BiomeKeys.WINDSWEPT_GRAVELLY_HILLS);
        this.getOrCreateTagBuilder(BiomeTags.IS_TAIGA).add(BiomeKeys.TAIGA).add(BiomeKeys.SNOWY_TAIGA).add(BiomeKeys.OLD_GROWTH_PINE_TAIGA).add(BiomeKeys.OLD_GROWTH_SPRUCE_TAIGA);
        this.getOrCreateTagBuilder(BiomeTags.IS_JUNGLE).add(BiomeKeys.BAMBOO_JUNGLE).add(BiomeKeys.JUNGLE).add(BiomeKeys.SPARSE_JUNGLE);
        this.getOrCreateTagBuilder(BiomeTags.IS_FOREST).add(BiomeKeys.FOREST).add(BiomeKeys.FLOWER_FOREST).add(BiomeKeys.BIRCH_FOREST).add(BiomeKeys.OLD_GROWTH_BIRCH_FOREST).add(BiomeKeys.DARK_FOREST).add(BiomeKeys.GROVE);
        this.getOrCreateTagBuilder(BiomeTags.IS_SAVANNA).add(BiomeKeys.SAVANNA).add(BiomeKeys.SAVANNA_PLATEAU).add(BiomeKeys.WINDSWEPT_SAVANNA);
        TagProvider.ProvidedTagBuilder<Biome> lv = this.getOrCreateTagBuilder(BiomeTags.IS_NETHER);
        MultiNoiseBiomeSourceParameterList.Preset.NETHER.biomeStream().forEach(lv::add);
        TagProvider.ProvidedTagBuilder<Biome> lv2 = this.getOrCreateTagBuilder(BiomeTags.IS_OVERWORLD);
        MultiNoiseBiomeSourceParameterList.Preset.OVERWORLD.biomeStream().forEach(lv2::add);
        this.getOrCreateTagBuilder(BiomeTags.IS_END).add(BiomeKeys.THE_END).add(BiomeKeys.END_HIGHLANDS).add(BiomeKeys.END_MIDLANDS).add(BiomeKeys.SMALL_END_ISLANDS).add(BiomeKeys.END_BARRENS);
        this.getOrCreateTagBuilder(BiomeTags.BURIED_TREASURE_HAS_STRUCTURE).addTag(BiomeTags.IS_BEACH);
        this.getOrCreateTagBuilder(BiomeTags.DESERT_PYRAMID_HAS_STRUCTURE).add(BiomeKeys.DESERT);
        this.getOrCreateTagBuilder(BiomeTags.IGLOO_HAS_STRUCTURE).add(BiomeKeys.SNOWY_TAIGA).add(BiomeKeys.SNOWY_PLAINS).add(BiomeKeys.SNOWY_SLOPES);
        this.getOrCreateTagBuilder(BiomeTags.JUNGLE_TEMPLE_HAS_STRUCTURE).add(BiomeKeys.BAMBOO_JUNGLE).add(BiomeKeys.JUNGLE);
        this.getOrCreateTagBuilder(BiomeTags.MINESHAFT_HAS_STRUCTURE).addTag(BiomeTags.IS_OCEAN).addTag(BiomeTags.IS_RIVER).addTag(BiomeTags.IS_BEACH).addTag(BiomeTags.IS_MOUNTAIN).addTag(BiomeTags.IS_HILL).addTag(BiomeTags.IS_TAIGA).addTag(BiomeTags.IS_JUNGLE).addTag(BiomeTags.IS_FOREST).add(BiomeKeys.STONY_SHORE).add(BiomeKeys.MUSHROOM_FIELDS).add(BiomeKeys.ICE_SPIKES).add(BiomeKeys.WINDSWEPT_SAVANNA).add(BiomeKeys.DESERT).add(BiomeKeys.SAVANNA).add(BiomeKeys.SNOWY_PLAINS).add(BiomeKeys.PLAINS).add(BiomeKeys.SUNFLOWER_PLAINS).add(BiomeKeys.SWAMP).add(BiomeKeys.MANGROVE_SWAMP).add(BiomeKeys.SAVANNA_PLATEAU).add(BiomeKeys.DRIPSTONE_CAVES).add(BiomeKeys.LUSH_CAVES);
        this.getOrCreateTagBuilder(BiomeTags.MINESHAFT_MESA_HAS_STRUCTURE).addTag(BiomeTags.IS_BADLANDS);
        this.getOrCreateTagBuilder(BiomeTags.MINESHAFT_BLOCKING).add(BiomeKeys.DEEP_DARK);
        this.getOrCreateTagBuilder(BiomeTags.OCEAN_MONUMENT_HAS_STRUCTURE).addTag(BiomeTags.IS_DEEP_OCEAN);
        this.getOrCreateTagBuilder(BiomeTags.REQUIRED_OCEAN_MONUMENT_SURROUNDING).addTag(BiomeTags.IS_OCEAN).addTag(BiomeTags.IS_RIVER);
        this.getOrCreateTagBuilder(BiomeTags.OCEAN_RUIN_COLD_HAS_STRUCTURE).add(BiomeKeys.FROZEN_OCEAN).add(BiomeKeys.COLD_OCEAN).add(BiomeKeys.OCEAN).add(BiomeKeys.DEEP_FROZEN_OCEAN).add(BiomeKeys.DEEP_COLD_OCEAN).add(BiomeKeys.DEEP_OCEAN);
        this.getOrCreateTagBuilder(BiomeTags.OCEAN_RUIN_WARM_HAS_STRUCTURE).add(BiomeKeys.LUKEWARM_OCEAN).add(BiomeKeys.WARM_OCEAN).add(BiomeKeys.DEEP_LUKEWARM_OCEAN);
        this.getOrCreateTagBuilder(BiomeTags.PILLAGER_OUTPOST_HAS_STRUCTURE).add(BiomeKeys.DESERT).add(BiomeKeys.PLAINS).add(BiomeKeys.SAVANNA).add(BiomeKeys.SNOWY_PLAINS).add(BiomeKeys.TAIGA).addTag(BiomeTags.IS_MOUNTAIN).add(BiomeKeys.GROVE);
        this.getOrCreateTagBuilder(BiomeTags.RUINED_PORTAL_DESERT_HAS_STRUCTURE).add(BiomeKeys.DESERT);
        this.getOrCreateTagBuilder(BiomeTags.RUINED_PORTAL_JUNGLE_HAS_STRUCTURE).addTag(BiomeTags.IS_JUNGLE);
        this.getOrCreateTagBuilder(BiomeTags.RUINED_PORTAL_OCEAN_HAS_STRUCTURE).addTag(BiomeTags.IS_OCEAN);
        this.getOrCreateTagBuilder(BiomeTags.RUINED_PORTAL_SWAMP_HAS_STRUCTURE).add(BiomeKeys.SWAMP).add(BiomeKeys.MANGROVE_SWAMP);
        this.getOrCreateTagBuilder(BiomeTags.RUINED_PORTAL_MOUNTAIN_HAS_STRUCTURE).addTag(BiomeTags.IS_BADLANDS).addTag(BiomeTags.IS_HILL).add(BiomeKeys.SAVANNA_PLATEAU).add(BiomeKeys.WINDSWEPT_SAVANNA).add(BiomeKeys.STONY_SHORE).addTag(BiomeTags.IS_MOUNTAIN);
        this.getOrCreateTagBuilder(BiomeTags.RUINED_PORTAL_STANDARD_HAS_STRUCTURE).addTag(BiomeTags.IS_BEACH).addTag(BiomeTags.IS_RIVER).addTag(BiomeTags.IS_TAIGA).addTag(BiomeTags.IS_FOREST).add(BiomeKeys.MUSHROOM_FIELDS).add(BiomeKeys.ICE_SPIKES).add(BiomeKeys.DRIPSTONE_CAVES).add(BiomeKeys.LUSH_CAVES).add(BiomeKeys.SAVANNA).add(BiomeKeys.SNOWY_PLAINS).add(BiomeKeys.PLAINS).add(BiomeKeys.SUNFLOWER_PLAINS);
        this.getOrCreateTagBuilder(BiomeTags.SHIPWRECK_BEACHED_HAS_STRUCTURE).addTag(BiomeTags.IS_BEACH);
        this.getOrCreateTagBuilder(BiomeTags.SHIPWRECK_HAS_STRUCTURE).addTag(BiomeTags.IS_OCEAN);
        this.getOrCreateTagBuilder(BiomeTags.SWAMP_HUT_HAS_STRUCTURE).add(BiomeKeys.SWAMP);
        this.getOrCreateTagBuilder(BiomeTags.VILLAGE_DESERT_HAS_STRUCTURE).add(BiomeKeys.DESERT);
        this.getOrCreateTagBuilder(BiomeTags.VILLAGE_PLAINS_HAS_STRUCTURE).add(BiomeKeys.PLAINS).add(BiomeKeys.MEADOW);
        this.getOrCreateTagBuilder(BiomeTags.VILLAGE_SAVANNA_HAS_STRUCTURE).add(BiomeKeys.SAVANNA);
        this.getOrCreateTagBuilder(BiomeTags.VILLAGE_SNOWY_HAS_STRUCTURE).add(BiomeKeys.SNOWY_PLAINS);
        this.getOrCreateTagBuilder(BiomeTags.VILLAGE_TAIGA_HAS_STRUCTURE).add(BiomeKeys.TAIGA);
        this.getOrCreateTagBuilder(BiomeTags.TRAIL_RUINS_HAS_STRUCTURE).add(BiomeKeys.TAIGA).add(BiomeKeys.SNOWY_TAIGA).add(BiomeKeys.OLD_GROWTH_PINE_TAIGA).add(BiomeKeys.OLD_GROWTH_SPRUCE_TAIGA).add(BiomeKeys.OLD_GROWTH_BIRCH_FOREST).add(BiomeKeys.JUNGLE);
        this.getOrCreateTagBuilder(BiomeTags.WOODLAND_MANSION_HAS_STRUCTURE).add(BiomeKeys.DARK_FOREST);
        this.getOrCreateTagBuilder(BiomeTags.STRONGHOLD_BIASED_TO).add(BiomeKeys.PLAINS).add(BiomeKeys.SUNFLOWER_PLAINS).add(BiomeKeys.SNOWY_PLAINS).add(BiomeKeys.ICE_SPIKES).add(BiomeKeys.DESERT).add(BiomeKeys.FOREST).add(BiomeKeys.FLOWER_FOREST).add(BiomeKeys.BIRCH_FOREST).add(BiomeKeys.DARK_FOREST).add(BiomeKeys.OLD_GROWTH_BIRCH_FOREST).add(BiomeKeys.OLD_GROWTH_PINE_TAIGA).add(BiomeKeys.OLD_GROWTH_SPRUCE_TAIGA).add(BiomeKeys.TAIGA).add(BiomeKeys.SNOWY_TAIGA).add(BiomeKeys.SAVANNA).add(BiomeKeys.SAVANNA_PLATEAU).add(BiomeKeys.WINDSWEPT_HILLS).add(BiomeKeys.WINDSWEPT_GRAVELLY_HILLS).add(BiomeKeys.WINDSWEPT_FOREST).add(BiomeKeys.WINDSWEPT_SAVANNA).add(BiomeKeys.JUNGLE).add(BiomeKeys.SPARSE_JUNGLE).add(BiomeKeys.BAMBOO_JUNGLE).add(BiomeKeys.BADLANDS).add(BiomeKeys.ERODED_BADLANDS).add(BiomeKeys.WOODED_BADLANDS).add(BiomeKeys.MEADOW).add(BiomeKeys.GROVE).add(BiomeKeys.SNOWY_SLOPES).add(BiomeKeys.FROZEN_PEAKS).add(BiomeKeys.JAGGED_PEAKS).add(BiomeKeys.STONY_PEAKS).add(BiomeKeys.MUSHROOM_FIELDS).add(BiomeKeys.DRIPSTONE_CAVES).add(BiomeKeys.LUSH_CAVES);
        this.getOrCreateTagBuilder(BiomeTags.STRONGHOLD_HAS_STRUCTURE).addTag(BiomeTags.IS_OVERWORLD);
        TagProvider.ProvidedTagBuilder<Biome> lv3 = this.getOrCreateTagBuilder(BiomeTags.TRIAL_CHAMBERS_HAS_STRUCTURE);
        MultiNoiseBiomeSourceParameterList.Preset.OVERWORLD.biomeStream().filter(Predicate.not(BiomeKeys.DEEP_DARK::equals)).forEach(lv3::add);
        this.getOrCreateTagBuilder(BiomeTags.NETHER_FORTRESS_HAS_STRUCTURE).addTag(BiomeTags.IS_NETHER);
        this.getOrCreateTagBuilder(BiomeTags.NETHER_FOSSIL_HAS_STRUCTURE).add(BiomeKeys.SOUL_SAND_VALLEY);
        this.getOrCreateTagBuilder(BiomeTags.BASTION_REMNANT_HAS_STRUCTURE).add(BiomeKeys.CRIMSON_FOREST).add(BiomeKeys.NETHER_WASTES).add(BiomeKeys.SOUL_SAND_VALLEY).add(BiomeKeys.WARPED_FOREST);
        this.getOrCreateTagBuilder(BiomeTags.ANCIENT_CITY_HAS_STRUCTURE).add(BiomeKeys.DEEP_DARK);
        this.getOrCreateTagBuilder(BiomeTags.RUINED_PORTAL_NETHER_HAS_STRUCTURE).addTag(BiomeTags.IS_NETHER);
        this.getOrCreateTagBuilder(BiomeTags.END_CITY_HAS_STRUCTURE).add(BiomeKeys.END_HIGHLANDS).add(BiomeKeys.END_MIDLANDS);
        this.getOrCreateTagBuilder(BiomeTags.PRODUCES_CORALS_FROM_BONEMEAL).add(BiomeKeys.WARM_OCEAN);
        this.getOrCreateTagBuilder(BiomeTags.PLAYS_UNDERWATER_MUSIC).addTag(BiomeTags.IS_OCEAN).addTag(BiomeTags.IS_RIVER);
        this.getOrCreateTagBuilder(BiomeTags.HAS_CLOSER_WATER_FOG).add(BiomeKeys.SWAMP).add(BiomeKeys.MANGROVE_SWAMP);
        this.getOrCreateTagBuilder(BiomeTags.WATER_ON_MAP_OUTLINES).addTag(BiomeTags.IS_OCEAN).addTag(BiomeTags.IS_RIVER).add(BiomeKeys.SWAMP).add(BiomeKeys.MANGROVE_SWAMP);
        this.getOrCreateTagBuilder(BiomeTags.WITHOUT_ZOMBIE_SIEGES).add(BiomeKeys.MUSHROOM_FIELDS);
        this.getOrCreateTagBuilder(BiomeTags.WITHOUT_PATROL_SPAWNS).add(BiomeKeys.MUSHROOM_FIELDS);
        this.getOrCreateTagBuilder(BiomeTags.WITHOUT_WANDERING_TRADER_SPAWNS).add(BiomeKeys.THE_VOID);
        this.getOrCreateTagBuilder(BiomeTags.SPAWNS_COLD_VARIANT_FROGS).add(BiomeKeys.SNOWY_PLAINS).add(BiomeKeys.ICE_SPIKES).add(BiomeKeys.FROZEN_PEAKS).add(BiomeKeys.JAGGED_PEAKS).add(BiomeKeys.SNOWY_SLOPES).add(BiomeKeys.FROZEN_OCEAN).add(BiomeKeys.DEEP_FROZEN_OCEAN).add(BiomeKeys.GROVE).add(BiomeKeys.DEEP_DARK).add(BiomeKeys.FROZEN_RIVER).add(BiomeKeys.SNOWY_TAIGA).add(BiomeKeys.SNOWY_BEACH).addTag(BiomeTags.IS_END);
        this.getOrCreateTagBuilder(BiomeTags.SPAWNS_WARM_VARIANT_FROGS).add(BiomeKeys.DESERT).add(BiomeKeys.WARM_OCEAN).addTag(BiomeTags.IS_JUNGLE).addTag(BiomeTags.IS_SAVANNA).addTag(BiomeTags.IS_NETHER).addTag(BiomeTags.IS_BADLANDS).add(BiomeKeys.MANGROVE_SWAMP);
        this.getOrCreateTagBuilder(BiomeTags.SPAWNS_GOLD_RABBITS).add(BiomeKeys.DESERT);
        this.getOrCreateTagBuilder(BiomeTags.SPAWNS_WHITE_RABBITS).add(BiomeKeys.SNOWY_PLAINS).add(BiomeKeys.ICE_SPIKES).add(BiomeKeys.FROZEN_OCEAN).add(BiomeKeys.SNOWY_TAIGA).add(BiomeKeys.FROZEN_RIVER).add(BiomeKeys.SNOWY_BEACH).add(BiomeKeys.FROZEN_PEAKS).add(BiomeKeys.JAGGED_PEAKS).add(BiomeKeys.SNOWY_SLOPES).add(BiomeKeys.GROVE);
        this.getOrCreateTagBuilder(BiomeTags.REDUCE_WATER_AMBIENT_SPAWNS).addTag(BiomeTags.IS_RIVER);
        this.getOrCreateTagBuilder(BiomeTags.ALLOWS_TROPICAL_FISH_SPAWNS_AT_ANY_HEIGHT).add(BiomeKeys.LUSH_CAVES);
        this.getOrCreateTagBuilder(BiomeTags.POLAR_BEARS_SPAWN_ON_ALTERNATE_BLOCKS).add(BiomeKeys.FROZEN_OCEAN).add(BiomeKeys.DEEP_FROZEN_OCEAN);
        this.getOrCreateTagBuilder(BiomeTags.MORE_FREQUENT_DROWNED_SPAWNS).addTag(BiomeTags.IS_RIVER);
        this.getOrCreateTagBuilder(BiomeTags.ALLOWS_SURFACE_SLIME_SPAWNS).add(BiomeKeys.SWAMP).add(BiomeKeys.MANGROVE_SWAMP);
        this.getOrCreateTagBuilder(BiomeTags.SPAWNS_SNOW_FOXES).add(BiomeKeys.SNOWY_PLAINS).add(BiomeKeys.ICE_SPIKES).add(BiomeKeys.FROZEN_OCEAN).add(BiomeKeys.SNOWY_TAIGA).add(BiomeKeys.FROZEN_RIVER).add(BiomeKeys.SNOWY_BEACH).add(BiomeKeys.FROZEN_PEAKS).add(BiomeKeys.JAGGED_PEAKS).add(BiomeKeys.SNOWY_SLOPES).add(BiomeKeys.GROVE);
        this.getOrCreateTagBuilder(BiomeTags.INCREASED_FIRE_BURNOUT).add(BiomeKeys.BAMBOO_JUNGLE).add(BiomeKeys.MUSHROOM_FIELDS).add(BiomeKeys.MANGROVE_SWAMP).add(BiomeKeys.SNOWY_SLOPES).add(BiomeKeys.FROZEN_PEAKS).add(BiomeKeys.JAGGED_PEAKS).add(BiomeKeys.SWAMP).add(BiomeKeys.JUNGLE);
        this.getOrCreateTagBuilder(BiomeTags.SNOW_GOLEM_MELTS).add(BiomeKeys.BADLANDS).add(BiomeKeys.BASALT_DELTAS).add(BiomeKeys.CRIMSON_FOREST).add(BiomeKeys.DESERT).add(BiomeKeys.ERODED_BADLANDS).add(BiomeKeys.NETHER_WASTES).add(BiomeKeys.SAVANNA).add(BiomeKeys.SAVANNA_PLATEAU).add(BiomeKeys.SOUL_SAND_VALLEY).add(BiomeKeys.WARPED_FOREST).add(BiomeKeys.WINDSWEPT_SAVANNA).add(BiomeKeys.WOODED_BADLANDS);
    }
}

