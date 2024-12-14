/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Leashable;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.decoration.LeashKnotEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class FormCaravanGoal
extends Goal {
    public final LlamaEntity llama;
    private double speed;
    private static final int MAX_CARAVAN_LENGTH = 8;
    private int counter;

    public FormCaravanGoal(LlamaEntity llama, double speed) {
        this.llama = llama;
        this.speed = speed;
        this.setControls(EnumSet.of(Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        double e;
        LlamaEntity lv3;
        if (this.llama.isLeashed() || this.llama.isFollowing()) {
            return false;
        }
        List<Entity> list = ((World)this.llama.getWorld()).getOtherEntities(this.llama, this.llama.getBoundingBox().expand(9.0, 4.0, 9.0), entity -> {
            EntityType<?> lv = entity.getType();
            return lv == EntityType.LLAMA || lv == EntityType.TRADER_LLAMA;
        });
        Leashable lv = null;
        double d = Double.MAX_VALUE;
        for (Entity lv2 : list) {
            lv3 = (LlamaEntity)lv2;
            if (!lv3.isFollowing() || lv3.hasFollower() || (e = this.llama.squaredDistanceTo(lv3)) > d) continue;
            d = e;
            lv = lv3;
        }
        if (lv == null) {
            for (Entity lv2 : list) {
                lv3 = (LlamaEntity)lv2;
                if (!lv3.isLeashed() || lv3.hasFollower() || (e = this.llama.squaredDistanceTo(lv3)) > d) continue;
                d = e;
                lv = lv3;
            }
        }
        if (lv == null) {
            return false;
        }
        if (d < 4.0) {
            return false;
        }
        if (!lv.isLeashed() && !this.canFollow((LlamaEntity)lv, 1)) {
            return false;
        }
        this.llama.follow((LlamaEntity)lv);
        return true;
    }

    @Override
    public boolean shouldContinue() {
        if (!(this.llama.isFollowing() && this.llama.getFollowing().isAlive() && this.canFollow(this.llama, 0))) {
            return false;
        }
        double d = this.llama.squaredDistanceTo(this.llama.getFollowing());
        if (d > 676.0) {
            if (this.speed <= 3.0) {
                this.speed *= 1.2;
                this.counter = FormCaravanGoal.toGoalTicks(40);
                return true;
            }
            if (this.counter == 0) {
                return false;
            }
        }
        if (this.counter > 0) {
            --this.counter;
        }
        return true;
    }

    @Override
    public void stop() {
        this.llama.stopFollowing();
        this.speed = 2.1;
    }

    @Override
    public void tick() {
        if (!this.llama.isFollowing()) {
            return;
        }
        if (this.llama.getLeashHolder() instanceof LeashKnotEntity) {
            return;
        }
        LlamaEntity lv = this.llama.getFollowing();
        double d = this.llama.distanceTo(lv);
        float f = 2.0f;
        Vec3d lv2 = new Vec3d(lv.getX() - this.llama.getX(), lv.getY() - this.llama.getY(), lv.getZ() - this.llama.getZ()).normalize().multiply(Math.max(d - 2.0, 0.0));
        this.llama.getNavigation().startMovingTo(this.llama.getX() + lv2.x, this.llama.getY() + lv2.y, this.llama.getZ() + lv2.z, this.speed);
    }

    private boolean canFollow(LlamaEntity llama, int length) {
        if (length > 8) {
            return false;
        }
        if (llama.isFollowing()) {
            if (llama.getFollowing().isLeashed()) {
                return true;
            }
            return this.canFollow(llama.getFollowing(), ++length);
        }
        return false;
    }
}

