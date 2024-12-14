/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.surfacebuilder;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.noise.NoiseParametersKeys;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;

public class VanillaSurfaceRules {
    private static final MaterialRules.MaterialRule AIR = VanillaSurfaceRules.block(Blocks.AIR);
    private static final MaterialRules.MaterialRule BEDROCK = VanillaSurfaceRules.block(Blocks.BEDROCK);
    private static final MaterialRules.MaterialRule WHITE_TERRACOTTA = VanillaSurfaceRules.block(Blocks.WHITE_TERRACOTTA);
    private static final MaterialRules.MaterialRule ORANGE_TERRACOTTA = VanillaSurfaceRules.block(Blocks.ORANGE_TERRACOTTA);
    private static final MaterialRules.MaterialRule TERRACOTTA = VanillaSurfaceRules.block(Blocks.TERRACOTTA);
    private static final MaterialRules.MaterialRule RED_SAND = VanillaSurfaceRules.block(Blocks.RED_SAND);
    private static final MaterialRules.MaterialRule RED_SANDSTONE = VanillaSurfaceRules.block(Blocks.RED_SANDSTONE);
    private static final MaterialRules.MaterialRule STONE = VanillaSurfaceRules.block(Blocks.STONE);
    private static final MaterialRules.MaterialRule DEEPSLATE = VanillaSurfaceRules.block(Blocks.DEEPSLATE);
    private static final MaterialRules.MaterialRule DIRT = VanillaSurfaceRules.block(Blocks.DIRT);
    private static final MaterialRules.MaterialRule PODZOL = VanillaSurfaceRules.block(Blocks.PODZOL);
    private static final MaterialRules.MaterialRule COARSE_DIRT = VanillaSurfaceRules.block(Blocks.COARSE_DIRT);
    private static final MaterialRules.MaterialRule MYCELIUM = VanillaSurfaceRules.block(Blocks.MYCELIUM);
    private static final MaterialRules.MaterialRule GRASS_BLOCK = VanillaSurfaceRules.block(Blocks.GRASS_BLOCK);
    private static final MaterialRules.MaterialRule CALCITE = VanillaSurfaceRules.block(Blocks.CALCITE);
    private static final MaterialRules.MaterialRule GRAVEL = VanillaSurfaceRules.block(Blocks.GRAVEL);
    private static final MaterialRules.MaterialRule SAND = VanillaSurfaceRules.block(Blocks.SAND);
    private static final MaterialRules.MaterialRule SANDSTONE = VanillaSurfaceRules.block(Blocks.SANDSTONE);
    private static final MaterialRules.MaterialRule PACKED_ICE = VanillaSurfaceRules.block(Blocks.PACKED_ICE);
    private static final MaterialRules.MaterialRule SNOW_BLOCK = VanillaSurfaceRules.block(Blocks.SNOW_BLOCK);
    private static final MaterialRules.MaterialRule MUD = VanillaSurfaceRules.block(Blocks.MUD);
    private static final MaterialRules.MaterialRule POWDER_SNOW = VanillaSurfaceRules.block(Blocks.POWDER_SNOW);
    private static final MaterialRules.MaterialRule ICE = VanillaSurfaceRules.block(Blocks.ICE);
    private static final MaterialRules.MaterialRule WATER = VanillaSurfaceRules.block(Blocks.WATER);
    private static final MaterialRules.MaterialRule LAVA = VanillaSurfaceRules.block(Blocks.LAVA);
    private static final MaterialRules.MaterialRule NETHERRACK = VanillaSurfaceRules.block(Blocks.NETHERRACK);
    private static final MaterialRules.MaterialRule SOUL_SAND = VanillaSurfaceRules.block(Blocks.SOUL_SAND);
    private static final MaterialRules.MaterialRule SOUL_SOIL = VanillaSurfaceRules.block(Blocks.SOUL_SOIL);
    private static final MaterialRules.MaterialRule BASALT = VanillaSurfaceRules.block(Blocks.BASALT);
    private static final MaterialRules.MaterialRule BLACKSTONE = VanillaSurfaceRules.block(Blocks.BLACKSTONE);
    private static final MaterialRules.MaterialRule WARPED_WART_BLOCK = VanillaSurfaceRules.block(Blocks.WARPED_WART_BLOCK);
    private static final MaterialRules.MaterialRule WARPED_NYLIUM = VanillaSurfaceRules.block(Blocks.WARPED_NYLIUM);
    private static final MaterialRules.MaterialRule NETHER_WART_BLOCK = VanillaSurfaceRules.block(Blocks.NETHER_WART_BLOCK);
    private static final MaterialRules.MaterialRule CRIMSON_NYLIUM = VanillaSurfaceRules.block(Blocks.CRIMSON_NYLIUM);
    private static final MaterialRules.MaterialRule END_STONE = VanillaSurfaceRules.block(Blocks.END_STONE);

