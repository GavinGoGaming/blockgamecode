/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.recipe;

import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

public class ShulkerBoxColoringRecipe
extends SpecialCraftingRecipe {
    public ShulkerBoxColoringRecipe(CraftingRecipeCategory arg) {
        super(arg);
    }

    @Override
    public boolean matches(CraftingRecipeInput arg, World arg2) {
        int i = 0;
        int j = 0;
        for (int k = 0; k < arg.getSize(); ++k) {
            ItemStack lv = arg.getStackInSlot(k);
            if (lv.isEmpty()) continue;
            if (Block.getBlockFromItem(lv.getItem()) instanceof ShulkerBoxBlock) {
                ++i;
            } else if (lv.getItem() instanceof DyeItem) {
                ++j;
            } else {
                return false;
            }
            if (j <= 1 && i <= 1) continue;
            return false;
        }
        return i == 1 && j == 1;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput arg, RegistryWrapper.WrapperLookup arg2) {
        ItemStack lv = ItemStack.EMPTY;
        DyeItem lv2 = (DyeItem)Items.WHITE_DYE;
        for (int i = 0; i < arg.getSize(); ++i) {
            ItemStack lv3 = arg.getStackInSlot(i);
            if (lv3.isEmpty()) continue;
            Item lv4 = lv3.getItem();
            if (Block.getBlockFromItem(lv4) instanceof ShulkerBoxBlock) {
                lv = lv3;
                continue;
            }
            if (!(lv4 instanceof DyeItem)) continue;
            lv2 = (DyeItem)lv4;
        }
        Block lv5 = ShulkerBoxBlock.get(lv2.getColor());
        return lv.copyComponentsToNewStack(lv5, 1);
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SHULKER_BOX;
    }
}

