/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.recipe;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

public class TippedArrowRecipe
extends SpecialCraftingRecipe {
    public TippedArrowRecipe(CraftingRecipeCategory arg) {
        super(arg);
    }

    @Override
    public boolean matches(CraftingRecipeInput arg, World arg2) {
        if (arg.getWidth() != 3 || arg.getHeight() != 3) {
            return false;
        }
        for (int i = 0; i < arg.getHeight(); ++i) {
            for (int j = 0; j < arg.getWidth(); ++j) {
                ItemStack lv = arg.getStackInSlot(j, i);
                if (lv.isEmpty()) {
                    return false;
                }
                if (!(j == 1 && i == 1 ? !lv.isOf(Items.LINGERING_POTION) : !lv.isOf(Items.ARROW))) continue;
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput arg, RegistryWrapper.WrapperLookup arg2) {
        ItemStack lv = arg.getStackInSlot(1, 1);
        if (!lv.isOf(Items.LINGERING_POTION)) {
            return ItemStack.EMPTY;
        }
        ItemStack lv2 = new ItemStack(Items.TIPPED_ARROW, 8);
        lv2.set(DataComponentTypes.POTION_CONTENTS, lv.get(DataComponentTypes.POTION_CONTENTS));
        return lv2;
    }

    @Override
    public boolean fits(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.TIPPED_ARROW;
    }
}

