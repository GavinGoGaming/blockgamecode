/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.recipe;

import net.minecraft.block.Blocks;
import net.minecraft.block.SuspiciousStewIngredient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.world.World;

public class SuspiciousStewRecipe
extends SpecialCraftingRecipe {
    public SuspiciousStewRecipe(CraftingRecipeCategory arg) {
        super(arg);
    }

    @Override
    public boolean matches(CraftingRecipeInput arg, World arg2) {
        boolean bl = false;
        boolean bl2 = false;
        boolean bl3 = false;
        boolean bl4 = false;
        for (int i = 0; i < arg.getSize(); ++i) {
            ItemStack lv = arg.getStackInSlot(i);
            if (lv.isEmpty()) continue;
            if (lv.isOf(Blocks.BROWN_MUSHROOM.asItem()) && !bl3) {
                bl3 = true;
                continue;
            }
            if (lv.isOf(Blocks.RED_MUSHROOM.asItem()) && !bl2) {
                bl2 = true;
                continue;
            }
            if (lv.isIn(ItemTags.SMALL_FLOWERS) && !bl) {
                bl = true;
                continue;
            }
            if (lv.isOf(Items.BOWL) && !bl4) {
                bl4 = true;
                continue;
            }
            return false;
        }
        return bl && bl3 && bl2 && bl4;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput arg, RegistryWrapper.WrapperLookup arg2) {
        ItemStack lv = new ItemStack(Items.SUSPICIOUS_STEW, 1);
        for (int i = 0; i < arg.getSize(); ++i) {
            SuspiciousStewIngredient lv3;
            ItemStack lv2 = arg.getStackInSlot(i);
            if (lv2.isEmpty() || (lv3 = SuspiciousStewIngredient.of(lv2.getItem())) == null) continue;
            lv.set(DataComponentTypes.SUSPICIOUS_STEW_EFFECTS, lv3.getStewEffects());
            break;
        }
        return lv;
    }

    @Override
    public boolean fits(int width, int height) {
        return width >= 2 && height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SUSPICIOUS_STEW;
    }
}

