/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;

public interface CrossbowUser
extends RangedAttackMob {
    public void setCharging(boolean var1);

    @Nullable
    public LivingEntity getTarget();

    public void postShoot();

    default public void shoot(LivingEntity entity, float speed) {
        Hand lv = ProjectileUtil.getHandPossiblyHolding(entity, Items.CROSSBOW);
        ItemStack lv2 = entity.getStackInHand(lv);
        Item item = lv2.getItem();
        if (item instanceof CrossbowItem) {
            CrossbowItem lv3 = (CrossbowItem)item;
            lv3.shootAll(entity.getWorld(), entity, lv, lv2, speed, 14 - entity.getWorld().getDifficulty().getId() * 4, this.getTarget());
        }
        this.postShoot();
    }
}

