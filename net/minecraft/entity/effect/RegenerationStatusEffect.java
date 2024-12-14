/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

class RegenerationStatusEffect
extends StatusEffect {
    protected RegenerationStatusEffect(StatusEffectCategory arg, int i) {
        super(arg, i);
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (entity.getHealth() < entity.getMaxHealth()) {
            entity.heal(1.0f);
        }
        return true;
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        int k = 50 >> amplifier;
        if (k > 0) {
            return duration % k == 0;
        }
        return true;
    }
}

