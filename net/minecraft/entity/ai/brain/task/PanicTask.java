/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;

public class PanicTask
extends MultiTickTask<VillagerEntity> {
    public PanicTask() {
        super(ImmutableMap.of());
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld arg, VillagerEntity arg2, long l) {
        return PanicTask.wasHurt(arg2) || PanicTask.isHostileNearby(arg2);
    }

    @Override
    protected void run(ServerWorld arg, VillagerEntity arg2, long l) {
        if (PanicTask.wasHurt(arg2) || PanicTask.isHostileNearby(arg2)) {
            Brain<VillagerEntity> lv = arg2.getBrain();
            if (!lv.hasActivity(Activity.PANIC)) {
                lv.forget(MemoryModuleType.PATH);
                lv.forget(MemoryModuleType.WALK_TARGET);
                lv.forget(MemoryModuleType.LOOK_TARGET);
                lv.forget(MemoryModuleType.BREED_TARGET);
                lv.forget(MemoryModuleType.INTERACTION_TARGET);
            }
            lv.doExclusively(Activity.PANIC);
        }
    }

    @Override
    protected void keepRunning(ServerWorld arg, VillagerEntity arg2, long l) {
        if (l % 100L == 0L) {
            arg2.summonGolem(arg, l, 3);
        }
    }

    public static boolean isHostileNearby(LivingEntity entity) {
        return entity.getBrain().hasMemoryModule(MemoryModuleType.NEAREST_HOSTILE);
    }

    public static boolean wasHurt(LivingEntity entity) {
        return entity.getBrain().hasMemoryModule(MemoryModuleType.HURT_BY);
    }

    @Override
    protected /* synthetic */ void run(ServerWorld world, LivingEntity entity, long time) {
        this.run(world, (VillagerEntity)entity, time);
    }
}

