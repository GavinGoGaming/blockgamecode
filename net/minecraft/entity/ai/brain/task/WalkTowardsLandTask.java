/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import org.apache.commons.lang3.mutable.MutableLong;

public class WalkTowardsLandTask {
    private static final int TASK_COOLDOWN = 60;

    public static Task<PathAwareEntity> create(int range, float speed) {
        MutableLong mutableLong = new MutableLong(0L);
        return TaskTriggerer.task(context -> context.group(context.queryMemoryAbsent(MemoryModuleType.ATTACK_TARGET), context.queryMemoryAbsent(MemoryModuleType.WALK_TARGET), context.queryMemoryOptional(MemoryModuleType.LOOK_TARGET)).apply(context, (attackTarget, walkTarget, lookTarget) -> (world, entity, time) -> {
            if (!world.getFluidState(entity.getBlockPos()).isIn(FluidTags.WATER)) {
                return false;
            }
            if (time < mutableLong.getValue()) {
                mutableLong.setValue(time + 60L);
                return true;
            }
            BlockPos lv = entity.getBlockPos();
            BlockPos.Mutable lv2 = new BlockPos.Mutable();
            ShapeContext lv3 = ShapeContext.of(entity);
            for (BlockPos lv4 : BlockPos.iterateOutwards(lv, range, range, range)) {
                if (lv4.getX() == lv.getX() && lv4.getZ() == lv.getZ()) continue;
                BlockState lv5 = world.getBlockState(lv4);
                BlockState lv6 = world.getBlockState(lv2.set((Vec3i)lv4, Direction.DOWN));
                if (lv5.isOf(Blocks.WATER) || !world.getFluidState(lv4).isEmpty() || !lv5.getCollisionShape(world, lv4, lv3).isEmpty() || !lv6.isSideSolidFullSquare(world, lv2, Direction.UP)) continue;
                BlockPos lv7 = lv4.toImmutable();
                lookTarget.remember(new BlockPosLookTarget(lv7));
                walkTarget.remember(new WalkTarget(new BlockPosLookTarget(lv7), speed, 1));
                break;
            }
            mutableLong.setValue(time + 60L);
            return true;
        }));
    }
}

