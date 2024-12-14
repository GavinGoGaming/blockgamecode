/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

public class EyeOfEnderEntity
extends Entity
implements FlyingItemEntity {
    private static final TrackedData<ItemStack> ITEM = DataTracker.registerData(EyeOfEnderEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
    private double targetX;
    private double targetY;
    private double targetZ;
    private int lifespan;
    private boolean dropsItem;

    public EyeOfEnderEntity(EntityType<? extends EyeOfEnderEntity> arg, World arg2) {
        super(arg, arg2);
    }

    public EyeOfEnderEntity(World world, double x, double y, double z) {
        this((EntityType<? extends EyeOfEnderEntity>)EntityType.EYE_OF_ENDER, world);
        this.setPosition(x, y, z);
    }

    public void setItem(ItemStack arg) {
        if (arg.isEmpty()) {
            this.getDataTracker().set(ITEM, this.getItem());
        } else {
            this.getDataTracker().set(ITEM, arg.copyWithCount(1));
        }
    }

    @Override
    public ItemStack getStack() {
        return this.getDataTracker().get(ITEM);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(ITEM, this.getItem());
    }

    @Override
    public boolean shouldRender(double distance) {
        double e = this.getBoundingBox().getAverageSideLength() * 4.0;
        if (Double.isNaN(e)) {
            e = 4.0;
        }
        return distance < (e *= 64.0) * e;
    }

    public void initTargetPos(BlockPos pos) {
        double g;
        double d = pos.getX();
        int i = pos.getY();
        double e = pos.getZ();
        double f = d - this.getX();
        double h = Math.sqrt(f * f + (g = e - this.getZ()) * g);
        if (h > 12.0) {
            this.targetX = this.getX() + f / h * 12.0;
            this.targetZ = this.getZ() + g / h * 12.0;
            this.targetY = this.getY() + 8.0;
        } else {
            this.targetX = d;
            this.targetY = i;
            this.targetZ = e;
        }
        this.lifespan = 0;
        this.dropsItem = this.random.nextInt(5) > 0;
    }

    @Override
    public void setVelocityClient(double x, double y, double z) {
        this.setVelocity(x, y, z);
        if (this.prevPitch == 0.0f && this.prevYaw == 0.0f) {
            double g = Math.sqrt(x * x + z * z);
            this.setYaw((float)(MathHelper.atan2(x, z) * 57.2957763671875));
            this.setPitch((float)(MathHelper.atan2(y, g) * 57.2957763671875));
            this.prevYaw = this.getYaw();
            this.prevPitch = this.getPitch();
        }
    }

    @Override
    public void tick() {
        super.tick();
        Vec3d lv = this.getVelocity();
        double d = this.getX() + lv.x;
        double e = this.getY() + lv.y;
        double f = this.getZ() + lv.z;
        double g = lv.horizontalLength();
        this.setPitch(ProjectileEntity.updateRotation(this.prevPitch, (float)(MathHelper.atan2(lv.y, g) * 57.2957763671875)));
        this.setYaw(ProjectileEntity.updateRotation(this.prevYaw, (float)(MathHelper.atan2(lv.x, lv.z) * 57.2957763671875)));
        if (!this.getWorld().isClient) {
            double h = this.targetX - d;
            double i = this.targetZ - f;
            float j = (float)Math.sqrt(h * h + i * i);
            float k = (float)MathHelper.atan2(i, h);
            double l = MathHelper.lerp(0.0025, g, (double)j);
            double m = lv.y;
            if (j < 1.0f) {
                l *= 0.8;
                m *= 0.8;
            }
            int n = this.getY() < this.targetY ? 1 : -1;
            lv = new Vec3d(Math.cos(k) * l, m + ((double)n - m) * (double)0.015f, Math.sin(k) * l);
            this.setVelocity(lv);
        }
        float o = 0.25f;
        if (this.isTouchingWater()) {
            for (int p = 0; p < 4; ++p) {
                this.getWorld().addParticle(ParticleTypes.BUBBLE, d - lv.x * 0.25, e - lv.y * 0.25, f - lv.z * 0.25, lv.x, lv.y, lv.z);
            }
        } else {
            this.getWorld().addParticle(ParticleTypes.PORTAL, d - lv.x * 0.25 + this.random.nextDouble() * 0.6 - 0.3, e - lv.y * 0.25 - 0.5, f - lv.z * 0.25 + this.random.nextDouble() * 0.6 - 0.3, lv.x, lv.y, lv.z);
        }
        if (!this.getWorld().isClient) {
            this.setPosition(d, e, f);
            ++this.lifespan;
            if (this.lifespan > 80 && !this.getWorld().isClient) {
                this.playSound(SoundEvents.ENTITY_ENDER_EYE_DEATH, 1.0f, 1.0f);
                this.discard();
                if (this.dropsItem) {
                    this.getWorld().spawnEntity(new ItemEntity(this.getWorld(), this.getX(), this.getY(), this.getZ(), this.getStack()));
                } else {
                    this.getWorld().syncWorldEvent(WorldEvents.EYE_OF_ENDER_BREAKS, this.getBlockPos(), 0);
                }
            }
        } else {
            this.setPos(d, e, f);
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.put("Item", this.getStack().encode(this.getRegistryManager()));
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        if (nbt.contains("Item", NbtElement.COMPOUND_TYPE)) {
            this.setItem(ItemStack.fromNbt(this.getRegistryManager(), nbt.getCompound("Item")).orElse(this.getItem()));
        } else {
            this.setItem(this.getItem());
        }
    }

    private ItemStack getItem() {
        return new ItemStack(Items.ENDER_EYE);
    }

    @Override
    public float getBrightnessAtEyes() {
        return 1.0f;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }
}

