/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.NearestLivingEntitiesSensor;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.mob.BreezeEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;

public class BreezeAttackablesSensor
extends NearestLivingEntitiesSensor<BreezeEntity> {
    public static final int RANGE = 24;

    @Override
    public Set<MemoryModuleType<?>> getOutputMemoryModules() {
        return ImmutableSet.copyOf(Iterables.concat(super.getOutputMemoryModules(), List.of(MemoryModuleType.NEAREST_ATTACKABLE)));
    }

    @Override
    protected void sense(ServerWorld arg, BreezeEntity arg22) {
        super.sense(arg, arg22);
        arg22.getBrain().getOptionalRegisteredMemory(MemoryModuleType.MOBS).stream().flatMap(Collection::stream).filter(EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR).filter(arg2 -> Sensor.testAttackableTargetPredicate(arg22, arg2)).findFirst().ifPresentOrElse(arg2 -> arg22.getBrain().remember(MemoryModuleType.NEAREST_ATTACKABLE, arg2), () -> arg22.getBrain().forget(MemoryModuleType.NEAREST_ATTACKABLE));
    }

    @Override
    protected int getHorizontalExpansion() {
        return 24;
    }

    @Override
    protected int getHeightExpansion() {
        return 24;
    }
}

