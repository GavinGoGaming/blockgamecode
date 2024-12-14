/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import net.minecraft.block.DispenserBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.WindChargeEntity;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class WindChargeItem
extends Item
implements ProjectileItem {
    private static final int COOLDOWN = 10;

    public WindChargeItem(Item.Settings arg) {
        super(arg);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient()) {
            WindChargeEntity lv = new WindChargeEntity(user, world, user.getPos().getX(), user.getEyePos().getY(), user.getPos().getZ());
            lv.setVelocity(user, user.getPitch(), user.getYaw(), 0.0f, 1.5f, 1.0f);
            world.spawnEntity(lv);
        }
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_WIND_CHARGE_THROW, SoundCategory.NEUTRAL, 0.5f, 0.4f / (world.getRandom().nextFloat() * 0.4f + 0.8f));
        ItemStack lv2 = user.getStackInHand(hand);
        user.getItemCooldownManager().set(this, 10);
        user.incrementStat(Stats.USED.getOrCreateStat(this));
        lv2.decrementUnlessCreative(1, user);
        return TypedActionResult.success(lv2, world.isClient());
    }

    @Override
    public ProjectileEntity createEntity(World world, Position pos, ItemStack stack, Direction direction) {
        Random lv = world.getRandom();
        double d = lv.nextTriangular(direction.getOffsetX(), 0.11485000000000001);
        double e = lv.nextTriangular(direction.getOffsetY(), 0.11485000000000001);
        double f = lv.nextTriangular(direction.getOffsetZ(), 0.11485000000000001);
        Vec3d lv2 = new Vec3d(d, e, f);
        WindChargeEntity lv3 = new WindChargeEntity(world, pos.getX(), pos.getY(), pos.getZ(), lv2);
        lv3.setVelocity(lv2);
        return lv3;
    }

    @Override
    public void initializeProjectile(ProjectileEntity entity, double x, double y, double z, float power, float uncertainty) {
    }

    @Override
    public ProjectileItem.Settings getProjectileSettings() {
        return ProjectileItem.Settings.builder().positionFunction((pointer, facing) -> DispenserBlock.getOutputLocation(pointer, 1.0, Vec3d.ZERO)).uncertainty(6.6666665f).power(1.0f).overrideDispenseEvent(1051).build();
    }
}

