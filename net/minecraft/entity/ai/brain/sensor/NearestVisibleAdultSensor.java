/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.Set;
import net.minecraft.entity.ai.brain.LivingTargetCache;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.server.world.ServerWorld;

public class NearestVisibleAdultSensor
extends Sensor<PassiveEntity> {
    @Override
    public Set<MemoryModuleType<?>> getOutputMemoryModules() {
        return ImmutableSet.of(MemoryModuleType.NEAREST_VISIBLE_ADULT, MemoryModuleType.VISIBLE_MOBS);
    }

    @Override
    protected void sense(ServerWorld arg, PassiveEntity arg22) {
        arg22.getBrain().getOptionalRegisteredMemory(MemoryModuleType.VISIBLE_MOBS).ifPresent(arg2 -> this.findNearestVisibleAdult(arg22, (LivingTargetCache)arg2));
    }

    private void findNearestVisibleAdult(PassiveEntity entity, LivingTargetCache livingTarget) {
        Optional<PassiveEntity> optional = livingTarget.findFirst(arg2 -> arg2.getType() == entity.getType() && !arg2.isBaby()).map(PassiveEntity.class::cast);
        entity.getBrain().remember(MemoryModuleType.NEAREST_VISIBLE_ADULT, optional);
    }
}

