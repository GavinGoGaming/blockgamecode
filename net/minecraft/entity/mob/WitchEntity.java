/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.mob;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.DisableableFollowTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.ProjectileAttackGoal;
import net.minecraft.entity.ai.goal.RaidGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class WitchEntity
extends RaiderEntity
implements RangedAttackMob {
    private static final Identifier DRINKING_SPEED_PENALTY_MODIFIER_ID = Identifier.ofVanilla("drinking");
    private static final EntityAttributeModifier DRINKING_SPEED_PENALTY_MODIFIER = new EntityAttributeModifier(DRINKING_SPEED_PENALTY_MODIFIER_ID, -0.25, EntityAttributeModifier.Operation.ADD_VALUE);
    private static final TrackedData<Boolean> DRINKING = DataTracker.registerData(WitchEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private int drinkTimeLeft;
    private RaidGoal<RaiderEntity> raidGoal;
    private DisableableFollowTargetGoal<PlayerEntity> attackPlayerGoal;

    public WitchEntity(EntityType<? extends WitchEntity> arg, World arg2) {
        super((EntityType<? extends RaiderEntity>)arg, arg2);
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.raidGoal = new RaidGoal<RaiderEntity>(this, RaiderEntity.class, true, entity -> entity != null && this.hasActiveRaid() && entity.getType() != EntityType.WITCH);
        this.attackPlayerGoal = new DisableableFollowTargetGoal<PlayerEntity>(this, PlayerEntity.class, 10, true, false, null);
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new ProjectileAttackGoal(this, 1.0, 60, 10.0f));
        this.goalSelector.add(2, new WanderAroundFarGoal(this, 1.0));
        this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(3, new LookAroundGoal(this));
        this.targetSelector.add(1, new RevengeGoal(this, RaiderEntity.class));
        this.targetSelector.add(2, this.raidGoal);
        this.targetSelector.add(3, this.attackPlayerGoal);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(DRINKING, false);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_WITCH_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_WITCH_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_WITCH_DEATH;
    }

    public void setDrinking(boolean drinking) {
        this.getDataTracker().set(DRINKING, drinking);
    }

    public boolean isDrinking() {
        return this.getDataTracker().get(DRINKING);
    }

    public static DefaultAttributeContainer.Builder createWitchAttributes() {
        return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 26.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25);
    }

    @Override
    public void tickMovement() {
        if (!this.getWorld().isClient && this.isAlive()) {
            this.raidGoal.decreaseCooldown();
            if (this.raidGoal.getCooldown() <= 0) {
                this.attackPlayerGoal.setEnabled(true);
            } else {
                this.attackPlayerGoal.setEnabled(false);
            }
            if (this.isDrinking()) {
                if (this.drinkTimeLeft-- <= 0) {
                    this.setDrinking(false);
                    ItemStack lv = this.getMainHandStack();
                    this.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                    PotionContentsComponent lv2 = lv.get(DataComponentTypes.POTION_CONTENTS);
                    if (lv.isOf(Items.POTION) && lv2 != null) {
                        lv2.forEachEffect(this::addStatusEffect);
                    }
                    this.emitGameEvent(GameEvent.DRINK);
                    this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).removeModifier(DRINKING_SPEED_PENALTY_MODIFIER.id());
                }
            } else {
                RegistryEntry<Potion> lv3 = null;
                if (this.random.nextFloat() < 0.15f && this.isSubmergedIn(FluidTags.WATER) && !this.hasStatusEffect(StatusEffects.WATER_BREATHING)) {
                    lv3 = Potions.WATER_BREATHING;
                } else if (this.random.nextFloat() < 0.15f && (this.isOnFire() || this.getRecentDamageSource() != null && this.getRecentDamageSource().isIn(DamageTypeTags.IS_FIRE)) && !this.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) {
                    lv3 = Potions.FIRE_RESISTANCE;
                } else if (this.random.nextFloat() < 0.05f && this.getHealth() < this.getMaxHealth()) {
                    lv3 = Potions.HEALING;
                } else if (this.random.nextFloat() < 0.5f && this.getTarget() != null && !this.hasStatusEffect(StatusEffects.SPEED) && this.getTarget().squaredDistanceTo(this) > 121.0) {
                    lv3 = Potions.SWIFTNESS;
                }
                if (lv3 != null) {
                    this.equipStack(EquipmentSlot.MAINHAND, PotionContentsComponent.createStack(Items.POTION, lv3));
                    this.drinkTimeLeft = this.getMainHandStack().getMaxUseTime(this);
                    this.setDrinking(true);
                    if (!this.isSilent()) {
                        this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_WITCH_DRINK, this.getSoundCategory(), 1.0f, 0.8f + this.random.nextFloat() * 0.4f);
                    }
                    EntityAttributeInstance lv4 = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
                    lv4.removeModifier(DRINKING_SPEED_PENALTY_MODIFIER_ID);
                    lv4.addTemporaryModifier(DRINKING_SPEED_PENALTY_MODIFIER);
                }
            }
            if (this.random.nextFloat() < 7.5E-4f) {
                this.getWorld().sendEntityStatus(this, EntityStatuses.ADD_WITCH_PARTICLES);
            }
        }
        super.tickMovement();
    }

    @Override
    public SoundEvent getCelebratingSound() {
        return SoundEvents.ENTITY_WITCH_CELEBRATE;
    }

    @Override
    public void handleStatus(byte status) {
        if (status == EntityStatuses.ADD_WITCH_PARTICLES) {
            for (int i = 0; i < this.random.nextInt(35) + 10; ++i) {
                this.getWorld().addParticle(ParticleTypes.WITCH, this.getX() + this.random.nextGaussian() * (double)0.13f, this.getBoundingBox().maxY + 0.5 + this.random.nextGaussian() * (double)0.13f, this.getZ() + this.random.nextGaussian() * (double)0.13f, 0.0, 0.0, 0.0);
            }
        } else {
            super.handleStatus(status);
        }
    }

    @Override
    protected float modifyAppliedDamage(DamageSource source, float amount) {
        amount = super.modifyAppliedDamage(source, amount);
        if (source.getAttacker() == this) {
            amount = 0.0f;
        }
        if (source.isIn(DamageTypeTags.WITCH_RESISTANT_TO)) {
            amount *= 0.15f;
        }
        return amount;
    }

    @Override
    public void shootAt(LivingEntity target, float pullProgress) {
        if (this.isDrinking()) {
            return;
        }
        Vec3d lv = target.getVelocity();
        double d = target.getX() + lv.x - this.getX();
        double e = target.getEyeY() - (double)1.1f - this.getY();
        double g = target.getZ() + lv.z - this.getZ();
        double h = Math.sqrt(d * d + g * g);
        RegistryEntry<Potion> lv2 = Potions.HARMING;
        if (target instanceof RaiderEntity) {
            lv2 = target.getHealth() <= 4.0f ? Potions.HEALING : Potions.REGENERATION;
            this.setTarget(null);
        } else if (h >= 8.0 && !target.hasStatusEffect(StatusEffects.SLOWNESS)) {
            lv2 = Potions.SLOWNESS;
        } else if (target.getHealth() >= 8.0f && !target.hasStatusEffect(StatusEffects.POISON)) {
            lv2 = Potions.POISON;
        } else if (h <= 3.0 && !target.hasStatusEffect(StatusEffects.WEAKNESS) && this.random.nextFloat() < 0.25f) {
            lv2 = Potions.WEAKNESS;
        }
        PotionEntity lv3 = new PotionEntity(this.getWorld(), this);
        lv3.setItem(PotionContentsComponent.createStack(Items.SPLASH_POTION, lv2));
        lv3.setPitch(lv3.getPitch() - -20.0f);
        lv3.setVelocity(d, e + h * 0.2, g, 0.75f, 8.0f);
        if (!this.isSilent()) {
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_WITCH_THROW, this.getSoundCategory(), 1.0f, 0.8f + this.random.nextFloat() * 0.4f);
        }
        this.getWorld().spawnEntity(lv3);
    }

    @Override
    public void addBonusForWave(ServerWorld world, int wave, boolean unused) {
    }

    @Override
    public boolean canLead() {
        return false;
    }
}

