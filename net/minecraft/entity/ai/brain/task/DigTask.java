/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;

public class DigTask<E extends WardenEntity>
extends MultiTickTask<E> {
    public DigTask(int duration) {
        super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT), duration);
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld arg, E arg2, long l) {
        return ((Entity)arg2).getRemovalReason() == null;
    }

    @Override
    protected boolean shouldRun(ServerWorld arg, E arg2) {
        return ((Entity)arg2).isOnGround() || ((Entity)arg2).isTouchingWater() || ((Entity)arg2).isInLava();
    }

    @Override
    protected void run(ServerWorld arg, E arg2, long l) {
        if (((Entity)arg2).isOnGround()) {
            ((Entity)arg2).setPose(EntityPose.DIGGING);
            ((Entity)arg2).playSound(SoundEvents.ENTITY_WARDEN_DIG, 5.0f, 1.0f);
        } else {
            ((Entity)arg2).playSound(SoundEvents.ENTITY_WARDEN_AGITATED, 5.0f, 1.0f);
            this.finishRunning(arg, arg2, l);
        }
    }

    @Override
    protected void finishRunning(ServerWorld arg, E arg2, long l) {
        if (((Entity)arg2).getRemovalReason() == null) {
            ((LivingEntity)arg2).remove(Entity.RemovalReason.DISCARDED);
        }
    }

    @Override
    protected /* synthetic */ void finishRunning(ServerWorld world, LivingEntity entity, long time) {
        this.finishRunning(world, (E)((WardenEntity)entity), time);
    }

    @Override
    protected /* synthetic */ void run(ServerWorld world, LivingEntity entity, long time) {
        this.run(world, (E)((WardenEntity)entity), time);
    }
}

