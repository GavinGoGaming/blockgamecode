/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.poi.PointOfInterestStorage;

public class GoToPointOfInterestTask {
    public static Task<VillagerEntity> create(float speed, int completionRange) {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryAbsent(MemoryModuleType.WALK_TARGET)).apply(context, walkTarget -> (world, entity, time) -> {
            if (world.isNearOccupiedPointOfInterest(entity.getBlockPos())) {
                return false;
            }
            PointOfInterestStorage lv = world.getPointOfInterestStorage();
            int j = lv.getDistanceFromNearestOccupied(ChunkSectionPos.from(entity.getBlockPos()));
            Vec3d lv2 = null;
            for (int k = 0; k < 5; ++k) {
                Vec3d lv3 = FuzzyTargeting.find(entity, 15, 7, pos -> -lv.getDistanceFromNearestOccupied(ChunkSectionPos.from(pos)));
                if (lv3 == null) continue;
                int m = lv.getDistanceFromNearestOccupied(ChunkSectionPos.from(BlockPos.ofFloored(lv3)));
                if (m < j) {
                    lv2 = lv3;
                    break;
                }
                if (m != j) continue;
                lv2 = lv3;
            }
            if (lv2 != null) {
                walkTarget.remember(new WalkTarget(lv2, speed, completionRange));
            }
            return true;
        }));
    }
}

