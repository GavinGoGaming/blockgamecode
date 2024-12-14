/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import java.util.Map;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.BreezeMovementUtil;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.mob.BreezeEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class BreezeSlideTowardsTargetTask
extends MultiTickTask<BreezeEntity> {
    public BreezeSlideTowardsTargetTask() {
        super(Map.of(MemoryModuleType.ATTACK_TARGET, MemoryModuleState.VALUE_PRESENT, MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.BREEZE_JUMP_COOLDOWN, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.BREEZE_SHOOT, MemoryModuleState.VALUE_ABSENT));
    }

    @Override
    protected boolean shouldRun(ServerWorld arg, BreezeEntity arg2) {
        return arg2.isOnGround() && !arg2.isTouchingWater() && arg2.getPose() == EntityPose.STANDING;
    }

    @Override
    protected void run(ServerWorld arg, BreezeEntity arg2, long l) {
        Vec3d lv3;
        LivingEntity lv = arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (lv == null) {
            return;
        }
        boolean bl = arg2.isWithinShortRange(lv.getPos());
        Vec3d lv2 = null;
        if (bl && (lv3 = NoPenaltyTargeting.findFrom(arg2, 5, 5, lv.getPos())) != null && BreezeMovementUtil.canMoveTo(arg2, lv3) && lv.squaredDistanceTo(lv3.x, lv3.y, lv3.z) > lv.squaredDistanceTo(arg2)) {
            lv2 = lv3;
        }
        if (lv2 == null) {
            lv2 = arg2.getRandom().nextBoolean() ? BreezeMovementUtil.getRandomPosBehindTarget(lv, arg2.getRandom()) : BreezeSlideTowardsTargetTask.getRandomPosInMediumRange(arg2, lv);
        }
        arg2.getBrain().remember(MemoryModuleType.WALK_TARGET, new WalkTarget(BlockPos.ofFloored(lv2), 0.6f, 1));
    }

    private static Vec3d getRandomPosInMediumRange(BreezeEntity breeze, LivingEntity target) {
        Vec3d lv = target.getPos().subtract(breeze.getPos());
        double d = lv.length() - MathHelper.lerp(breeze.getRandom().nextDouble(), 8.0, 4.0);
        Vec3d lv2 = lv.normalize().multiply(d, d, d);
        return breeze.getPos().add(lv2);
    }

    @Override
    protected /* synthetic */ void run(ServerWorld world, LivingEntity entity, long time) {
        this.run(world, (BreezeEntity)entity, time);
    }
}

