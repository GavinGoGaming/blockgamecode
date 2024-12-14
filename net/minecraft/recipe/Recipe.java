/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.recipe;

import com.mojang.serialization.Codec;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public interface Recipe<T extends RecipeInput> {
    public static final Codec<Recipe<?>> CODEC = Registries.RECIPE_SERIALIZER.getCodec().dispatch(Recipe::getSerializer, RecipeSerializer::codec);
    public static final PacketCodec<RegistryByteBuf, Recipe<?>> PACKET_CODEC = PacketCodecs.registryValue(RegistryKeys.RECIPE_SERIALIZER).dispatch(Recipe::getSerializer, RecipeSerializer::packetCodec);

    public boolean matches(T var1, World var2);

    public ItemStack craft(T var1, RegistryWrapper.WrapperLookup var2);

    public boolean fits(int var1, int var2);

    public ItemStack getResult(RegistryWrapper.WrapperLookup var1);

    default public DefaultedList<ItemStack> getRemainder(T input) {
        DefaultedList<ItemStack> lv = DefaultedList.ofSize(input.getSize(), ItemStack.EMPTY);
        for (int i = 0; i < lv.size(); ++i) {
            Item lv2 = input.getStackInSlot(i).getItem();
            if (!lv2.hasRecipeRemainder()) continue;
            lv.set(i, new ItemStack(lv2.getRecipeRemainder()));
        }
        return lv;
    }

    default public DefaultedList<Ingredient> getIngredients() {
        return DefaultedList.of();
    }

    default public boolean isIgnoredInRecipeBook() {
        return false;
    }

    default public boolean showNotification() {
        return true;
    }

    default public String getGroup() {
        return "";
    }

    default public ItemStack createIcon() {
        return new ItemStack(Blocks.CRAFTING_TABLE);
    }

    public RecipeSerializer<?> getSerializer();

    public RecipeType<?> getType();

    default public boolean isEmpty() {
        DefaultedList<Ingredient> lv = this.getIngredients();
        return lv.isEmpty() || lv.stream().anyMatch(ingredient -> ingredient.getMatchingStacks().length == 0);
    }
}

