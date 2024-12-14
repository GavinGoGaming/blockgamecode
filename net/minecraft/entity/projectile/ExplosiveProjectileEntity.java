/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.projectile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class ExplosiveProjectileEntity
extends ProjectileEntity {
    public static final double field_51891 = 0.1;
    public static final double field_51892 = 0.5;
    public double accelerationPower = 0.1;

    protected ExplosiveProjectileEntity(EntityType<? extends ExplosiveProjectileEntity> arg, World arg2) {
        super((EntityType<? extends ProjectileEntity>)arg, arg2);
    }

    protected ExplosiveProjectileEntity(EntityType<? extends ExplosiveProjectileEntity> type, double x, double y, double z, World world) {
        this(type, world);
        this.setPosition(x, y, z);
    }

    public ExplosiveProjectileEntity(EntityType<? extends ExplosiveProjectileEntity> type, double x, double y, double z, Vec3d velocity, World world) {
        this(type, world);
        this.refreshPositionAndAngles(x, y, z, this.getYaw(), this.getPitch());
        this.refreshPosition();
        this.setVelocityWithAcceleration(velocity, this.accelerationPower);
    }

    public ExplosiveProjectileEntity(EntityType<? extends ExplosiveProjectileEntity> type, LivingEntity owner, Vec3d velocity, World world) {
        this(type, owner.getX(), owner.getY(), owner.getZ(), velocity, world);
        this.setOwner(owner);
        this.setRotation(owner.getYaw(), owner.getPitch());
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
    }

    @Override
    public boolean shouldRender(double distance) {
        double e = this.getBoundingBox().getAverageSideLength() * 4.0;
        if (Double.isNaN(e)) {
            e = 4.0;
        }
        return distance < (e *= 64.0) * e;
    }

    protected RaycastContext.ShapeType getRaycastShapeType() {
        return RaycastContext.ShapeType.COLLIDER;
    }

    @Override
    public void tick() {
        float h;
        HitResult lv2;
        Entity lv = this.getOwner();
        if (!this.getWorld().isClient && (lv != null && lv.isRemoved() || !this.getWorld().isChunkLoaded(this.getBlockPos()))) {
            this.discard();
            return;
        }
        super.tick();
        if (this.isBurning()) {
            this.setOnFireFor(1.0f);
        }
        if ((lv2 = ProjectileUtil.getCollision((Entity)this, this::canHit, this.getRaycastShapeType())).getType() != HitResult.Type.MISS) {
            this.hitOrDeflect(lv2);
        }
        this.checkBlockCollision();
        Vec3d lv3 = this.getVelocity();
        double d = this.getX() + lv3.x;
        double e = this.getY() + lv3.y;
        double f = this.getZ() + lv3.z;
        ProjectileUtil.setRotationFromVelocity(this, 0.2f);
        if (this.isTouchingWater()) {
            for (int i = 0; i < 4; ++i) {
                float g = 0.25f;
                this.getWorld().addParticle(ParticleTypes.BUBBLE, d - lv3.x * 0.25, e - lv3.y * 0.25, f - lv3.z * 0.25, lv3.x, lv3.y, lv3.z);
            }
            h = this.getDragInWater();
        } else {
            h = this.getDrag();
        }
        this.setVelocity(lv3.add(lv3.normalize().multiply(this.accelerationPower)).multiply(h));
        ParticleEffect lv4 = this.getParticleType();
        if (lv4 != null) {
            this.getWorld().addParticle(lv4, d, e + 0.5, f, 0.0, 0.0, 0.0);
        }
        this.setPosition(d, e, f);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        return !this.isInvulnerableTo(source);
    }

    @Override
    protected boolean canHit(Entity entity) {
        return super.canHit(entity) && !entity.noClip;
    }

    protected boolean isBurning() {
        return true;
    }

    @Nullable
    protected ParticleEffect getParticleType() {
        return ParticleTypes.SMOKE;
    }

    protected float getDrag() {
        return 0.95f;
    }

    protected float getDragInWater() {
        return 0.8f;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putDouble("acceleration_power", this.accelerationPower);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("acceleration_power", NbtElement.DOUBLE_TYPE)) {
            this.accelerationPower = nbt.getDouble("acceleration_power");
        }
    }

    @Override
    public float getBrightnessAtEyes() {
        return 1.0f;
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket(EntityTrackerEntry entityTrackerEntry) {
        Entity lv = this.getOwner();
        int i = lv == null ? 0 : lv.getId();
        Vec3d lv2 = entityTrackerEntry.getPos();
        return new EntitySpawnS2CPacket(this.getId(), this.getUuid(), lv2.getX(), lv2.getY(), lv2.getZ(), entityTrackerEntry.getPitch(), entityTrackerEntry.getYaw(), this.getType(), i, entityTrackerEntry.getVelocity(), 0.0);
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        Vec3d lv = new Vec3d(packet.getVelocityX(), packet.getVelocityY(), packet.getVelocityZ());
        this.setVelocity(lv);
    }

    private void setVelocityWithAcceleration(Vec3d velocity, double accelerationPower) {
        this.setVelocity(velocity.normalize().multiply(accelerationPower));
        this.velocityDirty = true;
    }

    @Override
    protected void onDeflected(@Nullable Entity deflector, boolean fromAttack) {
        super.onDeflected(deflector, fromAttack);
        this.accelerationPower = fromAttack ? 0.1 : (this.accelerationPower *= 0.5);
    }
}

