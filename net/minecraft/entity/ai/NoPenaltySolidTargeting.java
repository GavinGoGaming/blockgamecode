/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.ai;

import net.minecraft.entity.ai.FuzzyPositions;
import net.minecraft.entity.ai.NavigationConditions;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class NoPenaltySolidTargeting {
    @Nullable
    public static Vec3d find(PathAwareEntity entity, int horizontalRange, int verticalRange, int startHeight, double directionX, double directionZ, double rangeAngle) {
        boolean bl = NavigationConditions.isPositionTargetInRange(entity, horizontalRange);
        return FuzzyPositions.guessBestPathTarget(entity, () -> NoPenaltySolidTargeting.tryMake(entity, horizontalRange, verticalRange, startHeight, directionX, directionZ, rangeAngle, bl));
    }

    @Nullable
    public static BlockPos tryMake(PathAwareEntity entity, int horizontalRange, int verticalRange, int startHeight, double directionX, double directionZ, double rangeAngle, boolean posTargetInRange) {
        BlockPos lv = FuzzyPositions.localFuzz(entity.getRandom(), horizontalRange, verticalRange, startHeight, directionX, directionZ, rangeAngle);
        if (lv == null) {
            return null;
        }
        BlockPos lv2 = FuzzyPositions.towardTarget(entity, horizontalRange, entity.getRandom(), lv);
        if (NavigationConditions.isHeightInvalid(lv2, entity) || NavigationConditions.isPositionTargetOutOfWalkRange(posTargetInRange, entity, lv2)) {
            return null;
        }
        if (NavigationConditions.hasPathfindingPenalty(entity, lv2 = FuzzyPositions.upWhile(lv2, entity.getWorld().getTopY(), pos -> NavigationConditions.isSolidAt(entity, pos)))) {
            return null;
        }
        return lv2;
    }
}

