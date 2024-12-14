/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.recipe;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworkExplosionComponent;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

public class FireworkStarFadeRecipe
extends SpecialCraftingRecipe {
    private static final Ingredient INPUT_STAR = Ingredient.ofItems(Items.FIREWORK_STAR);

    public FireworkStarFadeRecipe(CraftingRecipeCategory arg) {
        super(arg);
    }

    @Override
    public boolean matches(CraftingRecipeInput arg, World arg2) {
        boolean bl = false;
        boolean bl2 = false;
        for (int i = 0; i < arg.getSize(); ++i) {
            ItemStack lv = arg.getStackInSlot(i);
            if (lv.isEmpty()) continue;
            if (lv.getItem() instanceof DyeItem) {
                bl = true;
                continue;
            }
            if (INPUT_STAR.test(lv)) {
                if (bl2) {
                    return false;
                }
                bl2 = true;
                continue;
            }
            return false;
        }
        return bl2 && bl;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput arg, RegistryWrapper.WrapperLookup arg2) {
        IntArrayList intList = new IntArrayList();
        ItemStack lv = null;
        for (int i = 0; i < arg.getSize(); ++i) {
            ItemStack lv2 = arg.getStackInSlot(i);
            Item lv3 = lv2.getItem();
            if (lv3 instanceof DyeItem) {
                intList.add(((DyeItem)lv3).getColor().getFireworkColor());
                continue;
            }
            if (!INPUT_STAR.test(lv2)) continue;
            lv = lv2.copyWithCount(1);
        }
        if (lv == null || intList.isEmpty()) {
            return ItemStack.EMPTY;
        }
        lv.apply(DataComponentTypes.FIREWORK_EXPLOSION, FireworkExplosionComponent.DEFAULT, intList, FireworkExplosionComponent::withFadeColors);
        return lv;
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.FIREWORK_STAR_FADE;
    }
}

