/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render;

import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;

@Environment(value=EnvType.CLIENT)
public class RenderLayers {
    private static final Map<Block, RenderLayer> BLOCKS = Util.make(Maps.newHashMap(), map -> {
        RenderLayer lv = RenderLayer.getTripwire();
        map.put(Blocks.TRIPWIRE, lv);
        RenderLayer lv2 = RenderLayer.getCutoutMipped();
        map.put(Blocks.GRASS_BLOCK, lv2);
        map.put(Blocks.IRON_BARS, lv2);
        map.put(Blocks.GLASS_PANE, lv2);
        map.put(Blocks.TRIPWIRE_HOOK, lv2);
        map.put(Blocks.HOPPER, lv2);
        map.put(Blocks.CHAIN, lv2);
        map.put(Blocks.JUNGLE_LEAVES, lv2);
        map.put(Blocks.OAK_LEAVES, lv2);
        map.put(Blocks.SPRUCE_LEAVES, lv2);
        map.put(Blocks.ACACIA_LEAVES, lv2);
        map.put(Blocks.CHERRY_LEAVES, lv2);
        map.put(Blocks.BIRCH_LEAVES, lv2);
        map.put(Blocks.DARK_OAK_LEAVES, lv2);
        map.put(Blocks.AZALEA_LEAVES, lv2);
        map.put(Blocks.FLOWERING_AZALEA_LEAVES, lv2);
        map.put(Blocks.MANGROVE_ROOTS, lv2);
        map.put(Blocks.MANGROVE_LEAVES, lv2);
        RenderLayer lv3 = RenderLayer.getCutout();
        map.put(Blocks.OAK_SAPLING, lv3);
        map.put(Blocks.SPRUCE_SAPLING, lv3);
        map.put(Blocks.BIRCH_SAPLING, lv3);
        map.put(Blocks.JUNGLE_SAPLING, lv3);
        map.put(Blocks.ACACIA_SAPLING, lv3);
        map.put(Blocks.CHERRY_SAPLING, lv3);
        map.put(Blocks.DARK_OAK_SAPLING, lv3);
        map.put(Blocks.GLASS, lv3);
        map.put(Blocks.WHITE_BED, lv3);
        map.put(Blocks.ORANGE_BED, lv3);
        map.put(Blocks.MAGENTA_BED, lv3);
        map.put(Blocks.LIGHT_BLUE_BED, lv3);
        map.put(Blocks.YELLOW_BED, lv3);
        map.put(Blocks.LIME_BED, lv3);
        map.put(Blocks.PINK_BED, lv3);
        map.put(Blocks.GRAY_BED, lv3);
        map.put(Blocks.LIGHT_GRAY_BED, lv3);
        map.put(Blocks.CYAN_BED, lv3);
        map.put(Blocks.PURPLE_BED, lv3);
        map.put(Blocks.BLUE_BED, lv3);
        map.put(Blocks.BROWN_BED, lv3);
        map.put(Blocks.GREEN_BED, lv3);
        map.put(Blocks.RED_BED, lv3);
        map.put(Blocks.BLACK_BED, lv3);
        map.put(Blocks.POWERED_RAIL, lv3);
        map.put(Blocks.DETECTOR_RAIL, lv3);
        map.put(Blocks.COBWEB, lv3);
        map.put(Blocks.SHORT_GRASS, lv3);
        map.put(Blocks.FERN, lv3);
        map.put(Blocks.DEAD_BUSH, lv3);
        map.put(Blocks.SEAGRASS, lv3);
        map.put(Blocks.TALL_SEAGRASS, lv3);
        map.put(Blocks.DANDELION, lv3);
        map.put(Blocks.POPPY, lv3);
        map.put(Blocks.BLUE_ORCHID, lv3);
        map.put(Blocks.ALLIUM, lv3);
        map.put(Blocks.AZURE_BLUET, lv3);
        map.put(Blocks.RED_TULIP, lv3);
        map.put(Blocks.ORANGE_TULIP, lv3);
        map.put(Blocks.WHITE_TULIP, lv3);
        map.put(Blocks.PINK_TULIP, lv3);
        map.put(Blocks.OXEYE_DAISY, lv3);
        map.put(Blocks.CORNFLOWER, lv3);
        map.put(Blocks.WITHER_ROSE, lv3);
        map.put(Blocks.LILY_OF_THE_VALLEY, lv3);
        map.put(Blocks.BROWN_MUSHROOM, lv3);
        map.put(Blocks.RED_MUSHROOM, lv3);
        map.put(Blocks.TORCH, lv3);
        map.put(Blocks.WALL_TORCH, lv3);
        map.put(Blocks.SOUL_TORCH, lv3);
        map.put(Blocks.SOUL_WALL_TORCH, lv3);
        map.put(Blocks.FIRE, lv3);
        map.put(Blocks.SOUL_FIRE, lv3);
        map.put(Blocks.SPAWNER, lv3);
        map.put(Blocks.TRIAL_SPAWNER, lv3);
        map.put(Blocks.VAULT, lv3);
        map.put(Blocks.REDSTONE_WIRE, lv3);
        map.put(Blocks.WHEAT, lv3);
        map.put(Blocks.OAK_DOOR, lv3);
        map.put(Blocks.LADDER, lv3);
        map.put(Blocks.RAIL, lv3);
        map.put(Blocks.IRON_DOOR, lv3);
        map.put(Blocks.REDSTONE_TORCH, lv3);
        map.put(Blocks.REDSTONE_WALL_TORCH, lv3);
        map.put(Blocks.CACTUS, lv3);
        map.put(Blocks.SUGAR_CANE, lv3);
        map.put(Blocks.REPEATER, lv3);
        map.put(Blocks.OAK_TRAPDOOR, lv3);
        map.put(Blocks.SPRUCE_TRAPDOOR, lv3);
        map.put(Blocks.BIRCH_TRAPDOOR, lv3);
        map.put(Blocks.JUNGLE_TRAPDOOR, lv3);
        map.put(Blocks.ACACIA_TRAPDOOR, lv3);
        map.put(Blocks.CHERRY_TRAPDOOR, lv3);
        map.put(Blocks.DARK_OAK_TRAPDOOR, lv3);
        map.put(Blocks.CRIMSON_TRAPDOOR, lv3);
        map.put(Blocks.WARPED_TRAPDOOR, lv3);
        map.put(Blocks.MANGROVE_TRAPDOOR, lv3);
        map.put(Blocks.BAMBOO_TRAPDOOR, lv3);
        map.put(Blocks.COPPER_TRAPDOOR, lv3);
        map.put(Blocks.EXPOSED_COPPER_TRAPDOOR, lv3);
        map.put(Blocks.WEATHERED_COPPER_TRAPDOOR, lv3);
        map.put(Blocks.OXIDIZED_COPPER_TRAPDOOR, lv3);
        map.put(Blocks.WAXED_COPPER_TRAPDOOR, lv3);
        map.put(Blocks.WAXED_EXPOSED_COPPER_TRAPDOOR, lv3);
        map.put(Blocks.WAXED_WEATHERED_COPPER_TRAPDOOR, lv3);
        map.put(Blocks.WAXED_OXIDIZED_COPPER_TRAPDOOR, lv3);
        map.put(Blocks.ATTACHED_PUMPKIN_STEM, lv3);
        map.put(Blocks.ATTACHED_MELON_STEM, lv3);
        map.put(Blocks.PUMPKIN_STEM, lv3);
        map.put(Blocks.MELON_STEM, lv3);
        map.put(Blocks.VINE, lv3);
        map.put(Blocks.GLOW_LICHEN, lv3);
        map.put(Blocks.LILY_PAD, lv3);
        map.put(Blocks.NETHER_WART, lv3);
        map.put(Blocks.BREWING_STAND, lv3);
        map.put(Blocks.COCOA, lv3);
        map.put(Blocks.BEACON, lv3);
        map.put(Blocks.FLOWER_POT, lv3);
        map.put(Blocks.POTTED_OAK_SAPLING, lv3);
        map.put(Blocks.POTTED_SPRUCE_SAPLING, lv3);
        map.put(Blocks.POTTED_BIRCH_SAPLING, lv3);
        map.put(Blocks.POTTED_JUNGLE_SAPLING, lv3);
        map.put(Blocks.POTTED_ACACIA_SAPLING, lv3);
        map.put(Blocks.POTTED_CHERRY_SAPLING, lv3);
        map.put(Blocks.POTTED_DARK_OAK_SAPLING, lv3);
        map.put(Blocks.POTTED_MANGROVE_PROPAGULE, lv3);
        map.put(Blocks.POTTED_FERN, lv3);
        map.put(Blocks.POTTED_DANDELION, lv3);
        map.put(Blocks.POTTED_POPPY, lv3);
        map.put(Blocks.POTTED_BLUE_ORCHID, lv3);
        map.put(Blocks.POTTED_ALLIUM, lv3);
        map.put(Blocks.POTTED_AZURE_BLUET, lv3);
        map.put(Blocks.POTTED_RED_TULIP, lv3);
        map.put(Blocks.POTTED_ORANGE_TULIP, lv3);
        map.put(Blocks.POTTED_WHITE_TULIP, lv3);
        map.put(Blocks.POTTED_PINK_TULIP, lv3);
        map.put(Blocks.POTTED_OXEYE_DAISY, lv3);
        map.put(Blocks.POTTED_CORNFLOWER, lv3);
        map.put(Blocks.POTTED_LILY_OF_THE_VALLEY, lv3);
        map.put(Blocks.POTTED_WITHER_ROSE, lv3);
        map.put(Blocks.POTTED_RED_MUSHROOM, lv3);
        map.put(Blocks.POTTED_BROWN_MUSHROOM, lv3);
        map.put(Blocks.POTTED_DEAD_BUSH, lv3);
        map.put(Blocks.POTTED_CACTUS, lv3);
        map.put(Blocks.POTTED_AZALEA_BUSH, lv3);
        map.put(Blocks.POTTED_FLOWERING_AZALEA_BUSH, lv3);
        map.put(Blocks.POTTED_TORCHFLOWER, lv3);
        map.put(Blocks.CARROTS, lv3);
        map.put(Blocks.POTATOES, lv3);
        map.put(Blocks.COMPARATOR, lv3);
        map.put(Blocks.ACTIVATOR_RAIL, lv3);
        map.put(Blocks.IRON_TRAPDOOR, lv3);
        map.put(Blocks.SUNFLOWER, lv3);
        map.put(Blocks.LILAC, lv3);
        map.put(Blocks.ROSE_BUSH, lv3);
        map.put(Blocks.PEONY, lv3);
        map.put(Blocks.TALL_GRASS, lv3);
        map.put(Blocks.LARGE_FERN, lv3);
        map.put(Blocks.SPRUCE_DOOR, lv3);
        map.put(Blocks.BIRCH_DOOR, lv3);
        map.put(Blocks.JUNGLE_DOOR, lv3);
        map.put(Blocks.ACACIA_DOOR, lv3);
        map.put(Blocks.CHERRY_DOOR, lv3);
        map.put(Blocks.DARK_OAK_DOOR, lv3);
        map.put(Blocks.MANGROVE_DOOR, lv3);
        map.put(Blocks.BAMBOO_DOOR, lv3);
        map.put(Blocks.COPPER_DOOR, lv3);
        map.put(Blocks.EXPOSED_COPPER_DOOR, lv3);
        map.put(Blocks.WEATHERED_COPPER_DOOR, lv3);
        map.put(Blocks.OXIDIZED_COPPER_DOOR, lv3);
        map.put(Blocks.WAXED_COPPER_DOOR, lv3);
        map.put(Blocks.WAXED_EXPOSED_COPPER_DOOR, lv3);
        map.put(Blocks.WAXED_WEATHERED_COPPER_DOOR, lv3);
        map.put(Blocks.WAXED_OXIDIZED_COPPER_DOOR, lv3);
        map.put(Blocks.END_ROD, lv3);
        map.put(Blocks.CHORUS_PLANT, lv3);
        map.put(Blocks.CHORUS_FLOWER, lv3);
        map.put(Blocks.TORCHFLOWER, lv3);
        map.put(Blocks.TORCHFLOWER_CROP, lv3);
        map.put(Blocks.PITCHER_PLANT, lv3);
        map.put(Blocks.PITCHER_CROP, lv3);
        map.put(Blocks.BEETROOTS, lv3);
        map.put(Blocks.KELP, lv3);
        map.put(Blocks.KELP_PLANT, lv3);
        map.put(Blocks.TURTLE_EGG, lv3);
        map.put(Blocks.DEAD_TUBE_CORAL, lv3);
        map.put(Blocks.DEAD_BRAIN_CORAL, lv3);
        map.put(Blocks.DEAD_BUBBLE_CORAL, lv3);
        map.put(Blocks.DEAD_FIRE_CORAL, lv3);
        map.put(Blocks.DEAD_HORN_CORAL, lv3);
        map.put(Blocks.TUBE_CORAL, lv3);
        map.put(Blocks.BRAIN_CORAL, lv3);
        map.put(Blocks.BUBBLE_CORAL, lv3);
        map.put(Blocks.FIRE_CORAL, lv3);
        map.put(Blocks.HORN_CORAL, lv3);
        map.put(Blocks.DEAD_TUBE_CORAL_FAN, lv3);
        map.put(Blocks.DEAD_BRAIN_CORAL_FAN, lv3);
        map.put(Blocks.DEAD_BUBBLE_CORAL_FAN, lv3);
        map.put(Blocks.DEAD_FIRE_CORAL_FAN, lv3);
        map.put(Blocks.DEAD_HORN_CORAL_FAN, lv3);
        map.put(Blocks.TUBE_CORAL_FAN, lv3);
        map.put(Blocks.BRAIN_CORAL_FAN, lv3);
        map.put(Blocks.BUBBLE_CORAL_FAN, lv3);
        map.put(Blocks.FIRE_CORAL_FAN, lv3);
        map.put(Blocks.HORN_CORAL_FAN, lv3);
        map.put(Blocks.DEAD_TUBE_CORAL_WALL_FAN, lv3);
        map.put(Blocks.DEAD_BRAIN_CORAL_WALL_FAN, lv3);
        map.put(Blocks.DEAD_BUBBLE_CORAL_WALL_FAN, lv3);
        map.put(Blocks.DEAD_FIRE_CORAL_WALL_FAN, lv3);
        map.put(Blocks.DEAD_HORN_CORAL_WALL_FAN, lv3);
        map.put(Blocks.TUBE_CORAL_WALL_FAN, lv3);
        map.put(Blocks.BRAIN_CORAL_WALL_FAN, lv3);
        map.put(Blocks.BUBBLE_CORAL_WALL_FAN, lv3);
        map.put(Blocks.FIRE_CORAL_WALL_FAN, lv3);
        map.put(Blocks.HORN_CORAL_WALL_FAN, lv3);
        map.put(Blocks.SEA_PICKLE, lv3);
        map.put(Blocks.CONDUIT, lv3);
        map.put(Blocks.BAMBOO_SAPLING, lv3);
        map.put(Blocks.BAMBOO, lv3);
        map.put(Blocks.POTTED_BAMBOO, lv3);
        map.put(Blocks.SCAFFOLDING, lv3);
        map.put(Blocks.STONECUTTER, lv3);
        map.put(Blocks.LANTERN, lv3);
        map.put(Blocks.SOUL_LANTERN, lv3);
        map.put(Blocks.CAMPFIRE, lv3);
        map.put(Blocks.SOUL_CAMPFIRE, lv3);
        map.put(Blocks.SWEET_BERRY_BUSH, lv3);
        map.put(Blocks.WEEPING_VINES, lv3);
        map.put(Blocks.WEEPING_VINES_PLANT, lv3);
        map.put(Blocks.TWISTING_VINES, lv3);
        map.put(Blocks.TWISTING_VINES_PLANT, lv3);
        map.put(Blocks.NETHER_SPROUTS, lv3);
        map.put(Blocks.CRIMSON_FUNGUS, lv3);
        map.put(Blocks.WARPED_FUNGUS, lv3);
        map.put(Blocks.CRIMSON_ROOTS, lv3);
        map.put(Blocks.WARPED_ROOTS, lv3);
        map.put(Blocks.POTTED_CRIMSON_FUNGUS, lv3);
        map.put(Blocks.POTTED_WARPED_FUNGUS, lv3);
        map.put(Blocks.POTTED_CRIMSON_ROOTS, lv3);
        map.put(Blocks.POTTED_WARPED_ROOTS, lv3);
        map.put(Blocks.CRIMSON_DOOR, lv3);
        map.put(Blocks.WARPED_DOOR, lv3);
        map.put(Blocks.POINTED_DRIPSTONE, lv3);
        map.put(Blocks.SMALL_AMETHYST_BUD, lv3);
        map.put(Blocks.MEDIUM_AMETHYST_BUD, lv3);
        map.put(Blocks.LARGE_AMETHYST_BUD, lv3);
        map.put(Blocks.AMETHYST_CLUSTER, lv3);
        map.put(Blocks.LIGHTNING_ROD, lv3);
        map.put(Blocks.CAVE_VINES, lv3);
        map.put(Blocks.CAVE_VINES_PLANT, lv3);
        map.put(Blocks.SPORE_BLOSSOM, lv3);
        map.put(Blocks.FLOWERING_AZALEA, lv3);
        map.put(Blocks.AZALEA, lv3);
        map.put(Blocks.PINK_PETALS, lv3);
        map.put(Blocks.BIG_DRIPLEAF, lv3);
        map.put(Blocks.BIG_DRIPLEAF_STEM, lv3);
        map.put(Blocks.SMALL_DRIPLEAF, lv3);
        map.put(Blocks.HANGING_ROOTS, lv3);
        map.put(Blocks.SCULK_SENSOR, lv3);
        map.put(Blocks.CALIBRATED_SCULK_SENSOR, lv3);
        map.put(Blocks.SCULK_VEIN, lv3);
        map.put(Blocks.SCULK_SHRIEKER, lv3);
        map.put(Blocks.MANGROVE_PROPAGULE, lv3);
        map.put(Blocks.FROGSPAWN, lv3);
        map.put(Blocks.COPPER_GRATE, lv3);
        map.put(Blocks.EXPOSED_COPPER_GRATE, lv3);
        map.put(Blocks.WEATHERED_COPPER_GRATE, lv3);
        map.put(Blocks.OXIDIZED_COPPER_GRATE, lv3);
        map.put(Blocks.WAXED_COPPER_GRATE, lv3);
        map.put(Blocks.WAXED_EXPOSED_COPPER_GRATE, lv3);
        map.put(Blocks.WAXED_WEATHERED_COPPER_GRATE, lv3);
        map.put(Blocks.WAXED_OXIDIZED_COPPER_GRATE, lv3);
        RenderLayer lv4 = RenderLayer.getTranslucent();
        map.put(Blocks.ICE, lv4);
        map.put(Blocks.NETHER_PORTAL, lv4);
        map.put(Blocks.WHITE_STAINED_GLASS, lv4);
        map.put(Blocks.ORANGE_STAINED_GLASS, lv4);
        map.put(Blocks.MAGENTA_STAINED_GLASS, lv4);
        map.put(Blocks.LIGHT_BLUE_STAINED_GLASS, lv4);
        map.put(Blocks.YELLOW_STAINED_GLASS, lv4);
        map.put(Blocks.LIME_STAINED_GLASS, lv4);
        map.put(Blocks.PINK_STAINED_GLASS, lv4);
        map.put(Blocks.GRAY_STAINED_GLASS, lv4);
        map.put(Blocks.LIGHT_GRAY_STAINED_GLASS, lv4);
        map.put(Blocks.CYAN_STAINED_GLASS, lv4);
        map.put(Blocks.PURPLE_STAINED_GLASS, lv4);
        map.put(Blocks.BLUE_STAINED_GLASS, lv4);
        map.put(Blocks.BROWN_STAINED_GLASS, lv4);
        map.put(Blocks.GREEN_STAINED_GLASS, lv4);
        map.put(Blocks.RED_STAINED_GLASS, lv4);
        map.put(Blocks.BLACK_STAINED_GLASS, lv4);
        map.put(Blocks.WHITE_STAINED_GLASS_PANE, lv4);
        map.put(Blocks.ORANGE_STAINED_GLASS_PANE, lv4);
        map.put(Blocks.MAGENTA_STAINED_GLASS_PANE, lv4);
        map.put(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, lv4);
        map.put(Blocks.YELLOW_STAINED_GLASS_PANE, lv4);
        map.put(Blocks.LIME_STAINED_GLASS_PANE, lv4);
        map.put(Blocks.PINK_STAINED_GLASS_PANE, lv4);
        map.put(Blocks.GRAY_STAINED_GLASS_PANE, lv4);
        map.put(Blocks.LIGHT_GRAY_STAINED_GLASS_PANE, lv4);
        map.put(Blocks.CYAN_STAINED_GLASS_PANE, lv4);
        map.put(Blocks.PURPLE_STAINED_GLASS_PANE, lv4);
        map.put(Blocks.BLUE_STAINED_GLASS_PANE, lv4);
        map.put(Blocks.BROWN_STAINED_GLASS_PANE, lv4);
        map.put(Blocks.GREEN_STAINED_GLASS_PANE, lv4);
        map.put(Blocks.RED_STAINED_GLASS_PANE, lv4);
        map.put(Blocks.BLACK_STAINED_GLASS_PANE, lv4);
        map.put(Blocks.SLIME_BLOCK, lv4);
        map.put(Blocks.HONEY_BLOCK, lv4);
        map.put(Blocks.FROSTED_ICE, lv4);
        map.put(Blocks.BUBBLE_COLUMN, lv4);
        map.put(Blocks.TINTED_GLASS, lv4);
    });
    private static final Map<Fluid, RenderLayer> FLUIDS = Util.make(Maps.newHashMap(), map -> {
        RenderLayer lv = RenderLayer.getTranslucent();
        map.put(Fluids.FLOWING_WATER, lv);
        map.put(Fluids.WATER, lv);
    });
    private static boolean fancyGraphicsOrBetter;