    private static MaterialRules.MaterialRule block(Block block) {
        return MaterialRules.block(block.getDefaultState());
    }

    public static MaterialRules.MaterialRule createOverworldSurfaceRule() {
        return VanillaSurfaceRules.createDefaultRule(true, false, true);
    }

    public static MaterialRules.MaterialRule createDefaultRule(boolean surface, boolean bedrockRoof, boolean bedrockFloor) {
        MaterialRules.MaterialCondition lv = MaterialRules.aboveY(YOffset.fixed(97), 2);
        MaterialRules.MaterialCondition lv2 = MaterialRules.aboveY(YOffset.fixed(256), 0);
        MaterialRules.MaterialCondition lv3 = MaterialRules.aboveYWithStoneDepth(YOffset.fixed(63), -1);
        MaterialRules.MaterialCondition lv4 = MaterialRules.aboveYWithStoneDepth(YOffset.fixed(74), 1);
        MaterialRules.MaterialCondition lv5 = MaterialRules.aboveY(YOffset.fixed(60), 0);
        MaterialRules.MaterialCondition lv6 = MaterialRules.aboveY(YOffset.fixed(62), 0);
        MaterialRules.MaterialCondition lv7 = MaterialRules.aboveY(YOffset.fixed(63), 0);
        MaterialRules.MaterialCondition lv8 = MaterialRules.water(-1, 0);
        MaterialRules.MaterialCondition lv9 = MaterialRules.water(0, 0);
        MaterialRules.MaterialCondition lv10 = MaterialRules.waterWithStoneDepth(-6, -1);
        MaterialRules.MaterialCondition lv11 = MaterialRules.hole();
        MaterialRules.MaterialCondition lv12 = MaterialRules.biome(BiomeKeys.FROZEN_OCEAN, BiomeKeys.DEEP_FROZEN_OCEAN);
        MaterialRules.MaterialCondition lv13 = MaterialRules.steepSlope();
        MaterialRules.MaterialRule lv14 = MaterialRules.sequence(MaterialRules.condition(lv9, GRASS_BLOCK), DIRT);
        MaterialRules.MaterialRule lv15 = MaterialRules.sequence(MaterialRules.condition(MaterialRules.STONE_DEPTH_CEILING, SANDSTONE), SAND);
        MaterialRules.MaterialRule lv16 = MaterialRules.sequence(MaterialRules.condition(MaterialRules.STONE_DEPTH_CEILING, STONE), GRAVEL);
        MaterialRules.MaterialCondition lv17 = MaterialRules.biome(BiomeKeys.WARM_OCEAN, BiomeKeys.BEACH, BiomeKeys.SNOWY_BEACH);
        MaterialRules.MaterialCondition lv18 = MaterialRules.biome(BiomeKeys.DESERT);
        MaterialRules.MaterialRule lv19 = MaterialRules.sequence(MaterialRules.condition(MaterialRules.biome(BiomeKeys.STONY_PEAKS), MaterialRules.sequence(MaterialRules.condition(MaterialRules.noiseThreshold(NoiseParametersKeys.CALCITE, -0.0125, 0.0125), CALCITE), STONE)), MaterialRules.condition(MaterialRules.biome(BiomeKeys.STONY_SHORE), MaterialRules.sequence(MaterialRules.condition(MaterialRules.noiseThreshold(NoiseParametersKeys.GRAVEL, -0.05, 0.05), lv16), STONE)), MaterialRules.condition(MaterialRules.biome(BiomeKeys.WINDSWEPT_HILLS), MaterialRules.condition(VanillaSurfaceRules.surfaceNoiseThreshold(1.0), STONE)), MaterialRules.condition(lv17, lv15), MaterialRules.condition(lv18, lv15), MaterialRules.condition(MaterialRules.biome(BiomeKeys.DRIPSTONE_CAVES), STONE));
        MaterialRules.MaterialRule lv20 = MaterialRules.condition(MaterialRules.noiseThreshold(NoiseParametersKeys.POWDER_SNOW, 0.45, 0.58), MaterialRules.condition(lv9, POWDER_SNOW));
        MaterialRules.MaterialRule lv21 = MaterialRules.condition(MaterialRules.noiseThreshold(NoiseParametersKeys.POWDER_SNOW, 0.35, 0.6), MaterialRules.condition(lv9, POWDER_SNOW));
        MaterialRules.MaterialRule lv22 = MaterialRules.sequence(MaterialRules.condition(MaterialRules.biome(BiomeKeys.FROZEN_PEAKS), MaterialRules.sequence(MaterialRules.condition(lv13, PACKED_ICE), MaterialRules.condition(MaterialRules.noiseThreshold(NoiseParametersKeys.PACKED_ICE, -0.5, 0.2), PACKED_ICE), MaterialRules.condition(MaterialRules.noiseThreshold(NoiseParametersKeys.ICE, -0.0625, 0.025), ICE), MaterialRules.condition(lv9, SNOW_BLOCK))), MaterialRules.condition(MaterialRules.biome(BiomeKeys.SNOWY_SLOPES), MaterialRules.sequence(MaterialRules.condition(lv13, STONE), lv20, MaterialRules.condition(lv9, SNOW_BLOCK))), MaterialRules.condition(MaterialRules.biome(BiomeKeys.JAGGED_PEAKS), STONE), MaterialRules.condition(MaterialRules.biome(BiomeKeys.GROVE), MaterialRules.sequence(lv20, DIRT)), lv19, MaterialRules.condition(MaterialRules.biome(BiomeKeys.WINDSWEPT_SAVANNA), MaterialRules.condition(VanillaSurfaceRules.surfaceNoiseThreshold(1.75), STONE)), MaterialRules.condition(MaterialRules.biome(BiomeKeys.WINDSWEPT_GRAVELLY_HILLS), MaterialRules.sequence(MaterialRules.condition(VanillaSurfaceRules.surfaceNoiseThreshold(2.0), lv16), MaterialRules.condition(VanillaSurfaceRules.surfaceNoiseThreshold(1.0), STONE), MaterialRules.condition(VanillaSurfaceRules.surfaceNoiseThreshold(-1.0), DIRT), lv16)), MaterialRules.condition(MaterialRules.biome(BiomeKeys.MANGROVE_SWAMP), MUD), DIRT);
        MaterialRules.MaterialRule lv23 = MaterialRules.sequence(MaterialRules.condition(MaterialRules.biome(BiomeKeys.FROZEN_PEAKS), MaterialRules.sequence(MaterialRules.condition(lv13, PACKED_ICE), MaterialRules.condition(MaterialRules.noiseThreshold(NoiseParametersKeys.PACKED_ICE, 0.0, 0.2), PACKED_ICE), MaterialRules.condition(MaterialRules.noiseThreshold(NoiseParametersKeys.ICE, 0.0, 0.025), ICE), MaterialRules.condition(lv9, SNOW_BLOCK))), MaterialRules.condition(MaterialRules.biome(BiomeKeys.SNOWY_SLOPES), MaterialRules.sequence(MaterialRules.condition(lv13, STONE), lv21, MaterialRules.condition(lv9, SNOW_BLOCK))), MaterialRules.condition(MaterialRules.biome(BiomeKeys.JAGGED_PEAKS), MaterialRules.sequence(MaterialRules.condition(lv13, STONE), MaterialRules.condition(lv9, SNOW_BLOCK))), MaterialRules.condition(MaterialRules.biome(BiomeKeys.GROVE), MaterialRules.sequence(lv21, MaterialRules.condition(lv9, SNOW_BLOCK))), lv19, MaterialRules.condition(MaterialRules.biome(BiomeKeys.WINDSWEPT_SAVANNA), MaterialRules.sequence(MaterialRules.condition(VanillaSurfaceRules.surfaceNoiseThreshold(1.75), STONE), MaterialRules.condition(VanillaSurfaceRules.surfaceNoiseThreshold(-0.5), COARSE_DIRT))), MaterialRules.condition(MaterialRules.biome(BiomeKeys.WINDSWEPT_GRAVELLY_HILLS), MaterialRules.sequence(MaterialRules.condition(VanillaSurfaceRules.surfaceNoiseThreshold(2.0), lv16), MaterialRules.condition(VanillaSurfaceRules.surfaceNoiseThreshold(1.0), STONE), MaterialRules.condition(VanillaSurfaceRules.surfaceNoiseThreshold(-1.0), lv14), lv16)), MaterialRules.condition(MaterialRules.biome(BiomeKeys.OLD_GROWTH_PINE_TAIGA, BiomeKeys.OLD_GROWTH_SPRUCE_TAIGA), MaterialRules.sequence(MaterialRules.condition(VanillaSurfaceRules.surfaceNoiseThreshold(1.75), COARSE_DIRT), MaterialRules.condition(VanillaSurfaceRules.surfaceNoiseThreshold(-0.95), PODZOL))), MaterialRules.condition(MaterialRules.biome(BiomeKeys.ICE_SPIKES), MaterialRules.condition(lv9, SNOW_BLOCK)), MaterialRules.condition(MaterialRules.biome(BiomeKeys.MANGROVE_SWAMP), MUD), MaterialRules.condition(MaterialRules.biome(BiomeKeys.MUSHROOM_FIELDS), MYCELIUM), lv14);
        MaterialRules.MaterialCondition lv24 = MaterialRules.noiseThreshold(NoiseParametersKeys.SURFACE, -0.909, -0.5454);
        MaterialRules.MaterialCondition lv25 = MaterialRules.noiseThreshold(NoiseParametersKeys.SURFACE, -0.1818, 0.1818);
        MaterialRules.MaterialCondition lv26 = MaterialRules.noiseThreshold(NoiseParametersKeys.SURFACE, 0.5454, 0.909);
        MaterialRules.MaterialRule lv27 = MaterialRules.sequence(MaterialRules.condition(MaterialRules.STONE_DEPTH_FLOOR, MaterialRules.sequence(MaterialRules.condition(MaterialRules.biome(BiomeKeys.WOODED_BADLANDS), MaterialRules.condition(lv, MaterialRules.sequence(MaterialRules.condition(lv24, COARSE_DIRT), MaterialRules.condition(lv25, COARSE_DIRT), MaterialRules.condition(lv26, COARSE_DIRT), lv14))), MaterialRules.condition(MaterialRules.biome(BiomeKeys.SWAMP), MaterialRules.condition(lv6, MaterialRules.condition(MaterialRules.not(lv7), MaterialRules.condition(MaterialRules.noiseThreshold(NoiseParametersKeys.SURFACE_SWAMP, 0.0), WATER)))), MaterialRules.condition(MaterialRules.biome(BiomeKeys.MANGROVE_SWAMP), MaterialRules.condition(lv5, MaterialRules.condition(MaterialRules.not(lv7), MaterialRules.condition(MaterialRules.noiseThreshold(NoiseParametersKeys.SURFACE_SWAMP, 0.0), WATER)))))), MaterialRules.condition(MaterialRules.biome(BiomeKeys.BADLANDS, BiomeKeys.ERODED_BADLANDS, BiomeKeys.WOODED_BADLANDS), MaterialRules.sequence(MaterialRules.condition(MaterialRules.STONE_DEPTH_FLOOR, MaterialRules.sequence(MaterialRules.condition(lv2, ORANGE_TERRACOTTA), MaterialRules.condition(lv4, MaterialRules.sequence(MaterialRules.condition(lv24, TERRACOTTA), MaterialRules.condition(lv25, TERRACOTTA), MaterialRules.condition(lv26, TERRACOTTA), MaterialRules.terracottaBands())), MaterialRules.condition(lv8, MaterialRules.sequence(MaterialRules.condition(MaterialRules.STONE_DEPTH_CEILING, RED_SANDSTONE), RED_SAND)), MaterialRules.condition(MaterialRules.not(lv11), ORANGE_TERRACOTTA), MaterialRules.condition(lv10, WHITE_TERRACOTTA), lv16)), MaterialRules.condition(lv3, MaterialRules.sequence(MaterialRules.condition(lv7, MaterialRules.condition(MaterialRules.not(lv4), ORANGE_TERRACOTTA)), MaterialRules.terracottaBands())), MaterialRules.condition(MaterialRules.STONE_DEPTH_FLOOR_WITH_SURFACE_DEPTH, MaterialRules.condition(lv10, WHITE_TERRACOTTA)))), MaterialRules.condition(MaterialRules.STONE_DEPTH_FLOOR, MaterialRules.condition(lv8, MaterialRules.sequence(MaterialRules.condition(lv12, MaterialRules.condition(lv11, MaterialRules.sequence(MaterialRules.condition(lv9, AIR), MaterialRules.condition(MaterialRules.temperature(), ICE), WATER))), lv23))), MaterialRules.condition(lv10, MaterialRules.sequence(MaterialRules.condition(MaterialRules.STONE_DEPTH_FLOOR, MaterialRules.condition(lv12, MaterialRules.condition(lv11, WATER))), MaterialRules.condition(MaterialRules.STONE_DEPTH_FLOOR_WITH_SURFACE_DEPTH, lv22), MaterialRules.condition(lv17, MaterialRules.condition(MaterialRules.STONE_DEPTH_FLOOR_WITH_SURFACE_DEPTH_RANGE_6, SANDSTONE)), MaterialRules.condition(lv18, MaterialRules.condition(MaterialRules.STONE_DEPTH_FLOOR_WITH_SURFACE_DEPTH_RANGE_30, SANDSTONE)))), MaterialRules.condition(MaterialRules.STONE_DEPTH_FLOOR, MaterialRules.sequence(MaterialRules.condition(MaterialRules.biome(BiomeKeys.FROZEN_PEAKS, BiomeKeys.JAGGED_PEAKS), STONE), MaterialRules.condition(MaterialRules.biome(BiomeKeys.WARM_OCEAN, BiomeKeys.LUKEWARM_OCEAN, BiomeKeys.DEEP_LUKEWARM_OCEAN), lv15), lv16)));
        ImmutableList.Builder builder = ImmutableList.builder();
        if (bedrockRoof) {
            builder.add(MaterialRules.condition(MaterialRules.not(MaterialRules.verticalGradient("bedrock_roof", YOffset.belowTop(5), YOffset.getTop())), BEDROCK));
        }
        if (bedrockFloor) {
            builder.add(MaterialRules.condition(MaterialRules.verticalGradient("bedrock_floor", YOffset.getBottom(), YOffset.aboveBottom(5)), BEDROCK));
        }
        MaterialRules.MaterialRule lv28 = MaterialRules.condition(MaterialRules.surface(), lv27);
        builder.add(surface ? lv28 : lv27);
        builder.add(MaterialRules.condition(MaterialRules.verticalGradient("deepslate", YOffset.fixed(0), YOffset.fixed(8)), DEEPSLATE));
        return MaterialRules.sequence((MaterialRules.MaterialRule[])builder.build().toArray(MaterialRules.MaterialRule[]::new));
    }

