/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

public class MapCloningRecipe
extends SpecialCraftingRecipe {
    public MapCloningRecipe(CraftingRecipeCategory arg) {
        super(arg);
    }

    @Override
    public boolean matches(CraftingRecipeInput arg, World arg2) {
        int i = 0;
        ItemStack lv = ItemStack.EMPTY;
        for (int j = 0; j < arg.getSize(); ++j) {
            ItemStack lv2 = arg.getStackInSlot(j);
            if (lv2.isEmpty()) continue;
            if (lv2.isOf(Items.FILLED_MAP)) {
                if (!lv.isEmpty()) {
                    return false;
                }
                lv = lv2;
                continue;
            }
            if (lv2.isOf(Items.MAP)) {
                ++i;
                continue;
            }
            return false;
        }
        return !lv.isEmpty() && i > 0;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput arg, RegistryWrapper.WrapperLookup arg2) {
        int i = 0;
        ItemStack lv = ItemStack.EMPTY;
        for (int j = 0; j < arg.getSize(); ++j) {
            ItemStack lv2 = arg.getStackInSlot(j);
            if (lv2.isEmpty()) continue;
            if (lv2.isOf(Items.FILLED_MAP)) {
                if (!lv.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                lv = lv2;
                continue;
            }
            if (lv2.isOf(Items.MAP)) {
                ++i;
                continue;
            }
            return ItemStack.EMPTY;
        }
        if (lv.isEmpty() || i < 1) {
            return ItemStack.EMPTY;
        }
        return lv.copyWithCount(i + 1);
    }

    @Override
    public boolean fits(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.MAP_CLONING;
    }
}

