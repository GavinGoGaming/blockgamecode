/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.ai.brain.task;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.BreezeMovementUtil;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.ai.brain.task.StayAboveWaterTask;
import net.minecraft.entity.mob.BreezeEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.LongJumpUtil;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.RaycastContext;
import org.jetbrains.annotations.Nullable;

public class BreezeJumpTask
extends MultiTickTask<BreezeEntity> {
    private static final int REQUIRED_SPACE_ABOVE = 4;
    private static final int JUMP_COOLDOWN_EXPIRY = 10;
    private static final int JUMP_COOLDOWN_EXPIRY_WHEN_HURT = 2;
    private static final int JUMP_INHALING_EXPIRY = Math.round(10.0f);
    private static final float MAX_JUMP_VELOCITY = 1.4f;
    private static final ObjectArrayList<Integer> POSSIBLE_JUMP_ANGLES = new ObjectArrayList<Integer>(Lists.newArrayList(40, 55, 60, 75, 80));

    @VisibleForTesting
    public BreezeJumpTask() {
        super(Map.of(MemoryModuleType.ATTACK_TARGET, MemoryModuleState.VALUE_PRESENT, MemoryModuleType.BREEZE_JUMP_COOLDOWN, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.BREEZE_JUMP_INHALING, MemoryModuleState.REGISTERED, MemoryModuleType.BREEZE_JUMP_TARGET, MemoryModuleState.REGISTERED, MemoryModuleType.BREEZE_SHOOT, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.BREEZE_LEAVING_WATER, MemoryModuleState.REGISTERED), 200);
    }

    public static boolean shouldJump(ServerWorld world, BreezeEntity breeze) {
        if (!breeze.isOnGround() && !breeze.isTouchingWater()) {
            return false;
        }
        if (StayAboveWaterTask.isUnderwater(breeze)) {
            return false;
        }
        if (breeze.getBrain().isMemoryInState(MemoryModuleType.BREEZE_JUMP_TARGET, MemoryModuleState.VALUE_PRESENT)) {
            return true;
        }
        LivingEntity lv = breeze.getBrain().getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (lv == null) {
            return false;
        }
        if (BreezeJumpTask.isTargetOutOfRange(breeze, lv)) {
            breeze.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
            return false;
        }
        if (BreezeJumpTask.isTargetTooClose(breeze, lv)) {
            return false;
        }
        if (!BreezeJumpTask.hasRoomToJump(world, breeze)) {
            return false;
        }
        BlockPos lv2 = BreezeJumpTask.getPosToJumpTo(breeze, BreezeMovementUtil.getRandomPosBehindTarget(lv, breeze.getRandom()));
        if (lv2 == null) {
            return false;
        }
        BlockState lv3 = world.getBlockState(lv2.down());
        if (breeze.getType().isInvalidSpawn(lv3)) {
            return false;
        }
        if (!BreezeMovementUtil.canMoveTo(breeze, lv2.toCenterPos()) && !BreezeMovementUtil.canMoveTo(breeze, lv2.up(4).toCenterPos())) {
            return false;
        }
        breeze.getBrain().remember(MemoryModuleType.BREEZE_JUMP_TARGET, lv2);
        return true;
    }

    @Override
    protected boolean shouldRun(ServerWorld arg, BreezeEntity arg2) {
        return BreezeJumpTask.shouldJump(arg, arg2);
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld arg, BreezeEntity arg2, long l) {
        return arg2.getPose() != EntityPose.STANDING && !arg2.getBrain().hasMemoryModule(MemoryModuleType.BREEZE_JUMP_COOLDOWN);
    }

    @Override
    protected void run(ServerWorld arg, BreezeEntity arg2, long l) {
        if (arg2.getBrain().isMemoryInState(MemoryModuleType.BREEZE_JUMP_INHALING, MemoryModuleState.VALUE_ABSENT)) {
            arg2.getBrain().remember(MemoryModuleType.BREEZE_JUMP_INHALING, Unit.INSTANCE, JUMP_INHALING_EXPIRY);
        }
        arg2.setPose(EntityPose.INHALING);
        arg.playSoundFromEntity(null, arg2, SoundEvents.ENTITY_BREEZE_CHARGE, SoundCategory.HOSTILE, 1.0f, 1.0f);
        arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.BREEZE_JUMP_TARGET).ifPresent(jumpTarget -> arg2.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, jumpTarget.toCenterPos()));
    }

    @Override
    protected void keepRunning(ServerWorld arg, BreezeEntity arg2, long l) {
        boolean bl = arg2.isTouchingWater();
        if (!bl && arg2.getBrain().isMemoryInState(MemoryModuleType.BREEZE_LEAVING_WATER, MemoryModuleState.VALUE_PRESENT)) {
            arg2.getBrain().forget(MemoryModuleType.BREEZE_LEAVING_WATER);
        }
        if (BreezeJumpTask.shouldStopInhalingPose(arg2)) {
            Vec3d lv = arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.BREEZE_JUMP_TARGET).flatMap(jumpTarget -> BreezeJumpTask.getJumpingVelocity(arg2, arg2.getRandom(), Vec3d.ofBottomCenter(jumpTarget))).orElse(null);
            if (lv == null) {
                arg2.setPose(EntityPose.STANDING);
                return;
            }
            if (bl) {
                arg2.getBrain().remember(MemoryModuleType.BREEZE_LEAVING_WATER, Unit.INSTANCE);
            }
            arg2.playSound(SoundEvents.ENTITY_BREEZE_JUMP, 1.0f, 1.0f);
            arg2.setPose(EntityPose.LONG_JUMPING);
            arg2.setYaw(arg2.bodyYaw);
            arg2.setNoDrag(true);
            arg2.setVelocity(lv);
        } else if (BreezeJumpTask.shouldStopLongJumpingPose(arg2)) {
            arg2.playSound(SoundEvents.ENTITY_BREEZE_LAND, 1.0f, 1.0f);
            arg2.setPose(EntityPose.STANDING);
            arg2.setNoDrag(false);
            boolean bl2 = arg2.getBrain().hasMemoryModule(MemoryModuleType.HURT_BY);
            arg2.getBrain().remember(MemoryModuleType.BREEZE_JUMP_COOLDOWN, Unit.INSTANCE, bl2 ? 2L : 10L);
            arg2.getBrain().remember(MemoryModuleType.BREEZE_SHOOT, Unit.INSTANCE, 100L);
        }
    }

    @Override
    protected void finishRunning(ServerWorld arg, BreezeEntity arg2, long l) {
        if (arg2.getPose() == EntityPose.LONG_JUMPING || arg2.getPose() == EntityPose.INHALING) {
            arg2.setPose(EntityPose.STANDING);
        }
        arg2.getBrain().forget(MemoryModuleType.BREEZE_JUMP_TARGET);
        arg2.getBrain().forget(MemoryModuleType.BREEZE_JUMP_INHALING);
        arg2.getBrain().forget(MemoryModuleType.BREEZE_LEAVING_WATER);
    }

    private static boolean shouldStopInhalingPose(BreezeEntity breeze) {
        return breeze.getBrain().getOptionalRegisteredMemory(MemoryModuleType.BREEZE_JUMP_INHALING).isEmpty() && breeze.getPose() == EntityPose.INHALING;
    }

    private static boolean shouldStopLongJumpingPose(BreezeEntity breeze) {
        boolean bl = breeze.getPose() == EntityPose.LONG_JUMPING;
        boolean bl2 = breeze.isOnGround();
        boolean bl3 = breeze.isTouchingWater() && breeze.getBrain().isMemoryInState(MemoryModuleType.BREEZE_LEAVING_WATER, MemoryModuleState.VALUE_ABSENT);
        return bl && (bl2 || bl3);
    }

    @Nullable
    private static BlockPos getPosToJumpTo(LivingEntity breeze, Vec3d pos) {
        RaycastContext lv = new RaycastContext(pos, pos.offset(Direction.DOWN, 10.0), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, breeze);
        BlockHitResult lv2 = breeze.getWorld().raycast(lv);
        if (((HitResult)lv2).getType() == HitResult.Type.BLOCK) {
            return BlockPos.ofFloored(lv2.getPos()).up();
        }
        RaycastContext lv3 = new RaycastContext(pos, pos.offset(Direction.UP, 10.0), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, breeze);
        BlockHitResult lv4 = breeze.getWorld().raycast(lv3);
        if (((HitResult)lv4).getType() == HitResult.Type.BLOCK) {
            return BlockPos.ofFloored(lv4.getPos()).up();
        }
        return null;
    }

    private static boolean isTargetOutOfRange(BreezeEntity breeze, LivingEntity target) {
        return !target.isInRange(breeze, 24.0);
    }

    private static boolean isTargetTooClose(BreezeEntity breeze, LivingEntity target) {
        return target.distanceTo(breeze) - 4.0f <= 0.0f;
    }

    private static boolean hasRoomToJump(ServerWorld world, BreezeEntity breeze) {
        BlockPos lv = breeze.getBlockPos();
        for (int i = 1; i <= 4; ++i) {
            BlockPos lv2 = lv.offset(Direction.UP, i);
            if (world.getBlockState(lv2).isAir() || world.getFluidState(lv2).isIn(FluidTags.WATER)) continue;
            return false;
        }
        return true;
    }

    private static Optional<Vec3d> getJumpingVelocity(BreezeEntity breeze, Random random, Vec3d jumpTarget) {
        List<Integer> list = Util.copyShuffled(POSSIBLE_JUMP_ANGLES, random);
        for (int i : list) {
            Optional<Vec3d> optional = LongJumpUtil.getJumpingVelocity(breeze, jumpTarget, 1.4f, i, false);
            if (!optional.isPresent()) continue;
            return optional;
        }
        return Optional.empty();
    }

    @Override
    protected /* synthetic */ boolean shouldRun(ServerWorld world, LivingEntity entity) {
        return this.shouldRun(world, (BreezeEntity)entity);
    }

    @Override
    protected /* synthetic */ void finishRunning(ServerWorld world, LivingEntity entity, long time) {
        this.finishRunning(world, (BreezeEntity)entity, time);
    }

    @Override
    protected /* synthetic */ void run(ServerWorld world, LivingEntity entity, long time) {
        this.run(world, (BreezeEntity)entity, time);
    }
}

