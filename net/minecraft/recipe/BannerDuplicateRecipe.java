/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.recipe;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.item.BannerItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.DyeColor;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class BannerDuplicateRecipe
extends SpecialCraftingRecipe {
    public BannerDuplicateRecipe(CraftingRecipeCategory arg) {
        super(arg);
    }

    @Override
    public boolean matches(CraftingRecipeInput arg, World arg2) {
        DyeColor lv = null;
        ItemStack lv2 = null;
        ItemStack lv3 = null;
        for (int i = 0; i < arg.getSize(); ++i) {
            ItemStack lv4 = arg.getStackInSlot(i);
            if (lv4.isEmpty()) continue;
            Item lv5 = lv4.getItem();
            if (!(lv5 instanceof BannerItem)) {
                return false;
            }
            BannerItem lv6 = (BannerItem)lv5;
            if (lv == null) {
                lv = lv6.getColor();
            } else if (lv != lv6.getColor()) {
                return false;
            }
            int j = lv4.getOrDefault(DataComponentTypes.BANNER_PATTERNS, BannerPatternsComponent.DEFAULT).layers().size();
            if (j > 6) {
                return false;
            }
            if (j > 0) {
                if (lv2 == null) {
                    lv2 = lv4;
                    continue;
                }
                return false;
            }
            if (lv3 == null) {
                lv3 = lv4;
                continue;
            }
            return false;
        }
        return lv2 != null && lv3 != null;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput arg, RegistryWrapper.WrapperLookup arg2) {
        for (int i = 0; i < arg.getSize(); ++i) {
            int j;
            ItemStack lv = arg.getStackInSlot(i);
            if (lv.isEmpty() || (j = lv.getOrDefault(DataComponentTypes.BANNER_PATTERNS, BannerPatternsComponent.DEFAULT).layers().size()) <= 0 || j > 6) continue;
            return lv.copyWithCount(1);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public DefaultedList<ItemStack> getRemainder(CraftingRecipeInput arg) {
        DefaultedList<ItemStack> lv = DefaultedList.ofSize(arg.getSize(), ItemStack.EMPTY);
        for (int i = 0; i < lv.size(); ++i) {
            ItemStack lv2 = arg.getStackInSlot(i);
            if (lv2.isEmpty()) continue;
            if (lv2.getItem().hasRecipeRemainder()) {
                lv.set(i, new ItemStack(lv2.getItem().getRecipeRemainder()));
                continue;
            }
            if (lv2.getOrDefault(DataComponentTypes.BANNER_PATTERNS, BannerPatternsComponent.DEFAULT).layers().isEmpty()) continue;
            lv.set(i, lv2.copyWithCount(1));
        }
        return lv;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.BANNER_DUPLICATE;
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }
}

