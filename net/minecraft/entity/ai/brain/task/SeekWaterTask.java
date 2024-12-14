/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.mutable.MutableLong;

public class SeekWaterTask {
    public static Task<PathAwareEntity> create(int range, float speed) {
        MutableLong mutableLong = new MutableLong(0L);
        return TaskTriggerer.task(context -> context.group(context.queryMemoryAbsent(MemoryModuleType.ATTACK_TARGET), context.queryMemoryAbsent(MemoryModuleType.WALK_TARGET), context.queryMemoryOptional(MemoryModuleType.LOOK_TARGET)).apply(context, (attackTarget, walkTarget, lookTarget) -> (world, entity, time) -> {
            if (world.getFluidState(entity.getBlockPos()).isIn(FluidTags.WATER)) {
                return false;
            }
            if (time < mutableLong.getValue()) {
                mutableLong.setValue(time + 20L + 2L);
                return true;
            }
            BlockPos lv = null;
            BlockPos lv2 = null;
            BlockPos lv3 = entity.getBlockPos();
            Iterable<BlockPos> iterable = BlockPos.iterateOutwards(lv3, range, range, range);
            for (BlockPos lv4 : iterable) {
                if (lv4.getX() == lv3.getX() && lv4.getZ() == lv3.getZ()) continue;
                BlockState lv5 = entity.getWorld().getBlockState(lv4.up());
                BlockState lv6 = entity.getWorld().getBlockState(lv4);
                if (!lv6.isOf(Blocks.WATER)) continue;
                if (lv5.isAir()) {
                    lv = lv4.toImmutable();
                    break;
                }
                if (lv2 != null || lv4.isWithinDistance(entity.getPos(), 1.5)) continue;
                lv2 = lv4.toImmutable();
            }
            if (lv == null) {
                lv = lv2;
            }
            if (lv != null) {
                lookTarget.remember(new BlockPosLookTarget(lv));
                walkTarget.remember(new WalkTarget(new BlockPosLookTarget(lv), speed, 0));
            }
            mutableLong.setValue(time + 40L);
            return true;
        }));
    }
}

