/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.passive.FrogEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;

public class FrogEatEntityTask
extends MultiTickTask<FrogEntity> {
    public static final int RUN_TIME = 100;
    public static final int CATCH_DURATION = 6;
    public static final int EAT_DURATION = 10;
    private static final float MAX_DISTANCE = 1.75f;
    private static final float VELOCITY_MULTIPLIER = 0.75f;
    public static final int UNREACHABLE_TONGUE_TARGETS_START_TIME = 100;
    public static final int MAX_UNREACHABLE_TONGUE_TARGETS = 5;
    private int eatTick;
    private int moveToTargetTick;
    private final SoundEvent tongueSound;
    private final SoundEvent eatSound;
    private Vec3d targetPos;
    private Phase phase = Phase.DONE;

    public FrogEatEntityTask(SoundEvent tongueSound, SoundEvent eatSound) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED, MemoryModuleType.ATTACK_TARGET, MemoryModuleState.VALUE_PRESENT, MemoryModuleType.IS_PANICKING, MemoryModuleState.VALUE_ABSENT), 100);
        this.tongueSound = tongueSound;
        this.eatSound = eatSound;
    }

    @Override
    protected boolean shouldRun(ServerWorld arg, FrogEntity arg2) {
        LivingEntity lv = arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET).get();
        boolean bl = this.isTargetReachable(arg2, lv);
        if (!bl) {
            arg2.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
            this.markTargetAsUnreachable(arg2, lv);
        }
        return bl && arg2.getPose() != EntityPose.CROAKING && FrogEntity.isValidFrogFood(lv);
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld arg, FrogEntity arg2, long l) {
        return arg2.getBrain().hasMemoryModule(MemoryModuleType.ATTACK_TARGET) && this.phase != Phase.DONE && !arg2.getBrain().hasMemoryModule(MemoryModuleType.IS_PANICKING);
    }

    @Override
    protected void run(ServerWorld arg, FrogEntity arg2, long l) {
        LivingEntity lv = arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET).get();
        LookTargetUtil.lookAt(arg2, lv);
        arg2.setFrogTarget(lv);
        arg2.getBrain().remember(MemoryModuleType.WALK_TARGET, new WalkTarget(lv.getPos(), 2.0f, 0));
        this.moveToTargetTick = 10;
        this.phase = Phase.MOVE_TO_TARGET;
    }

    @Override
    protected void finishRunning(ServerWorld arg, FrogEntity arg2, long l) {
        arg2.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
        arg2.clearFrogTarget();
        arg2.setPose(EntityPose.STANDING);
    }

    private void eat(ServerWorld world, FrogEntity frog) {
        Entity lv;
        world.playSoundFromEntity(null, frog, this.eatSound, SoundCategory.NEUTRAL, 2.0f, 1.0f);
        Optional<Entity> optional = frog.getFrogTarget();
        if (optional.isPresent() && (lv = optional.get()).isAlive()) {
            frog.tryAttack(lv);
            if (!lv.isAlive()) {
                lv.remove(Entity.RemovalReason.KILLED);
            }
        }
    }

    @Override
    protected void keepRunning(ServerWorld arg, FrogEntity arg2, long l) {
        LivingEntity lv = arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET).get();
        arg2.setFrogTarget(lv);
        switch (this.phase.ordinal()) {
            case 0: {
                if (lv.distanceTo(arg2) < 1.75f) {
                    arg.playSoundFromEntity(null, arg2, this.tongueSound, SoundCategory.NEUTRAL, 2.0f, 1.0f);
                    arg2.setPose(EntityPose.USING_TONGUE);
                    lv.setVelocity(lv.getPos().relativize(arg2.getPos()).normalize().multiply(0.75));
                    this.targetPos = lv.getPos();
                    this.eatTick = 0;
                    this.phase = Phase.CATCH_ANIMATION;
                    break;
                }
                if (this.moveToTargetTick <= 0) {
                    arg2.getBrain().remember(MemoryModuleType.WALK_TARGET, new WalkTarget(lv.getPos(), 2.0f, 0));
                    this.moveToTargetTick = 10;
                    break;
                }
                --this.moveToTargetTick;
                break;
            }
            case 1: {
                if (this.eatTick++ < 6) break;
                this.phase = Phase.EAT_ANIMATION;
                this.eat(arg, arg2);
                break;
            }
            case 2: {
                if (this.eatTick >= 10) {
                    this.phase = Phase.DONE;
                    break;
                }
                ++this.eatTick;
                break;
            }
        }
    }

    private boolean isTargetReachable(FrogEntity entity, LivingEntity target) {
        Path lv = entity.getNavigation().findPathTo(target, 0);
        return lv != null && lv.getManhattanDistanceFromTarget() < 1.75f;
    }

    private void markTargetAsUnreachable(FrogEntity entity, LivingEntity target) {
        boolean bl;
        List list = entity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.UNREACHABLE_TONGUE_TARGETS).orElseGet(ArrayList::new);
        boolean bl2 = bl = !list.contains(target.getUuid());
        if (list.size() == 5 && bl) {
            list.remove(0);
        }
        if (bl) {
            list.add(target.getUuid());
        }
        entity.getBrain().remember(MemoryModuleType.UNREACHABLE_TONGUE_TARGETS, list, 100L);
    }

    @Override
    protected /* synthetic */ void finishRunning(ServerWorld world, LivingEntity entity, long time) {
        this.finishRunning(world, (FrogEntity)entity, time);
    }

    @Override
    protected /* synthetic */ void keepRunning(ServerWorld world, LivingEntity entity, long time) {
        this.keepRunning(world, (FrogEntity)entity, time);
    }

    @Override
    protected /* synthetic */ void run(ServerWorld world, LivingEntity entity, long time) {
        this.run(world, (FrogEntity)entity, time);
    }

    static enum Phase {
        MOVE_TO_TARGET,
        CATCH_ANIMATION,
        EAT_ANIMATION,
        DONE;

    }
}

