/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.passive;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.VariantHolder;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.AttackWithOwnerGoal;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.PounceAtTargetGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SitGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TrackOwnerAttackerGoal;
import net.minecraft.entity.ai.goal.UniversalAngerGoal;
import net.minecraft.entity.ai.goal.UntamedActiveTargetGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.goal.WolfBegGoal;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.Cracks;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.passive.WolfVariant;
import net.minecraft.entity.passive.WolfVariants;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.EntityView;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class WolfEntity
extends TameableEntity
implements Angerable,
VariantHolder<RegistryEntry<WolfVariant>> {
    private static final TrackedData<Boolean> BEGGING = DataTracker.registerData(WolfEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> COLLAR_COLOR = DataTracker.registerData(WolfEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> ANGER_TIME = DataTracker.registerData(WolfEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<RegistryEntry<WolfVariant>> VARIANT = DataTracker.registerData(WolfEntity.class, TrackedDataHandlerRegistry.WOLF_VARIANT);
    public static final Predicate<LivingEntity> FOLLOW_TAMED_PREDICATE = entity -> {
        EntityType<?> lv = entity.getType();
        return lv == EntityType.SHEEP || lv == EntityType.RABBIT || lv == EntityType.FOX;
    };
    private static final float WILD_MAX_HEALTH = 8.0f;
    private static final float TAMED_MAX_HEALTH = 40.0f;
    private static final float field_49237 = 0.125f;
    private float begAnimationProgress;
    private float lastBegAnimationProgress;
    private boolean furWet;
    private boolean canShakeWaterOff;
    private float shakeProgress;
    private float lastShakeProgress;
    private static final UniformIntProvider ANGER_TIME_RANGE = TimeHelper.betweenSeconds(20, 39);
    @Nullable
    private UUID angryAt;

    public WolfEntity(EntityType<? extends WolfEntity> arg, World arg2) {
        super((EntityType<? extends TameableEntity>)arg, arg2);
        this.setTamed(false, false);
        this.setPathfindingPenalty(PathNodeType.POWDER_SNOW, -1.0f);
        this.setPathfindingPenalty(PathNodeType.DANGER_POWDER_SNOW, -1.0f);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(1, new TameableEntity.TameableEscapeDangerGoal(1.5, DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES));
        this.goalSelector.add(2, new SitGoal(this));
        this.goalSelector.add(3, new AvoidLlamaGoal<LlamaEntity>(this, LlamaEntity.class, 24.0f, 1.5, 1.5));
        this.goalSelector.add(4, new PounceAtTargetGoal(this, 0.4f));
        this.goalSelector.add(5, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.add(6, new FollowOwnerGoal(this, 1.0, 10.0f, 2.0f));
        this.goalSelector.add(7, new AnimalMateGoal(this, 1.0));
        this.goalSelector.add(8, new WanderAroundFarGoal(this, 1.0));
        this.goalSelector.add(9, new WolfBegGoal(this, 8.0f));
        this.goalSelector.add(10, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(10, new LookAroundGoal(this));
        this.targetSelector.add(1, new TrackOwnerAttackerGoal(this));
        this.targetSelector.add(2, new AttackWithOwnerGoal(this));
        this.targetSelector.add(3, new RevengeGoal(this, new Class[0]).setGroupRevenge(new Class[0]));
        this.targetSelector.add(4, new ActiveTargetGoal<PlayerEntity>(this, PlayerEntity.class, 10, true, false, this::shouldAngerAt));
        this.targetSelector.add(5, new UntamedActiveTargetGoal<AnimalEntity>(this, AnimalEntity.class, false, FOLLOW_TAMED_PREDICATE));
        this.targetSelector.add(6, new UntamedActiveTargetGoal<TurtleEntity>(this, TurtleEntity.class, false, TurtleEntity.BABY_TURTLE_ON_LAND_FILTER));
        this.targetSelector.add(7, new ActiveTargetGoal<AbstractSkeletonEntity>((MobEntity)this, AbstractSkeletonEntity.class, false));
        this.targetSelector.add(8, new UniversalAngerGoal<WolfEntity>(this, true));
    }

    public Identifier getTextureId() {
        WolfVariant lv = (WolfVariant)this.getVariant().value();
        if (this.isTamed()) {
            return lv.getTameTextureId();
        }
        if (this.hasAngerTime()) {
            return lv.getAngryTextureId();
        }
        return lv.getWildTextureId();
    }

    @Override
    public RegistryEntry<WolfVariant> getVariant() {
        return this.dataTracker.get(VARIANT);
    }

    @Override
    public void setVariant(RegistryEntry<WolfVariant> arg) {
        this.dataTracker.set(VARIANT, arg);
    }

    public static DefaultAttributeContainer.Builder createWolfAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3f).add(EntityAttributes.GENERIC_MAX_HEALTH, 8.0).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        DynamicRegistryManager lv = this.getRegistryManager();
        Registry<WolfVariant> lv2 = lv.get(RegistryKeys.WOLF_VARIANT);
        builder.add(VARIANT, (RegistryEntry)lv2.getEntry(WolfVariants.DEFAULT).or(lv2::getDefaultEntry).orElseThrow());
        builder.add(BEGGING, false);
        builder.add(COLLAR_COLOR, DyeColor.RED.getId());
        builder.add(ANGER_TIME, 0);
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.ENTITY_WOLF_STEP, 0.15f, 1.0f);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putByte("CollarColor", (byte)this.getCollarColor().getId());
        this.getVariant().getKey().ifPresent(arg2 -> nbt.putString("variant", arg2.getValue().toString()));
        this.writeAngerToNbt(nbt);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        Optional.ofNullable(Identifier.tryParse(nbt.getString("variant"))).map(variantId -> RegistryKey.of(RegistryKeys.WOLF_VARIANT, variantId)).flatMap(variantKey -> this.getRegistryManager().get(RegistryKeys.WOLF_VARIANT).getEntry((RegistryKey<WolfVariant>)variantKey)).ifPresent(this::setVariant);
        if (nbt.contains("CollarColor", NbtElement.NUMBER_TYPE)) {
            this.setCollarColor(DyeColor.byId(nbt.getInt("CollarColor")));
        }
        this.readAngerFromNbt((World)this.getWorld(), nbt);
    }

    @Override
    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        RegistryEntry<WolfVariant> lv3;
        RegistryEntry<Biome> lv = world.getBiome(this.getBlockPos());
        if (entityData instanceof WolfData) {
            WolfData lv2 = (WolfData)entityData;
            lv3 = lv2.variant;
        } else {
            lv3 = WolfVariants.fromBiome(this.getRegistryManager(), lv);
            entityData = new WolfData(lv3);
        }
        this.setVariant(lv3);
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.hasAngerTime()) {
            return SoundEvents.ENTITY_WOLF_GROWL;
        }
        if (this.random.nextInt(3) == 0) {
            if (this.isTamed() && this.getHealth() < 20.0f) {
                return SoundEvents.ENTITY_WOLF_WHINE;
            }
            return SoundEvents.ENTITY_WOLF_PANT;
        }
        return SoundEvents.ENTITY_WOLF_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        if (this.shouldArmorAbsorbDamage(source)) {
            return SoundEvents.ITEM_WOLF_ARMOR_DAMAGE;
        }
        return SoundEvents.ENTITY_WOLF_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_WOLF_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4f;
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (!((World)this.getWorld()).isClient && this.furWet && !this.canShakeWaterOff && !this.isNavigating() && this.isOnGround()) {
            this.canShakeWaterOff = true;
            this.shakeProgress = 0.0f;
            this.lastShakeProgress = 0.0f;
            ((World)this.getWorld()).sendEntityStatus(this, EntityStatuses.SHAKE_OFF_WATER);
        }
        if (!((World)this.getWorld()).isClient) {
            this.tickAngerLogic((ServerWorld)this.getWorld(), true);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.isAlive()) {
            return;
        }
        this.lastBegAnimationProgress = this.begAnimationProgress;
        this.begAnimationProgress = this.isBegging() ? (this.begAnimationProgress += (1.0f - this.begAnimationProgress) * 0.4f) : (this.begAnimationProgress += (0.0f - this.begAnimationProgress) * 0.4f);
        if (this.isWet()) {
            this.furWet = true;
            if (this.canShakeWaterOff && !((World)this.getWorld()).isClient) {
                ((World)this.getWorld()).sendEntityStatus(this, EntityStatuses.RESET_WOLF_SHAKE);
                this.resetShake();
            }
        } else if ((this.furWet || this.canShakeWaterOff) && this.canShakeWaterOff) {
            if (this.shakeProgress == 0.0f) {
                this.playSound(SoundEvents.ENTITY_WOLF_SHAKE, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
                this.emitGameEvent(GameEvent.ENTITY_ACTION);
            }
            this.lastShakeProgress = this.shakeProgress;
            this.shakeProgress += 0.05f;
            if (this.lastShakeProgress >= 2.0f) {
                this.furWet = false;
                this.canShakeWaterOff = false;
                this.lastShakeProgress = 0.0f;
                this.shakeProgress = 0.0f;
            }
            if (this.shakeProgress > 0.4f) {
                float f = (float)this.getY();
                int i = (int)(MathHelper.sin((this.shakeProgress - 0.4f) * (float)Math.PI) * 7.0f);
                Vec3d lv = this.getVelocity();
                for (int j = 0; j < i; ++j) {
                    float g = (this.random.nextFloat() * 2.0f - 1.0f) * this.getWidth() * 0.5f;
                    float h = (this.random.nextFloat() * 2.0f - 1.0f) * this.getWidth() * 0.5f;
                    ((World)this.getWorld()).addParticle(ParticleTypes.SPLASH, this.getX() + (double)g, f + 0.8f, this.getZ() + (double)h, lv.x, lv.y, lv.z);
                }
            }
        }
    }

    private void resetShake() {
        this.canShakeWaterOff = false;
        this.shakeProgress = 0.0f;
        this.lastShakeProgress = 0.0f;
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        this.furWet = false;
        this.canShakeWaterOff = false;
        this.lastShakeProgress = 0.0f;
        this.shakeProgress = 0.0f;
        super.onDeath(damageSource);
    }

    public boolean isFurWet() {
        return this.furWet;
    }

    public float getFurWetBrightnessMultiplier(float tickDelta) {
        return Math.min(0.75f + MathHelper.lerp(tickDelta, this.lastShakeProgress, this.shakeProgress) / 2.0f * 0.25f, 1.0f);
    }

    public float getShakeAnimationProgress(float tickDelta, float g) {
        float h = (MathHelper.lerp(tickDelta, this.lastShakeProgress, this.shakeProgress) + g) / 1.8f;
        if (h < 0.0f) {
            h = 0.0f;
        } else if (h > 1.0f) {
            h = 1.0f;
        }
        return MathHelper.sin(h * (float)Math.PI) * MathHelper.sin(h * (float)Math.PI * 11.0f) * 0.15f * (float)Math.PI;
    }

    public float getBegAnimationProgress(float tickDelta) {
        return MathHelper.lerp(tickDelta, this.lastBegAnimationProgress, this.begAnimationProgress) * 0.15f * (float)Math.PI;
    }

    @Override
    public int getMaxLookPitchChange() {
        if (this.isInSittingPose()) {
            return 20;
        }
        return super.getMaxLookPitchChange();
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        if (!((World)this.getWorld()).isClient) {
            this.setSitting(false);
        }
        return super.damage(source, amount);
    }

    @Override
    public boolean canUseSlot(EquipmentSlot slot) {
        return true;
    }

    @Override
    protected void applyDamage(DamageSource source, float amount) {
        if (!this.shouldArmorAbsorbDamage(source)) {
            super.applyDamage(source, amount);
            return;
        }
        ItemStack lv = this.getBodyArmor();
        int i = lv.getDamage();
        int j = lv.getMaxDamage();
        lv.damage(MathHelper.ceil(amount), this, EquipmentSlot.BODY);
        if (Cracks.WOLF_ARMOR.getCrackLevel(i, j) != Cracks.WOLF_ARMOR.getCrackLevel(this.getBodyArmor())) {
            this.playSoundIfNotSilent(SoundEvents.ITEM_WOLF_ARMOR_CRACK);
            EntityView entityView = this.getWorld();
            if (entityView instanceof ServerWorld) {
                ServerWorld lv2 = (ServerWorld)entityView;
                lv2.spawnParticles(new ItemStackParticleEffect(ParticleTypes.ITEM, Items.ARMADILLO_SCUTE.getDefaultStack()), this.getX(), this.getY() + 1.0, this.getZ(), 20, 0.2, 0.1, 0.2, 0.1);
            }
        }
    }

    private boolean shouldArmorAbsorbDamage(DamageSource source) {
        return this.hasArmor() && !source.isIn(DamageTypeTags.BYPASSES_WOLF_ARMOR);
    }

    @Override
    protected void updateAttributesForTamed() {
        if (this.isTamed()) {
            this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(40.0);
            this.setHealth(40.0f);
        } else {
            this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(8.0);
        }
    }

    @Override
    protected void damageArmor(DamageSource source, float amount) {
        this.damageEquipment(source, amount, EquipmentSlot.BODY);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack lv = player.getStackInHand(hand);
        Item lv2 = lv.getItem();
        if (!(!((World)this.getWorld()).isClient || this.isBaby() && this.isBreedingItem(lv))) {
            boolean bl = this.isOwner(player) || this.isTamed() || lv.isOf(Items.BONE) && !this.isTamed() && !this.hasAngerTime();
            return bl ? ActionResult.CONSUME : ActionResult.PASS;
        }
        if (this.isTamed()) {
            if (this.isBreedingItem(lv) && this.getHealth() < this.getMaxHealth()) {
                lv.decrementUnlessCreative(1, player);
                FoodComponent lv3 = lv.get(DataComponentTypes.FOOD);
                float f = lv3 != null ? (float)lv3.nutrition() : 1.0f;
                this.heal(2.0f * f);
                return ActionResult.success(((World)this.getWorld()).isClient());
            }
            if (lv2 instanceof DyeItem) {
                DyeItem lv4 = (DyeItem)lv2;
                if (this.isOwner(player)) {
                    DyeColor lv5 = lv4.getColor();
                    if (lv5 == this.getCollarColor()) return super.interactMob(player, hand);
                    this.setCollarColor(lv5);
                    lv.decrementUnlessCreative(1, player);
                    return ActionResult.SUCCESS;
                }
            }
            if (lv.isOf(Items.WOLF_ARMOR) && this.isOwner(player) && this.getBodyArmor().isEmpty() && !this.isBaby()) {
                this.equipBodyArmor(lv.copyWithCount(1));
                lv.decrementUnlessCreative(1, player);
                return ActionResult.SUCCESS;
            }
            if (lv.isOf(Items.SHEARS) && this.isOwner(player) && this.hasArmor() && (!EnchantmentHelper.hasAnyEnchantmentsWith(this.getBodyArmor(), EnchantmentEffectComponentTypes.PREVENT_ARMOR_CHANGE) || player.isCreative())) {
                lv.damage(1, player, WolfEntity.getSlotForHand(hand));
                this.playSoundIfNotSilent(SoundEvents.ITEM_ARMOR_UNEQUIP_WOLF);
                ItemStack lv6 = this.getBodyArmor();
                this.equipBodyArmor(ItemStack.EMPTY);
                this.dropStack(lv6);
                return ActionResult.SUCCESS;
            }
            if (ArmorMaterials.ARMADILLO.value().repairIngredient().get().test(lv) && this.isInSittingPose() && this.hasArmor() && this.isOwner(player) && this.getBodyArmor().isDamaged()) {
                lv.decrement(1);
                this.playSoundIfNotSilent(SoundEvents.ITEM_WOLF_ARMOR_REPAIR);
                ItemStack lv6 = this.getBodyArmor();
                int i = (int)((float)lv6.getMaxDamage() * 0.125f);
                lv6.setDamage(Math.max(0, lv6.getDamage() - i));
                return ActionResult.SUCCESS;
            }
            ActionResult lv7 = super.interactMob(player, hand);
            if (lv7.isAccepted() || !this.isOwner(player)) return lv7;
            this.setSitting(!this.isSitting());
            this.jumping = false;
            this.navigation.stop();
            this.setTarget(null);
            return ActionResult.SUCCESS_NO_ITEM_USED;
        }
        if (!lv.isOf(Items.BONE) || this.hasAngerTime()) return super.interactMob(player, hand);
        lv.decrementUnlessCreative(1, player);
        this.tryTame(player);
        return ActionResult.SUCCESS;
    }

    private void tryTame(PlayerEntity player) {
        if (this.random.nextInt(3) == 0) {
            this.setOwner(player);
            this.navigation.stop();
            this.setTarget(null);
            this.setSitting(true);
            ((World)this.getWorld()).sendEntityStatus(this, EntityStatuses.ADD_POSITIVE_PLAYER_REACTION_PARTICLES);
        } else {
            ((World)this.getWorld()).sendEntityStatus(this, EntityStatuses.ADD_NEGATIVE_PLAYER_REACTION_PARTICLES);
        }
    }

    @Override
    public void handleStatus(byte status) {
        if (status == EntityStatuses.SHAKE_OFF_WATER) {
            this.canShakeWaterOff = true;
            this.shakeProgress = 0.0f;
            this.lastShakeProgress = 0.0f;
        } else if (status == EntityStatuses.RESET_WOLF_SHAKE) {
            this.resetShake();
        } else {
            super.handleStatus(status);
        }
    }

    public float getTailAngle() {
        if (this.hasAngerTime()) {
            return 1.5393804f;
        }
        if (this.isTamed()) {
            float f = this.getMaxHealth();
            float g = (f - this.getHealth()) / f;
            return (0.55f - g * 0.4f) * (float)Math.PI;
        }
        return 0.62831855f;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.isIn(ItemTags.WOLF_FOOD);
    }

    @Override
    public int getLimitPerChunk() {
        return 8;
    }

    @Override
    public int getAngerTime() {
        return this.dataTracker.get(ANGER_TIME);
    }

    @Override
    public void setAngerTime(int angerTime) {
        this.dataTracker.set(ANGER_TIME, angerTime);
    }

    @Override
    public void chooseRandomAngerTime() {
        this.setAngerTime(ANGER_TIME_RANGE.get(this.random));
    }

    @Override
    @Nullable
    public UUID getAngryAt() {
        return this.angryAt;
    }

    @Override
    public void setAngryAt(@Nullable UUID angryAt) {
        this.angryAt = angryAt;
    }

    public DyeColor getCollarColor() {
        return DyeColor.byId(this.dataTracker.get(COLLAR_COLOR));
    }

    public boolean hasArmor() {
        return this.getBodyArmor().isOf(Items.WOLF_ARMOR);
    }

    private void setCollarColor(DyeColor color) {
        this.dataTracker.set(COLLAR_COLOR, color.getId());
    }

    @Override
    @Nullable
    public WolfEntity createChild(ServerWorld arg, PassiveEntity arg2) {
        WolfEntity lv = EntityType.WOLF.create(arg);
        if (lv != null && arg2 instanceof WolfEntity) {
            WolfEntity lv2 = (WolfEntity)arg2;
            if (this.random.nextBoolean()) {
                lv.setVariant((RegistryEntry<WolfVariant>)this.getVariant());
            } else {
                lv.setVariant((RegistryEntry<WolfVariant>)lv2.getVariant());
            }
            if (this.isTamed()) {
                lv.setOwnerUuid(this.getOwnerUuid());
                lv.setTamed(true, true);
                if (this.random.nextBoolean()) {
                    lv.setCollarColor(this.getCollarColor());
                } else {
                    lv.setCollarColor(lv2.getCollarColor());
                }
            }
        }
        return lv;
    }

    public void setBegging(boolean begging) {
        this.dataTracker.set(BEGGING, begging);
    }

    @Override
    public boolean canBreedWith(AnimalEntity other) {
        if (other == this) {
            return false;
        }
        if (!this.isTamed()) {
            return false;
        }
        if (!(other instanceof WolfEntity)) {
            return false;
        }
        WolfEntity lv = (WolfEntity)other;
        if (!lv.isTamed()) {
            return false;
        }
        if (lv.isInSittingPose()) {
            return false;
        }
        return this.isInLove() && lv.isInLove();
    }

    public boolean isBegging() {
        return this.dataTracker.get(BEGGING);
    }

    @Override
    public boolean canAttackWithOwner(LivingEntity target, LivingEntity owner) {
        TameableEntity lv5;
        AbstractHorseEntity lv4;
        if (target instanceof CreeperEntity || target instanceof GhastEntity || target instanceof ArmorStandEntity) {
            return false;
        }
        if (target instanceof WolfEntity) {
            WolfEntity lv = (WolfEntity)target;
            return !lv.isTamed() || lv.getOwner() != owner;
        }
        if (target instanceof PlayerEntity) {
            PlayerEntity lv3;
            PlayerEntity lv2 = (PlayerEntity)target;
            if (owner instanceof PlayerEntity && !(lv3 = (PlayerEntity)owner).shouldDamagePlayer(lv2)) {
                return false;
            }
        }
        if (target instanceof AbstractHorseEntity && (lv4 = (AbstractHorseEntity)target).isTame()) {
            return false;
        }
        return !(target instanceof TameableEntity) || !(lv5 = (TameableEntity)target).isTamed();
    }

    @Override
    public boolean canBeLeashed() {
        return !this.hasAngerTime();
    }

    @Override
    public Vec3d getLeashOffset() {
        return new Vec3d(0.0, 0.6f * this.getStandingEyeHeight(), this.getWidth() * 0.4f);
    }

    public static boolean canSpawn(EntityType<WolfEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        return world.getBlockState(pos.down()).isIn(BlockTags.WOLVES_SPAWNABLE_ON) && WolfEntity.isLightLevelValidForNaturalSpawn(world, pos);
    }

    @Override
    @Nullable
    public /* synthetic */ PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return this.createChild(world, entity);
    }

    @Override
    public /* synthetic */ Object getVariant() {
        return this.getVariant();
    }

    @Override
    public /* synthetic */ void setVariant(Object variant) {
        this.setVariant((RegistryEntry)variant);
    }

    class AvoidLlamaGoal<T extends LivingEntity>
    extends FleeEntityGoal<T> {
        private final WolfEntity wolf;

        public AvoidLlamaGoal(WolfEntity wolf, Class<T> fleeFromType, float distance, double slowSpeed, double fastSpeed) {
            super(wolf, fleeFromType, distance, slowSpeed, fastSpeed);
            this.wolf = wolf;
        }

        @Override
        public boolean canStart() {
            if (super.canStart() && this.targetEntity instanceof LlamaEntity) {
                return !this.wolf.isTamed() && this.isScaredOf((LlamaEntity)this.targetEntity);
            }
            return false;
        }

        private boolean isScaredOf(LlamaEntity llama) {
            return llama.getStrength() >= WolfEntity.this.random.nextInt(5);
        }

        @Override
        public void start() {
            WolfEntity.this.setTarget(null);
            super.start();
        }

        @Override
        public void tick() {
            WolfEntity.this.setTarget(null);
            super.tick();
        }
    }

    public static class WolfData
    extends PassiveEntity.PassiveData {
        public final RegistryEntry<WolfVariant> variant;

        public WolfData(RegistryEntry<WolfVariant> variant) {
            super(false);
            this.variant = variant;
        }
    }
}

