/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.village.raid.Raid;
import net.minecraft.world.Difficulty;

class BadOmenStatusEffect
extends StatusEffect {
    protected BadOmenStatusEffect(StatusEffectCategory arg, int i) {
        super(arg, i);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        Raid lv3;
        ServerWorld lv2;
        ServerPlayerEntity lv;
        if (entity instanceof ServerPlayerEntity && !(lv = (ServerPlayerEntity)entity).isSpectator() && (lv2 = lv.getServerWorld()).getDifficulty() != Difficulty.PEACEFUL && lv2.isNearOccupiedPointOfInterest(lv.getBlockPos()) && ((lv3 = lv2.getRaidAt(lv.getBlockPos())) == null || lv3.getBadOmenLevel() < lv3.getMaxAcceptableBadOmenLevel())) {
            lv.addStatusEffect(new StatusEffectInstance(StatusEffects.RAID_OMEN, 600, amplifier));
            lv.setStartRaidPos(lv.getBlockPos());
            return false;
        }
        return true;
    }
}

