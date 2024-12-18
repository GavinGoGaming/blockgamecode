/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.mob;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class MagmaCubeEntity
extends SlimeEntity {
    public MagmaCubeEntity(EntityType<? extends MagmaCubeEntity> arg, World arg2) {
        super((EntityType<? extends SlimeEntity>)arg, arg2);
    }

    public static DefaultAttributeContainer.Builder createMagmaCubeAttributes() {
        return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2f);
    }

    public static boolean canMagmaCubeSpawn(EntityType<MagmaCubeEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        return world.getDifficulty() != Difficulty.PEACEFUL;
    }

    @Override
    public void setSize(int size, boolean heal) {
        super.setSize(size, heal);
        this.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).setBaseValue(size * 3);
    }

    @Override
    public float getBrightnessAtEyes() {
        return 1.0f;
    }

    @Override
    protected ParticleEffect getParticles() {
        return ParticleTypes.FLAME;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    protected int getTicksUntilNextJump() {
        return super.getTicksUntilNextJump() * 4;
    }

    @Override
    protected void updateStretch() {
        this.targetStretch *= 0.9f;
    }

    @Override
    public void jump() {
        Vec3d lv = this.getVelocity();
        float f = (float)this.getSize() * 0.1f;
        this.setVelocity(lv.x, this.getJumpVelocity() + f, lv.z);
        this.velocityDirty = true;
    }

    @Override
    protected void swimUpward(TagKey<Fluid> fluid) {
        if (fluid == FluidTags.LAVA) {
            Vec3d lv = this.getVelocity();
            this.setVelocity(lv.x, 0.22f + (float)this.getSize() * 0.05f, lv.z);
            this.velocityDirty = true;
        } else {
            super.swimUpward(fluid);
        }
    }

    @Override
    protected boolean canAttack() {
        return this.canMoveVoluntarily();
    }

    @Override
    protected float getDamageAmount() {
        return super.getDamageAmount() + 2.0f;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        if (this.isSmall()) {
            return SoundEvents.ENTITY_MAGMA_CUBE_HURT_SMALL;
        }
        return SoundEvents.ENTITY_MAGMA_CUBE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        if (this.isSmall()) {
            return SoundEvents.ENTITY_MAGMA_CUBE_DEATH_SMALL;
        }
        return SoundEvents.ENTITY_MAGMA_CUBE_DEATH;
    }

    @Override
    protected SoundEvent getSquishSound() {
        if (this.isSmall()) {
            return SoundEvents.ENTITY_MAGMA_CUBE_SQUISH_SMALL;
        }
        return SoundEvents.ENTITY_MAGMA_CUBE_SQUISH;
    }

    @Override
    protected SoundEvent getJumpSound() {
        return SoundEvents.ENTITY_MAGMA_CUBE_JUMP;
    }
}

