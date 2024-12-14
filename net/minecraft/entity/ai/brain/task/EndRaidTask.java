/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.village.raid.Raid;

public class EndRaidTask {
    public static Task<LivingEntity> create() {
        return TaskTriggerer.task(context -> context.point((world, entity, time) -> {
            if (world.random.nextInt(20) != 0) {
                return false;
            }
            Brain<?> lv = entity.getBrain();
            Raid lv2 = world.getRaidAt(entity.getBlockPos());
            if (lv2 == null || lv2.hasStopped() || lv2.hasLost()) {
                lv.setDefaultActivity(Activity.IDLE);
                lv.refreshActivities(world.getTimeOfDay(), world.getTime());
            }
            return true;
        }));
    }
}

