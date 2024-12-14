/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class EscapeSunlightGoal
extends Goal {
    protected final PathAwareEntity mob;
    private double targetX;
    private double targetY;
    private double targetZ;
    private final double speed;
    private final World world;

    public EscapeSunlightGoal(PathAwareEntity mob, double speed) {
        this.mob = mob;
        this.speed = speed;
        this.world = mob.getWorld();
        this.setControls(EnumSet.of(Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        if (this.mob.getTarget() != null) {
            return false;
        }
        if (!this.world.isDay()) {
            return false;
        }
        if (!this.mob.isOnFire()) {
            return false;
        }
        if (!this.world.isSkyVisible(this.mob.getBlockPos())) {
            return false;
        }
        if (!this.mob.getEquippedStack(EquipmentSlot.HEAD).isEmpty()) {
            return false;
        }
        return this.targetShadedPos();
    }

    protected boolean targetShadedPos() {
        Vec3d lv = this.locateShadedPos();
        if (lv == null) {
            return false;
        }
        this.targetX = lv.x;
        this.targetY = lv.y;
        this.targetZ = lv.z;
        return true;
    }

    @Override
    public boolean shouldContinue() {
        return !this.mob.getNavigation().isIdle();
    }

    @Override
    public void start() {
        this.mob.getNavigation().startMovingTo(this.targetX, this.targetY, this.targetZ, this.speed);
    }

    @Nullable
    protected Vec3d locateShadedPos() {
        Random lv = this.mob.getRandom();
        BlockPos lv2 = this.mob.getBlockPos();
        for (int i = 0; i < 10; ++i) {
            BlockPos lv3 = lv2.add(lv.nextInt(20) - 10, lv.nextInt(6) - 3, lv.nextInt(20) - 10);
            if (this.world.isSkyVisible(lv3) || !(this.mob.getPathfindingFavor(lv3) < 0.0f)) continue;
            return Vec3d.ofBottomCenter(lv3);
        }
        return null;
    }
}

