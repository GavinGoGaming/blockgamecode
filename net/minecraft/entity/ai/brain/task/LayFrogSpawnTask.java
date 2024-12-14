/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.fluid.Fluids;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.event.GameEvent;

public class LayFrogSpawnTask {
    public static Task<LivingEntity> create(Block frogSpawn) {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryAbsent(MemoryModuleType.ATTACK_TARGET), context.queryMemoryValue(MemoryModuleType.WALK_TARGET), context.queryMemoryValue(MemoryModuleType.IS_PREGNANT)).apply(context, (attackTarget, walkTarget, isPregnant) -> (world, entity, time) -> {
            if (entity.isTouchingWater() || !entity.isOnGround()) {
                return false;
            }
            BlockPos lv = entity.getBlockPos().down();
            for (Direction lv2 : Direction.Type.HORIZONTAL) {
                BlockPos lv4;
                BlockPos lv3 = lv.offset(lv2);
                if (!world.getBlockState(lv3).getCollisionShape(world, lv3).getFace(Direction.UP).isEmpty() || !world.getFluidState(lv3).isOf(Fluids.WATER) || !world.getBlockState(lv4 = lv3.up()).isAir()) continue;
                BlockState lv5 = frogSpawn.getDefaultState();
                world.setBlockState(lv4, lv5, Block.NOTIFY_ALL);
                world.emitGameEvent(GameEvent.BLOCK_PLACE, lv4, GameEvent.Emitter.of(entity, lv5));
                world.playSoundFromEntity(null, entity, SoundEvents.ENTITY_FROG_LAY_SPAWN, SoundCategory.BLOCKS, 1.0f, 1.0f);
                isPregnant.forget();
                return true;
            }
            return true;
        }));
    }
}