    public static MaterialRules.MaterialRule createNetherSurfaceRule() {
        MaterialRules.MaterialCondition lv = MaterialRules.aboveY(YOffset.fixed(31), 0);
        MaterialRules.MaterialCondition lv2 = MaterialRules.aboveY(YOffset.fixed(32), 0);
        MaterialRules.MaterialCondition lv3 = MaterialRules.aboveYWithStoneDepth(YOffset.fixed(30), 0);
        MaterialRules.MaterialCondition lv4 = MaterialRules.not(MaterialRules.aboveYWithStoneDepth(YOffset.fixed(35), 0));
        MaterialRules.MaterialCondition lv5 = MaterialRules.aboveY(YOffset.belowTop(5), 0);
        MaterialRules.MaterialCondition lv6 = MaterialRules.hole();
        MaterialRules.MaterialCondition lv7 = MaterialRules.noiseThreshold(NoiseParametersKeys.SOUL_SAND_LAYER, -0.012);
        MaterialRules.MaterialCondition lv8 = MaterialRules.noiseThreshold(NoiseParametersKeys.GRAVEL_LAYER, -0.012);
        MaterialRules.MaterialCondition lv9 = MaterialRules.noiseThreshold(NoiseParametersKeys.PATCH, -0.012);
        MaterialRules.MaterialCondition lv10 = MaterialRules.noiseThreshold(NoiseParametersKeys.NETHERRACK, 0.54);
        MaterialRules.MaterialCondition lv11 = MaterialRules.noiseThreshold(NoiseParametersKeys.NETHER_WART, 1.17);
        MaterialRules.MaterialCondition lv12 = MaterialRules.noiseThreshold(NoiseParametersKeys.NETHER_STATE_SELECTOR, 0.0);
        MaterialRules.MaterialRule lv13 = MaterialRules.condition(lv9, MaterialRules.condition(lv3, MaterialRules.condition(lv4, GRAVEL)));
        return MaterialRules.sequence(MaterialRules.condition(MaterialRules.verticalGradient("bedrock_floor", YOffset.getBottom(), YOffset.aboveBottom(5)), BEDROCK), MaterialRules.condition(MaterialRules.not(MaterialRules.verticalGradient("bedrock_roof", YOffset.belowTop(5), YOffset.getTop())), BEDROCK), MaterialRules.condition(lv5, NETHERRACK), MaterialRules.condition(MaterialRules.biome(BiomeKeys.BASALT_DELTAS), MaterialRules.sequence(MaterialRules.condition(MaterialRules.STONE_DEPTH_CEILING_WITH_SURFACE_DEPTH, BASALT), MaterialRules.condition(MaterialRules.STONE_DEPTH_FLOOR_WITH_SURFACE_DEPTH, MaterialRules.sequence(lv13, MaterialRules.condition(lv12, BASALT), BLACKSTONE)))), MaterialRules.condition(MaterialRules.biome(BiomeKeys.SOUL_SAND_VALLEY), MaterialRules.sequence(MaterialRules.condition(MaterialRules.STONE_DEPTH_CEILING_WITH_SURFACE_DEPTH, MaterialRules.sequence(MaterialRules.condition(lv12, SOUL_SAND), SOUL_SOIL)), MaterialRules.condition(MaterialRules.STONE_DEPTH_FLOOR_WITH_SURFACE_DEPTH, MaterialRules.sequence(lv13, MaterialRules.condition(lv12, SOUL_SAND), SOUL_SOIL)))), MaterialRules.condition(MaterialRules.STONE_DEPTH_FLOOR, MaterialRules.sequence(MaterialRules.condition(MaterialRules.not(lv2), MaterialRules.condition(lv6, LAVA)), MaterialRules.condition(MaterialRules.biome(BiomeKeys.WARPED_FOREST), MaterialRules.condition(MaterialRules.not(lv10), MaterialRules.condition(lv, MaterialRules.sequence(MaterialRules.condition(lv11, WARPED_WART_BLOCK), WARPED_NYLIUM)))), MaterialRules.condition(MaterialRules.biome(BiomeKeys.CRIMSON_FOREST), MaterialRules.condition(MaterialRules.not(lv10), MaterialRules.condition(lv, MaterialRules.sequence(MaterialRules.condition(lv11, NETHER_WART_BLOCK), CRIMSON_NYLIUM)))))), MaterialRules.condition(MaterialRules.biome(BiomeKeys.NETHER_WASTES), MaterialRules.sequence(MaterialRules.condition(MaterialRules.STONE_DEPTH_FLOOR_WITH_SURFACE_DEPTH, MaterialRules.condition(lv7, MaterialRules.sequence(MaterialRules.condition(MaterialRules.not(lv6), MaterialRules.condition(lv3, MaterialRules.condition(lv4, SOUL_SAND))), NETHERRACK))), MaterialRules.condition(MaterialRules.STONE_DEPTH_FLOOR, MaterialRules.condition(lv, MaterialRules.condition(lv4, MaterialRules.condition(lv8, MaterialRules.sequence(MaterialRules.condition(lv2, GRAVEL), MaterialRules.condition(MaterialRules.not(lv6), GRAVEL)))))))), NETHERRACK);
    }

    public static MaterialRules.MaterialRule getEndStoneRule() {
        return END_STONE;
    }

    public static MaterialRules.MaterialRule getAirRule() {
        return AIR;
    }

    private static MaterialRules.MaterialCondition surfaceNoiseThreshold(double min) {
        return MaterialRules.noiseThreshold(NoiseParametersKeys.SURFACE, min / 8.25, Double.MAX_VALUE);
    }
}

