/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.projectile;

import com.google.common.base.MoreObjects;
import it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair;
import java.util.UUID;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.ProjectileDeflection;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public abstract class ProjectileEntity
extends Entity
implements Ownable {
    @Nullable
    private UUID ownerUuid;
    @Nullable
    private Entity owner;
    private boolean leftOwner;
    private boolean shot;
    @Nullable
    private Entity lastDeflectedEntity;

    ProjectileEntity(EntityType<? extends ProjectileEntity> arg, World arg2) {
        super(arg, arg2);
    }

    public void setOwner(@Nullable Entity entity) {
        if (entity != null) {
            this.ownerUuid = entity.getUuid();
            this.owner = entity;
        }
    }

    @Override
    @Nullable
    public Entity getOwner() {
        World world;
        if (this.owner != null && !this.owner.isRemoved()) {
            return this.owner;
        }
        if (this.ownerUuid != null && (world = this.getWorld()) instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)world;
            this.owner = lv.getEntity(this.ownerUuid);
            return this.owner;
        }
        return null;
    }

    public Entity getEffectCause() {
        return MoreObjects.firstNonNull(this.getOwner(), this);
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        if (this.ownerUuid != null) {
            nbt.putUuid("Owner", this.ownerUuid);
        }
        if (this.leftOwner) {
            nbt.putBoolean("LeftOwner", true);
        }
        nbt.putBoolean("HasBeenShot", this.shot);
    }

    protected boolean isOwner(Entity entity) {
        return entity.getUuid().equals(this.ownerUuid);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        if (nbt.containsUuid("Owner")) {
            this.ownerUuid = nbt.getUuid("Owner");
            this.owner = null;
        }
        this.leftOwner = nbt.getBoolean("LeftOwner");
        this.shot = nbt.getBoolean("HasBeenShot");
    }

    @Override
    public void copyFrom(Entity original) {
        super.copyFrom(original);
        if (original instanceof ProjectileEntity) {
            ProjectileEntity lv = (ProjectileEntity)original;
            this.owner = lv.owner;
        }
    }

    @Override
    public void tick() {
        if (!this.shot) {
            this.emitGameEvent(GameEvent.PROJECTILE_SHOOT, this.getOwner());
            this.shot = true;
        }
        if (!this.leftOwner) {
            this.leftOwner = this.shouldLeaveOwner();
        }
        super.tick();
    }

    private boolean shouldLeaveOwner() {
        Entity lv = this.getOwner();
        if (lv != null) {
            for (Entity lv2 : this.getWorld().getOtherEntities(this, this.getBoundingBox().stretch(this.getVelocity()).expand(1.0), entity -> !entity.isSpectator() && entity.canHit())) {
                if (lv2.getRootVehicle() != lv.getRootVehicle()) continue;
                return false;
            }
        }
        return true;
    }

    public Vec3d calculateVelocity(double x, double y, double z, float power, float uncertainty) {
        return new Vec3d(x, y, z).normalize().add(this.random.nextTriangular(0.0, 0.0172275 * (double)uncertainty), this.random.nextTriangular(0.0, 0.0172275 * (double)uncertainty), this.random.nextTriangular(0.0, 0.0172275 * (double)uncertainty)).multiply(power);
    }

    public void setVelocity(double x, double y, double z, float power, float uncertainty) {
        Vec3d lv = this.calculateVelocity(x, y, z, power, uncertainty);
        this.setVelocity(lv);
        this.velocityDirty = true;
        double i = lv.horizontalLength();
        this.setYaw((float)(MathHelper.atan2(lv.x, lv.z) * 57.2957763671875));
        this.setPitch((float)(MathHelper.atan2(lv.y, i) * 57.2957763671875));
        this.prevYaw = this.getYaw();
        this.prevPitch = this.getPitch();
    }

    public void setVelocity(Entity shooter, float pitch, float yaw, float roll, float speed, float divergence) {
        float k = -MathHelper.sin(yaw * ((float)Math.PI / 180)) * MathHelper.cos(pitch * ((float)Math.PI / 180));
        float l = -MathHelper.sin((pitch + roll) * ((float)Math.PI / 180));
        float m = MathHelper.cos(yaw * ((float)Math.PI / 180)) * MathHelper.cos(pitch * ((float)Math.PI / 180));
        this.setVelocity(k, l, m, speed, divergence);
        Vec3d lv = shooter.getMovement();
        this.setVelocity(this.getVelocity().add(lv.x, shooter.isOnGround() ? 0.0 : lv.y, lv.z));
    }

    protected ProjectileDeflection hitOrDeflect(HitResult hitResult) {
        EntityHitResult lv;
        Entity lv2;
        ProjectileDeflection lv3;
        if (hitResult.getType() == HitResult.Type.ENTITY && (lv3 = (lv2 = (lv = (EntityHitResult)hitResult).getEntity()).getProjectileDeflection(this)) != ProjectileDeflection.NONE) {
            if (lv2 != this.lastDeflectedEntity && this.deflect(lv3, lv2, this.getOwner(), false)) {
                this.lastDeflectedEntity = lv2;
            }
            return lv3;
        }
        this.onCollision(hitResult);
        return ProjectileDeflection.NONE;
    }

    public boolean deflect(ProjectileDeflection deflection, @Nullable Entity deflector, @Nullable Entity owner, boolean fromAttack) {
        if (!this.getWorld().isClient) {
            deflection.deflect(this, deflector, this.random);
            this.setOwner(owner);
            this.onDeflected(deflector, fromAttack);
        }
        return true;
    }

    protected void onDeflected(@Nullable Entity deflector, boolean fromAttack) {
    }

    protected void onCollision(HitResult hitResult) {
        HitResult.Type lv = hitResult.getType();
        if (lv == HitResult.Type.ENTITY) {
            EntityHitResult lv2 = (EntityHitResult)hitResult;
            Entity lv3 = lv2.getEntity();
            if (lv3.getType().isIn(EntityTypeTags.REDIRECTABLE_PROJECTILE) && lv3 instanceof ProjectileEntity) {
                ProjectileEntity lv4 = (ProjectileEntity)lv3;
                lv4.deflect(ProjectileDeflection.REDIRECTED, this.getOwner(), this.getOwner(), true);
            }
            this.onEntityHit(lv2);
            this.getWorld().emitGameEvent(GameEvent.PROJECTILE_LAND, hitResult.getPos(), GameEvent.Emitter.of(this, null));
        } else if (lv == HitResult.Type.BLOCK) {
            BlockHitResult lv5 = (BlockHitResult)hitResult;
            this.onBlockHit(lv5);
            BlockPos lv6 = lv5.getBlockPos();
            this.getWorld().emitGameEvent(GameEvent.PROJECTILE_LAND, lv6, GameEvent.Emitter.of(this, this.getWorld().getBlockState(lv6)));
        }
    }

    protected void onEntityHit(EntityHitResult entityHitResult) {
    }

    protected void onBlockHit(BlockHitResult blockHitResult) {
        BlockState lv = this.getWorld().getBlockState(blockHitResult.getBlockPos());
        lv.onProjectileHit(this.getWorld(), lv, blockHitResult, this);
    }

    @Override
    public void setVelocityClient(double x, double y, double z) {
        this.setVelocity(x, y, z);
        if (this.prevPitch == 0.0f && this.prevYaw == 0.0f) {
            double g = Math.sqrt(x * x + z * z);
            this.setPitch((float)(MathHelper.atan2(y, g) * 57.2957763671875));
            this.setYaw((float)(MathHelper.atan2(x, z) * 57.2957763671875));
            this.prevPitch = this.getPitch();
            this.prevYaw = this.getYaw();
            this.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), this.getPitch());
        }
    }

    protected boolean canHit(Entity entity) {
        if (!entity.canBeHitByProjectile()) {
            return false;
        }
        Entity lv = this.getOwner();
        return lv == null || this.leftOwner || !lv.isConnectedThroughVehicle(entity);
    }

    protected void updateRotation() {
        Vec3d lv = this.getVelocity();
        double d = lv.horizontalLength();
        this.setPitch(ProjectileEntity.updateRotation(this.prevPitch, (float)(MathHelper.atan2(lv.y, d) * 57.2957763671875)));
        this.setYaw(ProjectileEntity.updateRotation(this.prevYaw, (float)(MathHelper.atan2(lv.x, lv.z) * 57.2957763671875)));
    }

    protected static float updateRotation(float prevRot, float newRot) {
        while (newRot - prevRot < -180.0f) {
            prevRot -= 360.0f;
        }
        while (newRot - prevRot >= 180.0f) {
            prevRot += 360.0f;
        }
        return MathHelper.lerp(0.2f, prevRot, newRot);
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket(EntityTrackerEntry entityTrackerEntry) {
        Entity lv = this.getOwner();
        return new EntitySpawnS2CPacket((Entity)this, entityTrackerEntry, lv == null ? 0 : lv.getId());
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        Entity lv = this.getWorld().getEntityById(packet.getEntityData());
        if (lv != null) {
            this.setOwner(lv);
        }
    }

    @Override
    public boolean canModifyAt(World world, BlockPos pos) {
        Entity lv = this.getOwner();
        if (lv instanceof PlayerEntity) {
            return lv.canModifyAt(world, pos);
        }
        return lv == null || world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING);
    }

    public boolean canBreakBlocks(World world) {
        return this.getType().isIn(EntityTypeTags.IMPACT_PROJECTILES) && world.getGameRules().getBoolean(GameRules.PROJECTILES_CAN_BREAK_BLOCKS);
    }

    @Override
    public boolean canHit() {
        return this.getType().isIn(EntityTypeTags.REDIRECTABLE_PROJECTILE);
    }

    @Override
    public float getTargetingMargin() {
        return this.canHit() ? 1.0f : 0.0f;
    }

    public DoubleDoubleImmutablePair getKnockback(LivingEntity target, DamageSource source) {
        double d = this.getVelocity().x;
        double e = this.getVelocity().z;
        return DoubleDoubleImmutablePair.of(d, e);
    }
}

