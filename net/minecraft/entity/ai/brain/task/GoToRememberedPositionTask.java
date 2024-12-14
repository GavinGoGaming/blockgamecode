/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import java.util.Optional;
import java.util.function.Function;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.SingleTickTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class GoToRememberedPositionTask {
    public static Task<PathAwareEntity> createPosBased(MemoryModuleType<BlockPos> posModule, float speed, int range, boolean requiresWalkTarget) {
        return GoToRememberedPositionTask.create(posModule, speed, range, requiresWalkTarget, Vec3d::ofBottomCenter);
    }

    public static SingleTickTask<PathAwareEntity> createEntityBased(MemoryModuleType<? extends Entity> entityModule, float speed, int range, boolean requiresWalkTarget) {
        return GoToRememberedPositionTask.create(entityModule, speed, range, requiresWalkTarget, Entity::getPos);
    }

    private static <T> SingleTickTask<PathAwareEntity> create(MemoryModuleType<T> posSource, float speed, int range, boolean requiresWalkTarget, Function<T, Vec3d> posGetter) {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryOptional(MemoryModuleType.WALK_TARGET), context.queryMemoryValue(posSource)).apply(context, (walkTarget, posSourcex) -> (world, entity, time) -> {
            Vec3d lv4;
            Vec3d lv3;
            Vec3d lv2;
            Optional optional = context.getOptionalValue(walkTarget);
            if (optional.isPresent() && !requiresWalkTarget) {
                return false;
            }
            Vec3d lv = entity.getPos();
            if (!lv.isInRange(lv2 = (Vec3d)posGetter.apply(context.getValue(posSourcex)), range)) {
                return false;
            }
            if (optional.isPresent() && ((WalkTarget)optional.get()).getSpeed() == speed && (lv3 = ((WalkTarget)optional.get()).getLookTarget().getPos().subtract(lv)).dotProduct(lv4 = lv2.subtract(lv)) < 0.0) {
                return false;
            }
            for (int j = 0; j < 10; ++j) {
                lv4 = FuzzyTargeting.findFrom(entity, 16, 7, lv2);
                if (lv4 == null) continue;
                walkTarget.remember(new WalkTarget(lv4, speed, 0));
                break;
            }
            return true;
        }));
    }
}

