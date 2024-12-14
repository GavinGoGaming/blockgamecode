/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.ai;

import net.minecraft.entity.ai.FuzzyPositions;
import net.minecraft.entity.ai.NavigationConditions;
import net.minecraft.entity.ai.NoPenaltySolidTargeting;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class NoWaterTargeting {
    @Nullable
    public static Vec3d find(PathAwareEntity entity, int horizontalRange, int verticalRange, int startHeight, Vec3d direction, double angleRange) {
        Vec3d lv = direction.subtract(entity.getX(), entity.getY(), entity.getZ());
        boolean bl = NavigationConditions.isPositionTargetInRange(entity, horizontalRange);
        return FuzzyPositions.guessBestPathTarget(entity, () -> {
            BlockPos lv = NoPenaltySolidTargeting.tryMake(entity, horizontalRange, verticalRange, startHeight, arg2.x, arg2.z, angleRange, bl);
            if (lv == null || NavigationConditions.isWaterAt(entity, lv)) {
                return null;
            }
            return lv;
        });
    }
}

