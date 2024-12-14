/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import org.jetbrains.annotations.Nullable;

public class GoToVillageGoal
extends Goal {
    private static final int field_30228 = 10;
    private final PathAwareEntity mob;
    private final int searchRange;
    @Nullable
    private BlockPos targetPosition;

    public GoToVillageGoal(PathAwareEntity mob, int searchRange) {
        this.mob = mob;
        this.searchRange = GoToVillageGoal.toGoalTicks(searchRange);
        this.setControls(EnumSet.of(Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        BlockPos lv2;
        if (this.mob.hasControllingPassenger()) {
            return false;
        }
        if (this.mob.getWorld().isDay()) {
            return false;
        }
        if (this.mob.getRandom().nextInt(this.searchRange) != 0) {
            return false;
        }
        ServerWorld lv = (ServerWorld)this.mob.getWorld();
        if (!lv.isNearOccupiedPointOfInterest(lv2 = this.mob.getBlockPos(), 6)) {
            return false;
        }
        Vec3d lv3 = FuzzyTargeting.find(this.mob, 15, 7, arg2 -> -lv.getOccupiedPointOfInterestDistance(ChunkSectionPos.from(arg2)));
        this.targetPosition = lv3 == null ? null : BlockPos.ofFloored(lv3);
        return this.targetPosition != null;
    }

    @Override
    public boolean shouldContinue() {
        return this.targetPosition != null && !this.mob.getNavigation().isIdle() && this.mob.getNavigation().getTargetPos().equals(this.targetPosition);
    }

    @Override
    public void tick() {
        if (this.targetPosition == null) {
            return;
        }
        EntityNavigation lv = this.mob.getNavigation();
        if (lv.isIdle() && !this.targetPosition.isWithinDistance(this.mob.getPos(), 10.0)) {
            Vec3d lv2 = Vec3d.ofBottomCenter(this.targetPosition);
            Vec3d lv3 = this.mob.getPos();
            Vec3d lv4 = lv3.subtract(lv2);
            lv2 = lv4.multiply(0.4).add(lv2);
            Vec3d lv5 = lv2.subtract(lv3).normalize().multiply(10.0).add(lv3);
            BlockPos lv6 = BlockPos.ofFloored(lv5);
            lv6 = this.mob.getWorld().getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, lv6);
            if (!lv.startMovingTo(lv6.getX(), lv6.getY(), lv6.getZ(), 1.0)) {
                this.findOtherWaypoint();
            }
        }
    }

    private void findOtherWaypoint() {
        Random lv = this.mob.getRandom();
        BlockPos lv2 = this.mob.getWorld().getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, this.mob.getBlockPos().add(-8 + lv.nextInt(16), 0, -8 + lv.nextInt(16)));
        this.mob.getNavigation().startMovingTo(lv2.getX(), lv2.getY(), lv2.getZ(), 1.0);
    }
}

