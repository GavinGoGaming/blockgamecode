/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.recipe;

import com.mojang.datafixers.util.Pair;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class RepairItemRecipe
extends SpecialCraftingRecipe {
    public RepairItemRecipe(CraftingRecipeCategory arg) {
        super(arg);
    }

    @Nullable
    private Pair<ItemStack, ItemStack> findPair(CraftingRecipeInput input) {
        ItemStack lv = null;
        ItemStack lv2 = null;
        for (int i = 0; i < input.getSize(); ++i) {
            ItemStack lv3 = input.getStackInSlot(i);
            if (lv3.isEmpty()) continue;
            if (lv == null) {
                lv = lv3;
                continue;
            }
            if (lv2 == null) {
                lv2 = lv3;
                continue;
            }
            return null;
        }
        if (lv != null && lv2 != null && RepairItemRecipe.canCombineStacks(lv, lv2)) {
            return Pair.of(lv, lv2);
        }
        return null;
    }

    private static boolean canCombineStacks(ItemStack first, ItemStack second) {
        return second.isOf(first.getItem()) && first.getCount() == 1 && second.getCount() == 1 && first.contains(DataComponentTypes.MAX_DAMAGE) && second.contains(DataComponentTypes.MAX_DAMAGE) && first.contains(DataComponentTypes.DAMAGE) && second.contains(DataComponentTypes.DAMAGE);
    }

    @Override
    public boolean matches(CraftingRecipeInput arg, World arg2) {
        return this.findPair(arg) != null;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput arg, RegistryWrapper.WrapperLookup arg2) {
        Pair<ItemStack, ItemStack> pair = this.findPair(arg);
        if (pair == null) {
            return ItemStack.EMPTY;
        }
        ItemStack lv = pair.getFirst();
        ItemStack lv2 = pair.getSecond();
        int i = Math.max(lv.getMaxDamage(), lv2.getMaxDamage());
        int j = lv.getMaxDamage() - lv.getDamage();
        int k = lv2.getMaxDamage() - lv2.getDamage();
        int l = j + k + i * 5 / 100;
        ItemStack lv3 = new ItemStack(lv.getItem());
        lv3.set(DataComponentTypes.MAX_DAMAGE, i);
        lv3.setDamage(Math.max(i - l, 0));
        ItemEnchantmentsComponent lv4 = EnchantmentHelper.getEnchantments(lv);
        ItemEnchantmentsComponent lv5 = EnchantmentHelper.getEnchantments(lv2);
        EnchantmentHelper.apply(lv3, builder -> arg2.getWrapperOrThrow(RegistryKeys.ENCHANTMENT).streamEntries().filter(enchantment -> enchantment.isIn(EnchantmentTags.CURSE)).forEach(enchantment -> {
            int i = Math.max(lv4.getLevel((RegistryEntry<Enchantment>)enchantment), lv5.getLevel((RegistryEntry<Enchantment>)enchantment));
            if (i > 0) {
                builder.add((RegistryEntry<Enchantment>)enchantment, i);
            }
        }));
        return lv3;
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.REPAIR_ITEM;
    }
}

