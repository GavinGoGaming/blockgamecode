/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class EnderPearlItem
extends Item {
    public EnderPearlItem(Item.Settings arg) {
        super(arg);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack lv = user.getStackInHand(hand);
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_ENDER_PEARL_THROW, SoundCategory.NEUTRAL, 0.5f, 0.4f / (world.getRandom().nextFloat() * 0.4f + 0.8f));
        user.getItemCooldownManager().set(this, 20);
        if (!world.isClient) {
            EnderPearlEntity lv2 = new EnderPearlEntity(world, user);
            lv2.setItem(lv);
            lv2.setVelocity(user, user.getPitch(), user.getYaw(), 0.0f, 1.5f, 1.0f);
            world.spawnEntity(lv2);
        }
        user.incrementStat(Stats.USED.getOrCreateStat(this));
        lv.decrementUnlessCreative(1, user);
        return TypedActionResult.success(lv, world.isClient());
    }
}

