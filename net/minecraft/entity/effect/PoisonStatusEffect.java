/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

class PoisonStatusEffect
extends StatusEffect {
    protected PoisonStatusEffect(StatusEffectCategory arg, int i) {
        super(arg, i);
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (entity.getHealth() > 1.0f) {
            entity.damage(entity.getDamageSources().magic(), 1.0f);
        }
        return true;
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        int k = 25 >> amplifier;
        if (k > 0) {
            return duration % k == 0;
        }
        return true;
    }
}

