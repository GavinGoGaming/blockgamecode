/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import java.util.function.BiPredicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;

public class RidingTask {
    public static <E extends LivingEntity> Task<E> create(int range, BiPredicate<E, Entity> alternativeRideCondition) {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryOptional(MemoryModuleType.RIDE_TARGET)).apply(context, rideTarget -> (world, entity, time) -> {
            Entity lv3;
            Entity lv = entity.getVehicle();
            Entity lv2 = context.getOptionalValue(rideTarget).orElse(null);
            if (lv == null && lv2 == null) {
                return false;
            }
            Entity entity2 = lv3 = lv == null ? lv2 : lv;
            if (!RidingTask.canRideTarget(entity, lv3, range) || alternativeRideCondition.test(entity, lv3)) {
                entity.stopRiding();
                rideTarget.forget();
                return true;
            }
            return false;
        }));
    }

    private static boolean canRideTarget(LivingEntity entity, Entity vehicle, int range) {
        return vehicle.isAlive() && vehicle.isInRange(entity, range) && vehicle.getWorld() == entity.getWorld();
    }
}

