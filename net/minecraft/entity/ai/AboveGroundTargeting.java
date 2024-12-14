/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.ai;

import net.minecraft.entity.ai.FuzzyPositions;
import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.NavigationConditions;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class AboveGroundTargeting {
    @Nullable
    public static Vec3d find(PathAwareEntity entity, int horizontalRange, int verticalRange, double x, double z, float angle, int maxAboveSolid, int minAboveSolid) {
        boolean bl = NavigationConditions.isPositionTargetInRange(entity, horizontalRange);
        return FuzzyPositions.guessBestPathTarget(entity, () -> {
            BlockPos lv = FuzzyPositions.localFuzz(entity.getRandom(), horizontalRange, verticalRange, 0, x, z, angle);
            if (lv == null) {
                return null;
            }
            BlockPos lv2 = FuzzyTargeting.towardTarget(entity, horizontalRange, bl, lv);
            if (lv2 == null) {
                return null;
            }
            if (NavigationConditions.isWaterAt(entity, lv2 = FuzzyPositions.upWhile(lv2, entity.getRandom().nextInt(maxAboveSolid - minAboveSolid + 1) + minAboveSolid, entity.getWorld().getTopY(), pos -> NavigationConditions.isSolidAt(entity, pos))) || NavigationConditions.hasPathfindingPenalty(entity, lv2)) {
                return null;
            }
            return lv2;
        });
    }
}

