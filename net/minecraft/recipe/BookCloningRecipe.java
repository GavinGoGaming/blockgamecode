/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.recipe;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class BookCloningRecipe
extends SpecialCraftingRecipe {
    public BookCloningRecipe(CraftingRecipeCategory arg) {
        super(arg);
    }

    @Override
    public boolean matches(CraftingRecipeInput arg, World arg2) {
        int i = 0;
        ItemStack lv = ItemStack.EMPTY;
        for (int j = 0; j < arg.getSize(); ++j) {
            ItemStack lv2 = arg.getStackInSlot(j);
            if (lv2.isEmpty()) continue;
            if (lv2.isOf(Items.WRITTEN_BOOK)) {
                if (!lv.isEmpty()) {
                    return false;
                }
                lv = lv2;
                continue;
            }
            if (lv2.isOf(Items.WRITABLE_BOOK)) {
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
            if (lv2.isOf(Items.WRITTEN_BOOK)) {
                if (!lv.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                lv = lv2;
                continue;
            }
            if (lv2.isOf(Items.WRITABLE_BOOK)) {
                ++i;
                continue;
            }
            return ItemStack.EMPTY;
        }
        WrittenBookContentComponent lv3 = lv.get(DataComponentTypes.WRITTEN_BOOK_CONTENT);
        if (lv.isEmpty() || i < 1 || lv3 == null) {
            return ItemStack.EMPTY;
        }
        WrittenBookContentComponent lv4 = lv3.copy();
        if (lv4 == null) {
            return ItemStack.EMPTY;
        }
        ItemStack lv5 = lv.copyWithCount(i);
        lv5.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, lv4);
        return lv5;
    }

    @Override
    public DefaultedList<ItemStack> getRemainder(CraftingRecipeInput arg) {
        DefaultedList<ItemStack> lv = DefaultedList.ofSize(arg.getSize(), ItemStack.EMPTY);
        for (int i = 0; i < lv.size(); ++i) {
            ItemStack lv2 = arg.getStackInSlot(i);
            if (lv2.getItem().hasRecipeRemainder()) {
                lv.set(i, new ItemStack(lv2.getItem().getRecipeRemainder()));
                continue;
            }
            if (!(lv2.getItem() instanceof WrittenBookItem)) continue;
            lv.set(i, lv2.copyWithCount(1));
            break;
        }
        return lv;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.BOOK_CLONING;
    }

    @Override
    public boolean fits(int width, int height) {
        return width >= 3 && height >= 3;
    }
}

