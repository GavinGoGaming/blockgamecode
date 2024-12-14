/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ProjectileItem;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;

public class ExperienceBottleItem
extends Item
implements ProjectileItem {
    public ExperienceBottleItem(Item.Settings arg) {
        super(arg);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack lv = user.getStackInHand(hand);
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_EXPERIENCE_BOTTLE_THROW, SoundCategory.NEUTRAL, 0.5f, 0.4f / (world.getRandom().nextFloat() * 0.4f + 0.8f));
        if (!world.isClient) {
            ExperienceBottleEntity lv2 = new ExperienceBottleEntity(world, user);
            lv2.setItem(lv);
            lv2.setVelocity(user, user.getPitch(), user.getYaw(), -20.0f, 0.7f, 1.0f);
            world.spawnEntity(lv2);
        }
        user.incrementStat(Stats.USED.getOrCreateStat(this));
        lv.decrementUnlessCreative(1, user);
        return TypedActionResult.success(lv, world.isClient());
    }

    @Override
    public ProjectileEntity createEntity(World world, Position pos, ItemStack stack, Direction direction) {
        ExperienceBottleEntity lv = new ExperienceBottleEntity(world, pos.getX(), pos.getY(), pos.getZ());
        lv.setItem(stack);
        return lv;
    }

    @Override
    public ProjectileItem.Settings getProjectileSettings() {
        return ProjectileItem.Settings.builder().uncertainty(ProjectileItem.Settings.DEFAULT.uncertainty() * 0.5f).power(ProjectileItem.Settings.DEFAULT.power() * 1.25f).build();
    }
}

