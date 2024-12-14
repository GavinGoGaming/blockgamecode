/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Unit;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class RangedWeaponItem
extends Item {
    public static final Predicate<ItemStack> BOW_PROJECTILES = stack -> stack.isIn(ItemTags.ARROWS);
    public static final Predicate<ItemStack> CROSSBOW_HELD_PROJECTILES = BOW_PROJECTILES.or(stack -> stack.isOf(Items.FIREWORK_ROCKET));

    public RangedWeaponItem(Item.Settings arg) {
        super(arg);
    }

    public Predicate<ItemStack> getHeldProjectiles() {
        return this.getProjectiles();
    }

    public abstract Predicate<ItemStack> getProjectiles();

    public static ItemStack getHeldProjectile(LivingEntity entity, Predicate<ItemStack> predicate) {
        if (predicate.test(entity.getStackInHand(Hand.OFF_HAND))) {
            return entity.getStackInHand(Hand.OFF_HAND);
        }
        if (predicate.test(entity.getStackInHand(Hand.MAIN_HAND))) {
            return entity.getStackInHand(Hand.MAIN_HAND);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int getEnchantability() {
        return 1;
    }

    public abstract int getRange();

    protected void shootAll(ServerWorld world, LivingEntity shooter, Hand hand, ItemStack stack, List<ItemStack> projectiles, float speed, float divergence, boolean critical, @Nullable LivingEntity target) {
        float h = EnchantmentHelper.getProjectileSpread(world, stack, shooter, 0.0f);
        float i = projectiles.size() == 1 ? 0.0f : 2.0f * h / (float)(projectiles.size() - 1);
        float j = (float)((projectiles.size() - 1) % 2) * i / 2.0f;
        float k = 1.0f;
        for (int l = 0; l < projectiles.size(); ++l) {
            ItemStack lv = projectiles.get(l);
            if (lv.isEmpty()) continue;
            float m = j + k * (float)((l + 1) / 2) * i;
            k = -k;
            ProjectileEntity lv2 = this.createArrowEntity(world, shooter, stack, lv, critical);
            this.shoot(shooter, lv2, l, speed, divergence, m, target);
            world.spawnEntity(lv2);
            stack.damage(this.getWeaponStackDamage(lv), shooter, LivingEntity.getSlotForHand(hand));
            if (stack.isEmpty()) break;
        }
    }

    protected int getWeaponStackDamage(ItemStack projectile) {
        return 1;
    }

    protected abstract void shoot(LivingEntity var1, ProjectileEntity var2, int var3, float var4, float var5, float var6, @Nullable LivingEntity var7);

    protected ProjectileEntity createArrowEntity(World world, LivingEntity shooter, ItemStack weaponStack, ItemStack projectileStack, boolean critical) {
        ArrowItem lv;
        Item item = projectileStack.getItem();
        ArrowItem lv2 = item instanceof ArrowItem ? (lv = (ArrowItem)item) : (ArrowItem)Items.ARROW;
        PersistentProjectileEntity lv3 = lv2.createArrow(world, projectileStack, shooter, weaponStack);
        if (critical) {
            lv3.setCritical(true);
        }
        return lv3;
    }

    protected static List<ItemStack> load(ItemStack stack, ItemStack projectileStack, LivingEntity shooter) {
        int n;
        if (projectileStack.isEmpty()) {
            return List.of();
        }
        World world = shooter.getWorld();
        if (world instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)world;
            n = EnchantmentHelper.getProjectileCount(lv, stack, shooter, 1);
        } else {
            n = 1;
        }
        int i = n;
        ArrayList<ItemStack> list = new ArrayList<ItemStack>(i);
        ItemStack lv2 = projectileStack.copy();
        for (int j = 0; j < i; ++j) {
            ItemStack lv3 = RangedWeaponItem.getProjectile(stack, j == 0 ? projectileStack : lv2, shooter, j > 0);
            if (lv3.isEmpty()) continue;
            list.add(lv3);
        }
        return list;
    }

    protected static ItemStack getProjectile(ItemStack stack, ItemStack projectileStack, LivingEntity shooter, boolean multishot) {
        ItemStack lv2;
        int i;
        World world;
        if (!multishot && !shooter.isInCreativeMode() && (world = shooter.getWorld()) instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)world;
            v0 = EnchantmentHelper.getAmmoUse(lv, stack, projectileStack, 1);
        } else {
            v0 = i = 0;
        }
        if (i > projectileStack.getCount()) {
            return ItemStack.EMPTY;
        }
        if (i == 0) {
            lv2 = projectileStack.copyWithCount(1);
            lv2.set(DataComponentTypes.INTANGIBLE_PROJECTILE, Unit.INSTANCE);
            return lv2;
        }
        lv2 = projectileStack.split(i);
        if (projectileStack.isEmpty() && shooter instanceof PlayerEntity) {
            PlayerEntity lv3 = (PlayerEntity)shooter;
            lv3.getInventory().removeOne(projectileStack);
        }
        return lv2;
    }
}

