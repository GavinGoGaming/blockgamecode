/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.ai.brain.task.OpenDoorsTask;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;

public class SleepTask
extends MultiTickTask<LivingEntity> {
    public static final int RUN_TIME = 100;
    private long startTime;

    public SleepTask() {
        super(ImmutableMap.of(MemoryModuleType.HOME, MemoryModuleState.VALUE_PRESENT, MemoryModuleType.LAST_WOKEN, MemoryModuleState.REGISTERED));
    }

    @Override
    protected boolean shouldRun(ServerWorld world, LivingEntity entity) {
        long l;
        if (entity.hasVehicle()) {
            return false;
        }
        Brain<?> lv = entity.getBrain();
        GlobalPos lv2 = lv.getOptionalRegisteredMemory(MemoryModuleType.HOME).get();
        if (world.getRegistryKey() != lv2.dimension()) {
            return false;
        }
        Optional<Long> optional = lv.getOptionalRegisteredMemory(MemoryModuleType.LAST_WOKEN);
        if (optional.isPresent() && (l = world.getTime() - optional.get()) > 0L && l < 100L) {
            return false;
        }
        BlockState lv3 = world.getBlockState(lv2.pos());
        return lv2.pos().isWithinDistance(entity.getPos(), 2.0) && lv3.isIn(BlockTags.BEDS) && lv3.get(BedBlock.OCCUPIED) == false;
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld world, LivingEntity entity, long time) {
        Optional<GlobalPos> optional = entity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.HOME);
        if (optional.isEmpty()) {
            return false;
        }
        BlockPos lv = optional.get().pos();
        return entity.getBrain().hasActivity(Activity.REST) && entity.getY() > (double)lv.getY() + 0.4 && lv.isWithinDistance(entity.getPos(), 1.14);
    }

    @Override
    protected void run(ServerWorld world, LivingEntity entity, long time) {
        if (time > this.startTime) {
            Brain<Collection<Object>> lv = entity.getBrain();
            if (lv.hasMemoryModule(MemoryModuleType.DOORS_TO_CLOSE)) {
                Set<GlobalPos> set = lv.getOptionalRegisteredMemory(MemoryModuleType.DOORS_TO_CLOSE).get();
                Optional<List<LivingEntity>> optional = lv.hasMemoryModule(MemoryModuleType.MOBS) ? lv.getOptionalRegisteredMemory(MemoryModuleType.MOBS) : Optional.empty();
                OpenDoorsTask.pathToDoor(world, entity, null, null, set, optional);
            }
            entity.sleep(entity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.HOME).get().pos());
        }
    }

    @Override
    protected boolean isTimeLimitExceeded(long time) {
        return false;
    }

    @Override
    protected void finishRunning(ServerWorld world, LivingEntity entity, long time) {
        if (entity.isSleeping()) {
            entity.wakeUp();
            this.startTime = time + 40L;
        }
    }
}

