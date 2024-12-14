/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.entity.CrossbowUser;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.brain.EntityLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;

public class CrossbowAttackTask<E extends MobEntity, T extends LivingEntity>
extends MultiTickTask<E> {
    private static final int RUN_TIME = 1200;
    private int chargingCooldown;
    private CrossbowState state = CrossbowState.UNCHARGED;

    public CrossbowAttackTask() {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED, MemoryModuleType.ATTACK_TARGET, MemoryModuleState.VALUE_PRESENT), 1200);
    }

    @Override
    protected boolean shouldRun(ServerWorld arg, E arg2) {
        LivingEntity lv = CrossbowAttackTask.getAttackTarget(arg2);
        return ((LivingEntity)arg2).isHolding(Items.CROSSBOW) && LookTargetUtil.isVisibleInMemory(arg2, lv) && LookTargetUtil.isTargetWithinAttackRange(arg2, lv, 0);
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld arg, E arg2, long l) {
        return ((LivingEntity)arg2).getBrain().hasMemoryModule(MemoryModuleType.ATTACK_TARGET) && this.shouldRun(arg, arg2);
    }

    @Override
    protected void keepRunning(ServerWorld arg, E arg2, long l) {
        LivingEntity lv = CrossbowAttackTask.getAttackTarget(arg2);
        this.setLookTarget((MobEntity)arg2, lv);
        this.tickState(arg2, lv);
    }

    @Override
    protected void finishRunning(ServerWorld arg, E arg2, long l) {
        if (((LivingEntity)arg2).isUsingItem()) {
            ((LivingEntity)arg2).clearActiveItem();
        }
        if (((LivingEntity)arg2).isHolding(Items.CROSSBOW)) {
            ((CrossbowUser)arg2).setCharging(false);
            ((LivingEntity)arg2).getActiveItem().set(DataComponentTypes.CHARGED_PROJECTILES, ChargedProjectilesComponent.DEFAULT);
        }
    }

    private void tickState(E entity, LivingEntity target) {
        if (this.state == CrossbowState.UNCHARGED) {
            ((LivingEntity)entity).setCurrentHand(ProjectileUtil.getHandPossiblyHolding(entity, Items.CROSSBOW));
            this.state = CrossbowState.CHARGING;
            ((CrossbowUser)entity).setCharging(true);
        } else if (this.state == CrossbowState.CHARGING) {
            ItemStack lv;
            int i;
            if (!((LivingEntity)entity).isUsingItem()) {
                this.state = CrossbowState.UNCHARGED;
            }
            if ((i = ((LivingEntity)entity).getItemUseTime()) >= CrossbowItem.getPullTime(lv = ((LivingEntity)entity).getActiveItem(), entity)) {
                ((LivingEntity)entity).stopUsingItem();
                this.state = CrossbowState.CHARGED;
                this.chargingCooldown = 20 + ((Entity)entity).getRandom().nextInt(20);
                ((CrossbowUser)entity).setCharging(false);
            }
        } else if (this.state == CrossbowState.CHARGED) {
            --this.chargingCooldown;
            if (this.chargingCooldown == 0) {
                this.state = CrossbowState.READY_TO_ATTACK;
            }
        } else if (this.state == CrossbowState.READY_TO_ATTACK) {
            ((RangedAttackMob)entity).shootAt(target, 1.0f);
            this.state = CrossbowState.UNCHARGED;
        }
    }

    private void setLookTarget(MobEntity entity, LivingEntity target) {
        entity.getBrain().remember(MemoryModuleType.LOOK_TARGET, new EntityLookTarget(target, true));
    }

    private static LivingEntity getAttackTarget(LivingEntity entity) {
        return entity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET).get();
    }

    @Override
    protected /* synthetic */ void finishRunning(ServerWorld world, LivingEntity entity, long time) {
        this.finishRunning(world, (E)((MobEntity)entity), time);
    }

    @Override
    protected /* synthetic */ void keepRunning(ServerWorld world, LivingEntity entity, long time) {
        this.keepRunning(world, (E)((MobEntity)entity), time);
    }

    static enum CrossbowState {
        UNCHARGED,
        CHARGING,
        CHARGED,
        READY_TO_ATTACK;

    }
}

