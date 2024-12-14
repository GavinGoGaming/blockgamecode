/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.BreezeEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.RaycastContext;

public class BreezeMovementUtil {
    private static final double MAX_MOVE_DISTANCE = 50.0;

    public static Vec3d getRandomPosBehindTarget(LivingEntity target, Random random) {
        int i = 90;
        float f = target.headYaw + 180.0f + (float)random.nextGaussian() * 90.0f / 2.0f;
        float g = MathHelper.lerp(random.nextFloat(), 4.0f, 8.0f);
        Vec3d lv = Vec3d.fromPolar(0.0f, f).multiply(g);
        return target.getPos().add(lv);
    }

    public static boolean canMoveTo(BreezeEntity breeze, Vec3d pos) {
        Vec3d lv = new Vec3d(breeze.getX(), breeze.getY(), breeze.getZ());
        if (pos.distanceTo(lv) > 50.0) {
            return false;
        }
        return breeze.getWorld().raycast(new RaycastContext(lv, pos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, breeze)).getType() == HitResult.Type.MISS;
    }
}

