/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.enchantment.effect.value;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.EnchantmentValueEffect;
import net.minecraft.util.math.random.Random;

public record RemoveBinomialEnchantmentEffect(EnchantmentLevelBasedValue chance) implements EnchantmentValueEffect
{
    public static final MapCodec<RemoveBinomialEnchantmentEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)EnchantmentLevelBasedValue.CODEC.fieldOf("chance")).forGetter(RemoveBinomialEnchantmentEffect::chance)).apply((Applicative<RemoveBinomialEnchantmentEffect, ?>)instance, RemoveBinomialEnchantmentEffect::new));

    @Override
    public float apply(int level, Random random, float inputValue) {
        float g = this.chance.getValue(level);
        int j = 0;
        int k = 0;
        while ((float)k < inputValue) {
            if (random.nextFloat() < g) {
                ++j;
            }
            ++k;
        }
        return inputValue - (float)j;
    }

    public MapCodec<RemoveBinomialEnchantmentEffect> getCodec() {
        return CODEC;
    }
}

