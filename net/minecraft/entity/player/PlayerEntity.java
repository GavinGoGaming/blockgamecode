/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.player;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Predicate;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.block.entity.JigsawBlockEntity;
import net.minecraft.block.entity.SculkShriekerWarningManager;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAttachmentType;
import net.minecraft.entity.EntityAttachments;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.ProjectileDeflection;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.item.SwordItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.ClickType;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.TradeOfferList;
import net.minecraft.world.CommandBlockExecutor;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class PlayerEntity
extends LivingEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Arm DEFAULT_MAIN_ARM = Arm.RIGHT;
    public static final int field_46175 = 0;
    public static final int field_30644 = 20;
    public static final int field_30645 = 100;
    public static final int field_30646 = 10;
    public static final int field_30647 = 200;
    public static final int field_49734 = 499;
    public static final int field_49735 = 500;
    public static final float field_47819 = 4.5f;
    public static final float field_47820 = 3.0f;
    public static final float field_30648 = 1.5f;
    public static final float field_30649 = 0.6f;
    public static final float field_30650 = 0.6f;
    public static final float DEFAULT_EYE_HEIGHT = 1.62f;
    private static final int field_52222 = 40;
    public static final Vec3d VEHICLE_ATTACHMENT_POS = new Vec3d(0.0, 0.6, 0.0);
    public static final EntityDimensions STANDING_DIMENSIONS = EntityDimensions.changing(0.6f, 1.8f).withEyeHeight(1.62f).withAttachments(EntityAttachments.builder().add(EntityAttachmentType.VEHICLE, VEHICLE_ATTACHMENT_POS));
    private static final Map<EntityPose, EntityDimensions> POSE_DIMENSIONS = ImmutableMap.builder().put(EntityPose.STANDING, STANDING_DIMENSIONS).put(EntityPose.SLEEPING, SLEEPING_DIMENSIONS).put(EntityPose.FALL_FLYING, EntityDimensions.changing(0.6f, 0.6f).withEyeHeight(0.4f)).put(EntityPose.SWIMMING, EntityDimensions.changing(0.6f, 0.6f).withEyeHeight(0.4f)).put(EntityPose.SPIN_ATTACK, EntityDimensions.changing(0.6f, 0.6f).withEyeHeight(0.4f)).put(EntityPose.CROUCHING, EntityDimensions.changing(0.6f, 1.5f).withEyeHeight(1.27f).withAttachments(EntityAttachments.builder().add(EntityAttachmentType.VEHICLE, VEHICLE_ATTACHMENT_POS))).put(EntityPose.DYING, EntityDimensions.fixed(0.2f, 0.2f).withEyeHeight(1.62f)).build();
    private static final TrackedData<Float> ABSORPTION_AMOUNT = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Integer> SCORE = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.INTEGER);
    protected static final TrackedData<Byte> PLAYER_MODEL_PARTS = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.BYTE);
    protected static final TrackedData<Byte> MAIN_ARM = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.BYTE);
    protected static final TrackedData<NbtCompound> LEFT_SHOULDER_ENTITY = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.NBT_COMPOUND);
    protected static final TrackedData<NbtCompound> RIGHT_SHOULDER_ENTITY = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.NBT_COMPOUND);
    private long shoulderEntityAddedTime;
    final PlayerInventory inventory = new PlayerInventory(this);
    protected EnderChestInventory enderChestInventory = new EnderChestInventory();
    public final PlayerScreenHandler playerScreenHandler;
    public ScreenHandler currentScreenHandler;
    protected HungerManager hungerManager = new HungerManager();
    protected int abilityResyncCountdown;
    public float prevStrideDistance;
    public float strideDistance;
    public int experiencePickUpDelay;
    public double prevCapeX;
    public double prevCapeY;
    public double prevCapeZ;
    public double capeX;
    public double capeY;
    public double capeZ;
    private int sleepTimer;
    protected boolean isSubmergedInWater;
    private final PlayerAbilities abilities = new PlayerAbilities();
    public int experienceLevel;
    public int totalExperience;
    public float experienceProgress;
    protected int enchantmentTableSeed;
    protected final float field_7509 = 0.02f;
    private int lastPlayedLevelUpSoundTime;
    private final GameProfile gameProfile;
    private boolean reducedDebugInfo;
    private ItemStack selectedItem = ItemStack.EMPTY;
    private final ItemCooldownManager itemCooldownManager = this.createCooldownManager();
    private Optional<GlobalPos> lastDeathPos = Optional.empty();
    @Nullable
    public FishingBobberEntity fishHook;
    protected float damageTiltYaw;
    @Nullable
    public Vec3d currentExplosionImpactPos;
    @Nullable
    public Entity explodedBy;
    private boolean ignoreFallDamageFromCurrentExplosion;
    private int currentExplosionResetGraceTime;

    public PlayerEntity(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super((EntityType<? extends LivingEntity>)EntityType.PLAYER, world);
        this.setUuid(gameProfile.getId());
        this.gameProfile = gameProfile;
        this.playerScreenHandler = new PlayerScreenHandler(this.inventory, !world.isClient, this);
        this.currentScreenHandler = this.playerScreenHandler;
        this.refreshPositionAndAngles((double)pos.getX() + 0.5, pos.getY() + 1, (double)pos.getZ() + 0.5, yaw, 0.0f);
        this.field_6215 = 180.0f;
    }

    public boolean isBlockBreakingRestricted(World world, BlockPos pos, GameMode gameMode) {
        if (!gameMode.isBlockBreakingRestricted()) {
            return false;
        }
        if (gameMode == GameMode.SPECTATOR) {
            return true;
        }
        if (this.canModifyBlocks()) {
            return false;
        }
        ItemStack lv = this.getMainHandStack();
        return lv.isEmpty() || !lv.canBreak(new CachedBlockPosition(world, pos, false));
    }

    public static DefaultAttributeContainer.Builder createPlayerAttributes() {
        return LivingEntity.createLivingAttributes().add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 1.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.1f).add(EntityAttributes.GENERIC_ATTACK_SPEED).add(EntityAttributes.GENERIC_LUCK).add(EntityAttributes.PLAYER_BLOCK_INTERACTION_RANGE, 4.5).add(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE, 3.0).add(EntityAttributes.PLAYER_BLOCK_BREAK_SPEED).add(EntityAttributes.PLAYER_SUBMERGED_MINING_SPEED).add(EntityAttributes.PLAYER_SNEAKING_SPEED).add(EntityAttributes.PLAYER_MINING_EFFICIENCY).add(EntityAttributes.PLAYER_SWEEPING_DAMAGE_RATIO);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(ABSORPTION_AMOUNT, Float.valueOf(0.0f));
        builder.add(SCORE, 0);
        builder.add(PLAYER_MODEL_PARTS, (byte)0);
        builder.add(MAIN_ARM, (byte)DEFAULT_MAIN_ARM.getId());
        builder.add(LEFT_SHOULDER_ENTITY, new NbtCompound());
        builder.add(RIGHT_SHOULDER_ENTITY, new NbtCompound());
    }

    @Override
    public void tick() {
        this.noClip = this.isSpectator();
        if (this.isSpectator()) {
            this.setOnGround(false);
        }
        if (this.experiencePickUpDelay > 0) {
            --this.experiencePickUpDelay;
        }
        if (this.isSleeping()) {
            ++this.sleepTimer;
            if (this.sleepTimer > 100) {
                this.sleepTimer = 100;
            }
            if (!this.getWorld().isClient && this.getWorld().isDay()) {
                this.wakeUp(false, true);
            }
        } else if (this.sleepTimer > 0) {
            ++this.sleepTimer;
            if (this.sleepTimer >= 110) {
                this.sleepTimer = 0;
            }
        }
        this.updateWaterSubmersionState();
        super.tick();
        if (!this.getWorld().isClient && this.currentScreenHandler != null && !this.currentScreenHandler.canUse(this)) {
            this.closeHandledScreen();
            this.currentScreenHandler = this.playerScreenHandler;
        }
        this.updateCapeAngles();
        if (!this.getWorld().isClient) {
            this.hungerManager.update(this);
            this.incrementStat(Stats.PLAY_TIME);
            this.incrementStat(Stats.TOTAL_WORLD_TIME);
            if (this.isAlive()) {
                this.incrementStat(Stats.TIME_SINCE_DEATH);
            }
            if (this.isSneaky()) {
                this.incrementStat(Stats.SNEAK_TIME);
            }
            if (!this.isSleeping()) {
                this.incrementStat(Stats.TIME_SINCE_REST);
            }
        }
        int i = 29999999;
        double d = MathHelper.clamp(this.getX(), -2.9999999E7, 2.9999999E7);
        double e = MathHelper.clamp(this.getZ(), -2.9999999E7, 2.9999999E7);
        if (d != this.getX() || e != this.getZ()) {
            this.setPosition(d, this.getY(), e);
        }
        ++this.lastAttackedTicks;
        ItemStack lv = this.getMainHandStack();
        if (!ItemStack.areEqual(this.selectedItem, lv)) {
            if (!ItemStack.areItemsEqual(this.selectedItem, lv)) {
                this.resetLastAttackedTicks();
            }
            this.selectedItem = lv.copy();
        }
        this.updateTurtleHelmet();
        this.itemCooldownManager.update();
        this.updatePose();
        if (this.currentExplosionResetGraceTime > 0) {
            --this.currentExplosionResetGraceTime;
        }
    }

    @Override
    protected float getMaxRelativeHeadRotation() {
        if (this.isBlocking()) {
            return 15.0f;
        }
        return super.getMaxRelativeHeadRotation();
    }

    public boolean shouldCancelInteraction() {
        return this.isSneaking();
    }

    protected boolean shouldDismount() {
        return this.isSneaking();
    }

    protected boolean clipAtLedge() {
        return this.isSneaking();
    }

    protected boolean updateWaterSubmersionState() {
        this.isSubmergedInWater = this.isSubmergedIn(FluidTags.WATER);
        return this.isSubmergedInWater;
    }

    private void updateTurtleHelmet() {
        ItemStack lv = this.getEquippedStack(EquipmentSlot.HEAD);
        if (lv.isOf(Items.TURTLE_HELMET) && !this.isSubmergedIn(FluidTags.WATER)) {
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 200, 0, false, false, true));
        }
    }

    protected ItemCooldownManager createCooldownManager() {
        return new ItemCooldownManager();
    }

    private void updateCapeAngles() {
        this.prevCapeX = this.capeX;
        this.prevCapeY = this.capeY;
        this.prevCapeZ = this.capeZ;
        double d = this.getX() - this.capeX;
        double e = this.getY() - this.capeY;
        double f = this.getZ() - this.capeZ;
        double g = 10.0;
        if (d > 10.0) {
            this.prevCapeX = this.capeX = this.getX();
        }
        if (f > 10.0) {
            this.prevCapeZ = this.capeZ = this.getZ();
        }
        if (e > 10.0) {
            this.prevCapeY = this.capeY = this.getY();
        }
        if (d < -10.0) {
            this.prevCapeX = this.capeX = this.getX();
        }
        if (f < -10.0) {
            this.prevCapeZ = this.capeZ = this.getZ();
        }
        if (e < -10.0) {
            this.prevCapeY = this.capeY = this.getY();
        }
        this.capeX += d * 0.25;
        this.capeZ += f * 0.25;
        this.capeY += e * 0.25;
    }

    protected void updatePose() {
        if (!this.canChangeIntoPose(EntityPose.SWIMMING)) {
            return;
        }
        EntityPose lv = this.isFallFlying() ? EntityPose.FALL_FLYING : (this.isSleeping() ? EntityPose.SLEEPING : (this.isSwimming() ? EntityPose.SWIMMING : (this.isUsingRiptide() ? EntityPose.SPIN_ATTACK : (this.isSneaking() && !this.abilities.flying ? EntityPose.CROUCHING : EntityPose.STANDING))));
        EntityPose lv2 = this.isSpectator() || this.hasVehicle() || this.canChangeIntoPose(lv) ? lv : (this.canChangeIntoPose(EntityPose.CROUCHING) ? EntityPose.CROUCHING : EntityPose.SWIMMING);
        this.setPose(lv2);
    }

    protected boolean canChangeIntoPose(EntityPose pose) {
        return this.getWorld().isSpaceEmpty(this, this.getDimensions(pose).getBoxAt(this.getPos()).contract(1.0E-7));
    }

    @Override
    protected SoundEvent getSwimSound() {
        return SoundEvents.ENTITY_PLAYER_SWIM;
    }

    @Override
    protected SoundEvent getSplashSound() {
        return SoundEvents.ENTITY_PLAYER_SPLASH;
    }

    @Override
    protected SoundEvent getHighSpeedSplashSound() {
        return SoundEvents.ENTITY_PLAYER_SPLASH_HIGH_SPEED;
    }

    @Override
    public int getDefaultPortalCooldown() {
        return 10;
    }

    @Override
    public void playSound(SoundEvent sound, float volume, float pitch) {
        this.getWorld().playSound(this, this.getX(), this.getY(), this.getZ(), sound, this.getSoundCategory(), volume, pitch);
    }

    public void playSoundToPlayer(SoundEvent sound, SoundCategory category, float volume, float pitch) {
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.PLAYERS;
    }

    @Override
    protected int getBurningDuration() {
        return 20;
    }

    @Override
    public void handleStatus(byte status) {
        if (status == EntityStatuses.CONSUME_ITEM) {
            this.consumeItem();
        } else if (status == EntityStatuses.USE_FULL_DEBUG_INFO) {
            this.reducedDebugInfo = false;
        } else if (status == EntityStatuses.USE_REDUCED_DEBUG_INFO) {
            this.reducedDebugInfo = true;
        } else {
            super.handleStatus(status);
        }
    }

    protected void closeHandledScreen() {
        this.currentScreenHandler = this.playerScreenHandler;
    }

    protected void onHandledScreenClosed() {
    }

    @Override
    public void tickRiding() {
        if (!this.getWorld().isClient && this.shouldDismount() && this.hasVehicle()) {
            this.stopRiding();
            this.setSneaking(false);
            return;
        }
        super.tickRiding();
        this.prevStrideDistance = this.strideDistance;
        this.strideDistance = 0.0f;
    }

    @Override
    protected void tickNewAi() {
        super.tickNewAi();
        this.tickHandSwing();
        this.headYaw = this.getYaw();
    }

    @Override
    public void tickMovement() {
        if (this.abilityResyncCountdown > 0) {
            --this.abilityResyncCountdown;
        }
        if (this.getWorld().getDifficulty() == Difficulty.PEACEFUL && this.getWorld().getGameRules().getBoolean(GameRules.NATURAL_REGENERATION)) {
            if (this.getHealth() < this.getMaxHealth() && this.age % 20 == 0) {
                this.heal(1.0f);
            }
            if (this.hungerManager.getSaturationLevel() < 20.0f && this.age % 20 == 0) {
                this.hungerManager.setSaturationLevel(this.hungerManager.getSaturationLevel() + 1.0f);
            }
            if (this.hungerManager.isNotFull() && this.age % 10 == 0) {
                this.hungerManager.setFoodLevel(this.hungerManager.getFoodLevel() + 1);
            }
        }
        this.inventory.updateItems();
        this.prevStrideDistance = this.strideDistance;
        super.tickMovement();
        this.setMovementSpeed((float)this.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED));
        float f = !this.isOnGround() || this.isDead() || this.isSwimming() ? 0.0f : Math.min(0.1f, (float)this.getVelocity().horizontalLength());
        this.strideDistance += (f - this.strideDistance) * 0.4f;
        if (this.getHealth() > 0.0f && !this.isSpectator()) {
            Box lv = this.hasVehicle() && !this.getVehicle().isRemoved() ? this.getBoundingBox().union(this.getVehicle().getBoundingBox()).expand(1.0, 0.0, 1.0) : this.getBoundingBox().expand(1.0, 0.5, 1.0);
            List<Entity> list = this.getWorld().getOtherEntities(this, lv);
            ArrayList<Entity> list2 = Lists.newArrayList();
            for (Entity lv2 : list) {
                if (lv2.getType() == EntityType.EXPERIENCE_ORB) {
                    list2.add(lv2);
                    continue;
                }
                if (lv2.isRemoved()) continue;
                this.collideWithEntity(lv2);
            }
            if (!list2.isEmpty()) {
                this.collideWithEntity((Entity)Util.getRandom(list2, this.random));
            }
        }
        this.updateShoulderEntity(this.getShoulderEntityLeft());
        this.updateShoulderEntity(this.getShoulderEntityRight());
        if (!this.getWorld().isClient && (this.fallDistance > 0.5f || this.isTouchingWater()) || this.abilities.flying || this.isSleeping() || this.inPowderSnow) {
            this.dropShoulderEntities();
        }
    }

    private void updateShoulderEntity(@Nullable NbtCompound entityNbt) {
        if (!(entityNbt == null || entityNbt.contains("Silent") && entityNbt.getBoolean("Silent") || this.getWorld().random.nextInt(200) != 0)) {
            String string = entityNbt.getString("id");
            EntityType.get(string).filter(entityType -> entityType == EntityType.PARROT).ifPresent(parrotType -> {
                if (!ParrotEntity.imitateNearbyMob(this.getWorld(), this)) {
                    this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), ParrotEntity.getRandomSound(this.getWorld(), this.getWorld().random), this.getSoundCategory(), 1.0f, ParrotEntity.getSoundPitch(this.getWorld().random));
                }
            });
        }
    }

    private void collideWithEntity(Entity entity) {
        entity.onPlayerCollision(this);
    }

    public int getScore() {
        return this.dataTracker.get(SCORE);
    }

    public void setScore(int score) {
        this.dataTracker.set(SCORE, score);
    }

    public void addScore(int score) {
        int j = this.getScore();
        this.dataTracker.set(SCORE, j + score);
    }

    public void useRiptide(int riptideTicks, float riptideAttackDamage, ItemStack stack) {
        this.riptideTicks = riptideTicks;
        this.riptideAttackDamage = riptideAttackDamage;
        this.riptideStack = stack;
        if (!this.getWorld().isClient) {
            this.dropShoulderEntities();
            this.setLivingFlag(LivingEntity.USING_RIPTIDE_FLAG, true);
        }
    }

    @Override
    @NotNull
    public ItemStack getWeaponStack() {
        if (this.isUsingRiptide() && this.riptideStack != null) {
            return this.riptideStack;
        }
        return super.getWeaponStack();
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        World world;
        super.onDeath(damageSource);
        this.refreshPosition();
        if (!this.isSpectator() && (world = this.getWorld()) instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)world;
            this.drop(lv, damageSource);
        }
        if (damageSource != null) {
            this.setVelocity(-MathHelper.cos((this.getDamageTiltYaw() + this.getYaw()) * ((float)Math.PI / 180)) * 0.1f, 0.1f, -MathHelper.sin((this.getDamageTiltYaw() + this.getYaw()) * ((float)Math.PI / 180)) * 0.1f);
        } else {
            this.setVelocity(0.0, 0.1, 0.0);
        }
        this.incrementStat(Stats.DEATHS);
        this.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_DEATH));
        this.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_REST));
        this.extinguish();
        this.setOnFire(false);
        this.setLastDeathPos(Optional.of(GlobalPos.create(this.getWorld().getRegistryKey(), this.getBlockPos())));
    }

    @Override
    protected void dropInventory() {
        super.dropInventory();
        if (!this.getWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
            this.vanishCursedItems();
            this.inventory.dropAll();
        }
    }

    protected void vanishCursedItems() {
        for (int i = 0; i < this.inventory.size(); ++i) {
            ItemStack lv = this.inventory.getStack(i);
            if (lv.isEmpty() || !EnchantmentHelper.hasAnyEnchantmentsWith(lv, EnchantmentEffectComponentTypes.PREVENT_EQUIPMENT_DROP)) continue;
            this.inventory.removeStack(i);
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return source.getType().effects().getSound();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_PLAYER_DEATH;
    }

    @Nullable
    public ItemEntity dropItem(ItemStack stack, boolean retainOwnership) {
        return this.dropItem(stack, false, retainOwnership);
    }

    @Nullable
    public ItemEntity dropItem(ItemStack stack, boolean throwRandomly, boolean retainOwnership) {
        if (stack.isEmpty()) {
            return null;
        }
        if (this.getWorld().isClient) {
            this.swingHand(Hand.MAIN_HAND);
        }
        double d = this.getEyeY() - (double)0.3f;
        ItemEntity lv = new ItemEntity(this.getWorld(), this.getX(), d, this.getZ(), stack);
        lv.setPickupDelay(40);
        if (retainOwnership) {
            lv.setThrower(this);
        }
        if (throwRandomly) {
            float f = this.random.nextFloat() * 0.5f;
            float g = this.random.nextFloat() * ((float)Math.PI * 2);
            lv.setVelocity(-MathHelper.sin(g) * f, 0.2f, MathHelper.cos(g) * f);
        } else {
            float f = 0.3f;
            float g = MathHelper.sin(this.getPitch() * ((float)Math.PI / 180));
            float h = MathHelper.cos(this.getPitch() * ((float)Math.PI / 180));
            float i = MathHelper.sin(this.getYaw() * ((float)Math.PI / 180));
            float j = MathHelper.cos(this.getYaw() * ((float)Math.PI / 180));
            float k = this.random.nextFloat() * ((float)Math.PI * 2);
            float l = 0.02f * this.random.nextFloat();
            lv.setVelocity((double)(-i * h * 0.3f) + Math.cos(k) * (double)l, -g * 0.3f + 0.1f + (this.random.nextFloat() - this.random.nextFloat()) * 0.1f, (double)(j * h * 0.3f) + Math.sin(k) * (double)l);
        }
        return lv;
    }

    public float getBlockBreakingSpeed(BlockState block) {
        float f = this.inventory.getBlockBreakingSpeed(block);
        if (f > 1.0f) {
            f += (float)this.getAttributeValue(EntityAttributes.PLAYER_MINING_EFFICIENCY);
        }
        if (StatusEffectUtil.hasHaste(this)) {
            f *= 1.0f + (float)(StatusEffectUtil.getHasteAmplifier(this) + 1) * 0.2f;
        }
        if (this.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
            f *= (switch (this.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) {
                case 0 -> 0.3f;
                case 1 -> 0.09f;
                case 2 -> 0.0027f;
                default -> 8.1E-4f;
            });
        }
        f *= (float)this.getAttributeValue(EntityAttributes.PLAYER_BLOCK_BREAK_SPEED);
        if (this.isSubmergedIn(FluidTags.WATER)) {
            f *= (float)this.getAttributeInstance(EntityAttributes.PLAYER_SUBMERGED_MINING_SPEED).getValue();
        }
        if (!this.isOnGround()) {
            f /= 5.0f;
        }
        return f;
    }

    public boolean canHarvest(BlockState state) {
        return !state.isToolRequired() || this.inventory.getMainHandStack().isSuitableFor(state);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setUuid(this.gameProfile.getId());
        NbtList lv = nbt.getList("Inventory", NbtElement.COMPOUND_TYPE);
        this.inventory.readNbt(lv);
        this.inventory.selectedSlot = nbt.getInt("SelectedItemSlot");
        this.sleepTimer = nbt.getShort("SleepTimer");
        this.experienceProgress = nbt.getFloat("XpP");
        this.experienceLevel = nbt.getInt("XpLevel");
        this.totalExperience = nbt.getInt("XpTotal");
        this.enchantmentTableSeed = nbt.getInt("XpSeed");
        if (this.enchantmentTableSeed == 0) {
            this.enchantmentTableSeed = this.random.nextInt();
        }
        this.setScore(nbt.getInt("Score"));
        this.hungerManager.readNbt(nbt);
        this.abilities.readNbt(nbt);
        this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(this.abilities.getWalkSpeed());
        if (nbt.contains("EnderItems", NbtElement.LIST_TYPE)) {
            this.enderChestInventory.readNbtList(nbt.getList("EnderItems", NbtElement.COMPOUND_TYPE), this.getRegistryManager());
        }
        if (nbt.contains("ShoulderEntityLeft", NbtElement.COMPOUND_TYPE)) {
            this.setShoulderEntityLeft(nbt.getCompound("ShoulderEntityLeft"));
        }
        if (nbt.contains("ShoulderEntityRight", NbtElement.COMPOUND_TYPE)) {
            this.setShoulderEntityRight(nbt.getCompound("ShoulderEntityRight"));
        }
        if (nbt.contains("LastDeathLocation", NbtElement.COMPOUND_TYPE)) {
            this.setLastDeathPos(GlobalPos.CODEC.parse(NbtOps.INSTANCE, nbt.get("LastDeathLocation")).resultOrPartial(LOGGER::error));
        }
        if (nbt.contains("current_explosion_impact_pos", NbtElement.LIST_TYPE)) {
            Vec3d.CODEC.parse(NbtOps.INSTANCE, nbt.get("current_explosion_impact_pos")).resultOrPartial(LOGGER::error).ifPresent(currentExplosionImpactPos -> {
                this.currentExplosionImpactPos = currentExplosionImpactPos;
            });
        }
        this.ignoreFallDamageFromCurrentExplosion = nbt.getBoolean("ignore_fall_damage_from_current_explosion");
        this.currentExplosionResetGraceTime = nbt.getInt("current_impulse_context_reset_grace_time");
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        NbtHelper.putDataVersion(nbt);
        nbt.put("Inventory", this.inventory.writeNbt(new NbtList()));
        nbt.putInt("SelectedItemSlot", this.inventory.selectedSlot);
        nbt.putShort("SleepTimer", (short)this.sleepTimer);
        nbt.putFloat("XpP", this.experienceProgress);
        nbt.putInt("XpLevel", this.experienceLevel);
        nbt.putInt("XpTotal", this.totalExperience);
        nbt.putInt("XpSeed", this.enchantmentTableSeed);
        nbt.putInt("Score", this.getScore());
        this.hungerManager.writeNbt(nbt);
        this.abilities.writeNbt(nbt);
        nbt.put("EnderItems", this.enderChestInventory.toNbtList(this.getRegistryManager()));
        if (!this.getShoulderEntityLeft().isEmpty()) {
            nbt.put("ShoulderEntityLeft", this.getShoulderEntityLeft());
        }
        if (!this.getShoulderEntityRight().isEmpty()) {
            nbt.put("ShoulderEntityRight", this.getShoulderEntityRight());
        }
        this.getLastDeathPos().flatMap(pos -> GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, (GlobalPos)pos).resultOrPartial(LOGGER::error)).ifPresent(pos -> nbt.put("LastDeathLocation", (NbtElement)pos));
        if (this.currentExplosionImpactPos != null) {
            nbt.put("current_explosion_impact_pos", Vec3d.CODEC.encodeStart(NbtOps.INSTANCE, this.currentExplosionImpactPos).getOrThrow());
        }
        nbt.putBoolean("ignore_fall_damage_from_current_explosion", this.ignoreFallDamageFromCurrentExplosion);
        nbt.putInt("current_impulse_context_reset_grace_time", this.currentExplosionResetGraceTime);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        if (super.isInvulnerableTo(damageSource)) {
            return true;
        }
        if (damageSource.isIn(DamageTypeTags.IS_DROWNING)) {
            return !this.getWorld().getGameRules().getBoolean(GameRules.DROWNING_DAMAGE);
        }
        if (damageSource.isIn(DamageTypeTags.IS_FALL)) {
            return !this.getWorld().getGameRules().getBoolean(GameRules.FALL_DAMAGE);
        }
        if (damageSource.isIn(DamageTypeTags.IS_FIRE)) {
            return !this.getWorld().getGameRules().getBoolean(GameRules.FIRE_DAMAGE);
        }
        if (damageSource.isIn(DamageTypeTags.IS_FREEZING)) {
            return !this.getWorld().getGameRules().getBoolean(GameRules.FREEZE_DAMAGE);
        }
        return false;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        if (this.abilities.invulnerable && !source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }
        this.despawnCounter = 0;
        if (this.isDead()) {
            return false;
        }
        if (!this.getWorld().isClient) {
            this.dropShoulderEntities();
        }
        if (source.isScaledWithDifficulty()) {
            if (this.getWorld().getDifficulty() == Difficulty.PEACEFUL) {
                amount = 0.0f;
            }
            if (this.getWorld().getDifficulty() == Difficulty.EASY) {
                amount = Math.min(amount / 2.0f + 1.0f, amount);
            }
            if (this.getWorld().getDifficulty() == Difficulty.HARD) {
                amount = amount * 3.0f / 2.0f;
            }
        }
        if (amount == 0.0f) {
            return false;
        }
        return super.damage(source, amount);
    }

    @Override
    protected void takeShieldHit(LivingEntity attacker) {
        super.takeShieldHit(attacker);
        if (attacker.disablesShield()) {
            this.disableShield();
        }
    }

    @Override
    public boolean canTakeDamage() {
        return !this.getAbilities().invulnerable && super.canTakeDamage();
    }

    public boolean shouldDamagePlayer(PlayerEntity player) {
        Team lv = this.getScoreboardTeam();
        Team lv2 = player.getScoreboardTeam();
        if (lv == null) {
            return true;
        }
        if (!lv.isEqual(lv2)) {
            return true;
        }
        return ((AbstractTeam)lv).isFriendlyFireAllowed();
    }

    @Override
    protected void damageArmor(DamageSource source, float amount) {
        this.damageEquipment(source, amount, EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD);
    }

    @Override
    protected void damageHelmet(DamageSource source, float amount) {
        this.damageEquipment(source, amount, EquipmentSlot.HEAD);
    }

    @Override
    protected void damageShield(float amount) {
        if (!this.activeItemStack.isOf(Items.SHIELD)) {
            return;
        }
        if (!this.getWorld().isClient) {
            this.incrementStat(Stats.USED.getOrCreateStat(this.activeItemStack.getItem()));
        }
        if (amount >= 3.0f) {
            int i = 1 + MathHelper.floor(amount);
            Hand lv = this.getActiveHand();
            this.activeItemStack.damage(i, this, PlayerEntity.getSlotForHand(lv));
            if (this.activeItemStack.isEmpty()) {
                if (lv == Hand.MAIN_HAND) {
                    this.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                } else {
                    this.equipStack(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                }
                this.activeItemStack = ItemStack.EMPTY;
                this.playSound(SoundEvents.ITEM_SHIELD_BREAK, 0.8f, 0.8f + this.getWorld().random.nextFloat() * 0.4f);
            }
        }
    }

    @Override
    protected void applyDamage(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return;
        }
        amount = this.applyArmorToDamage(source, amount);
        float g = amount = this.modifyAppliedDamage(source, amount);
        amount = Math.max(amount - this.getAbsorptionAmount(), 0.0f);
        this.setAbsorptionAmount(this.getAbsorptionAmount() - (g - amount));
        float h = g - amount;
        if (h > 0.0f && h < 3.4028235E37f) {
            this.increaseStat(Stats.DAMAGE_ABSORBED, Math.round(h * 10.0f));
        }
        if (amount == 0.0f) {
            return;
        }
        this.addExhaustion(source.getExhaustion());
        this.getDamageTracker().onDamage(source, amount);
        this.setHealth(this.getHealth() - amount);
        if (amount < 3.4028235E37f) {
            this.increaseStat(Stats.DAMAGE_TAKEN, Math.round(amount * 10.0f));
        }
        this.emitGameEvent(GameEvent.ENTITY_DAMAGE);
    }

    public boolean shouldFilterText() {
        return false;
    }

    public void openEditSignScreen(SignBlockEntity sign, boolean front) {
    }

    public void openCommandBlockMinecartScreen(CommandBlockExecutor commandBlockExecutor) {
    }

    public void openCommandBlockScreen(CommandBlockBlockEntity commandBlock) {
    }

    public void openStructureBlockScreen(StructureBlockBlockEntity structureBlock) {
    }

    public void openJigsawScreen(JigsawBlockEntity jigsaw) {
    }

    public void openHorseInventory(AbstractHorseEntity horse, Inventory inventory) {
    }

    public OptionalInt openHandledScreen(@Nullable NamedScreenHandlerFactory factory) {
        return OptionalInt.empty();
    }

    public void sendTradeOffers(int syncId, TradeOfferList offers, int levelProgress, int experience, boolean leveled, boolean refreshable) {
    }

    public void useBook(ItemStack book, Hand hand) {
    }

    public ActionResult interact(Entity entity, Hand hand) {
        if (this.isSpectator()) {
            if (entity instanceof NamedScreenHandlerFactory) {
                this.openHandledScreen((NamedScreenHandlerFactory)((Object)entity));
            }
            return ActionResult.PASS;
        }
        ItemStack lv = this.getStackInHand(hand);
        ItemStack lv2 = lv.copy();
        ActionResult lv3 = entity.interact(this, hand);
        if (lv3.isAccepted()) {
            if (this.abilities.creativeMode && lv == this.getStackInHand(hand) && lv.getCount() < lv2.getCount()) {
                lv.setCount(lv2.getCount());
            }
            return lv3;
        }
        if (!lv.isEmpty() && entity instanceof LivingEntity) {
            ActionResult lv4;
            if (this.abilities.creativeMode) {
                lv = lv2;
            }
            if ((lv4 = lv.useOnEntity(this, (LivingEntity)entity, hand)).isAccepted()) {
                this.getWorld().emitGameEvent(GameEvent.ENTITY_INTERACT, entity.getPos(), GameEvent.Emitter.of(this));
                if (lv.isEmpty() && !this.abilities.creativeMode) {
                    this.setStackInHand(hand, ItemStack.EMPTY);
                }
                return lv4;
            }
        }
        return ActionResult.PASS;
    }

    @Override
    public void dismountVehicle() {
        super.dismountVehicle();
        this.ridingCooldown = 0;
    }

    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || this.isSleeping();
    }

    @Override
    public boolean shouldSwimInFluids() {
        return !this.abilities.flying;
    }

    @Override
    protected Vec3d adjustMovementForSneaking(Vec3d movement, MovementType type) {
        double d;
        float f = this.getStepHeight();
        if (this.abilities.flying || movement.y > 0.0 || type != MovementType.SELF && type != MovementType.PLAYER || !this.clipAtLedge() || !this.method_30263(f)) {
            return movement;
        }
        double e = movement.z;
        double g = 0.05;
        double h = Math.signum(d) * 0.05;
        double i = Math.signum(e) * 0.05;
        for (d = movement.x; d != 0.0 && this.isSpaceAroundPlayerEmpty(d, 0.0, f); d -= h) {
            if (!(Math.abs(d) <= 0.05)) continue;
            d = 0.0;
            break;
        }
        while (e != 0.0 && this.isSpaceAroundPlayerEmpty(0.0, e, f)) {
            if (Math.abs(e) <= 0.05) {
                e = 0.0;
                break;
            }
            e -= i;
        }
        while (d != 0.0 && e != 0.0 && this.isSpaceAroundPlayerEmpty(d, e, f)) {
            d = Math.abs(d) <= 0.05 ? 0.0 : (d -= h);
            if (Math.abs(e) <= 0.05) {
                e = 0.0;
                continue;
            }
            e -= i;
        }
        return new Vec3d(d, movement.y, e);
    }

    private boolean method_30263(float f) {
        return this.isOnGround() || this.fallDistance < f && !this.isSpaceAroundPlayerEmpty(0.0, 0.0, f - this.fallDistance);
    }

    private boolean isSpaceAroundPlayerEmpty(double offsetX, double offsetZ, float f) {
        Box lv = this.getBoundingBox();
        return this.getWorld().isSpaceEmpty(this, new Box(lv.minX + offsetX, lv.minY - (double)f - (double)1.0E-5f, lv.minZ + offsetZ, lv.maxX + offsetX, lv.minY, lv.maxZ + offsetZ));
    }

    public void attack(Entity target) {
        ProjectileEntity lv3;
        if (!target.isAttackable()) {
            return;
        }
        if (target.handleAttack(this)) {
            return;
        }
        float f = this.isUsingRiptide() ? this.riptideAttackDamage : (float)this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        ItemStack lv = this.getWeaponStack();
        DamageSource lv2 = this.getDamageSources().playerAttack(this);
        float g = this.getDamageAgainst(target, f, lv2) - f;
        float h = this.getAttackCooldownProgress(0.5f);
        f *= 0.2f + h * h * 0.8f;
        g *= h;
        this.resetLastAttackedTicks();
        if (target.getType().isIn(EntityTypeTags.REDIRECTABLE_PROJECTILE) && target instanceof ProjectileEntity && (lv3 = (ProjectileEntity)target).deflect(ProjectileDeflection.REDIRECTED, this, this, true)) {
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE, this.getSoundCategory());
            return;
        }
        if (f > 0.0f || g > 0.0f) {
            ItemStack lv4;
            boolean bl3;
            boolean bl2;
            boolean bl;
            boolean bl4 = bl = h > 0.9f;
            if (this.isSprinting() && bl) {
                this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, this.getSoundCategory(), 1.0f, 1.0f);
                bl2 = true;
            } else {
                bl2 = false;
            }
            f += lv.getItem().getBonusAttackDamage(target, f, lv2);
            boolean bl5 = bl3 = bl && this.fallDistance > 0.0f && !this.isOnGround() && !this.isClimbing() && !this.isTouchingWater() && !this.hasStatusEffect(StatusEffects.BLINDNESS) && !this.hasVehicle() && target instanceof LivingEntity && !this.isSprinting();
            if (bl3) {
                f *= 1.5f;
            }
            float i = f + g;
            boolean bl42 = false;
            double d = this.horizontalSpeed - this.prevHorizontalSpeed;
            if (bl && !bl3 && !bl2 && this.isOnGround() && d < (double)this.getMovementSpeed() && (lv4 = this.getStackInHand(Hand.MAIN_HAND)).getItem() instanceof SwordItem) {
                bl42 = true;
            }
            float j = 0.0f;
            if (target instanceof LivingEntity) {
                LivingEntity lv5 = (LivingEntity)target;
                j = lv5.getHealth();
            }
            Vec3d lv6 = target.getVelocity();
            boolean bl52 = target.damage(lv2, i);
            if (bl52) {
                float k = this.getKnockbackAgainst(target, lv2) + (bl2 ? 1.0f : 0.0f);
                if (k > 0.0f) {
                    if (target instanceof LivingEntity) {
                        LivingEntity lv7 = (LivingEntity)target;
                        lv7.takeKnockback(k * 0.5f, MathHelper.sin(this.getYaw() * ((float)Math.PI / 180)), -MathHelper.cos(this.getYaw() * ((float)Math.PI / 180)));
                    } else {
                        target.addVelocity(-MathHelper.sin(this.getYaw() * ((float)Math.PI / 180)) * k * 0.5f, 0.1, MathHelper.cos(this.getYaw() * ((float)Math.PI / 180)) * k * 0.5f);
                    }
                    this.setVelocity(this.getVelocity().multiply(0.6, 1.0, 0.6));
                    this.setSprinting(false);
                }
                if (bl42) {
                    float l = 1.0f + (float)this.getAttributeValue(EntityAttributes.PLAYER_SWEEPING_DAMAGE_RATIO) * f;
                    List<LivingEntity> list = this.getWorld().getNonSpectatingEntities(LivingEntity.class, target.getBoundingBox().expand(1.0, 0.25, 1.0));
                    for (LivingEntity livingEntity : list) {
                        if (livingEntity == this || livingEntity == target || this.isTeammate(livingEntity) || livingEntity instanceof ArmorStandEntity && ((ArmorStandEntity)livingEntity).isMarker() || !(this.squaredDistanceTo(livingEntity) < 9.0)) continue;
                        float m = this.getDamageAgainst(livingEntity, l, lv2) * h;
                        livingEntity.takeKnockback(0.4f, MathHelper.sin(this.getYaw() * ((float)Math.PI / 180)), -MathHelper.cos(this.getYaw() * ((float)Math.PI / 180)));
                        livingEntity.damage(lv2, m);
                        World world = this.getWorld();
                        if (!(world instanceof ServerWorld)) continue;
                        ServerWorld lv9 = (ServerWorld)world;
                        EnchantmentHelper.onTargetDamaged(lv9, livingEntity, lv2);
                    }
                    this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, this.getSoundCategory(), 1.0f, 1.0f);
                    this.spawnSweepAttackParticles();
                }
                if (target instanceof ServerPlayerEntity && target.velocityModified) {
                    ((ServerPlayerEntity)target).networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(target));
                    target.velocityModified = false;
                    target.setVelocity(lv6);
                }
                if (bl3) {
                    this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, this.getSoundCategory(), 1.0f, 1.0f);
                    this.addCritParticles(target);
                }
                if (!bl3 && !bl42) {
                    if (bl) {
                        this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, this.getSoundCategory(), 1.0f, 1.0f);
                    } else {
                        this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, this.getSoundCategory(), 1.0f, 1.0f);
                    }
                }
                if (g > 0.0f) {
                    this.addEnchantedHitParticles(target);
                }
                this.onAttacking(target);
                Entity lv10 = target;
                if (target instanceof EnderDragonPart) {
                    lv10 = ((EnderDragonPart)target).owner;
                }
                boolean bl6 = false;
                World world = this.getWorld();
                if (world instanceof ServerWorld) {
                    ServerWorld lv11 = (ServerWorld)world;
                    if (lv10 instanceof LivingEntity) {
                        LivingEntity livingEntity = (LivingEntity)lv10;
                        bl6 = lv.postHit(livingEntity, this);
                    }
                    EnchantmentHelper.onTargetDamaged(lv11, target, lv2);
                }
                if (!this.getWorld().isClient && !lv.isEmpty() && lv10 instanceof LivingEntity) {
                    if (bl6) {
                        lv.postDamageEntity((LivingEntity)lv10, this);
                    }
                    if (lv.isEmpty()) {
                        if (lv == this.getMainHandStack()) {
                            this.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
                        } else {
                            this.setStackInHand(Hand.OFF_HAND, ItemStack.EMPTY);
                        }
                    }
                }
                if (target instanceof LivingEntity) {
                    float n = j - ((LivingEntity)target).getHealth();
                    this.increaseStat(Stats.DAMAGE_DEALT, Math.round(n * 10.0f));
                    if (this.getWorld() instanceof ServerWorld && n > 2.0f) {
                        int n2 = (int)((double)n * 0.5);
                        ((ServerWorld)this.getWorld()).spawnParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getBodyY(0.5), target.getZ(), n2, 0.1, 0.0, 0.1, 0.2);
                    }
                }
                this.addExhaustion(0.1f);
            } else {
                this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE, this.getSoundCategory(), 1.0f, 1.0f);
            }
        }
    }

    protected float getDamageAgainst(Entity target, float baseDamage, DamageSource damageSource) {
        return baseDamage;
    }

    @Override
    protected void attackLivingEntity(LivingEntity target) {
        this.attack(target);
    }

    public void disableShield() {
        this.getItemCooldownManager().set(Items.SHIELD, 100);
        this.clearActiveItem();
        this.getWorld().sendEntityStatus(this, EntityStatuses.BREAK_SHIELD);
    }

    public void addCritParticles(Entity target) {
    }

    public void addEnchantedHitParticles(Entity target) {
    }

    public void spawnSweepAttackParticles() {
        double d = -MathHelper.sin(this.getYaw() * ((float)Math.PI / 180));
        double e = MathHelper.cos(this.getYaw() * ((float)Math.PI / 180));
        if (this.getWorld() instanceof ServerWorld) {
            ((ServerWorld)this.getWorld()).spawnParticles(ParticleTypes.SWEEP_ATTACK, this.getX() + d, this.getBodyY(0.5), this.getZ() + e, 0, d, 0.0, e, 0.0);
        }
    }

    public void requestRespawn() {
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        super.remove(reason);
        this.playerScreenHandler.onClosed(this);
        if (this.currentScreenHandler != null && this.shouldCloseHandledScreenOnRespawn()) {
            this.onHandledScreenClosed();
        }
    }

    public boolean isMainPlayer() {
        return false;
    }

    public GameProfile getGameProfile() {
        return this.gameProfile;
    }

    public PlayerInventory getInventory() {
        return this.inventory;
    }

    public PlayerAbilities getAbilities() {
        return this.abilities;
    }

    @Override
    public boolean isInCreativeMode() {
        return this.abilities.creativeMode;
    }

    public void onPickupSlotClick(ItemStack cursorStack, ItemStack slotStack, ClickType clickType) {
    }

    public boolean shouldCloseHandledScreenOnRespawn() {
        return this.currentScreenHandler != this.playerScreenHandler;
    }

    public Either<SleepFailureReason, Unit> trySleep(BlockPos pos) {
        this.sleep(pos);
        this.sleepTimer = 0;
        return Either.right(Unit.INSTANCE);
    }

    public void wakeUp(boolean skipSleepTimer, boolean updateSleepingPlayers) {
        super.wakeUp();
        if (this.getWorld() instanceof ServerWorld && updateSleepingPlayers) {
            ((ServerWorld)this.getWorld()).updateSleepingPlayers();
        }
        this.sleepTimer = skipSleepTimer ? 0 : 100;
    }

    @Override
    public void wakeUp() {
        this.wakeUp(true, true);
    }

    public boolean canResetTimeBySleeping() {
        return this.isSleeping() && this.sleepTimer >= 100;
    }

    public int getSleepTimer() {
        return this.sleepTimer;
    }

    public void sendMessage(Text message, boolean overlay) {
    }

    public void incrementStat(Identifier stat) {
        this.incrementStat(Stats.CUSTOM.getOrCreateStat(stat));
    }

    public void increaseStat(Identifier stat, int amount) {
        this.increaseStat(Stats.CUSTOM.getOrCreateStat(stat), amount);
    }

    public void incrementStat(Stat<?> stat) {
        this.increaseStat(stat, 1);
    }

    public void increaseStat(Stat<?> stat, int amount) {
    }

    public void resetStat(Stat<?> stat) {
    }

    public int unlockRecipes(Collection<RecipeEntry<?>> recipes) {
        return 0;
    }

    public void onRecipeCrafted(RecipeEntry<?> recipe, List<ItemStack> ingredients) {
    }

    public void unlockRecipes(List<Identifier> recipes) {
    }

    public int lockRecipes(Collection<RecipeEntry<?>> recipes) {
        return 0;
    }

    @Override
    public void jump() {
        super.jump();
        this.incrementStat(Stats.JUMP);
        if (this.isSprinting()) {
            this.addExhaustion(0.2f);
        } else {
            this.addExhaustion(0.05f);
        }
    }

    @Override
    public void travel(Vec3d movementInput) {
        double d;
        if (this.isSwimming() && !this.hasVehicle()) {
            double e;
            d = this.getRotationVector().y;
            double d2 = e = d < -0.2 ? 0.085 : 0.06;
            if (d <= 0.0 || this.jumping || !this.getWorld().getBlockState(BlockPos.ofFloored(this.getX(), this.getY() + 1.0 - 0.1, this.getZ())).getFluidState().isEmpty()) {
                Vec3d lv = this.getVelocity();
                this.setVelocity(lv.add(0.0, (d - lv.y) * e, 0.0));
            }
        }
        if (this.abilities.flying && !this.hasVehicle()) {
            d = this.getVelocity().y;
            super.travel(movementInput);
            Vec3d lv2 = this.getVelocity();
            this.setVelocity(lv2.x, d * 0.6, lv2.z);
            this.onLanding();
            this.setFlag(Entity.FALL_FLYING_FLAG_INDEX, false);
        } else {
            super.travel(movementInput);
        }
    }

    @Override
    public void updateSwimming() {
        if (this.abilities.flying) {
            this.setSwimming(false);
        } else {
            super.updateSwimming();
        }
    }

    protected boolean doesNotSuffocate(BlockPos pos) {
        return !this.getWorld().getBlockState(pos).shouldSuffocate(this.getWorld(), pos);
    }

    @Override
    public float getMovementSpeed() {
        return (float)this.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED);
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        boolean bl;
        if (this.abilities.allowFlying) {
            return false;
        }
        if (fallDistance >= 2.0f) {
            this.increaseStat(Stats.FALL_ONE_CM, (int)Math.round((double)fallDistance * 100.0));
        }
        if (this.ignoreFallDamageFromCurrentExplosion && this.currentExplosionImpactPos != null) {
            double d = this.currentExplosionImpactPos.y;
            this.tryClearCurrentExplosion();
            if (d < this.getY()) {
                return false;
            }
            float h = Math.min(fallDistance, (float)(d - this.getY()));
            bl = super.handleFallDamage(h, damageMultiplier, damageSource);
        } else {
            bl = super.handleFallDamage(fallDistance, damageMultiplier, damageSource);
        }
        if (bl) {
            this.clearCurrentExplosion();
        }
        return bl;
    }

    public boolean checkFallFlying() {
        ItemStack lv;
        if (!this.isOnGround() && !this.isFallFlying() && !this.isTouchingWater() && !this.hasStatusEffect(StatusEffects.LEVITATION) && (lv = this.getEquippedStack(EquipmentSlot.CHEST)).isOf(Items.ELYTRA) && ElytraItem.isUsable(lv)) {
            this.startFallFlying();
            return true;
        }
        return false;
    }

    public void startFallFlying() {
        this.setFlag(Entity.FALL_FLYING_FLAG_INDEX, true);
    }

    public void stopFallFlying() {
        this.setFlag(Entity.FALL_FLYING_FLAG_INDEX, true);
        this.setFlag(Entity.FALL_FLYING_FLAG_INDEX, false);
    }

    @Override
    protected void onSwimmingStart() {
        if (!this.isSpectator()) {
            super.onSwimmingStart();
        }
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        if (this.isTouchingWater()) {
            this.playSwimSound();
            this.playSecondaryStepSound(state);
        } else {
            BlockPos lv = this.getStepSoundPos(pos);
            if (!pos.equals(lv)) {
                BlockState lv2 = this.getWorld().getBlockState(lv);
                if (lv2.isIn(BlockTags.COMBINATION_STEP_SOUND_BLOCKS)) {
                    this.playCombinationStepSounds(lv2, state);
                } else {
                    super.playStepSound(lv, lv2);
                }
            } else {
                super.playStepSound(pos, state);
            }
        }
    }

    @Override
    public LivingEntity.FallSounds getFallSounds() {
        return new LivingEntity.FallSounds(SoundEvents.ENTITY_PLAYER_SMALL_FALL, SoundEvents.ENTITY_PLAYER_BIG_FALL);
    }

    @Override
    public boolean onKilledOther(ServerWorld world, LivingEntity other) {
        this.incrementStat(Stats.KILLED.getOrCreateStat(other.getType()));
        return true;
    }

    @Override
    public void slowMovement(BlockState state, Vec3d multiplier) {
        if (!this.abilities.flying) {
            super.slowMovement(state, multiplier);
        }
        this.tryClearCurrentExplosion();
    }

    public void addExperience(int experience) {
        this.addScore(experience);
        this.experienceProgress += (float)experience / (float)this.getNextLevelExperience();
        this.totalExperience = MathHelper.clamp(this.totalExperience + experience, 0, Integer.MAX_VALUE);
        while (this.experienceProgress < 0.0f) {
            float f = this.experienceProgress * (float)this.getNextLevelExperience();
            if (this.experienceLevel > 0) {
                this.addExperienceLevels(-1);
                this.experienceProgress = 1.0f + f / (float)this.getNextLevelExperience();
                continue;
            }
            this.addExperienceLevels(-1);
            this.experienceProgress = 0.0f;
        }
        while (this.experienceProgress >= 1.0f) {
            this.experienceProgress = (this.experienceProgress - 1.0f) * (float)this.getNextLevelExperience();
            this.addExperienceLevels(1);
            this.experienceProgress /= (float)this.getNextLevelExperience();
        }
    }

    public int getEnchantmentTableSeed() {
        return this.enchantmentTableSeed;
    }

    public void applyEnchantmentCosts(ItemStack enchantedItem, int experienceLevels) {
        this.experienceLevel -= experienceLevels;
        if (this.experienceLevel < 0) {
            this.experienceLevel = 0;
            this.experienceProgress = 0.0f;
            this.totalExperience = 0;
        }
        this.enchantmentTableSeed = this.random.nextInt();
    }

    public void addExperienceLevels(int levels) {
        this.experienceLevel += levels;
        if (this.experienceLevel < 0) {
            this.experienceLevel = 0;
            this.experienceProgress = 0.0f;
            this.totalExperience = 0;
        }
        if (levels > 0 && this.experienceLevel % 5 == 0 && (float)this.lastPlayedLevelUpSoundTime < (float)this.age - 100.0f) {
            float f = this.experienceLevel > 30 ? 1.0f : (float)this.experienceLevel / 30.0f;
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_LEVELUP, this.getSoundCategory(), f * 0.75f, 1.0f);
            this.lastPlayedLevelUpSoundTime = this.age;
        }
    }

    public int getNextLevelExperience() {
        if (this.experienceLevel >= 30) {
            return 112 + (this.experienceLevel - 30) * 9;
        }
        if (this.experienceLevel >= 15) {
            return 37 + (this.experienceLevel - 15) * 5;
        }
        return 7 + this.experienceLevel * 2;
    }

    public void addExhaustion(float exhaustion) {
        if (this.abilities.invulnerable) {
            return;
        }
        if (!this.getWorld().isClient) {
            this.hungerManager.addExhaustion(exhaustion);
        }
    }

    public Optional<SculkShriekerWarningManager> getSculkShriekerWarningManager() {
        return Optional.empty();
    }

    public HungerManager getHungerManager() {
        return this.hungerManager;
    }

    public boolean canConsume(boolean ignoreHunger) {
        return this.abilities.invulnerable || ignoreHunger || this.hungerManager.isNotFull();
    }

    public boolean canFoodHeal() {
        return this.getHealth() > 0.0f && this.getHealth() < this.getMaxHealth();
    }

    public boolean canModifyBlocks() {
        return this.abilities.allowModifyWorld;
    }

    public boolean canPlaceOn(BlockPos pos, Direction facing, ItemStack stack) {
        if (this.abilities.allowModifyWorld) {
            return true;
        }
        BlockPos lv = pos.offset(facing.getOpposite());
        CachedBlockPosition lv2 = new CachedBlockPosition(this.getWorld(), lv, false);
        return stack.canPlaceOn(lv2);
    }

    @Override
    protected int getXpToDrop() {
        if (this.getWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY) || this.isSpectator()) {
            return 0;
        }
        int i = this.experienceLevel * 7;
        if (i > 100) {
            return 100;
        }
        return i;
    }

    @Override
    protected boolean shouldAlwaysDropXp() {
        return true;
    }

    @Override
    public boolean shouldRenderName() {
        return true;
    }

    @Override
    protected Entity.MoveEffect getMoveEffect() {
        return !this.abilities.flying && (!this.isOnGround() || !this.isSneaky()) ? Entity.MoveEffect.ALL : Entity.MoveEffect.NONE;
    }

    public void sendAbilitiesUpdate() {
    }

    @Override
    public Text getName() {
        return Text.literal(this.gameProfile.getName());
    }

    public EnderChestInventory getEnderChestInventory() {
        return this.enderChestInventory;
    }

    @Override
    public ItemStack getEquippedStack(EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            return this.inventory.getMainHandStack();
        }
        if (slot == EquipmentSlot.OFFHAND) {
            return this.inventory.offHand.get(0);
        }
        if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
            return this.inventory.armor.get(slot.getEntitySlotId());
        }
        return ItemStack.EMPTY;
    }

    @Override
    protected boolean isArmorSlot(EquipmentSlot slot) {
        return slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR;
    }

    @Override
    public void equipStack(EquipmentSlot slot, ItemStack stack) {
        this.processEquippedStack(stack);
        if (slot == EquipmentSlot.MAINHAND) {
            this.onEquipStack(slot, this.inventory.main.set(this.inventory.selectedSlot, stack), stack);
        } else if (slot == EquipmentSlot.OFFHAND) {
            this.onEquipStack(slot, this.inventory.offHand.set(0, stack), stack);
        } else if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
            this.onEquipStack(slot, this.inventory.armor.set(slot.getEntitySlotId(), stack), stack);
        }
    }

    public boolean giveItemStack(ItemStack stack) {
        return this.inventory.insertStack(stack);
    }

    @Override
    public Iterable<ItemStack> getHandItems() {
        return Lists.newArrayList(this.getMainHandStack(), this.getOffHandStack());
    }

    @Override
    public Iterable<ItemStack> getArmorItems() {
        return this.inventory.armor;
    }

    @Override
    public boolean canUseSlot(EquipmentSlot slot) {
        return slot != EquipmentSlot.BODY;
    }

    public boolean addShoulderEntity(NbtCompound entityNbt) {
        if (this.hasVehicle() || !this.isOnGround() || this.isTouchingWater() || this.inPowderSnow) {
            return false;
        }
        if (this.getShoulderEntityLeft().isEmpty()) {
            this.setShoulderEntityLeft(entityNbt);
            this.shoulderEntityAddedTime = this.getWorld().getTime();
            return true;
        }
        if (this.getShoulderEntityRight().isEmpty()) {
            this.setShoulderEntityRight(entityNbt);
            this.shoulderEntityAddedTime = this.getWorld().getTime();
            return true;
        }
        return false;
    }

    protected void dropShoulderEntities() {
        if (this.shoulderEntityAddedTime + 20L < this.getWorld().getTime()) {
            this.dropShoulderEntity(this.getShoulderEntityLeft());
            this.setShoulderEntityLeft(new NbtCompound());
            this.dropShoulderEntity(this.getShoulderEntityRight());
            this.setShoulderEntityRight(new NbtCompound());
        }
    }

    private void dropShoulderEntity(NbtCompound entityNbt) {
        if (!this.getWorld().isClient && !entityNbt.isEmpty()) {
            EntityType.getEntityFromNbt(entityNbt, this.getWorld()).ifPresent(entity -> {
                if (entity instanceof TameableEntity) {
                    ((TameableEntity)entity).setOwnerUuid(this.uuid);
                }
                entity.setPosition(this.getX(), this.getY() + (double)0.7f, this.getZ());
                ((ServerWorld)this.getWorld()).tryLoadEntity((Entity)entity);
            });
        }
    }

    @Override
    public abstract boolean isSpectator();

    @Override
    public boolean canBeHitByProjectile() {
        return !this.isSpectator() && super.canBeHitByProjectile();
    }

    @Override
    public boolean isSwimming() {
        return !this.abilities.flying && !this.isSpectator() && super.isSwimming();
    }

    public abstract boolean isCreative();

    @Override
    public boolean isPushedByFluids() {
        return !this.abilities.flying;
    }

    public Scoreboard getScoreboard() {
        return this.getWorld().getScoreboard();
    }

    @Override
    public Text getDisplayName() {
        MutableText lv = Team.decorateName(this.getScoreboardTeam(), this.getName());
        return this.addTellClickEvent(lv);
    }

    private MutableText addTellClickEvent(MutableText component) {
        String string = this.getGameProfile().getName();
        return component.styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tell " + string + " ")).withHoverEvent(this.getHoverEvent()).withInsertion(string));
    }

    @Override
    public String getNameForScoreboard() {
        return this.getGameProfile().getName();
    }

    @Override
    protected void setAbsorptionAmountUnclamped(float absorptionAmount) {
        this.getDataTracker().set(ABSORPTION_AMOUNT, Float.valueOf(absorptionAmount));
    }

    @Override
    public float getAbsorptionAmount() {
        return this.getDataTracker().get(ABSORPTION_AMOUNT).floatValue();
    }

    public boolean isPartVisible(PlayerModelPart modelPart) {
        return (this.getDataTracker().get(PLAYER_MODEL_PARTS) & modelPart.getBitFlag()) == modelPart.getBitFlag();
    }

    @Override
    public StackReference getStackReference(int mappedIndex) {
        if (mappedIndex == 499) {
            return new StackReference(){

                @Override
                public ItemStack get() {
                    return PlayerEntity.this.currentScreenHandler.getCursorStack();
                }

                @Override
                public boolean set(ItemStack stack) {
                    PlayerEntity.this.currentScreenHandler.setCursorStack(stack);
                    return true;
                }
            };
        }
        final int j = mappedIndex - 500;
        if (j >= 0 && j < 4) {
            return new StackReference(){

                @Override
                public ItemStack get() {
                    return PlayerEntity.this.playerScreenHandler.getCraftingInput().getStack(j);
                }

                @Override
                public boolean set(ItemStack stack) {
                    PlayerEntity.this.playerScreenHandler.getCraftingInput().setStack(j, stack);
                    PlayerEntity.this.playerScreenHandler.onContentChanged(PlayerEntity.this.inventory);
                    return true;
                }
            };
        }
        if (mappedIndex >= 0 && mappedIndex < this.inventory.main.size()) {
            return StackReference.of(this.inventory, mappedIndex);
        }
        int k = mappedIndex - 200;
        if (k >= 0 && k < this.enderChestInventory.size()) {
            return StackReference.of(this.enderChestInventory, k);
        }
        return super.getStackReference(mappedIndex);
    }

    public boolean hasReducedDebugInfo() {
        return this.reducedDebugInfo;
    }

    public void setReducedDebugInfo(boolean reducedDebugInfo) {
        this.reducedDebugInfo = reducedDebugInfo;
    }

    @Override
    public void setFireTicks(int fireTicks) {
        super.setFireTicks(this.abilities.invulnerable ? Math.min(fireTicks, 1) : fireTicks);
    }

    @Override
    public Arm getMainArm() {
        return this.dataTracker.get(MAIN_ARM) == 0 ? Arm.LEFT : Arm.RIGHT;
    }

    public void setMainArm(Arm arm) {
        this.dataTracker.set(MAIN_ARM, (byte)(arm != Arm.LEFT ? 1 : 0));
    }

    public NbtCompound getShoulderEntityLeft() {
        return this.dataTracker.get(LEFT_SHOULDER_ENTITY);
    }

    protected void setShoulderEntityLeft(NbtCompound entityNbt) {
        this.dataTracker.set(LEFT_SHOULDER_ENTITY, entityNbt);
    }

    public NbtCompound getShoulderEntityRight() {
        return this.dataTracker.get(RIGHT_SHOULDER_ENTITY);
    }

    protected void setShoulderEntityRight(NbtCompound entityNbt) {
        this.dataTracker.set(RIGHT_SHOULDER_ENTITY, entityNbt);
    }

    public float getAttackCooldownProgressPerTick() {
        return (float)(1.0 / this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED) * 20.0);
    }

    public float getAttackCooldownProgress(float baseTime) {
        return MathHelper.clamp(((float)this.lastAttackedTicks + baseTime) / this.getAttackCooldownProgressPerTick(), 0.0f, 1.0f);
    }

    public void resetLastAttackedTicks() {
        this.lastAttackedTicks = 0;
    }

    public ItemCooldownManager getItemCooldownManager() {
        return this.itemCooldownManager;
    }

    @Override
    protected float getVelocityMultiplier() {
        return this.abilities.flying || this.isFallFlying() ? 1.0f : super.getVelocityMultiplier();
    }

    public float getLuck() {
        return (float)this.getAttributeValue(EntityAttributes.GENERIC_LUCK);
    }

    public boolean isCreativeLevelTwoOp() {
        return this.abilities.creativeMode && this.getPermissionLevel() >= 2;
    }

    @Override
    public boolean canEquip(ItemStack stack) {
        EquipmentSlot lv = this.getPreferredEquipmentSlot(stack);
        return this.getEquippedStack(lv).isEmpty();
    }

    @Override
    public EntityDimensions getBaseDimensions(EntityPose pose) {
        return POSE_DIMENSIONS.getOrDefault((Object)pose, STANDING_DIMENSIONS);
    }

    @Override
    public ImmutableList<EntityPose> getPoses() {
        return ImmutableList.of(EntityPose.STANDING, EntityPose.CROUCHING, EntityPose.SWIMMING);
    }

    @Override
    public ItemStack getProjectileType(ItemStack stack) {
        if (!(stack.getItem() instanceof RangedWeaponItem)) {
            return ItemStack.EMPTY;
        }
        Predicate<ItemStack> predicate = ((RangedWeaponItem)stack.getItem()).getHeldProjectiles();
        ItemStack lv = RangedWeaponItem.getHeldProjectile(this, predicate);
        if (!lv.isEmpty()) {
            return lv;
        }
        predicate = ((RangedWeaponItem)stack.getItem()).getProjectiles();
        for (int i = 0; i < this.inventory.size(); ++i) {
            ItemStack lv2 = this.inventory.getStack(i);
            if (!predicate.test(lv2)) continue;
            return lv2;
        }
        return this.abilities.creativeMode ? new ItemStack(Items.ARROW) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack eatFood(World world, ItemStack stack, FoodComponent foodComponent) {
        this.getHungerManager().eat(foodComponent);
        this.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
        world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 0.5f, world.random.nextFloat() * 0.1f + 0.9f);
        if (this instanceof ServerPlayerEntity) {
            Criteria.CONSUME_ITEM.trigger((ServerPlayerEntity)this, stack);
        }
        ItemStack lv = super.eatFood(world, stack, foodComponent);
        Optional<ItemStack> optional = foodComponent.usingConvertsTo();
        if (optional.isPresent() && !this.isInCreativeMode()) {
            if (lv.isEmpty()) {
                return optional.get().copy();
            }
            if (!this.getWorld().isClient()) {
                this.getInventory().insertStack(optional.get().copy());
            }
        }
        return lv;
    }

    @Override
    public Vec3d getLeashPos(float delta) {
        double d = 0.22 * (this.getMainArm() == Arm.RIGHT ? -1.0 : 1.0);
        float g = MathHelper.lerp(delta * 0.5f, this.getPitch(), this.prevPitch) * ((float)Math.PI / 180);
        float h = MathHelper.lerp(delta, this.prevBodyYaw, this.bodyYaw) * ((float)Math.PI / 180);
        if (this.isFallFlying() || this.isUsingRiptide()) {
            float l;
            Vec3d lv = this.getRotationVec(delta);
            Vec3d lv2 = this.getVelocity();
            double e = lv2.horizontalLengthSquared();
            double i = lv.horizontalLengthSquared();
            if (e > 0.0 && i > 0.0) {
                double j = (lv2.x * lv.x + lv2.z * lv.z) / Math.sqrt(e * i);
                double k = lv2.x * lv.z - lv2.z * lv.x;
                l = (float)(Math.signum(k) * Math.acos(j));
            } else {
                l = 0.0f;
            }
            return this.getLerpedPos(delta).add(new Vec3d(d, -0.11, 0.85).rotateZ(-l).rotateX(-g).rotateY(-h));
        }
        if (this.isInSwimmingPose()) {
            return this.getLerpedPos(delta).add(new Vec3d(d, 0.2, -0.15).rotateX(-g).rotateY(-h));
        }
        double m = this.getBoundingBox().getLengthY() - 1.0;
        double e = this.isInSneakingPose() ? -0.2 : 0.07;
        return this.getLerpedPos(delta).add(new Vec3d(d, m, e).rotateY(-h));
    }

    @Override
    public boolean isPlayer() {
        return true;
    }

    public boolean isUsingSpyglass() {
        return this.isUsingItem() && this.getActiveItem().isOf(Items.SPYGLASS);
    }

    @Override
    public boolean shouldSave() {
        return false;
    }

    public Optional<GlobalPos> getLastDeathPos() {
        return this.lastDeathPos;
    }

    public void setLastDeathPos(Optional<GlobalPos> lastDeathPos) {
        this.lastDeathPos = lastDeathPos;
    }

    @Override
    public float getDamageTiltYaw() {
        return this.damageTiltYaw;
    }

    @Override
    public void animateDamage(float yaw) {
        super.animateDamage(yaw);
        this.damageTiltYaw = yaw;
    }

    @Override
    public boolean canSprintAsVehicle() {
        return true;
    }

    @Override
    protected float getOffGroundSpeed() {
        if (this.abilities.flying && !this.hasVehicle()) {
            return this.isSprinting() ? this.abilities.getFlySpeed() * 2.0f : this.abilities.getFlySpeed();
        }
        return this.isSprinting() ? 0.025999999f : 0.02f;
    }

    public double getBlockInteractionRange() {
        return this.getAttributeValue(EntityAttributes.PLAYER_BLOCK_INTERACTION_RANGE);
    }

    public double getEntityInteractionRange() {
        return this.getAttributeValue(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE);
    }

    public boolean canInteractWithEntity(Entity entity, double additionalRange) {
        if (entity.isRemoved()) {
            return false;
        }
        return this.canInteractWithEntityIn(entity.getBoundingBox(), additionalRange);
    }

    public boolean canInteractWithEntityIn(Box box, double additionalRange) {
        double e = this.getEntityInteractionRange() + additionalRange;
        return box.squaredMagnitude(this.getEyePos()) < e * e;
    }

    public boolean canInteractWithBlockAt(BlockPos pos, double additionalRange) {
        double e = this.getBlockInteractionRange() + additionalRange;
        return new Box(pos).squaredMagnitude(this.getEyePos()) < e * e;
    }

    public void setIgnoreFallDamageFromCurrentExplosion(boolean ignoreFallDamageFromCurrentExplosion) {
        this.ignoreFallDamageFromCurrentExplosion = ignoreFallDamageFromCurrentExplosion;
        this.currentExplosionResetGraceTime = ignoreFallDamageFromCurrentExplosion ? 40 : 0;
    }

    public boolean shouldIgnoreFallDamageFromCurrentExplosion() {
        return this.ignoreFallDamageFromCurrentExplosion;
    }

    public void tryClearCurrentExplosion() {
        if (this.currentExplosionResetGraceTime == 0) {
            this.clearCurrentExplosion();
        }
    }

    public void clearCurrentExplosion() {
        this.currentExplosionResetGraceTime = 0;
        this.explodedBy = null;
        this.currentExplosionImpactPos = null;
        this.ignoreFallDamageFromCurrentExplosion = false;
    }

    public static enum SleepFailureReason {
        NOT_POSSIBLE_HERE,
        NOT_POSSIBLE_NOW(Text.translatable("block.minecraft.bed.no_sleep")),
        TOO_FAR_AWAY(Text.translatable("block.minecraft.bed.too_far_away")),
        OBSTRUCTED(Text.translatable("block.minecraft.bed.obstructed")),
        OTHER_PROBLEM,
        NOT_SAFE(Text.translatable("block.minecraft.bed.not_safe"));

        @Nullable
        private final Text message;

        private SleepFailureReason() {
            this.message = null;
        }

        private SleepFailureReason(Text message) {
            this.message = message;
        }

        @Nullable
        public Text getMessage() {
            return this.message;
        }
    }
}

