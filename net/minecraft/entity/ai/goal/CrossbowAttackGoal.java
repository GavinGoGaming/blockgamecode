/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.entity.CrossbowUser;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.math.intprovider.UniformIntProvider;

public class CrossbowAttackGoal<T extends HostileEntity & CrossbowUser>
extends Goal {
    public static final UniformIntProvider COOLDOWN_RANGE = TimeHelper.betweenSeconds(1, 2);
    private final T actor;
    private Stage stage = Stage.UNCHARGED;
    private final double speed;
    private final float squaredRange;
    private int seeingTargetTicker;
    private int chargedTicksLeft;
    private int cooldown;

    public CrossbowAttackGoal(T actor, double speed, float range) {
        this.actor = actor;
        this.speed = speed;
        this.squaredRange = range * range;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        return this.hasAliveTarget() && this.isEntityHoldingCrossbow();
    }

    private boolean isEntityHoldingCrossbow() {
        return ((LivingEntity)this.actor).isHolding(Items.CROSSBOW);
    }

    @Override
    public boolean shouldContinue() {
        return this.hasAliveTarget() && (this.canStart() || !((MobEntity)this.actor).getNavigation().isIdle()) && this.isEntityHoldingCrossbow();
    }

    private boolean hasAliveTarget() {
        return ((MobEntity)this.actor).getTarget() != null && ((MobEntity)this.actor).getTarget().isAlive();
    }

    @Override
    public void stop() {
        super.stop();
        ((MobEntity)this.actor).setAttacking(false);
        ((MobEntity)this.actor).setTarget(null);
        this.seeingTargetTicker = 0;
        if (((LivingEntity)this.actor).isUsingItem()) {
            ((LivingEntity)this.actor).clearActiveItem();
            ((CrossbowUser)this.actor).setCharging(false);
            ((LivingEntity)this.actor).getActiveItem().set(DataComponentTypes.CHARGED_PROJECTILES, ChargedProjectilesComponent.DEFAULT);
        }
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        boolean bl3;
        boolean bl2;
        LivingEntity lv = ((MobEntity)this.actor).getTarget();
        if (lv == null) {
            return;
        }
        boolean bl = ((MobEntity)this.actor).getVisibilityCache().canSee(lv);
        boolean bl4 = bl2 = this.seeingTargetTicker > 0;
        if (bl != bl2) {
            this.seeingTargetTicker = 0;
        }
        this.seeingTargetTicker = bl ? ++this.seeingTargetTicker : --this.seeingTargetTicker;
        double d = ((Entity)this.actor).squaredDistanceTo(lv);
        boolean bl5 = bl3 = (d > (double)this.squaredRange || this.seeingTargetTicker < 5) && this.chargedTicksLeft == 0;
        if (bl3) {
            --this.cooldown;
            if (this.cooldown <= 0) {
                ((MobEntity)this.actor).getNavigation().startMovingTo(lv, this.isUncharged() ? this.speed : this.speed * 0.5);
                this.cooldown = COOLDOWN_RANGE.get(((Entity)this.actor).getRandom());
            }
        } else {
            this.cooldown = 0;
            ((MobEntity)this.actor).getNavigation().stop();
        }
        ((MobEntity)this.actor).getLookControl().lookAt(lv, 30.0f, 30.0f);
        if (this.stage == Stage.UNCHARGED) {
            if (!bl3) {
                ((LivingEntity)this.actor).setCurrentHand(ProjectileUtil.getHandPossiblyHolding(this.actor, Items.CROSSBOW));
                this.stage = Stage.CHARGING;
                ((CrossbowUser)this.actor).setCharging(true);
            }
        } else if (this.stage == Stage.CHARGING) {
            ItemStack lv2;
            int i;
            if (!((LivingEntity)this.actor).isUsingItem()) {
                this.stage = Stage.UNCHARGED;
            }
            if ((i = ((LivingEntity)this.actor).getItemUseTime()) >= CrossbowItem.getPullTime(lv2 = ((LivingEntity)this.actor).getActiveItem(), this.actor)) {
                ((LivingEntity)this.actor).stopUsingItem();
                this.stage = Stage.CHARGED;
                this.chargedTicksLeft = 20 + ((Entity)this.actor).getRandom().nextInt(20);
                ((CrossbowUser)this.actor).setCharging(false);
            }
        } else if (this.stage == Stage.CHARGED) {
            --this.chargedTicksLeft;
            if (this.chargedTicksLeft == 0) {
                this.stage = Stage.READY_TO_ATTACK;
            }
        } else if (this.stage == Stage.READY_TO_ATTACK && bl) {
            ((RangedAttackMob)this.actor).shootAt(lv, 1.0f);
            this.stage = Stage.UNCHARGED;
        }
    }

    private boolean isUncharged() {
        return this.stage == Stage.UNCHARGED;
    }

    static enum Stage {
        UNCHARGED,
        CHARGING,
        CHARGED,
        READY_TO_ATTACK;

    }
}

