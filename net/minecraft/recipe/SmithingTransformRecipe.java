/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.recipe;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.recipe.input.SmithingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

public class SmithingTransformRecipe
implements SmithingRecipe {
    final Ingredient template;
    final Ingredient base;
    final Ingredient addition;
    final ItemStack result;

    public SmithingTransformRecipe(Ingredient template, Ingredient base, Ingredient addition, ItemStack result) {
        this.template = template;
        this.base = base;
        this.addition = addition;
        this.result = result;
    }

    @Override
    public boolean matches(SmithingRecipeInput arg, World arg2) {
        return this.template.test(arg.template()) && this.base.test(arg.base()) && this.addition.test(arg.addition());
    }

    @Override
    public ItemStack craft(SmithingRecipeInput arg, RegistryWrapper.WrapperLookup arg2) {
        ItemStack lv = arg.base().copyComponentsToNewStack(this.result.getItem(), this.result.getCount());
        lv.applyUnvalidatedChanges(this.result.getComponentChanges());
        return lv;
    }

    @Override
    public ItemStack getResult(RegistryWrapper.WrapperLookup registriesLookup) {
        return this.result;
    }

    @Override
    public boolean testTemplate(ItemStack stack) {
        return this.template.test(stack);
    }

    @Override
    public boolean testBase(ItemStack stack) {
        return this.base.test(stack);
    }

    @Override
    public boolean testAddition(ItemStack stack) {
        return this.addition.test(stack);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SMITHING_TRANSFORM;
    }

    @Override
    public boolean isEmpty() {
        return Stream.of(this.template, this.base, this.addition).anyMatch(Ingredient::isEmpty);
    }

    public static class Serializer
    implements RecipeSerializer<SmithingTransformRecipe> {
        private static final MapCodec<SmithingTransformRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Ingredient.ALLOW_EMPTY_CODEC.fieldOf("template")).forGetter(recipe -> recipe.template), ((MapCodec)Ingredient.ALLOW_EMPTY_CODEC.fieldOf("base")).forGetter(recipe -> recipe.base), ((MapCodec)Ingredient.ALLOW_EMPTY_CODEC.fieldOf("addition")).forGetter(recipe -> recipe.addition), ((MapCodec)ItemStack.VALIDATED_CODEC.fieldOf("result")).forGetter(recipe -> recipe.result)).apply((Applicative<SmithingTransformRecipe, ?>)instance, SmithingTransformRecipe::new));
        public static final PacketCodec<RegistryByteBuf, SmithingTransformRecipe> PACKET_CODEC = PacketCodec.ofStatic(Serializer::write, Serializer::read);

        @Override
        public MapCodec<SmithingTransformRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, SmithingTransformRecipe> packetCodec() {
            return PACKET_CODEC;
        }

        private static SmithingTransformRecipe read(RegistryByteBuf buf) {
            Ingredient lv = (Ingredient)Ingredient.PACKET_CODEC.decode(buf);
            Ingredient lv2 = (Ingredient)Ingredient.PACKET_CODEC.decode(buf);
            Ingredient lv3 = (Ingredient)Ingredient.PACKET_CODEC.decode(buf);
            ItemStack lv4 = (ItemStack)ItemStack.PACKET_CODEC.decode(buf);
            return new SmithingTransformRecipe(lv, lv2, lv3, lv4);
        }

        private static void write(RegistryByteBuf buf, SmithingTransformRecipe recipe) {
            Ingredient.PACKET_CODEC.encode(buf, recipe.template);
            Ingredient.PACKET_CODEC.encode(buf, recipe.base);
            Ingredient.PACKET_CODEC.encode(buf, recipe.addition);
            ItemStack.PACKET_CODEC.encode(buf, recipe.result);
        }
    }
}

