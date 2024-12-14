/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.EntityLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class FollowCustomerTask
extends MultiTickTask<VillagerEntity> {
    private final float speed;

    public FollowCustomerTask(float speed) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryModuleState.REGISTERED, MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED), Integer.MAX_VALUE);
        this.speed = speed;
    }

    @Override
    protected boolean shouldRun(ServerWorld arg, VillagerEntity arg2) {
        PlayerEntity lv = arg2.getCustomer();
        return arg2.isAlive() && lv != null && !arg2.isTouchingWater() && !arg2.velocityModified && arg2.squaredDistanceTo(lv) <= 16.0 && lv.currentScreenHandler != null;
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld arg, VillagerEntity arg2, long l) {
        return this.shouldRun(arg, arg2);
    }

    @Override
    protected void run(ServerWorld arg, VillagerEntity arg2, long l) {
        this.update(arg2);
    }

    @Override
    protected void finishRunning(ServerWorld arg, VillagerEntity arg2, long l) {
        Brain<VillagerEntity> lv = arg2.getBrain();
        lv.forget(MemoryModuleType.WALK_TARGET);
        lv.forget(MemoryModuleType.LOOK_TARGET);
    }

    @Override
    protected void keepRunning(ServerWorld arg, VillagerEntity arg2, long l) {
        this.update(arg2);
    }

    @Override
    protected boolean isTimeLimitExceeded(long time) {
        return false;
    }

    private void update(VillagerEntity villager) {
        Brain<VillagerEntity> lv = villager.getBrain();
        lv.remember(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityLookTarget(villager.getCustomer(), false), this.speed, 2));
        lv.remember(MemoryModuleType.LOOK_TARGET, new EntityLookTarget(villager.getCustomer(), true));
    }

    @Override
    protected /* synthetic */ void finishRunning(ServerWorld world, LivingEntity entity, long time) {
        this.finishRunning(world, (VillagerEntity)entity, time);
    }

    @Override
    protected /* synthetic */ void keepRunning(ServerWorld world, LivingEntity entity, long time) {
        this.keepRunning(world, (VillagerEntity)entity, time);
    }

    @Override
    protected /* synthetic */ void run(ServerWorld world, LivingEntity entity, long time) {
        this.run(world, (VillagerEntity)entity, time);
    }
}

