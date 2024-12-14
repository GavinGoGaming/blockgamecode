/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class NameTagItem
extends Item {
    public NameTagItem(Item.Settings arg) {
        super(arg);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        Text lv = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (lv != null && !(entity instanceof PlayerEntity)) {
            if (!user.getWorld().isClient && entity.isAlive()) {
                entity.setCustomName(lv);
                if (entity instanceof MobEntity) {
                    MobEntity lv2 = (MobEntity)entity;
                    lv2.setPersistent();
                }
                stack.decrement(1);
            }
            return ActionResult.success(user.getWorld().isClient);
        }
        return ActionResult.PASS;
    }
}

