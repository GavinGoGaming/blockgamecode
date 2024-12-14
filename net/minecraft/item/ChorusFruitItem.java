/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class ChorusFruitItem
extends Item {
    public ChorusFruitItem(Item.Settings arg) {
        super(arg);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        ItemStack lv = super.finishUsing(stack, world, user);
        if (!world.isClient) {
            for (int i = 0; i < 16; ++i) {
                SoundCategory lv4;
                SoundEvent lv3;
                double d = user.getX() + (user.getRandom().nextDouble() - 0.5) * 16.0;
                double e = MathHelper.clamp(user.getY() + (double)(user.getRandom().nextInt(16) - 8), (double)world.getBottomY(), (double)(world.getBottomY() + ((ServerWorld)world).getLogicalHeight() - 1));
                double f = user.getZ() + (user.getRandom().nextDouble() - 0.5) * 16.0;
                if (user.hasVehicle()) {
                    user.stopRiding();
                }
                Vec3d lv2 = user.getPos();
                if (!user.teleport(d, e, f, true)) continue;
                world.emitGameEvent(GameEvent.TELEPORT, lv2, GameEvent.Emitter.of(user));
                if (user instanceof FoxEntity) {
                    lv3 = SoundEvents.ENTITY_FOX_TELEPORT;
                    lv4 = SoundCategory.NEUTRAL;
                } else {
                    lv3 = SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT;
                    lv4 = SoundCategory.PLAYERS;
                }
                world.playSound(null, user.getX(), user.getY(), user.getZ(), lv3, lv4);
                user.onLanding();
                break;
            }
            if (user instanceof PlayerEntity) {
                PlayerEntity lv5 = (PlayerEntity)user;
                lv5.clearCurrentExplosion();
                lv5.getItemCooldownManager().set(this, 20);
            }
        }
        return lv;
    }
}

