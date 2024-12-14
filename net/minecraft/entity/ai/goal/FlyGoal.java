/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.ai.goal;

import net.minecraft.entity.ai.AboveGroundTargeting;
import net.minecraft.entity.ai.NoPenaltySolidTargeting;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class FlyGoal
extends WanderAroundFarGoal {
    public FlyGoal(PathAwareEntity arg, double d) {
        super(arg, d);
    }

    @Override
    @Nullable
    protected Vec3d getWanderTarget() {
        Vec3d lv = this.mob.getRotationVec(0.0f);
        int i = 8;
        Vec3d lv2 = AboveGroundTargeting.find(this.mob, 8, 7, lv.x, lv.z, 1.5707964f, 3, 1);
        if (lv2 != null) {
            return lv2;
        }
        return NoPenaltySolidTargeting.find(this.mob, 8, 4, -2, lv.x, lv.z, 1.5707963705062866);
    }
}

