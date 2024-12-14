/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.LivingTargetCache;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.mob.AbstractPiglinEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.server.world.ServerWorld;

public class PiglinBruteSpecificSensor
extends Sensor<LivingEntity> {
    @Override
    public Set<MemoryModuleType<?>> getOutputMemoryModules() {
        return ImmutableSet.of(MemoryModuleType.VISIBLE_MOBS, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.NEARBY_ADULT_PIGLINS);
    }

    @Override
    protected void sense(ServerWorld world, LivingEntity entity) {
        Brain<?> lv = entity.getBrain();
        ArrayList<AbstractPiglinEntity> list = Lists.newArrayList();
        LivingTargetCache lv2 = lv.getOptionalRegisteredMemory(MemoryModuleType.VISIBLE_MOBS).orElse(LivingTargetCache.empty());
        Optional<MobEntity> optional = lv2.findFirst(arg -> arg instanceof WitherSkeletonEntity || arg instanceof WitherEntity).map(MobEntity.class::cast);
        List list2 = lv.getOptionalRegisteredMemory(MemoryModuleType.MOBS).orElse(ImmutableList.of());
        for (LivingEntity lv3 : list2) {
            if (!(lv3 instanceof AbstractPiglinEntity) || !((AbstractPiglinEntity)lv3).isAdult()) continue;
            list.add((AbstractPiglinEntity)lv3);
        }
        lv.remember(MemoryModuleType.NEAREST_VISIBLE_NEMESIS, optional);
        lv.remember(MemoryModuleType.NEARBY_ADULT_PIGLINS, list);
    }
}

