/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.recipe;

import net.minecraft.block.entity.DecoratedPotBlockEntity;
import net.minecraft.block.entity.Sherds;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.world.World;

public class CraftingDecoratedPotRecipe
extends SpecialCraftingRecipe {
    public CraftingDecoratedPotRecipe(CraftingRecipeCategory arg) {
        super(arg);
    }

    @Override
    public boolean matches(CraftingRecipeInput arg, World arg2) {
        if (!this.fits(arg.getWidth(), arg.getHeight())) {
            return false;
        }
        block3: for (int i = 0; i < arg.getSize(); ++i) {
            ItemStack lv = arg.getStackInSlot(i);
            switch (i) {
                case 1: 
                case 3: 
                case 5: 
                case 7: {
                    if (lv.isIn(ItemTags.DECORATED_POT_INGREDIENTS)) continue block3;
                    return false;
                }
                default: {
                    if (lv.isOf(Items.AIR)) continue block3;
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput arg, RegistryWrapper.WrapperLookup arg2) {
        Sherds lv = new Sherds(arg.getStackInSlot(1).getItem(), arg.getStackInSlot(3).getItem(), arg.getStackInSlot(5).getItem(), arg.getStackInSlot(7).getItem());
        return DecoratedPotBlockEntity.getStackWith(lv);
    }

    @Override
    public boolean fits(int width, int height) {
        return width == 3 && height == 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.CRAFTING_DECORATED_POT;
    }
}

