/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemSteerable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class OnAStickItem<T extends Entity>
extends Item {
    private final EntityType<T> target;
    private final int damagePerUse;

    public OnAStickItem(Item.Settings settings, EntityType<T> target, int damagePerUse) {
        super(settings);
        this.target = target;
        this.damagePerUse = damagePerUse;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack lv = user.getStackInHand(hand);
        if (world.isClient) {
            return TypedActionResult.pass(lv);
        }
        Entity lv2 = user.getControllingVehicle();
        if (user.hasVehicle() && lv2 instanceof ItemSteerable) {
            ItemSteerable lv3 = (ItemSteerable)((Object)lv2);
            if (lv2.getType() == this.target && lv3.consumeOnAStickItem()) {
                EquipmentSlot lv4 = LivingEntity.getSlotForHand(hand);
                ItemStack lv5 = lv.damage(this.damagePerUse, Items.FISHING_ROD, user, lv4);
                return TypedActionResult.success(lv5);
            }
        }
        user.incrementStat(Stats.USED.getOrCreateStat(this));
        return TypedActionResult.pass(lv);
    }
}

