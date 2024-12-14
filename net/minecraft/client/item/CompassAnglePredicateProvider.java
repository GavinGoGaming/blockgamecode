/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.ClampedModelPredicateProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class CompassAnglePredicateProvider
implements ClampedModelPredicateProvider {
    public static final int field_38798 = 0;
    private final AngleInterpolator aimedInterpolator = new AngleInterpolator();
    private final AngleInterpolator aimlessInterpolator = new AngleInterpolator();
    public final CompassTarget compassTarget;

    public CompassAnglePredicateProvider(CompassTarget compassTarget) {
        this.compassTarget = compassTarget;
    }

    @Override
    public float unclampedCall(ItemStack arg, @Nullable ClientWorld arg2, @Nullable LivingEntity arg3, int i) {
        Entity lv;
        Entity entity = lv = arg3 != null ? arg3 : arg.getHolder();
        if (lv == null) {
            return 0.0f;
        }
        if ((arg2 = this.getClientWorld(lv, arg2)) == null) {
            return 0.0f;
        }
        return this.getAngle(arg, arg2, i, lv);
    }

    private float getAngle(ItemStack stack, ClientWorld world, int seed, Entity entity) {
        GlobalPos lv = this.compassTarget.getPos(world, stack, entity);
        long l = world.getTime();
        if (!this.canPointTo(entity, lv)) {
            return this.getAimlessAngle(seed, l);
        }
        return this.getAngleTo(entity, l, lv.pos());
    }

    private float getAimlessAngle(int seed, long time) {
        if (this.aimlessInterpolator.shouldUpdate(time)) {
            this.aimlessInterpolator.update(time, Math.random());
        }
        double d = this.aimlessInterpolator.value + (double)((float)this.scatter(seed) / 2.1474836E9f);
        return MathHelper.floorMod((float)d, 1.0f);
    }

    private float getAngleTo(Entity entity, long time, BlockPos pos) {
        double f;
        PlayerEntity lv;
        double d = this.getAngleTo(entity, pos);
        double e = this.getBodyYaw(entity);
        if (entity instanceof PlayerEntity && (lv = (PlayerEntity)entity).isMainPlayer() && lv.getWorld().getTickManager().shouldTick()) {
            if (this.aimedInterpolator.shouldUpdate(time)) {
                this.aimedInterpolator.update(time, 0.5 - (e - 0.25));
            }
            f = d + this.aimedInterpolator.value;
        } else {
            f = 0.5 - (e - 0.25 - d);
        }
        return MathHelper.floorMod((float)f, 1.0f);
    }

    @Nullable
    private ClientWorld getClientWorld(Entity entity, @Nullable ClientWorld world) {
        if (world == null && entity.getWorld() instanceof ClientWorld) {
            return (ClientWorld)entity.getWorld();
        }
        return world;
    }

    private boolean canPointTo(Entity entity, @Nullable GlobalPos pos) {
        return pos != null && pos.dimension() == entity.getWorld().getRegistryKey() && !(pos.pos().getSquaredDistance(entity.getPos()) < (double)1.0E-5f);
    }

    private double getAngleTo(Entity entity, BlockPos pos) {
        Vec3d lv = Vec3d.ofCenter(pos);
        return Math.atan2(lv.getZ() - entity.getZ(), lv.getX() - entity.getX()) / 6.2831854820251465;
    }

    private double getBodyYaw(Entity entity) {
        return MathHelper.floorMod((double)(entity.getBodyYaw() / 360.0f), 1.0);
    }

    private int scatter(int seed) {
        return seed * 1327217883;
    }

    @Environment(value=EnvType.CLIENT)
    static class AngleInterpolator {
        double value;
        private double speed;
        private long lastUpdateTime;

        AngleInterpolator() {
        }

        boolean shouldUpdate(long time) {
            return this.lastUpdateTime != time;
        }

        void update(long time, double target) {
            this.lastUpdateTime = time;
            double e = target - this.value;
            e = MathHelper.floorMod(e + 0.5, 1.0) - 0.5;
            this.speed += e * 0.1;
            this.speed *= 0.8;
            this.value = MathHelper.floorMod(this.value + this.speed, 1.0);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static interface CompassTarget {
        @Nullable
        public GlobalPos getPos(ClientWorld var1, ItemStack var2, Entity var3);
    }
}

