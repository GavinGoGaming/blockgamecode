/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.projectile.thrown;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.EndermiteEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

public class EnderPearlEntity
extends ThrownItemEntity {
    public EnderPearlEntity(EntityType<? extends EnderPearlEntity> arg, World arg2) {
        super((EntityType<? extends ThrownItemEntity>)arg, arg2);
    }

    public EnderPearlEntity(World world, LivingEntity owner) {
        super((EntityType<? extends ThrownItemEntity>)EntityType.ENDER_PEARL, owner, world);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.ENDER_PEARL;
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        entityHitResult.getEntity().damage(this.getDamageSources().thrown(this, this.getOwner()), 0.0f);
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        ServerWorld lv;
        block12: {
            block11: {
                super.onCollision(hitResult);
                for (int i = 0; i < 32; ++i) {
                    this.getWorld().addParticle(ParticleTypes.PORTAL, this.getX(), this.getY() + this.random.nextDouble() * 2.0, this.getZ(), this.random.nextGaussian(), 0.0, this.random.nextGaussian());
                }
                World world = this.getWorld();
                if (!(world instanceof ServerWorld)) break block11;
                lv = (ServerWorld)world;
                if (!this.isRemoved()) break block12;
            }
            return;
        }
        Entity lv2 = this.getOwner();
        if (lv2 == null || !EnderPearlEntity.canTeleportEntityTo(lv2, lv)) {
            this.discard();
            return;
        }
        if (lv2.hasVehicle()) {
            lv2.detach();
        }
        if (lv2 instanceof ServerPlayerEntity) {
            ServerPlayerEntity lv3 = (ServerPlayerEntity)lv2;
            if (lv3.networkHandler.isConnectionOpen()) {
                EndermiteEntity lv4;
                if (this.random.nextFloat() < 0.05f && lv.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING) && (lv4 = EntityType.ENDERMITE.create(lv)) != null) {
                    lv4.refreshPositionAndAngles(lv2.getX(), lv2.getY(), lv2.getZ(), lv2.getYaw(), lv2.getPitch());
                    lv.spawnEntity(lv4);
                }
                lv2.teleportTo(new TeleportTarget(lv, this.getPos(), lv2.getVelocity(), lv2.getYaw(), lv2.getPitch(), TeleportTarget.NO_OP));
                lv2.onLanding();
                lv3.clearCurrentExplosion();
                lv2.damage(this.getDamageSources().fall(), 5.0f);
                this.playTeleportSound(lv, this.getPos());
            }
        } else {
            lv2.teleportTo(new TeleportTarget(lv, this.getPos(), lv2.getVelocity(), lv2.getYaw(), lv2.getPitch(), TeleportTarget.NO_OP));
            lv2.onLanding();
            this.playTeleportSound(lv, this.getPos());
        }
        this.discard();
    }

    private static boolean canTeleportEntityTo(Entity entity, World world) {
        if (entity.getWorld().getRegistryKey() == world.getRegistryKey()) {
            if (entity instanceof LivingEntity) {
                LivingEntity lv = (LivingEntity)entity;
                return lv.isAlive() && !lv.isSleeping();
            }
            return entity.isAlive();
        }
        return entity.canUsePortals(true);
    }

    @Override
    public void tick() {
        Entity lv = this.getOwner();
        if (lv instanceof ServerPlayerEntity && !lv.isAlive() && this.getWorld().getGameRules().getBoolean(GameRules.ENDER_PEARLS_VANISH_ON_DEATH)) {
            this.discard();
        } else {
            super.tick();
        }
    }

    private void playTeleportSound(World world, Vec3d pos) {
        world.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENTITY_PLAYER_TELEPORT, SoundCategory.PLAYERS);
    }

    @Override
    public boolean canTeleportBetween(World from, World to) {
        Entity entity;
        if (from.getRegistryKey() == World.END && (entity = this.getOwner()) instanceof ServerPlayerEntity) {
            ServerPlayerEntity lv = (ServerPlayerEntity)entity;
            return super.canTeleportBetween(from, to) && lv.seenCredits;
        }
        return super.canTeleportBetween(from, to);
    }

    @Override
    protected void onBlockCollision(BlockState state) {
        Entity entity;
        super.onBlockCollision(state);
        if (state.isOf(Blocks.END_GATEWAY) && (entity = this.getOwner()) instanceof ServerPlayerEntity) {
            ServerPlayerEntity lv = (ServerPlayerEntity)entity;
            lv.onBlockCollision(state);
        }
    }
}

