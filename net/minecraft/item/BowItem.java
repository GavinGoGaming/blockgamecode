/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.item;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BowItem
extends RangedWeaponItem {
    public static final int TICKS_PER_SECOND = 20;
    public static final int RANGE = 15;

    public BowItem(Item.Settings arg) {
        super(arg);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity)) {
            return;
        }
        PlayerEntity lv = (PlayerEntity)user;
        ItemStack lv2 = lv.getProjectileType(stack);
        if (lv2.isEmpty()) {
            return;
        }
        int j = this.getMaxUseTime(stack, user) - remainingUseTicks;
        float f = BowItem.getPullProgress(j);
        if ((double)f < 0.1) {
            return;
        }
        List<ItemStack> list = BowItem.load(stack, lv2, lv);
        if (world instanceof ServerWorld) {
            ServerWorld lv3 = (ServerWorld)world;
            if (!list.isEmpty()) {
                this.shootAll(lv3, lv, lv.getActiveHand(), stack, list, f * 3.0f, 1.0f, f == 1.0f, null);
            }
        }
        world.playSound(null, lv.getX(), lv.getY(), lv.getZ(), SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1.0f, 1.0f / (world.getRandom().nextFloat() * 0.4f + 1.2f) + f * 0.5f);
        lv.incrementStat(Stats.USED.getOrCreateStat(this));
    }

    @Override
    protected void shoot(LivingEntity shooter, ProjectileEntity projectile, int index, float speed, float divergence, float yaw, @Nullable LivingEntity target) {
        projectile.setVelocity(shooter, shooter.getPitch(), shooter.getYaw() + yaw, 0.0f, speed, divergence);
    }

    public static float getPullProgress(int useTicks) {
        float f = (float)useTicks / 20.0f;
        if ((f = (f * f + f * 2.0f) / 3.0f) > 1.0f) {
            f = 1.0f;
        }
        return f;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        boolean bl;
        ItemStack lv = user.getStackInHand(hand);
        boolean bl2 = bl = !user.getProjectileType(lv).isEmpty();
        if (user.isInCreativeMode() || bl) {
            user.setCurrentHand(hand);
            return TypedActionResult.consume(lv);
        }
        return TypedActionResult.fail(lv);
    }

    @Override
    public Predicate<ItemStack> getProjectiles() {
        return BOW_PROJECTILES;
    }

    @Override
    public int getRange() {
        return 15;
    }
}

