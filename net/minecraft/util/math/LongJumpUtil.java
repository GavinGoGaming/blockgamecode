/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.math;

import java.util.Optional;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class LongJumpUtil {
    public static Optional<Vec3d> getJumpingVelocity(MobEntity entity, Vec3d jumpTarget, float maxVelocity, int angle, boolean requireClearPath) {
        Vec3d lv = entity.getPos();
        Vec3d lv2 = new Vec3d(jumpTarget.x - lv.x, 0.0, jumpTarget.z - lv.z).normalize().multiply(0.5);
        Vec3d lv3 = jumpTarget.subtract(lv2);
        Vec3d lv4 = lv3.subtract(lv);
        float g = (float)angle * (float)Math.PI / 180.0f;
        double d = Math.atan2(lv4.z, lv4.x);
        double e = lv4.subtract(0.0, lv4.y, 0.0).lengthSquared();
        double h = Math.sqrt(e);
        double j = lv4.y;
        double k = entity.getFinalGravity();
        double l = Math.sin(2.0f * g);
        double m = Math.pow(Math.cos(g), 2.0);
        double n = Math.sin(g);
        double o = Math.cos(g);
        double p = Math.sin(d);
        double q = Math.cos(d);
        double r = e * k / (h * l - 2.0 * j * m);
        if (r < 0.0) {
            return Optional.empty();
        }
        double s = Math.sqrt(r);
        if (s > (double)maxVelocity) {
            return Optional.empty();
        }
        double t = s * o;
        double u = s * n;
        if (requireClearPath) {
            int v = MathHelper.ceil(h / t) * 2;
            double w = 0.0;
            Vec3d lv5 = null;
            EntityDimensions lv6 = entity.getDimensions(EntityPose.LONG_JUMPING);
            for (int x = 0; x < v - 1; ++x) {
                double y = n / o * (w += h / (double)v) - Math.pow(w, 2.0) * k / (2.0 * r * Math.pow(o, 2.0));
                double z = w * q;
                double aa = w * p;
                Vec3d lv7 = new Vec3d(lv.x + z, lv.y + y, lv.z + aa);
                if (lv5 != null && !LongJumpUtil.isPathClear(entity, lv6, lv5, lv7)) {
                    return Optional.empty();
                }
                lv5 = lv7;
            }
        }
        return Optional.of(new Vec3d(t * q, u, t * p).multiply(0.95f));
    }

    private static boolean isPathClear(MobEntity entity, EntityDimensions dimensions, Vec3d prevPos, Vec3d nextPos) {
        Vec3d lv = nextPos.subtract(prevPos);
        double d = Math.min(dimensions.width(), dimensions.height());
        int i = MathHelper.ceil(lv.length() / d);
        Vec3d lv2 = lv.normalize();
        Vec3d lv3 = prevPos;
        for (int j = 0; j < i; ++j) {
            Vec3d vec3d = lv3 = j == i - 1 ? nextPos : lv3.add(lv2.multiply(d * (double)0.9f));
            if (entity.getWorld().isSpaceEmpty(entity, dimensions.getBoxAt(lv3))) continue;
            return false;
        }
        return true;
    }
}

