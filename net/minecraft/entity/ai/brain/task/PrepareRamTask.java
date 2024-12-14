/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.EntityLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class PrepareRamTask<E extends PathAwareEntity>
extends MultiTickTask<E> {
    public static final int RUN_TIME = 160;
    private final ToIntFunction<E> cooldownFactory;
    private final int minRamDistance;
    private final int maxRamDistance;
    private final float speed;
    private final TargetPredicate targetPredicate;
    private final int prepareTime;
    private final Function<E, SoundEvent> soundFactory;
    private Optional<Long> prepareStartTime = Optional.empty();
    private Optional<Ram> ram = Optional.empty();

    public PrepareRamTask(ToIntFunction<E> cooldownFactory, int minDistance, int maxDistance, float speed, TargetPredicate targetPredicate, int prepareTime, Function<E, SoundEvent> soundFactory) {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED, MemoryModuleType.RAM_COOLDOWN_TICKS, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.VISIBLE_MOBS, MemoryModuleState.VALUE_PRESENT, MemoryModuleType.RAM_TARGET, MemoryModuleState.VALUE_ABSENT), 160);
        this.cooldownFactory = cooldownFactory;
        this.minRamDistance = minDistance;
        this.maxRamDistance = maxDistance;
        this.speed = speed;
        this.targetPredicate = targetPredicate;
        this.prepareTime = prepareTime;
        this.soundFactory = soundFactory;
    }

    @Override
    protected void run(ServerWorld arg, PathAwareEntity arg2, long l) {
        Brain<?> lv = arg2.getBrain();
        lv.getOptionalRegisteredMemory(MemoryModuleType.VISIBLE_MOBS).flatMap(mob -> mob.findFirst(mobx -> this.targetPredicate.test(arg2, (LivingEntity)mobx))).ifPresent(mob -> this.findRam(arg2, (LivingEntity)mob));
    }

    @Override
    protected void finishRunning(ServerWorld arg, E arg2, long l) {
        Brain<Vec3d> lv = ((LivingEntity)arg2).getBrain();
        if (!lv.hasMemoryModule(MemoryModuleType.RAM_TARGET)) {
            arg.sendEntityStatus((Entity)arg2, EntityStatuses.FINISH_RAM);
            lv.remember(MemoryModuleType.RAM_COOLDOWN_TICKS, this.cooldownFactory.applyAsInt(arg2));
        }
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld arg, PathAwareEntity arg2, long l) {
        return this.ram.isPresent() && this.ram.get().getEntity().isAlive();
    }

    @Override
    protected void keepRunning(ServerWorld arg, E arg2, long l) {
        boolean bl;
        if (this.ram.isEmpty()) {
            return;
        }
        ((LivingEntity)arg2).getBrain().remember(MemoryModuleType.WALK_TARGET, new WalkTarget(this.ram.get().getStart(), this.speed, 0));
        ((LivingEntity)arg2).getBrain().remember(MemoryModuleType.LOOK_TARGET, new EntityLookTarget(this.ram.get().getEntity(), true));
        boolean bl2 = bl = !this.ram.get().getEntity().getBlockPos().equals(this.ram.get().getEnd());
        if (bl) {
            arg.sendEntityStatus((Entity)arg2, EntityStatuses.FINISH_RAM);
            ((MobEntity)arg2).getNavigation().stop();
            this.findRam((PathAwareEntity)arg2, this.ram.get().entity);
        } else {
            BlockPos lv = ((Entity)arg2).getBlockPos();
            if (lv.equals(this.ram.get().getStart())) {
                arg.sendEntityStatus((Entity)arg2, EntityStatuses.PREPARE_RAM);
                if (this.prepareStartTime.isEmpty()) {
                    this.prepareStartTime = Optional.of(l);
                }
                if (l - this.prepareStartTime.get() >= (long)this.prepareTime) {
                    ((LivingEntity)arg2).getBrain().remember(MemoryModuleType.RAM_TARGET, this.calculateRamTarget(lv, this.ram.get().getEnd()));
                    arg.playSoundFromEntity(null, (Entity)arg2, this.soundFactory.apply(arg2), SoundCategory.NEUTRAL, 1.0f, ((LivingEntity)arg2).getSoundPitch());
                    this.ram = Optional.empty();
                }
            }
        }
    }

    private Vec3d calculateRamTarget(BlockPos start, BlockPos end) {
        double d = 0.5;
        double e = 0.5 * (double)MathHelper.sign(end.getX() - start.getX());
        double f = 0.5 * (double)MathHelper.sign(end.getZ() - start.getZ());
        return Vec3d.ofBottomCenter(end).add(e, 0.0, f);
    }

    private Optional<BlockPos> findRamStart(PathAwareEntity entity, LivingEntity target) {
        BlockPos lv = target.getBlockPos();
        if (!this.canReach(entity, lv)) {
            return Optional.empty();
        }
        ArrayList<BlockPos> list = Lists.newArrayList();
        BlockPos.Mutable lv2 = lv.mutableCopy();
        for (Direction lv3 : Direction.Type.HORIZONTAL) {
            lv2.set(lv);
            for (int i = 0; i < this.maxRamDistance; ++i) {
                if (this.canReach(entity, lv2.move(lv3))) continue;
                lv2.move(lv3.getOpposite());
                break;
            }
            if (lv2.getManhattanDistance(lv) < this.minRamDistance) continue;
            list.add(lv2.toImmutable());
        }
        EntityNavigation lv4 = entity.getNavigation();
        return list.stream().sorted(Comparator.comparingDouble(entity.getBlockPos()::getSquaredDistance)).filter(start -> {
            Path lv = lv4.findPathTo((BlockPos)start, 0);
            return lv != null && lv.reachesTarget();
        }).findFirst();
    }

    private boolean canReach(PathAwareEntity entity, BlockPos target) {
        return entity.getNavigation().isValidPosition(target) && entity.getPathfindingPenalty(LandPathNodeMaker.getLandNodeType(entity, target)) == 0.0f;
    }

    private void findRam(PathAwareEntity entity, LivingEntity target) {
        this.prepareStartTime = Optional.empty();
        this.ram = this.findRamStart(entity, target).map(start -> new Ram((BlockPos)start, target.getBlockPos(), target));
    }

    @Override
    protected /* synthetic */ boolean shouldKeepRunning(ServerWorld world, LivingEntity entity, long time) {
        return this.shouldKeepRunning(world, (PathAwareEntity)entity, time);
    }

    @Override
    protected /* synthetic */ void keepRunning(ServerWorld world, LivingEntity entity, long time) {
        this.keepRunning(world, (E)((PathAwareEntity)entity), time);
    }

    @Override
    protected /* synthetic */ void run(ServerWorld world, LivingEntity entity, long time) {
        this.run(world, (PathAwareEntity)entity, time);
    }

    public static class Ram {
        private final BlockPos start;
        private final BlockPos end;
        final LivingEntity entity;

        public Ram(BlockPos start, BlockPos end, LivingEntity entity) {
            this.start = start;
            this.end = end;
            this.entity = entity;
        }

        public BlockPos getStart() {
            return this.start;
        }

        public BlockPos getEnd() {
            return this.end;
        }

        public LivingEntity getEntity() {
            return this.entity;
        }
    }
}

