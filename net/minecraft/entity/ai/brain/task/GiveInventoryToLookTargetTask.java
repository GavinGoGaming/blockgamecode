/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.Entity;
import net.minecraft.entity.InventoryOwner;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.LookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.passive.AllayBrain;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class GiveInventoryToLookTargetTask<E extends LivingEntity>
extends MultiTickTask<E> {
    private static final int COMPLETION_RANGE = 3;
    private static final int ITEM_PICKUP_COOLDOWN_TICKS = 60;
    private final Function<LivingEntity, Optional<LookTarget>> lookTargetFunction;
    private final float speed;

    public GiveInventoryToLookTargetTask(Function<LivingEntity, Optional<LookTarget>> lookTargetFunction, float speed, int runTime) {
        super(Map.of(MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED, MemoryModuleType.WALK_TARGET, MemoryModuleState.REGISTERED, MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, MemoryModuleState.REGISTERED), runTime);
        this.lookTargetFunction = lookTargetFunction;
        this.speed = speed;
    }

    @Override
    protected boolean shouldRun(ServerWorld world, E entity) {
        return this.hasItemAndTarget(entity);
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld world, E entity, long time) {
        return this.hasItemAndTarget(entity);
    }

    @Override
    protected void run(ServerWorld world, E entity, long time) {
        this.lookTargetFunction.apply((LivingEntity)entity).ifPresent(target -> LookTargetUtil.walkTowards(entity, target, this.speed, 3));
    }

    @Override
    protected void keepRunning(ServerWorld world, E entity, long time) {
        ItemStack lv2;
        Optional<LookTarget> optional = this.lookTargetFunction.apply((LivingEntity)entity);
        if (optional.isEmpty()) {
            return;
        }
        LookTarget lv = optional.get();
        double d = lv.getPos().distanceTo(((Entity)entity).getEyePos());
        if (d < 3.0 && !(lv2 = ((InventoryOwner)entity).getInventory().removeStack(0, 1)).isEmpty()) {
            GiveInventoryToLookTargetTask.playThrowSound(entity, lv2, GiveInventoryToLookTargetTask.offsetTarget(lv));
            if (entity instanceof AllayEntity) {
                AllayEntity lv3 = (AllayEntity)entity;
                AllayBrain.getLikedPlayer(lv3).ifPresent(player -> this.triggerCriterion(lv, lv2, (ServerPlayerEntity)player));
            }
            ((LivingEntity)entity).getBrain().remember(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, 60);
        }
    }

    private void triggerCriterion(LookTarget target, ItemStack stack, ServerPlayerEntity player) {
        BlockPos lv = target.getBlockPos().down();
        Criteria.ALLAY_DROP_ITEM_ON_BLOCK.trigger(player, lv, stack);
    }

    private boolean hasItemAndTarget(E entity) {
        if (((InventoryOwner)entity).getInventory().isEmpty()) {
            return false;
        }
        Optional<LookTarget> optional = this.lookTargetFunction.apply((LivingEntity)entity);
        return optional.isPresent();
    }

    private static Vec3d offsetTarget(LookTarget target) {
        return target.getPos().add(0.0, 1.0, 0.0);
    }

    public static void playThrowSound(LivingEntity entity, ItemStack stack, Vec3d target) {
        Vec3d lv = new Vec3d(0.2f, 0.3f, 0.2f);
        LookTargetUtil.give(entity, stack, target, lv, 0.2f);
        World lv2 = entity.getWorld();
        if (lv2.getTime() % 7L == 0L && lv2.random.nextDouble() < 0.9) {
            float f = Util.getRandom(AllayEntity.THROW_SOUND_PITCHES, lv2.getRandom()).floatValue();
            lv2.playSoundFromEntity(null, entity, SoundEvents.ENTITY_ALLAY_ITEM_THROWN, SoundCategory.NEUTRAL, 1.0f, f);
        }
    }
}

