/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.projectile.thrown;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public abstract class ThrownEntity
extends ProjectileEntity {
    protected ThrownEntity(EntityType<? extends ThrownEntity> arg, World arg2) {
        super((EntityType<? extends ProjectileEntity>)arg, arg2);
    }

    protected ThrownEntity(EntityType<? extends ThrownEntity> type, double x, double y, double z, World world) {
        this(type, world);
        this.setPosition(x, y, z);
    }

    protected ThrownEntity(EntityType<? extends ThrownEntity> type, LivingEntity owner, World world) {
        this(type, owner.getX(), owner.getEyeY() - (double)0.1f, owner.getZ(), world);
        this.setOwner(owner);
    }

    @Override
    public boolean shouldRender(double distance) {
        double e = this.getBoundingBox().getAverageSideLength() * 4.0;
        if (Double.isNaN(e)) {
            e = 4.0;
        }
        return distance < (e *= 64.0) * e;
    }

    @Override
    public boolean canUsePortals(boolean allowVehicles) {
        return true;
    }

    @Override
    public void tick() {
        float h;
        super.tick();
        HitResult lv = ProjectileUtil.getCollision(this, this::canHit);
        if (lv.getType() != HitResult.Type.MISS) {
            this.hitOrDeflect(lv);
        }
        this.checkBlockCollision();
        Vec3d lv2 = this.getVelocity();
        double d = this.getX() + lv2.x;
        double e = this.getY() + lv2.y;
        double f = this.getZ() + lv2.z;
        this.updateRotation();
        if (this.isTouchingWater()) {
            for (int i = 0; i < 4; ++i) {
                float g = 0.25f;
                this.getWorld().addParticle(ParticleTypes.BUBBLE, d - lv2.x * 0.25, e - lv2.y * 0.25, f - lv2.z * 0.25, lv2.x, lv2.y, lv2.z);
            }
            h = 0.8f;
        } else {
            h = 0.99f;
        }
        this.setVelocity(lv2.multiply(h));
        this.applyGravity();
        this.setPosition(d, e, f);
    }

    @Override
    protected double getGravity() {
        return 0.03;
    }
}

