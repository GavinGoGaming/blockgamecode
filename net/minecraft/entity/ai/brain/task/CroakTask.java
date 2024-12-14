/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.passive.FrogEntity;
import net.minecraft.server.world.ServerWorld;

public class CroakTask
extends MultiTickTask<FrogEntity> {
    private static final int MAX_RUN_TICK = 60;
    private static final int RUN_TIME = 100;
    private int runningTicks;

    public CroakTask() {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT), 100);
    }

    @Override
    protected boolean shouldRun(ServerWorld arg, FrogEntity arg2) {
        return arg2.getPose() == EntityPose.STANDING;
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld arg, FrogEntity arg2, long l) {
        return this.runningTicks < 60;
    }

    @Override
    protected void run(ServerWorld arg, FrogEntity arg2, long l) {
        if (arg2.isInFluid()) {
            return;
        }
        arg2.setPose(EntityPose.CROAKING);
        this.runningTicks = 0;
    }

    @Override
    protected void finishRunning(ServerWorld arg, FrogEntity arg2, long l) {
        arg2.setPose(EntityPose.STANDING);
    }

    @Override
    protected void keepRunning(ServerWorld arg, FrogEntity arg2, long l) {
        ++this.runningTicks;
    }

    @Override
    protected /* synthetic */ void finishRunning(ServerWorld world, LivingEntity entity, long time) {
        this.finishRunning(world, (FrogEntity)entity, time);
    }

    @Override
    protected /* synthetic */ void keepRunning(ServerWorld world, LivingEntity entity, long time) {
        this.keepRunning(world, (FrogEntity)entity, time);
    }

    @Override
    protected /* synthetic */ void run(ServerWorld world, LivingEntity entity, long time) {
        this.run(world, (FrogEntity)entity, time);
    }
}

