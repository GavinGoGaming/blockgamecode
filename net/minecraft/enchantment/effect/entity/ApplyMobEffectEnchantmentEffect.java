/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.enchantment.effect.entity;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public record ApplyMobEffectEnchantmentEffect(RegistryEntryList<StatusEffect> toApply, EnchantmentLevelBasedValue minDuration, EnchantmentLevelBasedValue maxDuration, EnchantmentLevelBasedValue minAmplifier, EnchantmentLevelBasedValue maxAmplifier) implements EnchantmentEntityEffect
{
    public static final MapCodec<ApplyMobEffectEnchantmentEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)RegistryCodecs.entryList(RegistryKeys.STATUS_EFFECT).fieldOf("to_apply")).forGetter(ApplyMobEffectEnchantmentEffect::toApply), ((MapCodec)EnchantmentLevelBasedValue.CODEC.fieldOf("min_duration")).forGetter(ApplyMobEffectEnchantmentEffect::minDuration), ((MapCodec)EnchantmentLevelBasedValue.CODEC.fieldOf("max_duration")).forGetter(ApplyMobEffectEnchantmentEffect::maxDuration), ((MapCodec)EnchantmentLevelBasedValue.CODEC.fieldOf("min_amplifier")).forGetter(ApplyMobEffectEnchantmentEffect::minAmplifier), ((MapCodec)EnchantmentLevelBasedValue.CODEC.fieldOf("max_amplifier")).forGetter(ApplyMobEffectEnchantmentEffect::maxAmplifier)).apply((Applicative<ApplyMobEffectEnchantmentEffect, ?>)instance, ApplyMobEffectEnchantmentEffect::new));

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos) {
        LivingEntity lv;
        Random lv2;
        Optional<RegistryEntry<StatusEffect>> optional;
        if (user instanceof LivingEntity && (optional = this.toApply.getRandom(lv2 = (lv = (LivingEntity)user).getRandom())).isPresent()) {
            int j = Math.round(MathHelper.nextBetween(lv2, this.minDuration.getValue(level), this.maxDuration.getValue(level)) * 20.0f);
            int k = Math.max(0, Math.round(MathHelper.nextBetween(lv2, this.minAmplifier.getValue(level), this.maxAmplifier.getValue(level))));
            lv.addStatusEffect(new StatusEffectInstance(optional.get(), j, k));
        }
    }

    public MapCodec<ApplyMobEffectEnchantmentEffect> getCodec() {
        return CODEC;
    }
}

