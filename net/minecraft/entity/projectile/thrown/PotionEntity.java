/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.projectile.thrown;

import it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.block.AbstractCandleBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;

public class PotionEntity
extends ThrownItemEntity
implements FlyingItemEntity {
    public static final double field_30667 = 4.0;
    private static final double field_30668 = 16.0;
    public static final Predicate<LivingEntity> AFFECTED_BY_WATER = entity -> entity.hurtByWater() || entity.isOnFire();

    public PotionEntity(EntityType<? extends PotionEntity> arg, World arg2) {
        super((EntityType<? extends ThrownItemEntity>)arg, arg2);
    }

    public PotionEntity(World world, LivingEntity owner) {
        super((EntityType<? extends ThrownItemEntity>)EntityType.POTION, owner, world);
    }

    public PotionEntity(World world, double x, double y, double z) {
        super((EntityType<? extends ThrownItemEntity>)EntityType.POTION, x, y, z, world);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.SPLASH_POTION;
    }

    @Override
    protected double getGravity() {
        return 0.05;
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        if (this.getWorld().isClient) {
            return;
        }
        ItemStack lv = this.getStack();
        Direction lv2 = blockHitResult.getSide();
        BlockPos lv3 = blockHitResult.getBlockPos();
        BlockPos lv4 = lv3.offset(lv2);
        PotionContentsComponent lv5 = lv.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
        if (lv5.matches(Potions.WATER)) {
            this.extinguishFire(lv4);
            this.extinguishFire(lv4.offset(lv2.getOpposite()));
            for (Direction lv6 : Direction.Type.HORIZONTAL) {
                this.extinguishFire(lv4.offset(lv6));
            }
        }
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (this.getWorld().isClient) {
            return;
        }
        ItemStack lv = this.getStack();
        PotionContentsComponent lv2 = lv.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
        if (lv2.matches(Potions.WATER)) {
            this.applyWater();
        } else if (lv2.hasEffects()) {
            if (this.isLingering()) {
                this.applyLingeringPotion(lv2);
            } else {
                this.applySplashPotion(lv2.getEffects(), hitResult.getType() == HitResult.Type.ENTITY ? ((EntityHitResult)hitResult).getEntity() : null);
            }
        }
        int i = lv2.potion().isPresent() && lv2.potion().get().value().hasInstantEffect() ? WorldEvents.INSTANT_SPLASH_POTION_SPLASHED : WorldEvents.SPLASH_POTION_SPLASHED;
        this.getWorld().syncWorldEvent(i, this.getBlockPos(), lv2.getColor());
        this.discard();
    }

    private void applyWater() {
        Box lv = this.getBoundingBox().expand(4.0, 2.0, 4.0);
        List<LivingEntity> list = this.getWorld().getEntitiesByClass(LivingEntity.class, lv, AFFECTED_BY_WATER);
        for (LivingEntity lv2 : list) {
            double d = this.squaredDistanceTo(lv2);
            if (!(d < 16.0)) continue;
            if (lv2.hurtByWater()) {
                lv2.damage(this.getDamageSources().indirectMagic(this, this.getOwner()), 1.0f);
            }
            if (!lv2.isOnFire() || !lv2.isAlive()) continue;
            lv2.extinguishWithSound();
        }
        List<AxolotlEntity> list2 = this.getWorld().getNonSpectatingEntities(AxolotlEntity.class, lv);
        for (AxolotlEntity lv3 : list2) {
            lv3.hydrateFromPotion();
        }
    }

    private void applySplashPotion(Iterable<StatusEffectInstance> effects, @Nullable Entity entity) {
        Box lv = this.getBoundingBox().expand(4.0, 2.0, 4.0);
        List<LivingEntity> list = this.getWorld().getNonSpectatingEntities(LivingEntity.class, lv);
        if (!list.isEmpty()) {
            Entity lv2 = this.getEffectCause();
            for (LivingEntity lv3 : list) {
                double d;
                if (!lv3.isAffectedBySplashPotions() || !((d = this.squaredDistanceTo(lv3)) < 16.0)) continue;
                double e = lv3 == entity ? 1.0 : 1.0 - Math.sqrt(d) / 4.0;
                for (StatusEffectInstance lv4 : effects) {
                    RegistryEntry<StatusEffect> lv5 = lv4.getEffectType();
                    if (lv5.value().isInstant()) {
                        lv5.value().applyInstantEffect(this, this.getOwner(), lv3, lv4.getAmplifier(), e);
                        continue;
                    }
                    int i = lv4.mapDuration(duration -> (int)(e * (double)duration + 0.5));
                    StatusEffectInstance lv6 = new StatusEffectInstance(lv5, i, lv4.getAmplifier(), lv4.isAmbient(), lv4.shouldShowParticles());
                    if (lv6.isDurationBelow(20)) continue;
                    lv3.addStatusEffect(lv6, lv2);
                }
            }
        }
    }

    private void applyLingeringPotion(PotionContentsComponent potion) {
        AreaEffectCloudEntity lv = new AreaEffectCloudEntity(this.getWorld(), this.getX(), this.getY(), this.getZ());
        Entity entity = this.getOwner();
        if (entity instanceof LivingEntity) {
            LivingEntity lv2 = (LivingEntity)entity;
            lv.setOwner(lv2);
        }
        lv.setRadius(3.0f);
        lv.setRadiusOnUse(-0.5f);
        lv.setWaitTime(10);
        lv.setRadiusGrowth(-lv.getRadius() / (float)lv.getDuration());
        lv.setPotionContents(potion);
        this.getWorld().spawnEntity(lv);
    }

    private boolean isLingering() {
        return this.getStack().isOf(Items.LINGERING_POTION);
    }

    private void extinguishFire(BlockPos pos) {
        BlockState lv = this.getWorld().getBlockState(pos);
        if (lv.isIn(BlockTags.FIRE)) {
            this.getWorld().breakBlock(pos, false, this);
        } else if (AbstractCandleBlock.isLitCandle(lv)) {
            AbstractCandleBlock.extinguish(null, lv, this.getWorld(), pos);
        } else if (CampfireBlock.isLitCampfire(lv)) {
            this.getWorld().syncWorldEvent(null, WorldEvents.FIRE_EXTINGUISHED, pos, 0);
            CampfireBlock.extinguish(this.getOwner(), this.getWorld(), pos, lv);
            this.getWorld().setBlockState(pos, (BlockState)lv.with(CampfireBlock.LIT, false));
        }
    }

    @Override
    public DoubleDoubleImmutablePair getKnockback(LivingEntity target, DamageSource source) {
        double d = target.getPos().x - this.getPos().x;
        double e = target.getPos().z - this.getPos().z;
        return DoubleDoubleImmutablePair.of(d, e);
    }
}

