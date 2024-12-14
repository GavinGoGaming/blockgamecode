/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.mob.BreezeEntity;
import net.minecraft.entity.projectile.BreezeWindChargeEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class BreezeShootTask
extends MultiTickTask<BreezeEntity> {
    private static final int MIN_SQUARED_RANGE = 4;
    private static final int MAX_SQUARED_RANGE = 256;
    private static final int BASE_PROJECTILE_DIVERGENCY = 5;
    private static final int PROJECTILE_DIVERGENCY_DIFFICULTY_MODIFIER = 4;
    private static final float PROJECTILE_SPEED = 0.7f;
    private static final int SHOOT_CHARGING_EXPIRY = Math.round(15.0f);
    private static final int RECOVER_EXPIRY = Math.round(4.0f);
    private static final int SHOOT_COOLDOWN_EXPIRY = Math.round(10.0f);

    @VisibleForTesting
    public BreezeShootTask() {
        super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryModuleState.VALUE_PRESENT, MemoryModuleType.BREEZE_SHOOT_COOLDOWN, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.BREEZE_SHOOT_CHARGING, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.BREEZE_SHOOT_RECOVER, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.BREEZE_SHOOT, MemoryModuleState.VALUE_PRESENT, MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.BREEZE_JUMP_TARGET, MemoryModuleState.VALUE_ABSENT), SHOOT_CHARGING_EXPIRY + 1 + RECOVER_EXPIRY);
    }

    @Override
    protected boolean shouldRun(ServerWorld arg, BreezeEntity arg2) {
        if (arg2.getPose() != EntityPose.STANDING) {
            return false;
        }
        return arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET).map(target -> BreezeShootTask.isTargetWithinRange(arg2, target)).map(withinRange -> {
            if (!withinRange.booleanValue()) {
                arg2.getBrain().forget(MemoryModuleType.BREEZE_SHOOT);
            }
            return withinRange;
        }).orElse(false);
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld arg, BreezeEntity arg2, long l) {
        return arg2.getBrain().hasMemoryModule(MemoryModuleType.ATTACK_TARGET) && arg2.getBrain().hasMemoryModule(MemoryModuleType.BREEZE_SHOOT);
    }

    @Override
    protected void run(ServerWorld arg, BreezeEntity arg2, long l) {
        arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET).ifPresent(target -> arg2.setPose(EntityPose.SHOOTING));
        arg2.getBrain().remember(MemoryModuleType.BREEZE_SHOOT_CHARGING, Unit.INSTANCE, SHOOT_CHARGING_EXPIRY);
        arg2.playSound(SoundEvents.ENTITY_BREEZE_INHALE, 1.0f, 1.0f);
    }

    @Override
    protected void finishRunning(ServerWorld arg, BreezeEntity arg2, long l) {
        if (arg2.getPose() == EntityPose.SHOOTING) {
            arg2.setPose(EntityPose.STANDING);
        }
        arg2.getBrain().remember(MemoryModuleType.BREEZE_SHOOT_COOLDOWN, Unit.INSTANCE, SHOOT_COOLDOWN_EXPIRY);
        arg2.getBrain().forget(MemoryModuleType.BREEZE_SHOOT);
    }

    @Override
    protected void keepRunning(ServerWorld arg, BreezeEntity arg2, long l) {
        Brain<BreezeEntity> lv = arg2.getBrain();
        LivingEntity lv2 = lv.getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (lv2 == null) {
            return;
        }
        arg2.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, lv2.getPos());
        if (lv.getOptionalRegisteredMemory(MemoryModuleType.BREEZE_SHOOT_CHARGING).isPresent() || lv.getOptionalRegisteredMemory(MemoryModuleType.BREEZE_SHOOT_RECOVER).isPresent()) {
            return;
        }
        lv.remember(MemoryModuleType.BREEZE_SHOOT_RECOVER, Unit.INSTANCE, RECOVER_EXPIRY);
        if (BreezeShootTask.isFacingTarget(arg2, lv2)) {
            double d = lv2.getX() - arg2.getX();
            double e = lv2.getBodyY(lv2.hasVehicle() ? 0.8 : 0.3) - arg2.getBodyY(0.5);
            double f = lv2.getZ() - arg2.getZ();
            BreezeWindChargeEntity lv3 = new BreezeWindChargeEntity(arg2, (World)arg);
            arg2.playSound(SoundEvents.ENTITY_BREEZE_SHOOT, 1.5f, 1.0f);
            lv3.setVelocity(d, e, f, 0.7f, 5 - arg.getDifficulty().getId() * 4);
            arg.spawnEntity(lv3);
        }
    }

    @VisibleForTesting
    public static boolean isFacingTarget(BreezeEntity breeze, LivingEntity target) {
        Vec3d lv2;
        Vec3d lv = breeze.getRotationVec(1.0f);
        return lv.dotProduct(lv2 = target.getPos().subtract(breeze.getPos()).normalize()) > 0.5;
    }

    private static boolean isTargetWithinRange(BreezeEntity breeze, LivingEntity target) {
        double d = breeze.getPos().squaredDistanceTo(target.getPos());
        return d > 4.0 && d < 256.0;
    }

    @Override
    protected /* synthetic */ void finishRunning(ServerWorld world, LivingEntity entity, long time) {
        this.finishRunning(world, (BreezeEntity)entity, time);
    }

    @Override
    protected /* synthetic */ void keepRunning(ServerWorld world, LivingEntity entity, long time) {
        this.keepRunning(world, (BreezeEntity)entity, time);
    }

    @Override
    protected /* synthetic */ void run(ServerWorld world, LivingEntity entity, long time) {
        this.run(world, (BreezeEntity)entity, time);
    }
}

