/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.color.item;

import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColorProvider;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.FireworkExplosionComponent;
import net.minecraft.component.type.MapColorComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.util.collection.IdList;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.world.biome.FoliageColors;
import net.minecraft.world.biome.GrassColors;

@Environment(value=EnvType.CLIENT)
public class ItemColors {
    private static final int NO_COLOR = -1;
    private final IdList<ItemColorProvider> providers = new IdList(32);

    public static ItemColors create(BlockColors blockColors) {
        ItemColors lv = new ItemColors();
        lv.register((stack, tintIndex) -> tintIndex > 0 ? -1 : DyedColorComponent.getColor(stack, -6265536), Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS, Items.LEATHER_HORSE_ARMOR);
        lv.register((stack, tintIndex) -> tintIndex != 1 ? -1 : DyedColorComponent.getColor(stack, 0), Items.WOLF_ARMOR);
        lv.register((stack, tintIndex) -> GrassColors.getColor(0.5, 1.0), Blocks.TALL_GRASS, Blocks.LARGE_FERN);
        lv.register((stack, tintIndex) -> {
            if (tintIndex != 1) {
                return -1;
            }
            FireworkExplosionComponent lv = stack.get(DataComponentTypes.FIREWORK_EXPLOSION);
            IntList intList = lv != null ? lv.colors() : IntList.of();
            int j = intList.size();
            if (j == 0) {
                return -7697782;
            }
            if (j == 1) {
                return ColorHelper.Argb.fullAlpha(intList.getInt(0));
            }
            int k = 0;
            int l = 0;
            int m = 0;
            for (int n = 0; n < j; ++n) {
                int o = intList.getInt(n);
                k += ColorHelper.Argb.getRed(o);
                l += ColorHelper.Argb.getGreen(o);
                m += ColorHelper.Argb.getBlue(o);
            }
            return ColorHelper.Argb.getArgb(k / j, l / j, m / j);
        }, Items.FIREWORK_STAR);
        lv.register((stack, tintIndex) -> {
            if (tintIndex > 0) {
                return -1;
            }
            return ColorHelper.Argb.fullAlpha(stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT).getColor());
        }, Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION, Items.TIPPED_ARROW);
        for (SpawnEggItem lv2 : SpawnEggItem.getAll()) {
            lv.register((stack, tintIndex) -> ColorHelper.Argb.fullAlpha(lv2.getColor(tintIndex)), lv2);
        }
        lv.register((stack, tintIndex) -> {
            BlockState lv = ((BlockItem)stack.getItem()).getBlock().getDefaultState();
            return blockColors.getColor(lv, null, null, tintIndex);
        }, Blocks.GRASS_BLOCK, Blocks.SHORT_GRASS, Blocks.FERN, Blocks.VINE, Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES, Blocks.BIRCH_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.LILY_PAD);
        lv.register((stack, tintIndex) -> FoliageColors.getMangroveColor(), Blocks.MANGROVE_LEAVES);
        lv.register((stack, tintIndex) -> tintIndex == 0 ? -1 : ColorHelper.Argb.fullAlpha(stack.getOrDefault(DataComponentTypes.MAP_COLOR, MapColorComponent.DEFAULT).rgb()), Items.FILLED_MAP);
        return lv;
    }

    public int getColor(ItemStack item, int tintIndex) {
        ItemColorProvider lv = this.providers.get(Registries.ITEM.getRawId(item.getItem()));
        return lv == null ? -1 : lv.getColor(item, tintIndex);
    }

    public void register(ItemColorProvider provider, ItemConvertible ... items) {
        for (ItemConvertible lv : items) {
            this.providers.set(provider, Item.getRawId(lv.asItem()));
        }
    }
}

