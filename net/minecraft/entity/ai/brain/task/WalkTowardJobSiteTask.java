/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.poi.PointOfInterestStorage;

public class WalkTowardJobSiteTask
extends MultiTickTask<VillagerEntity> {
    private static final int RUN_TIME = 1200;
    final float speed;

    public WalkTowardJobSiteTask(float speed) {
        super(ImmutableMap.of(MemoryModuleType.POTENTIAL_JOB_SITE, MemoryModuleState.VALUE_PRESENT), 1200);
        this.speed = speed;
    }

    @Override
    protected boolean shouldRun(ServerWorld arg, VillagerEntity arg2) {
        return arg2.getBrain().getFirstPossibleNonCoreActivity().map(activity -> activity == Activity.IDLE || activity == Activity.WORK || activity == Activity.PLAY).orElse(true);
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld arg, VillagerEntity arg2, long l) {
        return arg2.getBrain().hasMemoryModule(MemoryModuleType.POTENTIAL_JOB_SITE);
    }

    @Override
    protected void keepRunning(ServerWorld arg, VillagerEntity arg2, long l) {
        LookTargetUtil.walkTowards((LivingEntity)arg2, arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.POTENTIAL_JOB_SITE).get().pos(), this.speed, 1);
    }

    @Override
    protected void finishRunning(ServerWorld arg, VillagerEntity arg2, long l) {
        Optional<GlobalPos> optional = arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.POTENTIAL_JOB_SITE);
        optional.ifPresent(pos -> {
            BlockPos lv = pos.pos();
            ServerWorld lv2 = arg.getServer().getWorld(pos.dimension());
            if (lv2 == null) {
                return;
            }
            PointOfInterestStorage lv3 = lv2.getPointOfInterestStorage();
            if (lv3.test(lv, poiType -> true)) {
                lv3.releaseTicket(lv);
            }
            DebugInfoSender.sendPointOfInterest(arg, lv);
        });
        arg2.getBrain().forget(MemoryModuleType.POTENTIAL_JOB_SITE);
    }

    @Override
    protected /* synthetic */ void finishRunning(ServerWorld world, LivingEntity entity, long time) {
        this.finishRunning(world, (VillagerEntity)entity, time);
    }

    @Override
    protected /* synthetic */ void keepRunning(ServerWorld world, LivingEntity entity, long time) {
        this.keepRunning(world, (VillagerEntity)entity, time);
    }
}

