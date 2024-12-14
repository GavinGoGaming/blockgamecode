/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import java.util.function.Predicate;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.EntityLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;

public class WalkToNearestVisibleWantedItemTask {
    public static Task<LivingEntity> create(float speed, boolean requiresWalkTarget, int radius) {
        return WalkToNearestVisibleWantedItemTask.create(entity -> true, speed, requiresWalkTarget, radius);
    }

    public static <E extends LivingEntity> Task<E> create(Predicate<E> startCondition, float speed, boolean requiresWalkTarget, int radius) {
        return TaskTriggerer.task(context -> {
            TaskTriggerer lv = requiresWalkTarget ? context.queryMemoryOptional(MemoryModuleType.WALK_TARGET) : context.queryMemoryAbsent(MemoryModuleType.WALK_TARGET);
            return context.group(context.queryMemoryOptional(MemoryModuleType.LOOK_TARGET), lv, context.queryMemoryValue(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM), context.queryMemoryOptional(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS)).apply(context, (lookTarget, walkTarget, nearestVisibleWantedItem, itemPickupCooldownTicks) -> (world, entity, time) -> {
                ItemEntity lv = (ItemEntity)context.getValue(nearestVisibleWantedItem);
                if (context.getOptionalValue(itemPickupCooldownTicks).isEmpty() && startCondition.test(entity) && lv.isInRange(entity, radius) && entity.getWorld().getWorldBorder().contains(lv.getBlockPos())) {
                    WalkTarget lv2 = new WalkTarget(new EntityLookTarget(lv, false), speed, 0);
                    lookTarget.remember(new EntityLookTarget(lv, true));
                    walkTarget.remember(lv2);
                    return true;
                }
                return false;
            });
        });
    }
}

