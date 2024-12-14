/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.goal;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.TameableShoulderEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public class SitOnOwnerShoulderGoal
extends Goal {
    private final TameableShoulderEntity tameable;
    private ServerPlayerEntity owner;
    private boolean mounted;

    public SitOnOwnerShoulderGoal(TameableShoulderEntity tameable) {
        this.tameable = tameable;
    }

    @Override
    public boolean canStart() {
        ServerPlayerEntity lv = (ServerPlayerEntity)this.tameable.getOwner();
        boolean bl = lv != null && !lv.isSpectator() && !lv.getAbilities().flying && !lv.isTouchingWater() && !lv.inPowderSnow;
        return !this.tameable.isSitting() && bl && this.tameable.isReadyToSitOnPlayer();
    }

    @Override
    public boolean canStop() {
        return !this.mounted;
    }

    @Override
    public void start() {
        this.owner = (ServerPlayerEntity)this.tameable.getOwner();
        this.mounted = false;
    }

    @Override
    public void tick() {
        if (this.mounted || this.tameable.isInSittingPose() || this.tameable.isLeashed()) {
            return;
        }
        if (this.tameable.getBoundingBox().intersects(this.owner.getBoundingBox())) {
            this.mounted = this.tameable.mountOnto(this.owner);
        }
    }
}

