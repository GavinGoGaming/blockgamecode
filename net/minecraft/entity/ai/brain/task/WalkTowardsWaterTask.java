/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

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

public class WalkTowardsWaterTask {
    public static Task<PathAwareEntity> create(int range, float speed) {
        MutableLong mutableLong = new MutableLong(0L);
        return TaskTriggerer.task(context -> context.group(context.queryMemoryAbsent(MemoryModuleType.ATTACK_TARGET), context.queryMemoryAbsent(MemoryModuleType.WALK_TARGET), context.queryMemoryOptional(MemoryModuleType.LOOK_TARGET)).apply(context, (attackTarget, walkTarget, lookTarget) -> (world, entity, time) -> {
            if (world.getFluidState(entity.getBlockPos()).isIn(FluidTags.WATER)) {
                return false;
            }
            if (time < mutableLong.getValue()) {
                mutableLong.setValue(time + 40L);
                return true;
            }
            ShapeContext lv = ShapeContext.of(entity);
            BlockPos lv2 = entity.getBlockPos();
            BlockPos.Mutable lv3 = new BlockPos.Mutable();
            block0: for (BlockPos lv4 : BlockPos.iterateOutwards(lv2, range, range, range)) {
                if (lv4.getX() == lv2.getX() && lv4.getZ() == lv2.getZ() || !world.getBlockState(lv4).getCollisionShape(world, lv4, lv).isEmpty() || world.getBlockState(lv3.set((Vec3i)lv4, Direction.DOWN)).getCollisionShape(world, lv4, lv).isEmpty()) continue;
                for (Direction lv5 : Direction.Type.HORIZONTAL) {
                    lv3.set((Vec3i)lv4, lv5);
                    if (!world.getBlockState(lv3).isAir() || !world.getBlockState(lv3.move(Direction.DOWN)).isOf(Blocks.WATER)) continue;
                    lookTarget.remember(new BlockPosLookTarget(lv4));
                    walkTarget.remember(new WalkTarget(new BlockPosLookTarget(lv4), speed, 0));
                    break block0;
                }
            }
            mutableLong.setValue(time + 40L);
            return true;
        }));
    }
}

