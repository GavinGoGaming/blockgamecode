/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.goal;

import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.entity.ai.NavigationConditions;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;

public abstract class DoorInteractGoal
extends Goal {
    protected MobEntity mob;
    protected BlockPos doorPos = BlockPos.ORIGIN;
    protected boolean doorValid;
    private boolean shouldStop;
    private float offsetX;
    private float offsetZ;

    public DoorInteractGoal(MobEntity mob) {
        this.mob = mob;
        if (!NavigationConditions.hasMobNavigation(mob)) {
            throw new IllegalArgumentException("Unsupported mob type for DoorInteractGoal");
        }
    }

    protected boolean isDoorOpen() {
        if (!this.doorValid) {
            return false;
        }
        BlockState lv = this.mob.getWorld().getBlockState(this.doorPos);
        if (!(lv.getBlock() instanceof DoorBlock)) {
            this.doorValid = false;
            return false;
        }
        return lv.get(DoorBlock.OPEN);
    }

    protected void setDoorOpen(boolean open) {
        BlockState lv;
        if (this.doorValid && (lv = this.mob.getWorld().getBlockState(this.doorPos)).getBlock() instanceof DoorBlock) {
            ((DoorBlock)lv.getBlock()).setOpen(this.mob, this.mob.getWorld(), lv, this.doorPos, open);
        }
    }

    @Override
    public boolean canStart() {
        if (!NavigationConditions.hasMobNavigation(this.mob)) {
            return false;
        }
        if (!this.mob.horizontalCollision) {
            return false;
        }
        MobNavigation lv = (MobNavigation)this.mob.getNavigation();
        Path lv2 = lv.getCurrentPath();
        if (lv2 == null || lv2.isFinished() || !lv.canEnterOpenDoors()) {
            return false;
        }
        for (int i = 0; i < Math.min(lv2.getCurrentNodeIndex() + 2, lv2.getLength()); ++i) {
            PathNode lv3 = lv2.getNode(i);
            this.doorPos = new BlockPos(lv3.x, lv3.y + 1, lv3.z);
            if (this.mob.squaredDistanceTo(this.doorPos.getX(), this.mob.getY(), this.doorPos.getZ()) > 2.25) continue;
            this.doorValid = DoorBlock.canOpenByHand(this.mob.getWorld(), this.doorPos);
            if (!this.doorValid) continue;
            return true;
        }
        this.doorPos = this.mob.getBlockPos().up();
        this.doorValid = DoorBlock.canOpenByHand(this.mob.getWorld(), this.doorPos);
        return this.doorValid;
    }

    @Override
    public boolean shouldContinue() {
        return !this.shouldStop;
    }

    @Override
    public void start() {
        this.shouldStop = false;
        this.offsetX = (float)((double)this.doorPos.getX() + 0.5 - this.mob.getX());
        this.offsetZ = (float)((double)this.doorPos.getZ() + 0.5 - this.mob.getZ());
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        float g;
        float f = (float)((double)this.doorPos.getX() + 0.5 - this.mob.getX());
        float h = this.offsetX * f + this.offsetZ * (g = (float)((double)this.doorPos.getZ() + 0.5 - this.mob.getZ()));
        if (h < 0.0f) {
            this.shouldStop = true;
        }
    }
}

