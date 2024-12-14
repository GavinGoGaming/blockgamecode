/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.recipe;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.world.World;

public class ArmorDyeRecipe
extends SpecialCraftingRecipe {
    public ArmorDyeRecipe(CraftingRecipeCategory arg) {
        super(arg);
    }

    @Override
    public boolean matches(CraftingRecipeInput arg, World arg2) {
        ItemStack lv = ItemStack.EMPTY;
        ArrayList<ItemStack> list = Lists.newArrayList();
        for (int i = 0; i < arg.getSize(); ++i) {
            ItemStack lv2 = arg.getStackInSlot(i);
            if (lv2.isEmpty()) continue;
            if (lv2.isIn(ItemTags.DYEABLE)) {
                if (!lv.isEmpty()) {
                    return false;
                }
                lv = lv2;
                continue;
            }
            if (lv2.getItem() instanceof DyeItem) {
                list.add(lv2);
                continue;
            }
            return false;
        }
        return !lv.isEmpty() && !list.isEmpty();
    }

    @Override
    public ItemStack craft(CraftingRecipeInput arg, RegistryWrapper.WrapperLookup arg2) {
        ArrayList<DyeItem> list = Lists.newArrayList();
        ItemStack lv = ItemStack.EMPTY;
        for (int i = 0; i < arg.getSize(); ++i) {
            ItemStack lv2 = arg.getStackInSlot(i);
            if (lv2.isEmpty()) continue;
            if (lv2.isIn(ItemTags.DYEABLE)) {
                if (!lv.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                lv = lv2.copy();
                continue;
            }
            Item item = lv2.getItem();
            if (item instanceof DyeItem) {
                DyeItem lv3 = (DyeItem)item;
                list.add(lv3);
                continue;
            }
            return ItemStack.EMPTY;
        }
        if (lv.isEmpty() || list.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return DyedColorComponent.setColor(lv, list);
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.ARMOR_DYE;
    }
}