    public static RenderLayer getBlockLayer(BlockState state) {
        Block lv = state.getBlock();
        if (lv instanceof LeavesBlock) {
            return fancyGraphicsOrBetter ? RenderLayer.getCutoutMipped() : RenderLayer.getSolid();
        }
        RenderLayer lv2 = BLOCKS.get(lv);
        if (lv2 != null) {
            return lv2;
        }
        return RenderLayer.getSolid();
    }

    public static RenderLayer getMovingBlockLayer(BlockState state) {
        Block lv = state.getBlock();
        if (lv instanceof LeavesBlock) {
            return fancyGraphicsOrBetter ? RenderLayer.getCutoutMipped() : RenderLayer.getSolid();
        }
        RenderLayer lv2 = BLOCKS.get(lv);
        if (lv2 != null) {
            if (lv2 == RenderLayer.getTranslucent()) {
                return RenderLayer.getTranslucentMovingBlock();
            }
            return lv2;
        }
        return RenderLayer.getSolid();
    }

    public static RenderLayer getEntityBlockLayer(BlockState state, boolean direct) {
        RenderLayer lv = RenderLayers.getBlockLayer(state);
        if (lv == RenderLayer.getTranslucent()) {
            if (!MinecraftClient.isFabulousGraphicsOrBetter()) {
                return TexturedRenderLayers.getEntityTranslucentCull();
            }
            return direct ? TexturedRenderLayers.getEntityTranslucentCull() : TexturedRenderLayers.getItemEntityTranslucentCull();
        }
        return TexturedRenderLayers.getEntityCutout();
    }

    public static RenderLayer getItemLayer(ItemStack stack, boolean direct) {
        Item lv = stack.getItem();
        if (lv instanceof BlockItem) {
            Block lv2 = ((BlockItem)lv).getBlock();
            return RenderLayers.getEntityBlockLayer(lv2.getDefaultState(), direct);
        }
        return direct ? TexturedRenderLayers.getEntityTranslucentCull() : TexturedRenderLayers.getItemEntityTranslucentCull();
    }

    public static RenderLayer getFluidLayer(FluidState state) {
        RenderLayer lv = FLUIDS.get(state.getFluid());
        if (lv != null) {
            return lv;
        }
        return RenderLayer.getSolid();
    }

    public static void setFancyGraphicsOrBetter(boolean fancyGraphicsOrBetter) {
        RenderLayers.fancyGraphicsOrBetter = fancyGraphicsOrBetter;
    }
}

