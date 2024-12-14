/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.projectile;

import net.minecraft.block.AbstractFireBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.AbstractFireballEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public class SmallFireballEntity
extends AbstractFireballEntity {
    public SmallFireballEntity(EntityType<? extends SmallFireballEntity> arg, World arg2) {
        super((EntityType<? extends AbstractFireballEntity>)arg, arg2);
    }

    public SmallFireballEntity(World world, LivingEntity owner, Vec3d velocity) {
        super((EntityType<? extends AbstractFireballEntity>)EntityType.SMALL_FIREBALL, owner, velocity, world);
    }

    public SmallFireballEntity(World world, double x, double y, double z, Vec3d velocity) {
        super((EntityType<? extends AbstractFireballEntity>)EntityType.SMALL_FIREBALL, x, y, z, velocity, world);
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        World world = this.getWorld();
        if (!(world instanceof ServerWorld)) {
            return;
        }
        ServerWorld lv = (ServerWorld)world;
        Entity lv2 = entityHitResult.getEntity();
        Entity lv3 = this.getOwner();
        int i = lv2.getFireTicks();
        lv2.setOnFireFor(5.0f);
        DamageSource lv4 = this.getDamageSources().fireball(this, lv3);
        if (!lv2.damage(lv4, 5.0f)) {
            lv2.setFireTicks(i);
        } else {
            EnchantmentHelper.onTargetDamaged(lv, lv2, lv4);
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        if (this.getWorld().isClient) {
            return;
        }
        Entity lv = this.getOwner();
        if (!(lv instanceof MobEntity) || this.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
            BlockPos lv2 = blockHitResult.getBlockPos().offset(blockHitResult.getSide());
            if (this.getWorld().isAir(lv2)) {
                this.getWorld().setBlockState(lv2, AbstractFireBlock.getState(this.getWorld(), lv2));
            }
        }
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (!this.getWorld().isClient) {
            this.discard();
        }
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        return false;
    }
}

