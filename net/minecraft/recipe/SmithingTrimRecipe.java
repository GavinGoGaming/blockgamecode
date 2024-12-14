/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.recipe;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.item.trim.ArmorTrimMaterial;
import net.minecraft.item.trim.ArmorTrimMaterials;
import net.minecraft.item.trim.ArmorTrimPattern;
import net.minecraft.item.trim.ArmorTrimPatterns;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.recipe.input.SmithingRecipeInput;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;

public class SmithingTrimRecipe
implements SmithingRecipe {
    final Ingredient template;
    final Ingredient base;
    final Ingredient addition;

    public SmithingTrimRecipe(Ingredient template, Ingredient base, Ingredient addition) {
        this.template = template;
        this.base = base;
        this.addition = addition;
    }

    @Override
    public boolean matches(SmithingRecipeInput arg, World arg2) {
        return this.template.test(arg.template()) && this.base.test(arg.base()) && this.addition.test(arg.addition());
    }

    @Override
    public ItemStack craft(SmithingRecipeInput arg, RegistryWrapper.WrapperLookup arg2) {
        ItemStack lv = arg.base();
        if (this.base.test(lv)) {
            Optional<RegistryEntry.Reference<ArmorTrimMaterial>> optional = ArmorTrimMaterials.get(arg2, arg.addition());
            Optional<RegistryEntry.Reference<ArmorTrimPattern>> optional2 = ArmorTrimPatterns.get(arg2, arg.template());
            if (optional.isPresent() && optional2.isPresent()) {
                ArmorTrim lv2 = lv.get(DataComponentTypes.TRIM);
                if (lv2 != null && lv2.equals((RegistryEntry<ArmorTrimPattern>)optional2.get(), (RegistryEntry<ArmorTrimMaterial>)optional.get())) {
                    return ItemStack.EMPTY;
                }
                ItemStack lv3 = lv.copyWithCount(1);
                lv3.set(DataComponentTypes.TRIM, new ArmorTrim((RegistryEntry<ArmorTrimMaterial>)optional.get(), (RegistryEntry<ArmorTrimPattern>)optional2.get()));
                return lv3;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getResult(RegistryWrapper.WrapperLookup registriesLookup) {
        ItemStack lv = new ItemStack(Items.IRON_CHESTPLATE);
        Optional optional = registriesLookup.getWrapperOrThrow(RegistryKeys.TRIM_PATTERN).streamEntries().findFirst();
        Optional<RegistryEntry.Reference<ArmorTrimMaterial>> optional2 = registriesLookup.getWrapperOrThrow(RegistryKeys.TRIM_MATERIAL).getOptional(ArmorTrimMaterials.REDSTONE);
        if (optional.isPresent() && optional2.isPresent()) {
            lv.set(DataComponentTypes.TRIM, new ArmorTrim((RegistryEntry<ArmorTrimMaterial>)optional2.get(), optional.get()));
        }
        return lv;
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
        return RecipeSerializer.SMITHING_TRIM;
    }

    @Override
    public boolean isEmpty() {
        return Stream.of(this.template, this.base, this.addition).anyMatch(Ingredient::isEmpty);
    }

    public static class Serializer
    implements RecipeSerializer<SmithingTrimRecipe> {
        private static final MapCodec<SmithingTrimRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Ingredient.ALLOW_EMPTY_CODEC.fieldOf("template")).forGetter(recipe -> recipe.template), ((MapCodec)Ingredient.ALLOW_EMPTY_CODEC.fieldOf("base")).forGetter(recipe -> recipe.base), ((MapCodec)Ingredient.ALLOW_EMPTY_CODEC.fieldOf("addition")).forGetter(recipe -> recipe.addition)).apply((Applicative<SmithingTrimRecipe, ?>)instance, SmithingTrimRecipe::new));
        public static final PacketCodec<RegistryByteBuf, SmithingTrimRecipe> PACKET_CODEC = PacketCodec.ofStatic(Serializer::write, Serializer::read);

        @Override
        public MapCodec<SmithingTrimRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, SmithingTrimRecipe> packetCodec() {
            return PACKET_CODEC;
        }

        private static SmithingTrimRecipe read(RegistryByteBuf buf) {
            Ingredient lv = (Ingredient)Ingredient.PACKET_CODEC.decode(buf);
            Ingredient lv2 = (Ingredient)Ingredient.PACKET_CODEC.decode(buf);
            Ingredient lv3 = (Ingredient)Ingredient.PACKET_CODEC.decode(buf);
            return new SmithingTrimRecipe(lv, lv2, lv3);
        }

        private static void write(RegistryByteBuf buf, SmithingTrimRecipe recipe) {
            Ingredient.PACKET_CODEC.encode(buf, recipe.template);
            Ingredient.PACKET_CODEC.encode(buf, recipe.base);
            Ingredient.PACKET_CODEC.encode(buf, recipe.addition);
        }
    }
}

