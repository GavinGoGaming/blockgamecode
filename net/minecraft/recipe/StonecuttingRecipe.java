/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.recipe;

import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CuttingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.world.World;

public class StonecuttingRecipe
extends CuttingRecipe {
    public StonecuttingRecipe(String group, Ingredient ingredient, ItemStack result) {
        super(RecipeType.STONECUTTING, RecipeSerializer.STONECUTTING, group, ingredient, result);
    }

    @Override
    public boolean matches(SingleStackRecipeInput arg, World arg2) {
        return this.ingredient.test(arg.item());
    }

    @Override
    public ItemStack createIcon() {
        return new ItemStack(Blocks.STONECUTTER);
    }
}

