/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.ai;

import java.util.function.ToDoubleFunction;
import net.minecraft.entity.ai.FuzzyPositions;
import net.minecraft.entity.ai.NavigationConditions;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class FuzzyTargeting {
    @Nullable
    public static Vec3d find(PathAwareEntity entity, int horizontalRange, int verticalRange) {
        return FuzzyTargeting.find(entity, horizontalRange, verticalRange, entity::getPathfindingFavor);
    }

    @Nullable
    public static Vec3d find(PathAwareEntity entity, int horizontalRange, int verticalRange, ToDoubleFunction<BlockPos> scorer) {
        boolean bl = NavigationConditions.isPositionTargetInRange(entity, horizontalRange);
        return FuzzyPositions.guessBest(() -> {
            BlockPos lv = FuzzyPositions.localFuzz(entity.getRandom(), horizontalRange, verticalRange);
            BlockPos lv2 = FuzzyTargeting.towardTarget(entity, horizontalRange, bl, lv);
            if (lv2 == null) {
                return null;
            }
            return FuzzyTargeting.validate(entity, lv2);
        }, scorer);
    }

    @Nullable
    public static Vec3d findTo(PathAwareEntity entity, int horizontalRange, int verticalRange, Vec3d end) {
        Vec3d lv = end.subtract(entity.getX(), entity.getY(), entity.getZ());
        boolean bl = NavigationConditions.isPositionTargetInRange(entity, horizontalRange);
        return FuzzyTargeting.findValid(entity, horizontalRange, verticalRange, lv, bl);
    }

    @Nullable
    public static Vec3d findFrom(PathAwareEntity entity, int horizontalRange, int verticalRange, Vec3d start) {
        Vec3d lv = entity.getPos().subtract(start);
        boolean bl = NavigationConditions.isPositionTargetInRange(entity, horizontalRange);
        return FuzzyTargeting.findValid(entity, horizontalRange, verticalRange, lv, bl);
    }

    @Nullable
    private static Vec3d findValid(PathAwareEntity entity, int horizontalRange, int verticalRange, Vec3d direction, boolean posTargetInRange) {
        return FuzzyPositions.guessBestPathTarget(entity, () -> {
            BlockPos lv = FuzzyPositions.localFuzz(entity.getRandom(), horizontalRange, verticalRange, 0, arg2.x, arg2.z, 1.5707963705062866);
            if (lv == null) {
                return null;
            }
            BlockPos lv2 = FuzzyTargeting.towardTarget(entity, horizontalRange, posTargetInRange, lv);
            if (lv2 == null) {
                return null;
            }
            return FuzzyTargeting.validate(entity, lv2);
        });
    }

    @Nullable
    public static BlockPos validate(PathAwareEntity entity, BlockPos pos) {
        if (NavigationConditions.isWaterAt(entity, pos = FuzzyPositions.upWhile(pos, entity.getWorld().getTopY(), currentPos -> NavigationConditions.isSolidAt(entity, currentPos))) || NavigationConditions.hasPathfindingPenalty(entity, pos)) {
            return null;
        }
        return pos;
    }

    @Nullable
    public static BlockPos towardTarget(PathAwareEntity entity, int horizontalRange, boolean posTargetInRange, BlockPos relativeInRangePos) {
        BlockPos lv = FuzzyPositions.towardTarget(entity, horizontalRange, entity.getRandom(), relativeInRangePos);
        if (NavigationConditions.isHeightInvalid(lv, entity) || NavigationConditions.isPositionTargetOutOfWalkRange(posTargetInRange, entity, lv) || NavigationConditions.isInvalidPosition(entity.getNavigation(), lv)) {
            return null;
        }
        return lv;
    }
}

