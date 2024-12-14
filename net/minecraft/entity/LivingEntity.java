/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HoneyBlock;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.PowderSnowBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.effect.EnchantmentLocationBasedEffect;
import net.minecraft.entity.Attackable;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.Flutterer;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LimbAnimator;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.Equipment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket;
import net.minecraft.network.packet.s2c.play.RemoveEntityStatusEffectS2CPacket;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.UseAction;
import net.minecraft.util.Util;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.CollisionView;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class LivingEntity
extends Entity
implements Attackable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String ACTIVE_EFFECTS_NBT_KEY = "active_effects";
    private static final Identifier POWDER_SNOW_SPEED_MODIFIER_ID = Identifier.ofVanilla("powder_snow");
    private static final Identifier SPRINTING_SPEED_MODIFIER_ID = Identifier.ofVanilla("sprinting");
    private static final EntityAttributeModifier SPRINTING_SPEED_BOOST = new EntityAttributeModifier(SPRINTING_SPEED_MODIFIER_ID, 0.3f, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    public static final int field_30069 = 2;
    public static final int field_30070 = 4;
    public static final int EQUIPMENT_SLOT_ID = 98;
    public static final int field_30072 = 100;
    public static final int field_48827 = 105;
    public static final int GLOWING_FLAG = 6;
    public static final int field_30074 = 100;
    private static final int field_30078 = 40;
    public static final double field_30075 = 0.003;
    public static final double GRAVITY = 0.08;
    public static final int DEATH_TICKS = 20;
    private static final int field_30080 = 10;
    private static final int field_30081 = 2;
    public static final int field_30063 = 4;
    public static final float field_44874 = 0.42f;
    private static final double MAX_ENTITY_VIEWING_DISTANCE = 128.0;
    protected static final int USING_ITEM_FLAG = 1;
    protected static final int OFF_HAND_ACTIVE_FLAG = 2;
    protected static final int USING_RIPTIDE_FLAG = 4;
    protected static final TrackedData<Byte> LIVING_FLAGS = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final TrackedData<Float> HEALTH = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<List<ParticleEffect>> POTION_SWIRLS = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.PARTICLE_LIST);
    private static final TrackedData<Boolean> POTION_SWIRLS_AMBIENT = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> STUCK_ARROW_COUNT = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> STINGER_COUNT = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Optional<BlockPos>> SLEEPING_POSITION = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS);
    private static final int field_49793 = 15;
    protected static final EntityDimensions SLEEPING_DIMENSIONS = EntityDimensions.fixed(0.2f, 0.2f).withEyeHeight(0.2f);
    public static final float BABY_SCALE_FACTOR = 0.5f;
    public static final float field_47756 = 0.5f;
    private static final float field_49972 = 0.21875f;
    public static final String ATTRIBUTES_NBT_KEY = "attributes";
    private final AttributeContainer attributes;
    private final DamageTracker damageTracker = new DamageTracker(this);
    private final Map<RegistryEntry<StatusEffect>, StatusEffectInstance> activeStatusEffects = Maps.newHashMap();
    private final DefaultedList<ItemStack> syncedHandStacks = DefaultedList.ofSize(2, ItemStack.EMPTY);
    private final DefaultedList<ItemStack> syncedArmorStacks = DefaultedList.ofSize(4, ItemStack.EMPTY);
    private ItemStack syncedBodyArmorStack = ItemStack.EMPTY;
    public boolean handSwinging;
    private boolean noDrag = false;
    public Hand preferredHand;
    public int handSwingTicks;
    public int stuckArrowTimer;
    public int stuckStingerTimer;
    public int hurtTime;
    public int maxHurtTime;
    public int deathTime;
    public float lastHandSwingProgress;
    public float handSwingProgress;
    protected int lastAttackedTicks;
    public final LimbAnimator limbAnimator = new LimbAnimator();
    public final int defaultMaxHealth = 20;
    public final float randomLargeSeed;
    public final float randomSmallSeed;
    public float bodyYaw;
    public float prevBodyYaw;
    public float headYaw;
    public float prevHeadYaw;
    @Nullable
    protected PlayerEntity attackingPlayer;
    protected int playerHitTimer;
    protected boolean dead;
    protected int despawnCounter;
    protected float prevStepBobbingAmount;
    protected float stepBobbingAmount;
    protected float lookDirection;
    protected float prevLookDirection;
    protected float field_6215;
    protected int scoreAmount;
    protected float lastDamageTaken;
    protected boolean jumping;
    public float sidewaysSpeed;
    public float upwardSpeed;
    public float forwardSpeed;
    protected int bodyTrackingIncrements;
    protected double serverX;
    protected double serverY;
    protected double serverZ;
    protected double serverYaw;
    protected double serverPitch;
    protected double serverHeadYaw;
    protected int headTrackingIncrements;
    private boolean effectsChanged = true;
    @Nullable
    private LivingEntity attacker;
    private int lastAttackedTime;
    @Nullable
    private LivingEntity attacking;
    private int lastAttackTime;
    private float movementSpeed;
    private int jumpingCooldown;
    private float absorptionAmount;
    protected ItemStack activeItemStack = ItemStack.EMPTY;
    protected int itemUseTimeLeft;
    protected int fallFlyingTicks;
    private BlockPos lastBlockPos;
    private Optional<BlockPos> climbingPos = Optional.empty();
    @Nullable
    private DamageSource lastDamageSource;
    private long lastDamageTime;
    protected int riptideTicks;
    protected float riptideAttackDamage;
    @Nullable
    protected ItemStack riptideStack;
    private float leaningPitch;
    private float lastLeaningPitch;
    protected Brain<?> brain;
    private boolean experienceDroppingDisabled;
    private final Reference2ObjectMap<Enchantment, Set<EnchantmentLocationBasedEffect>> locationBasedEnchantmentEffects = new Reference2ObjectArrayMap<Enchantment, Set<EnchantmentLocationBasedEffect>>();
    protected float prevScale = 1.0f;

    protected LivingEntity(EntityType<? extends LivingEntity> arg, World arg2) {
        super(arg, arg2);
        this.attributes = new AttributeContainer(DefaultAttributeRegistry.get(arg));
        this.setHealth(this.getMaxHealth());
        this.intersectionChecked = true;
        this.randomSmallSeed = (float)((Math.random() + 1.0) * (double)0.01f);
        this.refreshPosition();
        this.randomLargeSeed = (float)Math.random() * 12398.0f;
        this.setYaw((float)(Math.random() * 6.2831854820251465));
        this.headYaw = this.getYaw();
        NbtOps lv = NbtOps.INSTANCE;
        this.brain = this.deserializeBrain(new Dynamic<NbtElement>(lv, lv.createMap(ImmutableMap.of(lv.createString("memories"), (NbtElement)lv.emptyMap()))));
    }

    public Brain<?> getBrain() {
        return this.brain;
    }

    protected Brain.Profile<?> createBrainProfile() {
        return Brain.createProfile(ImmutableList.of(), ImmutableList.of());
    }

    protected Brain<?> deserializeBrain(Dynamic<?> dynamic) {
        return this.createBrainProfile().deserialize(dynamic);
    }

    @Override
    public void kill() {
        this.damage(this.getDamageSources().genericKill(), Float.MAX_VALUE);
    }

    public boolean canTarget(EntityType<?> type) {
        return true;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(LIVING_FLAGS, (byte)0);
        builder.add(POTION_SWIRLS, List.of());
        builder.add(POTION_SWIRLS_AMBIENT, false);
        builder.add(STUCK_ARROW_COUNT, 0);
        builder.add(STINGER_COUNT, 0);
        builder.add(HEALTH, Float.valueOf(1.0f));
        builder.add(SLEEPING_POSITION, Optional.empty());
    }

    public static DefaultAttributeContainer.Builder createLivingAttributes() {
        return DefaultAttributeContainer.builder().add(EntityAttributes.GENERIC_MAX_HEALTH).add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE).add(EntityAttributes.GENERIC_MOVEMENT_SPEED).add(EntityAttributes.GENERIC_ARMOR).add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).add(EntityAttributes.GENERIC_MAX_ABSORPTION).add(EntityAttributes.GENERIC_STEP_HEIGHT).add(EntityAttributes.GENERIC_SCALE).add(EntityAttributes.GENERIC_GRAVITY).add(EntityAttributes.GENERIC_SAFE_FALL_DISTANCE).add(EntityAttributes.GENERIC_FALL_DAMAGE_MULTIPLIER).add(EntityAttributes.GENERIC_JUMP_STRENGTH).add(EntityAttributes.GENERIC_OXYGEN_BONUS).add(EntityAttributes.GENERIC_BURNING_TIME).add(EntityAttributes.GENERIC_EXPLOSION_KNOCKBACK_RESISTANCE).add(EntityAttributes.GENERIC_WATER_MOVEMENT_EFFICIENCY).add(EntityAttributes.GENERIC_MOVEMENT_EFFICIENCY).add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK);
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
        World world;
        if (!this.isTouchingWater()) {
            this.checkWaterState();
        }
        if ((world = this.getWorld()) instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)world;
            if (onGround && this.fallDistance > 0.0f) {
                this.applyMovementEffects(lv, landedPosition);
                double e = this.getAttributeValue(EntityAttributes.GENERIC_SAFE_FALL_DISTANCE);
                if ((double)this.fallDistance > e && !state.isAir()) {
                    double f = this.getX();
                    double g = this.getY();
                    double h = this.getZ();
                    BlockPos lv2 = this.getBlockPos();
                    if (landedPosition.getX() != lv2.getX() || landedPosition.getZ() != lv2.getZ()) {
                        double i = f - (double)landedPosition.getX() - 0.5;
                        double j = h - (double)landedPosition.getZ() - 0.5;
                        double k = Math.max(Math.abs(i), Math.abs(j));
                        f = (double)landedPosition.getX() + 0.5 + i / k * 0.5;
                        h = (double)landedPosition.getZ() + 0.5 + j / k * 0.5;
                    }
                    float l = MathHelper.ceil((double)this.fallDistance - e);
                    double m = Math.min((double)(0.2f + l / 15.0f), 2.5);
                    int n = (int)(150.0 * m);
                    ((ServerWorld)this.getWorld()).spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, state), f, g, h, n, 0.0, 0.0, 0.0, 0.15f);
                }
            }
        }
        super.fall(heightDifference, onGround, state, landedPosition);
        if (onGround) {
            this.climbingPos = Optional.empty();
        }
    }

    public final boolean canBreatheInWater() {
        return this.getType().isIn(EntityTypeTags.CAN_BREATHE_UNDER_WATER);
    }

    public float getLeaningPitch(float tickDelta) {
        return MathHelper.lerp(tickDelta, this.lastLeaningPitch, this.leaningPitch);
    }

    public boolean hasLandedInFluid() {
        return this.getVelocity().getY() < (double)1.0E-5f && this.isInFluid();
    }

    @Override
    public void baseTick() {
        World world;
        this.lastHandSwingProgress = this.handSwingProgress;
        if (this.firstUpdate) {
            this.getSleepingPosition().ifPresent(this::setPositionInBed);
        }
        if ((world = this.getWorld()) instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)world;
            EnchantmentHelper.onTick(lv, this);
        }
        super.baseTick();
        this.getWorld().getProfiler().push("livingEntityBaseTick");
        if (this.isFireImmune() || this.getWorld().isClient) {
            this.extinguish();
        }
        if (this.isAlive()) {
            Object lv2;
            boolean bl = this instanceof PlayerEntity;
            if (!this.getWorld().isClient) {
                double e;
                double d;
                if (this.isInsideWall()) {
                    this.damage(this.getDamageSources().inWall(), 1.0f);
                } else if (bl && !this.getWorld().getWorldBorder().contains(this.getBoundingBox()) && (d = this.getWorld().getWorldBorder().getDistanceInsideBorder(this) + this.getWorld().getWorldBorder().getSafeZone()) < 0.0 && (e = this.getWorld().getWorldBorder().getDamagePerBlock()) > 0.0) {
                    this.damage(this.getDamageSources().outsideBorder(), Math.max(1, MathHelper.floor(-d * e)));
                }
            }
            if (this.isSubmergedIn(FluidTags.WATER) && !this.getWorld().getBlockState(BlockPos.ofFloored(this.getX(), this.getEyeY(), this.getZ())).isOf(Blocks.BUBBLE_COLUMN)) {
                boolean bl2;
                boolean bl3 = bl2 = !this.canBreatheInWater() && !StatusEffectUtil.hasWaterBreathing(this) && (!bl || !((PlayerEntity)this).getAbilities().invulnerable);
                if (bl2) {
                    this.setAir(this.getNextAirUnderwater(this.getAir()));
                    if (this.getAir() == -20) {
                        this.setAir(0);
                        lv2 = this.getVelocity();
                        for (int i = 0; i < 8; ++i) {
                            double f = this.random.nextDouble() - this.random.nextDouble();
                            double g = this.random.nextDouble() - this.random.nextDouble();
                            double h = this.random.nextDouble() - this.random.nextDouble();
                            this.getWorld().addParticle(ParticleTypes.BUBBLE, this.getX() + f, this.getY() + g, this.getZ() + h, ((Vec3d)lv2).x, ((Vec3d)lv2).y, ((Vec3d)lv2).z);
                        }
                        this.damage(this.getDamageSources().drown(), 2.0f);
                    }
                }
                if (!this.getWorld().isClient && this.hasVehicle() && this.getVehicle() != null && this.getVehicle().shouldDismountUnderwater()) {
                    this.stopRiding();
                }
            } else if (this.getAir() < this.getMaxAir()) {
                this.setAir(this.getNextAirOnLand(this.getAir()));
            }
            lv2 = this.getWorld();
            if (lv2 instanceof ServerWorld) {
                ServerWorld lv3 = (ServerWorld)lv2;
                BlockPos lv4 = this.getBlockPos();
                if (!Objects.equal(this.lastBlockPos, lv4)) {
                    this.lastBlockPos = lv4;
                    this.applyMovementEffects(lv3, lv4);
                }
            }
        }
        if (this.isAlive() && (this.isWet() || this.inPowderSnow)) {
            this.extinguishWithSound();
        }
        if (this.hurtTime > 0) {
            --this.hurtTime;
        }
        if (this.timeUntilRegen > 0 && !(this instanceof ServerPlayerEntity)) {
            --this.timeUntilRegen;
        }
        if (this.isDead() && this.getWorld().shouldUpdatePostDeath(this)) {
            this.updatePostDeath();
        }
        if (this.playerHitTimer > 0) {
            --this.playerHitTimer;
        } else {
            this.attackingPlayer = null;
        }
        if (this.attacking != null && !this.attacking.isAlive()) {
            this.attacking = null;
        }
        if (this.attacker != null) {
            if (!this.attacker.isAlive()) {
                this.setAttacker(null);
            } else if (this.age - this.lastAttackedTime > 100) {
                this.setAttacker(null);
            }
        }
        this.tickStatusEffects();
        this.prevLookDirection = this.lookDirection;
        this.prevBodyYaw = this.bodyYaw;
        this.prevHeadYaw = this.headYaw;
        this.prevYaw = this.getYaw();
        this.prevPitch = this.getPitch();
        this.getWorld().getProfiler().pop();
    }

    @Override
    protected float getVelocityMultiplier() {
        return MathHelper.lerp((float)this.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_EFFICIENCY), super.getVelocityMultiplier(), 1.0f);
    }

    protected void removePowderSnowSlow() {
        EntityAttributeInstance lv = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (lv == null) {
            return;
        }
        if (lv.getModifier(POWDER_SNOW_SPEED_MODIFIER_ID) != null) {
            lv.removeModifier(POWDER_SNOW_SPEED_MODIFIER_ID);
        }
    }

    protected void addPowderSnowSlowIfNeeded() {
        int i;
        if (!this.getLandingBlockState().isAir() && (i = this.getFrozenTicks()) > 0) {
            EntityAttributeInstance lv = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
            if (lv == null) {
                return;
            }
            float f = -0.05f * this.getFreezingScale();
            lv.addTemporaryModifier(new EntityAttributeModifier(POWDER_SNOW_SPEED_MODIFIER_ID, f, EntityAttributeModifier.Operation.ADD_VALUE));
        }
    }

    protected void applyMovementEffects(ServerWorld world, BlockPos pos) {
        EnchantmentHelper.applyLocationBasedEffects(world, this);
    }

    public boolean isBaby() {
        return false;
    }

    public float getScaleFactor() {
        return this.isBaby() ? 0.5f : 1.0f;
    }

    public float getScale() {
        AttributeContainer lv = this.getAttributes();
        if (lv == null) {
            return 1.0f;
        }
        return this.clampScale((float)lv.getValue(EntityAttributes.GENERIC_SCALE));
    }

    protected float clampScale(float scale) {
        return scale;
    }

    protected boolean shouldSwimInFluids() {
        return true;
    }

    protected void updatePostDeath() {
        ++this.deathTime;
        if (this.deathTime >= 20 && !this.getWorld().isClient() && !this.isRemoved()) {
            this.getWorld().sendEntityStatus(this, EntityStatuses.ADD_DEATH_PARTICLES);
            this.remove(Entity.RemovalReason.KILLED);
        }
    }

    public boolean shouldDropXp() {
        return !this.isBaby();
    }

    protected boolean shouldDropLoot() {
        return !this.isBaby();
    }

    protected int getNextAirUnderwater(int air) {
        EntityAttributeInstance lv = this.getAttributeInstance(EntityAttributes.GENERIC_OXYGEN_BONUS);
        double d = lv != null ? lv.getValue() : 0.0;
        if (d > 0.0 && this.random.nextDouble() >= 1.0 / (d + 1.0)) {
            return air;
        }
        return air - 1;
    }

    protected int getNextAirOnLand(int air) {
        return Math.min(air + 4, this.getMaxAir());
    }

    public final int getXpToDrop(ServerWorld world, @Nullable Entity attacker) {
        return EnchantmentHelper.getMobExperience(world, attacker, this, this.getXpToDrop());
    }

    protected int getXpToDrop() {
        return 0;
    }

    protected boolean shouldAlwaysDropXp() {
        return false;
    }

    @Nullable
    public LivingEntity getAttacker() {
        return this.attacker;
    }

    @Override
    public LivingEntity getLastAttacker() {
        return this.getAttacker();
    }

    public int getLastAttackedTime() {
        return this.lastAttackedTime;
    }

    public void setAttacking(@Nullable PlayerEntity attacking) {
        this.attackingPlayer = attacking;
        this.playerHitTimer = this.age;
    }

    public void setAttacker(@Nullable LivingEntity attacker) {
        this.attacker = attacker;
        this.lastAttackedTime = this.age;
    }

    @Nullable
    public LivingEntity getAttacking() {
        return this.attacking;
    }

    public int getLastAttackTime() {
        return this.lastAttackTime;
    }

    public void onAttacking(Entity target) {
        this.attacking = target instanceof LivingEntity ? (LivingEntity)target : null;
        this.lastAttackTime = this.age;
    }

    public int getDespawnCounter() {
        return this.despawnCounter;
    }

    public void setDespawnCounter(int despawnCounter) {
        this.despawnCounter = despawnCounter;
    }

    public boolean hasNoDrag() {
        return this.noDrag;
    }

    public void setNoDrag(boolean noDrag) {
        this.noDrag = noDrag;
    }

    protected boolean isArmorSlot(EquipmentSlot slot) {
        return true;
    }

    public void onEquipStack(EquipmentSlot slot, ItemStack oldStack, ItemStack newStack) {
        boolean bl;
        boolean bl2 = bl = newStack.isEmpty() && oldStack.isEmpty();
        if (bl || ItemStack.areItemsAndComponentsEqual(oldStack, newStack) || this.firstUpdate) {
            return;
        }
        Equipment lv = Equipment.fromStack(newStack);
        if (!this.getWorld().isClient() && !this.isSpectator()) {
            if (!this.isSilent() && lv != null && lv.getSlotType() == slot) {
                this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), lv.getEquipSound(), this.getSoundCategory(), 1.0f, 1.0f, this.random.nextLong());
            }
            if (this.isArmorSlot(slot)) {
                this.emitGameEvent(lv != null ? GameEvent.EQUIP : GameEvent.UNEQUIP);
            }
        }
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (reason == Entity.RemovalReason.KILLED || reason == Entity.RemovalReason.DISCARDED) {
            this.onRemoval(reason);
        }
        super.remove(reason);
        this.brain.forgetAll();
    }

    protected void onRemoval(Entity.RemovalReason reason) {
        for (StatusEffectInstance lv : this.getStatusEffects()) {
            lv.onEntityRemoval(this, reason);
        }
        this.activeStatusEffects.clear();
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putFloat("Health", this.getHealth());
        nbt.putShort("HurtTime", (short)this.hurtTime);
        nbt.putInt("HurtByTimestamp", this.lastAttackedTime);
        nbt.putShort("DeathTime", (short)this.deathTime);
        nbt.putFloat("AbsorptionAmount", this.getAbsorptionAmount());
        nbt.put(ATTRIBUTES_NBT_KEY, this.getAttributes().toNbt());
        if (!this.activeStatusEffects.isEmpty()) {
            NbtList lv = new NbtList();
            for (StatusEffectInstance lv2 : this.activeStatusEffects.values()) {
                lv.add(lv2.writeNbt());
            }
            nbt.put(ACTIVE_EFFECTS_NBT_KEY, lv);
        }
        nbt.putBoolean("FallFlying", this.isFallFlying());
        this.getSleepingPosition().ifPresent(pos -> {
            nbt.putInt("SleepingX", pos.getX());
            nbt.putInt("SleepingY", pos.getY());
            nbt.putInt("SleepingZ", pos.getZ());
        });
        DataResult<NbtElement> dataResult = this.brain.encode(NbtOps.INSTANCE);
        dataResult.resultOrPartial(LOGGER::error).ifPresent(brain -> nbt.put("Brain", (NbtElement)brain));
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        this.setAbsorptionAmountUnclamped(nbt.getFloat("AbsorptionAmount"));
        if (nbt.contains(ATTRIBUTES_NBT_KEY, NbtElement.LIST_TYPE) && this.getWorld() != null && !this.getWorld().isClient) {
            this.getAttributes().readNbt(nbt.getList(ATTRIBUTES_NBT_KEY, NbtElement.COMPOUND_TYPE));
        }
        if (nbt.contains(ACTIVE_EFFECTS_NBT_KEY, NbtElement.LIST_TYPE)) {
            NbtList lv = nbt.getList(ACTIVE_EFFECTS_NBT_KEY, NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < lv.size(); ++i) {
                NbtCompound lv2 = lv.getCompound(i);
                StatusEffectInstance lv3 = StatusEffectInstance.fromNbt(lv2);
                if (lv3 == null) continue;
                this.activeStatusEffects.put(lv3.getEffectType(), lv3);
            }
        }
        if (nbt.contains("Health", NbtElement.NUMBER_TYPE)) {
            this.setHealth(nbt.getFloat("Health"));
        }
        this.hurtTime = nbt.getShort("HurtTime");
        this.deathTime = nbt.getShort("DeathTime");
        this.lastAttackedTime = nbt.getInt("HurtByTimestamp");
        if (nbt.contains("Team", NbtElement.STRING_TYPE)) {
            boolean bl;
            String string = nbt.getString("Team");
            Scoreboard lv4 = this.getWorld().getScoreboard();
            Team lv5 = lv4.getTeam(string);
            boolean bl2 = bl = lv5 != null && lv4.addScoreHolderToTeam(this.getUuidAsString(), lv5);
            if (!bl) {
                LOGGER.warn("Unable to add mob to team \"{}\" (that team probably doesn't exist)", (Object)string);
            }
        }
        if (nbt.getBoolean("FallFlying")) {
            this.setFlag(Entity.FALL_FLYING_FLAG_INDEX, true);
        }
        if (nbt.contains("SleepingX", NbtElement.NUMBER_TYPE) && nbt.contains("SleepingY", NbtElement.NUMBER_TYPE) && nbt.contains("SleepingZ", NbtElement.NUMBER_TYPE)) {
            BlockPos lv6 = new BlockPos(nbt.getInt("SleepingX"), nbt.getInt("SleepingY"), nbt.getInt("SleepingZ"));
            this.setSleepingPosition(lv6);
            this.dataTracker.set(POSE, EntityPose.SLEEPING);
            if (!this.firstUpdate) {
                this.setPositionInBed(lv6);
            }
        }
        if (nbt.contains("Brain", NbtElement.COMPOUND_TYPE)) {
            this.brain = this.deserializeBrain(new Dynamic<NbtElement>(NbtOps.INSTANCE, nbt.get("Brain")));
        }
    }

    protected void tickStatusEffects() {
        List<ParticleEffect> list;
        Iterator<RegistryEntry<StatusEffect>> iterator = this.activeStatusEffects.keySet().iterator();
        try {
            while (iterator.hasNext()) {
                RegistryEntry<StatusEffect> lv = iterator.next();
                StatusEffectInstance lv2 = this.activeStatusEffects.get(lv);
                if (!lv2.update(this, () -> this.onStatusEffectUpgraded(lv2, true, null))) {
                    if (this.getWorld().isClient) continue;
                    iterator.remove();
                    this.onStatusEffectRemoved(lv2);
                    continue;
                }
                if (lv2.getDuration() % 600 != 0) continue;
                this.onStatusEffectUpgraded(lv2, false, null);
            }
        } catch (ConcurrentModificationException lv) {
            // empty catch block
        }
        if (this.effectsChanged) {
            if (!this.getWorld().isClient) {
                this.updatePotionVisibility();
                this.updateGlowing();
            }
            this.effectsChanged = false;
        }
        if (!(list = this.dataTracker.get(POTION_SWIRLS)).isEmpty()) {
            int j;
            boolean bl = this.dataTracker.get(POTION_SWIRLS_AMBIENT);
            int i = this.isInvisible() ? 15 : 4;
            int n = j = bl ? 5 : 1;
            if (this.random.nextInt(i * j) == 0) {
                this.getWorld().addParticle(Util.getRandom(list, this.random), this.getParticleX(0.5), this.getRandomBodyY(), this.getParticleZ(0.5), 1.0, 1.0, 1.0);
            }
        }
    }

    protected void updatePotionVisibility() {
        if (this.activeStatusEffects.isEmpty()) {
            this.clearPotionSwirls();
            this.setInvisible(false);
            return;
        }
        this.setInvisible(this.hasStatusEffect(StatusEffects.INVISIBILITY));
        this.updatePotionSwirls();
    }

    private void updatePotionSwirls() {
        List<ParticleEffect> list = this.activeStatusEffects.values().stream().filter(StatusEffectInstance::shouldShowParticles).map(StatusEffectInstance::createParticle).toList();
        this.dataTracker.set(POTION_SWIRLS, list);
        this.dataTracker.set(POTION_SWIRLS_AMBIENT, LivingEntity.containsOnlyAmbientEffects(this.activeStatusEffects.values()));
    }

    private void updateGlowing() {
        boolean bl = this.isGlowing();
        if (this.getFlag(Entity.GLOWING_FLAG_INDEX) != bl) {
            this.setFlag(Entity.GLOWING_FLAG_INDEX, bl);
        }
    }

    public double getAttackDistanceScalingFactor(@Nullable Entity entity) {
        double d = 1.0;
        if (this.isSneaky()) {
            d *= 0.8;
        }
        if (this.isInvisible()) {
            float f = this.getArmorVisibility();
            if (f < 0.1f) {
                f = 0.1f;
            }
            d *= 0.7 * (double)f;
        }
        if (entity != null) {
            ItemStack lv = this.getEquippedStack(EquipmentSlot.HEAD);
            EntityType<?> lv2 = entity.getType();
            if (lv2 == EntityType.SKELETON && lv.isOf(Items.SKELETON_SKULL) || lv2 == EntityType.ZOMBIE && lv.isOf(Items.ZOMBIE_HEAD) || lv2 == EntityType.PIGLIN && lv.isOf(Items.PIGLIN_HEAD) || lv2 == EntityType.PIGLIN_BRUTE && lv.isOf(Items.PIGLIN_HEAD) || lv2 == EntityType.CREEPER && lv.isOf(Items.CREEPER_HEAD)) {
                d *= 0.5;
            }
        }
        return d;
    }

    public boolean canTarget(LivingEntity target) {
        if (target instanceof PlayerEntity && this.getWorld().getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        return target.canTakeDamage();
    }

    public boolean isTarget(LivingEntity entity, TargetPredicate predicate) {
        return predicate.test(this, entity);
    }

    public boolean canTakeDamage() {
        return !this.isInvulnerable() && this.isPartOfGame();
    }

    public boolean isPartOfGame() {
        return !this.isSpectator() && this.isAlive();
    }

    public static boolean containsOnlyAmbientEffects(Collection<StatusEffectInstance> effects) {
        for (StatusEffectInstance lv : effects) {
            if (!lv.shouldShowParticles() || lv.isAmbient()) continue;
            return false;
        }
        return true;
    }

    protected void clearPotionSwirls() {
        this.dataTracker.set(POTION_SWIRLS, List.of());
    }

    public boolean clearStatusEffects() {
        if (this.getWorld().isClient) {
            return false;
        }
        Iterator<StatusEffectInstance> iterator = this.activeStatusEffects.values().iterator();
        boolean bl = false;
        while (iterator.hasNext()) {
            this.onStatusEffectRemoved(iterator.next());
            iterator.remove();
            bl = true;
        }
        return bl;
    }

    public Collection<StatusEffectInstance> getStatusEffects() {
        return this.activeStatusEffects.values();
    }

    public Map<RegistryEntry<StatusEffect>, StatusEffectInstance> getActiveStatusEffects() {
        return this.activeStatusEffects;
    }

    public boolean hasStatusEffect(RegistryEntry<StatusEffect> effect) {
        return this.activeStatusEffects.containsKey(effect);
    }

    @Nullable
    public StatusEffectInstance getStatusEffect(RegistryEntry<StatusEffect> effect) {
        return this.activeStatusEffects.get(effect);
    }

    public final boolean addStatusEffect(StatusEffectInstance effect) {
        return this.addStatusEffect(effect, null);
    }

    public boolean addStatusEffect(StatusEffectInstance effect, @Nullable Entity source) {
        if (!this.canHaveStatusEffect(effect)) {
            return false;
        }
        StatusEffectInstance lv = this.activeStatusEffects.get(effect.getEffectType());
        boolean bl = false;
        if (lv == null) {
            this.activeStatusEffects.put(effect.getEffectType(), effect);
            this.onStatusEffectApplied(effect, source);
            bl = true;
            effect.playApplySound(this);
        } else if (lv.upgrade(effect)) {
            this.onStatusEffectUpgraded(lv, true, source);
            bl = true;
        }
        effect.onApplied(this);
        return bl;
    }

    public boolean canHaveStatusEffect(StatusEffectInstance effect) {
        if (this.getType().isIn(EntityTypeTags.IMMUNE_TO_INFESTED)) {
            return !effect.equals(StatusEffects.INFESTED);
        }
        if (this.getType().isIn(EntityTypeTags.IMMUNE_TO_OOZING)) {
            return !effect.equals(StatusEffects.OOZING);
        }
        if (this.getType().isIn(EntityTypeTags.IGNORES_POISON_AND_REGEN)) {
            return !effect.equals(StatusEffects.REGENERATION) && !effect.equals(StatusEffects.POISON);
        }
        return true;
    }

    public void setStatusEffect(StatusEffectInstance effect, @Nullable Entity source) {
        if (!this.canHaveStatusEffect(effect)) {
            return;
        }
        StatusEffectInstance lv = this.activeStatusEffects.put(effect.getEffectType(), effect);
        if (lv == null) {
            this.onStatusEffectApplied(effect, source);
        } else {
            effect.copyFadingFrom(lv);
            this.onStatusEffectUpgraded(effect, true, source);
        }
    }

    public boolean hasInvertedHealingAndHarm() {
        return this.getType().isIn(EntityTypeTags.INVERTED_HEALING_AND_HARM);
    }

    @Nullable
    public StatusEffectInstance removeStatusEffectInternal(RegistryEntry<StatusEffect> effect) {
        return this.activeStatusEffects.remove(effect);
    }

    public boolean removeStatusEffect(RegistryEntry<StatusEffect> effect) {
        StatusEffectInstance lv = this.removeStatusEffectInternal(effect);
        if (lv != null) {
            this.onStatusEffectRemoved(lv);
            return true;
        }
        return false;
    }

    protected void onStatusEffectApplied(StatusEffectInstance effect, @Nullable Entity source) {
        this.effectsChanged = true;
        if (!this.getWorld().isClient) {
            effect.getEffectType().value().onApplied(this.getAttributes(), effect.getAmplifier());
            this.sendEffectToControllingPlayer(effect);
        }
    }

    public void sendEffectToControllingPlayer(StatusEffectInstance effect) {
        for (Entity lv : this.getPassengerList()) {
            if (!(lv instanceof ServerPlayerEntity)) continue;
            ServerPlayerEntity lv2 = (ServerPlayerEntity)lv;
            lv2.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(this.getId(), effect, false));
        }
    }

    protected void onStatusEffectUpgraded(StatusEffectInstance effect, boolean reapplyEffect, @Nullable Entity source) {
        this.effectsChanged = true;
        if (reapplyEffect && !this.getWorld().isClient) {
            StatusEffect lv = effect.getEffectType().value();
            lv.onRemoved(this.getAttributes());
            lv.onApplied(this.getAttributes(), effect.getAmplifier());
            this.updateAttributes();
        }
        if (!this.getWorld().isClient) {
            this.sendEffectToControllingPlayer(effect);
        }
    }

    protected void onStatusEffectRemoved(StatusEffectInstance effect) {
        this.effectsChanged = true;
        if (!this.getWorld().isClient) {
            effect.getEffectType().value().onRemoved(this.getAttributes());
            this.updateAttributes();
            for (Entity lv : this.getPassengerList()) {
                if (!(lv instanceof ServerPlayerEntity)) continue;
                ServerPlayerEntity lv2 = (ServerPlayerEntity)lv;
                lv2.networkHandler.sendPacket(new RemoveEntityStatusEffectS2CPacket(this.getId(), effect.getEffectType()));
            }
        }
    }

    private void updateAttributes() {
        Set<EntityAttributeInstance> set = this.getAttributes().getPendingUpdate();
        for (EntityAttributeInstance lv : set) {
            this.updateAttribute(lv.getAttribute());
        }
        set.clear();
    }

    private void updateAttribute(RegistryEntry<EntityAttribute> attribute) {
        if (attribute.matches(EntityAttributes.GENERIC_MAX_HEALTH)) {
            float f = this.getMaxHealth();
            if (this.getHealth() > f) {
                this.setHealth(f);
            }
        } else if (attribute.matches(EntityAttributes.GENERIC_MAX_ABSORPTION)) {
            float f = this.getMaxAbsorption();
            if (this.getAbsorptionAmount() > f) {
                this.setAbsorptionAmount(f);
            }
        }
    }

    public void heal(float amount) {
        float g = this.getHealth();
        if (g > 0.0f) {
            this.setHealth(g + amount);
        }
    }

    public float getHealth() {
        return this.dataTracker.get(HEALTH).floatValue();
    }

    public void setHealth(float health) {
        this.dataTracker.set(HEALTH, Float.valueOf(MathHelper.clamp(health, 0.0f, this.getMaxHealth())));
    }

    public boolean isDead() {
        return this.getHealth() <= 0.0f;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        boolean bl3;
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        if (this.getWorld().isClient) {
            return false;
        }
        if (this.isDead()) {
            return false;
        }
        if (source.isIn(DamageTypeTags.IS_FIRE) && this.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) {
            return false;
        }
        if (this.isSleeping() && !this.getWorld().isClient) {
            this.wakeUp();
        }
        this.despawnCounter = 0;
        float g = amount;
        boolean bl = false;
        float h = 0.0f;
        if (amount > 0.0f && this.blockedByShield(source)) {
            Entity lv;
            this.damageShield(amount);
            h = amount;
            amount = 0.0f;
            if (!source.isIn(DamageTypeTags.IS_PROJECTILE) && (lv = source.getSource()) instanceof LivingEntity) {
                LivingEntity lv2 = (LivingEntity)lv;
                this.takeShieldHit(lv2);
            }
            bl = true;
        }
        if (source.isIn(DamageTypeTags.IS_FREEZING) && this.getType().isIn(EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES)) {
            amount *= 5.0f;
        }
        if (source.isIn(DamageTypeTags.DAMAGES_HELMET) && !this.getEquippedStack(EquipmentSlot.HEAD).isEmpty()) {
            this.damageHelmet(source, amount);
            amount *= 0.75f;
        }
        this.limbAnimator.setSpeed(1.5f);
        boolean bl2 = true;
        if ((float)this.timeUntilRegen > 10.0f && !source.isIn(DamageTypeTags.BYPASSES_COOLDOWN)) {
            if (amount <= this.lastDamageTaken) {
                return false;
            }
            this.applyDamage(source, amount - this.lastDamageTaken);
            this.lastDamageTaken = amount;
            bl2 = false;
        } else {
            this.lastDamageTaken = amount;
            this.timeUntilRegen = 20;
            this.applyDamage(source, amount);
            this.hurtTime = this.maxHurtTime = 10;
        }
        Entity lv3 = source.getAttacker();
        if (lv3 != null) {
            WolfEntity lv6;
            if (lv3 instanceof LivingEntity) {
                LivingEntity lv4 = (LivingEntity)lv3;
                if (!(source.isIn(DamageTypeTags.NO_ANGER) || source.isOf(DamageTypes.WIND_CHARGE) && this.getType().isIn(EntityTypeTags.NO_ANGER_FROM_WIND_CHARGE))) {
                    this.setAttacker(lv4);
                }
            }
            if (lv3 instanceof PlayerEntity) {
                PlayerEntity lv5 = (PlayerEntity)lv3;
                this.playerHitTimer = 100;
                this.attackingPlayer = lv5;
            } else if (lv3 instanceof WolfEntity && (lv6 = (WolfEntity)lv3).isTamed()) {
                PlayerEntity lv7;
                this.playerHitTimer = 100;
                LivingEntity livingEntity = lv6.getOwner();
                this.attackingPlayer = livingEntity instanceof PlayerEntity ? (lv7 = (PlayerEntity)livingEntity) : null;
            }
        }
        if (bl2) {
            if (bl) {
                this.getWorld().sendEntityStatus(this, EntityStatuses.BLOCK_WITH_SHIELD);
            } else {
                this.getWorld().sendEntityDamage(this, source);
            }
            if (!(source.isIn(DamageTypeTags.NO_IMPACT) || bl && !(amount > 0.0f))) {
                this.scheduleVelocityUpdate();
            }
            if (!source.isIn(DamageTypeTags.NO_KNOCKBACK)) {
                double d = 0.0;
                double e = 0.0;
                Entity entity = source.getSource();
                if (entity instanceof ProjectileEntity) {
                    ProjectileEntity lv8 = (ProjectileEntity)entity;
                    DoubleDoubleImmutablePair doubleDoubleImmutablePair = lv8.getKnockback(this, source);
                    d = -doubleDoubleImmutablePair.leftDouble();
                    e = -doubleDoubleImmutablePair.rightDouble();
                } else if (source.getPosition() != null) {
                    d = source.getPosition().getX() - this.getX();
                    e = source.getPosition().getZ() - this.getZ();
                }
                this.takeKnockback(0.4f, d, e);
                if (!bl) {
                    this.tiltScreen(d, e);
                }
            }
        }
        if (this.isDead()) {
            if (!this.tryUseTotem(source)) {
                if (bl2) {
                    this.playSound(this.getDeathSound());
                }
                this.onDeath(source);
            }
        } else if (bl2) {
            this.playHurtSound(source);
        }
        boolean bl4 = bl3 = !bl || amount > 0.0f;
        if (bl3) {
            this.lastDamageSource = source;
            this.lastDamageTime = this.getWorld().getTime();
            for (StatusEffectInstance lv9 : this.getStatusEffects()) {
                lv9.onEntityDamage(this, source, amount);
            }
        }
        if (this instanceof ServerPlayerEntity) {
            Criteria.ENTITY_HURT_PLAYER.trigger((ServerPlayerEntity)this, source, g, amount, bl);
            if (h > 0.0f && h < 3.4028235E37f) {
                ((ServerPlayerEntity)this).increaseStat(Stats.DAMAGE_BLOCKED_BY_SHIELD, Math.round(h * 10.0f));
            }
        }
        if (lv3 instanceof ServerPlayerEntity) {
            Criteria.PLAYER_HURT_ENTITY.trigger((ServerPlayerEntity)lv3, this, source, g, amount, bl);
        }
        return bl3;
    }

    protected void takeShieldHit(LivingEntity attacker) {
        attacker.knockback(this);
    }

    protected void knockback(LivingEntity target) {
        target.takeKnockback(0.5, target.getX() - this.getX(), target.getZ() - this.getZ());
    }

    private boolean tryUseTotem(DamageSource source) {
        if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }
        ItemStack lv = null;
        for (Hand lv2 : Hand.values()) {
            ItemStack lv3 = this.getStackInHand(lv2);
            if (!lv3.isOf(Items.TOTEM_OF_UNDYING)) continue;
            lv = lv3.copy();
            lv3.decrement(1);
            break;
        }
        if (lv != null) {
            LivingEntity livingEntity = this;
            if (livingEntity instanceof ServerPlayerEntity) {
                ServerPlayerEntity lv4 = (ServerPlayerEntity)livingEntity;
                lv4.incrementStat(Stats.USED.getOrCreateStat(Items.TOTEM_OF_UNDYING));
                Criteria.USED_TOTEM.trigger(lv4, lv);
                this.emitGameEvent(GameEvent.ITEM_INTERACT_FINISH);
            }
            this.setHealth(1.0f);
            this.clearStatusEffects();
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 900, 1));
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 100, 1));
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 800, 0));
            this.getWorld().sendEntityStatus(this, EntityStatuses.USE_TOTEM_OF_UNDYING);
        }
        return lv != null;
    }

    @Nullable
    public DamageSource getRecentDamageSource() {
        if (this.getWorld().getTime() - this.lastDamageTime > 40L) {
            this.lastDamageSource = null;
        }
        return this.lastDamageSource;
    }

    protected void playHurtSound(DamageSource damageSource) {
        this.playSound(this.getHurtSound(damageSource));
    }

    public void playSound(@Nullable SoundEvent sound) {
        if (sound != null) {
            this.playSound(sound, this.getSoundVolume(), this.getSoundPitch());
        }
    }

    public boolean blockedByShield(DamageSource source) {
        Vec3d lv3;
        PersistentProjectileEntity lv2;
        Entity lv = source.getSource();
        boolean bl = false;
        if (lv instanceof PersistentProjectileEntity && (lv2 = (PersistentProjectileEntity)lv).getPierceLevel() > 0) {
            bl = true;
        }
        if (!source.isIn(DamageTypeTags.BYPASSES_SHIELD) && this.isBlocking() && !bl && (lv3 = source.getPosition()) != null) {
            Vec3d lv4 = this.getRotationVector(0.0f, this.getHeadYaw());
            Vec3d lv5 = lv3.relativize(this.getPos());
            lv5 = new Vec3d(lv5.x, 0.0, lv5.z).normalize();
            return lv5.dotProduct(lv4) < 0.0;
        }
        return false;
    }

    private void playEquipmentBreakEffects(ItemStack stack) {
        if (!stack.isEmpty()) {
            if (!this.isSilent()) {
                this.getWorld().playSound(this.getX(), this.getY(), this.getZ(), stack.getBreakSound(), this.getSoundCategory(), 0.8f, 0.8f + this.getWorld().random.nextFloat() * 0.4f, false);
            }
            this.spawnItemParticles(stack, 5);
        }
    }

    public void onDeath(DamageSource damageSource) {
        if (this.isRemoved() || this.dead) {
            return;
        }
        Entity lv = damageSource.getAttacker();
        LivingEntity lv2 = this.getPrimeAdversary();
        if (this.scoreAmount >= 0 && lv2 != null) {
            lv2.updateKilledAdvancementCriterion(this, this.scoreAmount, damageSource);
        }
        if (this.isSleeping()) {
            this.wakeUp();
        }
        if (!this.getWorld().isClient && this.hasCustomName()) {
            LOGGER.info("Named entity {} died: {}", (Object)this, (Object)this.getDamageTracker().getDeathMessage().getString());
        }
        this.dead = true;
        this.getDamageTracker().update();
        World world = this.getWorld();
        if (world instanceof ServerWorld) {
            ServerWorld lv3 = (ServerWorld)world;
            if (lv == null || lv.onKilledOther(lv3, this)) {
                this.emitGameEvent(GameEvent.ENTITY_DIE);
                this.drop(lv3, damageSource);
                this.onKilledBy(lv2);
            }
            this.getWorld().sendEntityStatus(this, EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES);
        }
        this.setPose(EntityPose.DYING);
    }

    protected void onKilledBy(@Nullable LivingEntity adversary) {
        if (this.getWorld().isClient) {
            return;
        }
        boolean bl = false;
        if (adversary instanceof WitherEntity) {
            if (this.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
                BlockPos lv = this.getBlockPos();
                BlockState lv2 = Blocks.WITHER_ROSE.getDefaultState();
                if (this.getWorld().getBlockState(lv).isAir() && lv2.canPlaceAt(this.getWorld(), lv)) {
                    this.getWorld().setBlockState(lv, lv2, Block.NOTIFY_ALL);
                    bl = true;
                }
            }
            if (!bl) {
                ItemEntity lv3 = new ItemEntity(this.getWorld(), this.getX(), this.getY(), this.getZ(), new ItemStack(Items.WITHER_ROSE));
                this.getWorld().spawnEntity(lv3);
            }
        }
    }

    protected void drop(ServerWorld world, DamageSource damageSource) {
        boolean bl;
        boolean bl2 = bl = this.playerHitTimer > 0;
        if (this.shouldDropLoot() && world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) {
            this.dropLoot(damageSource, bl);
            this.dropEquipment(world, damageSource, bl);
        }
        this.dropInventory();
        this.dropXp(damageSource.getAttacker());
    }

    protected void dropInventory() {
    }

    protected void dropXp(@Nullable Entity attacker) {
        World world = this.getWorld();
        if (world instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)world;
            if (!this.isExperienceDroppingDisabled() && (this.shouldAlwaysDropXp() || this.playerHitTimer > 0 && this.shouldDropXp() && this.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_LOOT))) {
                ExperienceOrbEntity.spawn(lv, this.getPos(), this.getXpToDrop(lv, attacker));
            }
        }
    }

    protected void dropEquipment(ServerWorld world, DamageSource source, boolean causedByPlayer) {
    }

    public RegistryKey<LootTable> getLootTable() {
        return this.getType().getLootTableId();
    }

    public long getLootTableSeed() {
        return 0L;
    }

    protected float getKnockbackAgainst(Entity target, DamageSource damageSource) {
        float f = (float)this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_KNOCKBACK);
        World world = this.getWorld();
        if (world instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)world;
            return EnchantmentHelper.modifyKnockback(lv, this.getWeaponStack(), target, damageSource, f);
        }
        return f;
    }

    protected void dropLoot(DamageSource damageSource, boolean causedByPlayer) {
        RegistryKey<LootTable> lv = this.getLootTable();
        LootTable lv2 = this.getWorld().getServer().getReloadableRegistries().getLootTable(lv);
        LootContextParameterSet.Builder lv3 = new LootContextParameterSet.Builder((ServerWorld)this.getWorld()).add(LootContextParameters.THIS_ENTITY, this).add(LootContextParameters.ORIGIN, this.getPos()).add(LootContextParameters.DAMAGE_SOURCE, damageSource).addOptional(LootContextParameters.ATTACKING_ENTITY, damageSource.getAttacker()).addOptional(LootContextParameters.DIRECT_ATTACKING_ENTITY, damageSource.getSource());
        if (causedByPlayer && this.attackingPlayer != null) {
            lv3 = lv3.add(LootContextParameters.LAST_DAMAGE_PLAYER, this.attackingPlayer).luck(this.attackingPlayer.getLuck());
        }
        LootContextParameterSet lv4 = lv3.build(LootContextTypes.ENTITY);
        lv2.generateLoot(lv4, this.getLootTableSeed(), this::dropStack);
    }

    public void takeKnockback(double strength, double x, double z) {
        if ((strength *= 1.0 - this.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE)) <= 0.0) {
            return;
        }
        this.velocityDirty = true;
        Vec3d lv = this.getVelocity();
        while (x * x + z * z < (double)1.0E-5f) {
            x = (Math.random() - Math.random()) * 0.01;
            z = (Math.random() - Math.random()) * 0.01;
        }
        Vec3d lv2 = new Vec3d(x, 0.0, z).normalize().multiply(strength);
        this.setVelocity(lv.x / 2.0 - lv2.x, this.isOnGround() ? Math.min(0.4, lv.y / 2.0 + strength) : lv.y, lv.z / 2.0 - lv2.z);
    }

    public void tiltScreen(double deltaX, double deltaZ) {
    }

    @Nullable
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_GENERIC_HURT;
    }

    @Nullable
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_GENERIC_DEATH;
    }

    private SoundEvent getFallSound(int distance) {
        return distance > 4 ? this.getFallSounds().big() : this.getFallSounds().small();
    }

    public void disableExperienceDropping() {
        this.experienceDroppingDisabled = true;
    }

    public boolean isExperienceDroppingDisabled() {
        return this.experienceDroppingDisabled;
    }

    public float getDamageTiltYaw() {
        return 0.0f;
    }

    protected Box getHitbox() {
        Box lv = this.getBoundingBox();
        Entity lv2 = this.getVehicle();
        if (lv2 != null) {
            Vec3d lv3 = lv2.getPassengerRidingPos(this);
            return lv.withMinY(Math.max(lv3.y, lv.minY));
        }
        return lv;
    }

    public Map<Enchantment, Set<EnchantmentLocationBasedEffect>> getLocationBasedEnchantmentEffects() {
        return this.locationBasedEnchantmentEffects;
    }

    public FallSounds getFallSounds() {
        return new FallSounds(SoundEvents.ENTITY_GENERIC_SMALL_FALL, SoundEvents.ENTITY_GENERIC_BIG_FALL);
    }

    protected SoundEvent getDrinkSound(ItemStack stack) {
        return stack.getDrinkSound();
    }

    public SoundEvent getEatSound(ItemStack stack) {
        return stack.getEatSound();
    }

    public Optional<BlockPos> getClimbingPos() {
        return this.climbingPos;
    }

    public boolean isClimbing() {
        if (this.isSpectator()) {
            return false;
        }
        BlockPos lv = this.getBlockPos();
        BlockState lv2 = this.getBlockStateAtPos();
        if (lv2.isIn(BlockTags.CLIMBABLE)) {
            this.climbingPos = Optional.of(lv);
            return true;
        }
        if (lv2.getBlock() instanceof TrapdoorBlock && this.canEnterTrapdoor(lv, lv2)) {
            this.climbingPos = Optional.of(lv);
            return true;
        }
        return false;
    }

    private boolean canEnterTrapdoor(BlockPos pos, BlockState state) {
        if (state.get(TrapdoorBlock.OPEN).booleanValue()) {
            BlockState lv = this.getWorld().getBlockState(pos.down());
            return lv.isOf(Blocks.LADDER) && lv.get(LadderBlock.FACING) == state.get(TrapdoorBlock.FACING);
        }
        return false;
    }

    @Override
    public boolean isAlive() {
        return !this.isRemoved() && this.getHealth() > 0.0f;
    }

    @Override
    public int getSafeFallDistance() {
        return this.getSafeFallDistance(0.0f);
    }

    protected final int getSafeFallDistance(float health) {
        return MathHelper.floor(health + 3.0f);
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        boolean bl = super.handleFallDamage(fallDistance, damageMultiplier, damageSource);
        int i = this.computeFallDamage(fallDistance, damageMultiplier);
        if (i > 0) {
            this.playSound(this.getFallSound(i), 1.0f, 1.0f);
            this.playBlockFallSound();
            this.damage(damageSource, i);
            return true;
        }
        return bl;
    }

    protected int computeFallDamage(float fallDistance, float damageMultiplier) {
        if (this.getType().isIn(EntityTypeTags.FALL_DAMAGE_IMMUNE)) {
            return 0;
        }
        float h = (float)this.getAttributeValue(EntityAttributes.GENERIC_SAFE_FALL_DISTANCE);
        float i = fallDistance - h;
        return MathHelper.ceil((double)(i * damageMultiplier) * this.getAttributeValue(EntityAttributes.GENERIC_FALL_DAMAGE_MULTIPLIER));
    }

    protected void playBlockFallSound() {
        if (this.isSilent()) {
            return;
        }
        int i = MathHelper.floor(this.getX());
        int j = MathHelper.floor(this.getY() - (double)0.2f);
        int k = MathHelper.floor(this.getZ());
        BlockState lv = this.getWorld().getBlockState(new BlockPos(i, j, k));
        if (!lv.isAir()) {
            BlockSoundGroup lv2 = lv.getSoundGroup();
            this.playSound(lv2.getFallSound(), lv2.getVolume() * 0.5f, lv2.getPitch() * 0.75f);
        }
    }

    @Override
    public void animateDamage(float yaw) {
        this.hurtTime = this.maxHurtTime = 10;
    }

    public int getArmor() {
        return MathHelper.floor(this.getAttributeValue(EntityAttributes.GENERIC_ARMOR));
    }

    protected void damageArmor(DamageSource source, float amount) {
    }

    protected void damageHelmet(DamageSource source, float amount) {
    }

    protected void damageShield(float amount) {
    }

    protected void damageEquipment(DamageSource source, float amount, EquipmentSlot ... slots) {
        if (amount <= 0.0f) {
            return;
        }
        int i = (int)Math.max(1.0f, amount / 4.0f);
        for (EquipmentSlot lv : slots) {
            ItemStack lv2 = this.getEquippedStack(lv);
            if (!(lv2.getItem() instanceof ArmorItem) || !lv2.takesDamageFrom(source)) continue;
            lv2.damage(i, this, lv);
        }
    }

    protected float applyArmorToDamage(DamageSource source, float amount) {
        if (!source.isIn(DamageTypeTags.BYPASSES_ARMOR)) {
            this.damageArmor(source, amount);
            amount = DamageUtil.getDamageLeft(this, amount, source, this.getArmor(), (float)this.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS));
        }
        return amount;
    }

    protected float modifyAppliedDamage(DamageSource source, float amount) {
        float l;
        int i;
        int j;
        float g;
        float h;
        float k;
        if (source.isIn(DamageTypeTags.BYPASSES_EFFECTS)) {
            return amount;
        }
        if (this.hasStatusEffect(StatusEffects.RESISTANCE) && !source.isIn(DamageTypeTags.BYPASSES_RESISTANCE) && (k = (h = amount) - (amount = Math.max((g = amount * (float)(j = 25 - (i = (this.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1) * 5))) / 25.0f, 0.0f))) > 0.0f && k < 3.4028235E37f) {
            if (this instanceof ServerPlayerEntity) {
                ((ServerPlayerEntity)this).increaseStat(Stats.DAMAGE_RESISTED, Math.round(k * 10.0f));
            } else if (source.getAttacker() instanceof ServerPlayerEntity) {
                ((ServerPlayerEntity)source.getAttacker()).increaseStat(Stats.DAMAGE_DEALT_RESISTED, Math.round(k * 10.0f));
            }
        }
        if (amount <= 0.0f) {
            return 0.0f;
        }
        if (source.isIn(DamageTypeTags.BYPASSES_ENCHANTMENTS)) {
            return amount;
        }
        World world = this.getWorld();
        if (world instanceof ServerWorld) {
            ServerWorld lv = (ServerWorld)world;
            l = EnchantmentHelper.getProtectionAmount(lv, this, source);
        } else {
            l = 0.0f;
        }
        if (l > 0.0f) {
            amount = DamageUtil.getInflictedDamage(amount, l);
        }
        return amount;
    }

    protected void applyDamage(DamageSource source, float amount) {
        Entity entity;
        if (this.isInvulnerableTo(source)) {
            return;
        }
        amount = this.applyArmorToDamage(source, amount);
        float g = amount = this.modifyAppliedDamage(source, amount);
        amount = Math.max(amount - this.getAbsorptionAmount(), 0.0f);
        this.setAbsorptionAmount(this.getAbsorptionAmount() - (g - amount));
        float h = g - amount;
        if (h > 0.0f && h < 3.4028235E37f && (entity = source.getAttacker()) instanceof ServerPlayerEntity) {
            ServerPlayerEntity lv = (ServerPlayerEntity)entity;
            lv.increaseStat(Stats.DAMAGE_DEALT_ABSORBED, Math.round(h * 10.0f));
        }
        if (amount == 0.0f) {
            return;
        }
        this.getDamageTracker().onDamage(source, amount);
        this.setHealth(this.getHealth() - amount);
        this.setAbsorptionAmount(this.getAbsorptionAmount() - amount);
        this.emitGameEvent(GameEvent.ENTITY_DAMAGE);
    }

    public DamageTracker getDamageTracker() {
        return this.damageTracker;
    }

    @Nullable
    public LivingEntity getPrimeAdversary() {
        if (this.attackingPlayer != null) {
            return this.attackingPlayer;
        }
        if (this.attacker != null) {
            return this.attacker;
        }
        return null;
    }

    public final float getMaxHealth() {
        return (float)this.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH);
    }

    public final float getMaxAbsorption() {
        return (float)this.getAttributeValue(EntityAttributes.GENERIC_MAX_ABSORPTION);
    }

    public final int getStuckArrowCount() {
        return this.dataTracker.get(STUCK_ARROW_COUNT);
    }

    public final void setStuckArrowCount(int stuckArrowCount) {
        this.dataTracker.set(STUCK_ARROW_COUNT, stuckArrowCount);
    }

    public final int getStingerCount() {
        return this.dataTracker.get(STINGER_COUNT);
    }

    public final void setStingerCount(int stingerCount) {
        this.dataTracker.set(STINGER_COUNT, stingerCount);
    }

    private int getHandSwingDuration() {
        if (StatusEffectUtil.hasHaste(this)) {
            return 6 - (1 + StatusEffectUtil.getHasteAmplifier(this));
        }
        if (this.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
            return 6 + (1 + this.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) * 2;
        }
        return 6;
    }

    public void swingHand(Hand hand) {
        this.swingHand(hand, false);
    }

    public void swingHand(Hand hand, boolean fromServerPlayer) {
        if (!this.handSwinging || this.handSwingTicks >= this.getHandSwingDuration() / 2 || this.handSwingTicks < 0) {
            this.handSwingTicks = -1;
            this.handSwinging = true;
            this.preferredHand = hand;
            if (this.getWorld() instanceof ServerWorld) {
                EntityAnimationS2CPacket lv = new EntityAnimationS2CPacket(this, hand == Hand.MAIN_HAND ? EntityAnimationS2CPacket.SWING_MAIN_HAND : EntityAnimationS2CPacket.SWING_OFF_HAND);
                ServerChunkManager lv2 = ((ServerWorld)this.getWorld()).getChunkManager();
                if (fromServerPlayer) {
                    lv2.sendToNearbyPlayers(this, lv);
                } else {
                    lv2.sendToOtherNearbyPlayers(this, lv);
                }
            }
        }
    }

    @Override
    public void onDamaged(DamageSource damageSource) {
        this.limbAnimator.setSpeed(1.5f);
        this.timeUntilRegen = 20;
        this.hurtTime = this.maxHurtTime = 10;
        SoundEvent lv = this.getHurtSound(damageSource);
        if (lv != null) {
            this.playSound(lv, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
        }
        this.damage(this.getDamageSources().generic(), 0.0f);
        this.lastDamageSource = damageSource;
        this.lastDamageTime = this.getWorld().getTime();
    }

    @Override
    public void handleStatus(byte status) {
        switch (status) {
            case 3: {
                SoundEvent lv = this.getDeathSound();
                if (lv != null) {
                    this.playSound(lv, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
                }
                if (this instanceof PlayerEntity) break;
                this.setHealth(0.0f);
                this.onDeath(this.getDamageSources().generic());
                break;
            }
            case 30: {
                this.playSound(SoundEvents.ITEM_SHIELD_BREAK, 0.8f, 0.8f + this.getWorld().random.nextFloat() * 0.4f);
                break;
            }
            case 29: {
                this.playSound(SoundEvents.ITEM_SHIELD_BLOCK, 1.0f, 0.8f + this.getWorld().random.nextFloat() * 0.4f);
                break;
            }
            case 46: {
                int i = 128;
                for (int j = 0; j < 128; ++j) {
                    double d = (double)j / 127.0;
                    float f = (this.random.nextFloat() - 0.5f) * 0.2f;
                    float g = (this.random.nextFloat() - 0.5f) * 0.2f;
                    float h = (this.random.nextFloat() - 0.5f) * 0.2f;
                    double e = MathHelper.lerp(d, this.prevX, this.getX()) + (this.random.nextDouble() - 0.5) * (double)this.getWidth() * 2.0;
                    double k = MathHelper.lerp(d, this.prevY, this.getY()) + this.random.nextDouble() * (double)this.getHeight();
                    double l = MathHelper.lerp(d, this.prevZ, this.getZ()) + (this.random.nextDouble() - 0.5) * (double)this.getWidth() * 2.0;
                    this.getWorld().addParticle(ParticleTypes.PORTAL, e, k, l, f, g, h);
                }
                break;
            }
            case 47: {
                this.playEquipmentBreakEffects(this.getEquippedStack(EquipmentSlot.MAINHAND));
                break;
            }
            case 48: {
                this.playEquipmentBreakEffects(this.getEquippedStack(EquipmentSlot.OFFHAND));
                break;
            }
            case 49: {
                this.playEquipmentBreakEffects(this.getEquippedStack(EquipmentSlot.HEAD));
                break;
            }
            case 50: {
                this.playEquipmentBreakEffects(this.getEquippedStack(EquipmentSlot.CHEST));
                break;
            }
            case 51: {
                this.playEquipmentBreakEffects(this.getEquippedStack(EquipmentSlot.LEGS));
                break;
            }
            case 52: {
                this.playEquipmentBreakEffects(this.getEquippedStack(EquipmentSlot.FEET));
                break;
            }
            case 65: {
                this.playEquipmentBreakEffects(this.getEquippedStack(EquipmentSlot.BODY));
                break;
            }
            case 54: {
                HoneyBlock.addRichParticles(this);
                break;
            }
            case 55: {
                this.swapHandStacks();
                break;
            }
            case 60: {
                this.addDeathParticles();
                break;
            }
            default: {
                super.handleStatus(status);
            }
        }
    }

    private void addDeathParticles() {
        for (int i = 0; i < 20; ++i) {
            double d = this.random.nextGaussian() * 0.02;
            double e = this.random.nextGaussian() * 0.02;
            double f = this.random.nextGaussian() * 0.02;
            this.getWorld().addParticle(ParticleTypes.POOF, this.getParticleX(1.0), this.getRandomBodyY(), this.getParticleZ(1.0), d, e, f);
        }
    }

    private void swapHandStacks() {
        ItemStack lv = this.getEquippedStack(EquipmentSlot.OFFHAND);
        this.equipStack(EquipmentSlot.OFFHAND, this.getEquippedStack(EquipmentSlot.MAINHAND));
        this.equipStack(EquipmentSlot.MAINHAND, lv);
    }

    @Override
    protected void tickInVoid() {
        this.damage(this.getDamageSources().outOfWorld(), 4.0f);
    }

    protected void tickHandSwing() {
        int i = this.getHandSwingDuration();
        if (this.handSwinging) {
            ++this.handSwingTicks;
            if (this.handSwingTicks >= i) {
                this.handSwingTicks = 0;
                this.handSwinging = false;
            }
        } else {
            this.handSwingTicks = 0;
        }
        this.handSwingProgress = (float)this.handSwingTicks / (float)i;
    }

    @Nullable
    public EntityAttributeInstance getAttributeInstance(RegistryEntry<EntityAttribute> attribute) {
        return this.getAttributes().getCustomInstance(attribute);
    }

    public double getAttributeValue(RegistryEntry<EntityAttribute> attribute) {
        return this.getAttributes().getValue(attribute);
    }

    public double getAttributeBaseValue(RegistryEntry<EntityAttribute> attribute) {
        return this.getAttributes().getBaseValue(attribute);
    }

    public AttributeContainer getAttributes() {
        return this.attributes;
    }

    public ItemStack getMainHandStack() {
        return this.getEquippedStack(EquipmentSlot.MAINHAND);
    }

    public ItemStack getOffHandStack() {
        return this.getEquippedStack(EquipmentSlot.OFFHAND);
    }

    @Override
    @NotNull
    public ItemStack getWeaponStack() {
        return this.getMainHandStack();
    }

    public boolean isHolding(Item item) {
        return this.isHolding((ItemStack stack) -> stack.isOf(item));
    }

    public boolean isHolding(Predicate<ItemStack> predicate) {
        return predicate.test(this.getMainHandStack()) || predicate.test(this.getOffHandStack());
    }

    public ItemStack getStackInHand(Hand hand) {
        if (hand == Hand.MAIN_HAND) {
            return this.getEquippedStack(EquipmentSlot.MAINHAND);
        }
        if (hand == Hand.OFF_HAND) {
            return this.getEquippedStack(EquipmentSlot.OFFHAND);
        }
        throw new IllegalArgumentException("Invalid hand " + String.valueOf((Object)hand));
    }

    public void setStackInHand(Hand hand, ItemStack stack) {
        if (hand == Hand.MAIN_HAND) {
            this.equipStack(EquipmentSlot.MAINHAND, stack);
        } else if (hand == Hand.OFF_HAND) {
            this.equipStack(EquipmentSlot.OFFHAND, stack);
        } else {
            throw new IllegalArgumentException("Invalid hand " + String.valueOf((Object)hand));
        }
    }

    public boolean hasStackEquipped(EquipmentSlot slot) {
        return !this.getEquippedStack(slot).isEmpty();
    }

    public boolean canUseSlot(EquipmentSlot slot) {
        return false;
    }

    public abstract Iterable<ItemStack> getArmorItems();

    public abstract ItemStack getEquippedStack(EquipmentSlot var1);

    public abstract void equipStack(EquipmentSlot var1, ItemStack var2);

    public Iterable<ItemStack> getHandItems() {
        return List.of();
    }

    public Iterable<ItemStack> getAllArmorItems() {
        return this.getArmorItems();
    }

    public Iterable<ItemStack> getEquippedItems() {
        return Iterables.concat(this.getHandItems(), this.getAllArmorItems());
    }

    protected void processEquippedStack(ItemStack stack) {
        stack.getItem().postProcessComponents(stack);
    }

    public float getArmorVisibility() {
        Iterable<ItemStack> iterable = this.getArmorItems();
        int i = 0;
        int j = 0;
        for (ItemStack lv : iterable) {
            if (!lv.isEmpty()) {
                ++j;
            }
            ++i;
        }
        return i > 0 ? (float)j / (float)i : 0.0f;
    }

    @Override
    public void setSprinting(boolean sprinting) {
        super.setSprinting(sprinting);
        EntityAttributeInstance lv = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        lv.removeModifier(SPRINTING_SPEED_BOOST.id());
        if (sprinting) {
            lv.addTemporaryModifier(SPRINTING_SPEED_BOOST);
        }
    }

    protected float getSoundVolume() {
        return 1.0f;
    }

    public float getSoundPitch() {
        if (this.isBaby()) {
            return (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.5f;
        }
        return (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f;
    }

    protected boolean isImmobile() {
        return this.isDead();
    }

    @Override
    public void pushAwayFrom(Entity entity) {
        if (!this.isSleeping()) {
            super.pushAwayFrom(entity);
        }
    }

    private void onDismounted(Entity vehicle) {
        Vec3d lv;
        if (this.isRemoved()) {
            lv = this.getPos();
        } else if (vehicle.isRemoved() || this.getWorld().getBlockState(vehicle.getBlockPos()).isIn(BlockTags.PORTALS)) {
            double d = Math.max(this.getY(), vehicle.getY());
            lv = new Vec3d(this.getX(), d, this.getZ());
        } else {
            lv = vehicle.updatePassengerForDismount(this);
        }
        this.requestTeleportAndDismount(lv.x, lv.y, lv.z);
    }

    @Override
    public boolean shouldRenderName() {
        return this.isCustomNameVisible();
    }

    protected float getJumpVelocity() {
        return this.getJumpVelocity(1.0f);
    }

    protected float getJumpVelocity(float strength) {
        return (float)this.getAttributeValue(EntityAttributes.GENERIC_JUMP_STRENGTH) * strength * this.getJumpVelocityMultiplier() + this.getJumpBoostVelocityModifier();
    }

    public float getJumpBoostVelocityModifier() {
        return this.hasStatusEffect(StatusEffects.JUMP_BOOST) ? 0.1f * ((float)this.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier() + 1.0f) : 0.0f;
    }

    @VisibleForTesting
    public void jump() {
        float f = this.getJumpVelocity();
        if (f <= 1.0E-5f) {
            return;
        }
        Vec3d lv = this.getVelocity();
        this.setVelocity(lv.x, f, lv.z);
        if (this.isSprinting()) {
            float g = this.getYaw() * ((float)Math.PI / 180);
            this.addVelocityInternal(new Vec3d((double)(-MathHelper.sin(g)) * 0.2, 0.0, (double)MathHelper.cos(g) * 0.2));
        }
        this.velocityDirty = true;
    }

    protected void knockDownwards() {
        this.setVelocity(this.getVelocity().add(0.0, -0.04f, 0.0));
    }

    protected void swimUpward(TagKey<Fluid> fluid) {
        this.setVelocity(this.getVelocity().add(0.0, 0.04f, 0.0));
    }

    protected float getBaseMovementSpeedMultiplier() {
        return 0.8f;
    }

    public boolean canWalkOnFluid(FluidState state) {
        return false;
    }

    @Override
    protected double getGravity() {
        return this.getAttributeValue(EntityAttributes.GENERIC_GRAVITY);
    }

    public void travel(Vec3d movementInput) {
        if (this.isLogicalSideForUpdatingMovement()) {
            boolean bl;
            double d = this.getFinalGravity();
            boolean bl2 = bl = this.getVelocity().y <= 0.0;
            if (bl && this.hasStatusEffect(StatusEffects.SLOW_FALLING)) {
                d = Math.min(d, 0.01);
            }
            FluidState lv = this.getWorld().getFluidState(this.getBlockPos());
            if (this.isTouchingWater() && this.shouldSwimInFluids() && !this.canWalkOnFluid(lv)) {
                double e = this.getY();
                float f = this.isSprinting() ? 0.9f : this.getBaseMovementSpeedMultiplier();
                float g = 0.02f;
                float h = (float)this.getAttributeValue(EntityAttributes.GENERIC_WATER_MOVEMENT_EFFICIENCY);
                if (!this.isOnGround()) {
                    h *= 0.5f;
                }
                if (h > 0.0f) {
                    f += (0.54600006f - f) * h;
                    g += (this.getMovementSpeed() - g) * h;
                }
                if (this.hasStatusEffect(StatusEffects.DOLPHINS_GRACE)) {
                    f = 0.96f;
                }
                this.updateVelocity(g, movementInput);
                this.move(MovementType.SELF, this.getVelocity());
                Vec3d lv2 = this.getVelocity();
                if (this.horizontalCollision && this.isClimbing()) {
                    lv2 = new Vec3d(lv2.x, 0.2, lv2.z);
                }
                this.setVelocity(lv2.multiply(f, 0.8f, f));
                Vec3d lv3 = this.applyFluidMovingSpeed(d, bl, this.getVelocity());
                this.setVelocity(lv3);
                if (this.horizontalCollision && this.doesNotCollide(lv3.x, lv3.y + (double)0.6f - this.getY() + e, lv3.z)) {
                    this.setVelocity(lv3.x, 0.3f, lv3.z);
                }
            } else if (this.isInLava() && this.shouldSwimInFluids() && !this.canWalkOnFluid(lv)) {
                Vec3d lv4;
                double e = this.getY();
                this.updateVelocity(0.02f, movementInput);
                this.move(MovementType.SELF, this.getVelocity());
                if (this.getFluidHeight(FluidTags.LAVA) <= this.getSwimHeight()) {
                    this.setVelocity(this.getVelocity().multiply(0.5, 0.8f, 0.5));
                    lv4 = this.applyFluidMovingSpeed(d, bl, this.getVelocity());
                    this.setVelocity(lv4);
                } else {
                    this.setVelocity(this.getVelocity().multiply(0.5));
                }
                if (d != 0.0) {
                    this.setVelocity(this.getVelocity().add(0.0, -d / 4.0, 0.0));
                }
                lv4 = this.getVelocity();
                if (this.horizontalCollision && this.doesNotCollide(lv4.x, lv4.y + (double)0.6f - this.getY() + e, lv4.z)) {
                    this.setVelocity(lv4.x, 0.3f, lv4.z);
                }
            } else if (this.isFallFlying()) {
                double n;
                float o;
                double m;
                this.limitFallDistance();
                Vec3d lv5 = this.getVelocity();
                Vec3d lv6 = this.getRotationVector();
                float f = this.getPitch() * ((float)Math.PI / 180);
                double i = Math.sqrt(lv6.x * lv6.x + lv6.z * lv6.z);
                double j = lv5.horizontalLength();
                double k = lv6.length();
                double l = Math.cos(f);
                l = l * l * Math.min(1.0, k / 0.4);
                lv5 = this.getVelocity().add(0.0, d * (-1.0 + l * 0.75), 0.0);
                if (lv5.y < 0.0 && i > 0.0) {
                    m = lv5.y * -0.1 * l;
                    lv5 = lv5.add(lv6.x * m / i, m, lv6.z * m / i);
                }
                if (f < 0.0f && i > 0.0) {
                    m = j * (double)(-MathHelper.sin(f)) * 0.04;
                    lv5 = lv5.add(-lv6.x * m / i, m * 3.2, -lv6.z * m / i);
                }
                if (i > 0.0) {
                    lv5 = lv5.add((lv6.x / i * j - lv5.x) * 0.1, 0.0, (lv6.z / i * j - lv5.z) * 0.1);
                }
                this.setVelocity(lv5.multiply(0.99f, 0.98f, 0.99f));
                this.move(MovementType.SELF, this.getVelocity());
                if (this.horizontalCollision && !this.getWorld().isClient && (o = (float)((n = j - (m = this.getVelocity().horizontalLength())) * 10.0 - 3.0)) > 0.0f) {
                    this.playSound(this.getFallSound((int)o), 1.0f, 1.0f);
                    this.damage(this.getDamageSources().flyIntoWall(), o);
                }
                if (this.isOnGround() && !this.getWorld().isClient) {
                    this.setFlag(Entity.FALL_FLYING_FLAG_INDEX, false);
                }
            } else {
                BlockPos lv7 = this.getVelocityAffectingPos();
                float p = this.getWorld().getBlockState(lv7).getBlock().getSlipperiness();
                float f = this.isOnGround() ? p * 0.91f : 0.91f;
                Vec3d lv8 = this.applyMovementInput(movementInput, p);
                double q = lv8.y;
                q = this.hasStatusEffect(StatusEffects.LEVITATION) ? (q += (0.05 * (double)(this.getStatusEffect(StatusEffects.LEVITATION).getAmplifier() + 1) - lv8.y) * 0.2) : (!this.getWorld().isClient || this.getWorld().isChunkLoaded(lv7) ? (q -= d) : (this.getY() > (double)this.getWorld().getBottomY() ? -0.1 : 0.0));
                if (this.hasNoDrag()) {
                    this.setVelocity(lv8.x, q, lv8.z);
                } else {
                    this.setVelocity(lv8.x * (double)f, this instanceof Flutterer ? q * (double)f : q * (double)0.98f, lv8.z * (double)f);
                }
            }
        }
        this.updateLimbs(this instanceof Flutterer);
    }

    private void travelControlled(PlayerEntity controllingPlayer, Vec3d movementInput) {
        Vec3d lv = this.getControlledMovementInput(controllingPlayer, movementInput);
        this.tickControlled(controllingPlayer, lv);
        if (this.isLogicalSideForUpdatingMovement()) {
            this.setMovementSpeed(this.getSaddledSpeed(controllingPlayer));
            this.travel(lv);
        } else {
            this.updateLimbs(false);
            this.setVelocity(Vec3d.ZERO);
            this.tryCheckBlockCollision();
        }
    }

    protected void tickControlled(PlayerEntity controllingPlayer, Vec3d movementInput) {
    }

    protected Vec3d getControlledMovementInput(PlayerEntity controllingPlayer, Vec3d movementInput) {
        return movementInput;
    }

    protected float getSaddledSpeed(PlayerEntity controllingPlayer) {
        return this.getMovementSpeed();
    }

    public void updateLimbs(boolean flutter) {
        float f = (float)MathHelper.magnitude(this.getX() - this.prevX, flutter ? this.getY() - this.prevY : 0.0, this.getZ() - this.prevZ);
        this.updateLimbs(f);
    }

    protected void updateLimbs(float posDelta) {
        float g = Math.min(posDelta * 4.0f, 1.0f);
        this.limbAnimator.updateLimbs(g, 0.4f);
    }

    public Vec3d applyMovementInput(Vec3d movementInput, float slipperiness) {
        this.updateVelocity(this.getMovementSpeed(slipperiness), movementInput);
        this.setVelocity(this.applyClimbingSpeed(this.getVelocity()));
        this.move(MovementType.SELF, this.getVelocity());
        Vec3d lv = this.getVelocity();
        if ((this.horizontalCollision || this.jumping) && (this.isClimbing() || this.getBlockStateAtPos().isOf(Blocks.POWDER_SNOW) && PowderSnowBlock.canWalkOnPowderSnow(this))) {
            lv = new Vec3d(lv.x, 0.2, lv.z);
        }
        return lv;
    }

    public Vec3d applyFluidMovingSpeed(double gravity, boolean falling, Vec3d motion) {
        if (gravity != 0.0 && !this.isSprinting()) {
            double e = falling && Math.abs(motion.y - 0.005) >= 0.003 && Math.abs(motion.y - gravity / 16.0) < 0.003 ? -0.003 : motion.y - gravity / 16.0;
            return new Vec3d(motion.x, e, motion.z);
        }
        return motion;
    }

    private Vec3d applyClimbingSpeed(Vec3d motion) {
        if (this.isClimbing()) {
            this.onLanding();
            float f = 0.15f;
            double d = MathHelper.clamp(motion.x, (double)-0.15f, (double)0.15f);
            double e = MathHelper.clamp(motion.z, (double)-0.15f, (double)0.15f);
            double g = Math.max(motion.y, (double)-0.15f);
            if (g < 0.0 && !this.getBlockStateAtPos().isOf(Blocks.SCAFFOLDING) && this.isHoldingOntoLadder() && this instanceof PlayerEntity) {
                g = 0.0;
            }
            motion = new Vec3d(d, g, e);
        }
        return motion;
    }

    private float getMovementSpeed(float slipperiness) {
        if (this.isOnGround()) {
            return this.getMovementSpeed() * (0.21600002f / (slipperiness * slipperiness * slipperiness));
        }
        return this.getOffGroundSpeed();
    }

    protected float getOffGroundSpeed() {
        return this.getControllingPassenger() instanceof PlayerEntity ? this.getMovementSpeed() * 0.1f : 0.02f;
    }

    public float getMovementSpeed() {
        return this.movementSpeed;
    }

    public void setMovementSpeed(float movementSpeed) {
        this.movementSpeed = movementSpeed;
    }

    public boolean tryAttack(Entity target) {
        this.onAttacking(target);
        return false;
    }

    @Override
    public void tick() {
        float l;
        super.tick();
        this.tickActiveItemStack();
        this.updateLeaningPitch();
        if (!this.getWorld().isClient) {
            int j;
            int i = this.getStuckArrowCount();
            if (i > 0) {
                if (this.stuckArrowTimer <= 0) {
                    this.stuckArrowTimer = 20 * (30 - i);
                }
                --this.stuckArrowTimer;
                if (this.stuckArrowTimer <= 0) {
                    this.setStuckArrowCount(i - 1);
                }
            }
            if ((j = this.getStingerCount()) > 0) {
                if (this.stuckStingerTimer <= 0) {
                    this.stuckStingerTimer = 20 * (30 - j);
                }
                --this.stuckStingerTimer;
                if (this.stuckStingerTimer <= 0) {
                    this.setStingerCount(j - 1);
                }
            }
            this.sendEquipmentChanges();
            if (this.age % 20 == 0) {
                this.getDamageTracker().update();
            }
            if (this.isSleeping() && !this.isSleepingInBed()) {
                this.wakeUp();
            }
        }
        if (!this.isRemoved()) {
            this.tickMovement();
        }
        double d = this.getX() - this.prevX;
        double e = this.getZ() - this.prevZ;
        float f = (float)(d * d + e * e);
        float g = this.bodyYaw;
        float h = 0.0f;
        this.prevStepBobbingAmount = this.stepBobbingAmount;
        float k = 0.0f;
        if (f > 0.0025000002f) {
            k = 1.0f;
            h = (float)Math.sqrt(f) * 3.0f;
            l = (float)MathHelper.atan2(e, d) * 57.295776f - 90.0f;
            float m = MathHelper.abs(MathHelper.wrapDegrees(this.getYaw()) - l);
            g = 95.0f < m && m < 265.0f ? l - 180.0f : l;
        }
        if (this.handSwingProgress > 0.0f) {
            g = this.getYaw();
        }
        if (!this.isOnGround()) {
            k = 0.0f;
        }
        this.stepBobbingAmount += (k - this.stepBobbingAmount) * 0.3f;
        this.getWorld().getProfiler().push("headTurn");
        h = this.turnHead(g, h);
        this.getWorld().getProfiler().pop();
        this.getWorld().getProfiler().push("rangeChecks");
        while (this.getYaw() - this.prevYaw < -180.0f) {
            this.prevYaw -= 360.0f;
        }
        while (this.getYaw() - this.prevYaw >= 180.0f) {
            this.prevYaw += 360.0f;
        }
        while (this.bodyYaw - this.prevBodyYaw < -180.0f) {
            this.prevBodyYaw -= 360.0f;
        }
        while (this.bodyYaw - this.prevBodyYaw >= 180.0f) {
            this.prevBodyYaw += 360.0f;
        }
        while (this.getPitch() - this.prevPitch < -180.0f) {
            this.prevPitch -= 360.0f;
        }
        while (this.getPitch() - this.prevPitch >= 180.0f) {
            this.prevPitch += 360.0f;
        }
        while (this.headYaw - this.prevHeadYaw < -180.0f) {
            this.prevHeadYaw -= 360.0f;
        }
        while (this.headYaw - this.prevHeadYaw >= 180.0f) {
            this.prevHeadYaw += 360.0f;
        }
        this.getWorld().getProfiler().pop();
        this.lookDirection += h;
        this.fallFlyingTicks = this.isFallFlying() ? ++this.fallFlyingTicks : 0;
        if (this.isSleeping()) {
            this.setPitch(0.0f);
        }
        this.updateAttributes();
        l = this.getScale();
        if (l != this.prevScale) {
            this.prevScale = l;
            this.calculateDimensions();
        }
    }

    private void sendEquipmentChanges() {
        Map<EquipmentSlot, ItemStack> map = this.getEquipmentChanges();
        if (map != null) {
            this.checkHandStackSwap(map);
            if (!map.isEmpty()) {
                this.sendEquipmentChanges(map);
            }
        }
    }

    @Nullable
    private Map<EquipmentSlot, ItemStack> getEquipmentChanges() {
        EnumMap<EquipmentSlot, ItemStack> map = null;
        for (EquipmentSlot lv : EquipmentSlot.values()) {
            ItemStack lv2 = switch (lv.getType()) {
                default -> throw new MatchException(null, null);
                case EquipmentSlot.Type.HAND -> this.getSyncedHandStack(lv);
                case EquipmentSlot.Type.HUMANOID_ARMOR -> this.getSyncedArmorStack(lv);
                case EquipmentSlot.Type.ANIMAL_ARMOR -> this.syncedBodyArmorStack;
            };
            ItemStack lv3 = this.getEquippedStack(lv);
            if (!this.areItemsDifferent(lv2, lv3)) continue;
            if (map == null) {
                map = Maps.newEnumMap(EquipmentSlot.class);
            }
            map.put(lv, lv3);
            AttributeContainer lv4 = this.getAttributes();
            if (lv2.isEmpty()) continue;
            lv2.applyAttributeModifiers(lv, (attribute, modifier) -> {
                EntityAttributeInstance lv = lv4.getCustomInstance((RegistryEntry<EntityAttribute>)attribute);
                if (lv != null) {
                    lv.removeModifier((EntityAttributeModifier)modifier);
                }
                EnchantmentHelper.removeLocationBasedEffects(lv2, this, lv);
            });
        }
        if (map != null) {
            for (Map.Entry entry : map.entrySet()) {
                EquipmentSlot lv5 = (EquipmentSlot)entry.getKey();
                ItemStack lv6 = (ItemStack)entry.getValue();
                if (lv6.isEmpty()) continue;
                lv6.applyAttributeModifiers(lv5, (arg3, arg4) -> {
                    World lv2;
                    EntityAttributeInstance lv = this.attributes.getCustomInstance((RegistryEntry<EntityAttribute>)arg3);
                    if (lv != null) {
                        lv.removeModifier(arg4.id());
                        lv.addTemporaryModifier((EntityAttributeModifier)arg4);
                    }
                    if ((lv2 = this.getWorld()) instanceof ServerWorld) {
                        ServerWorld lv3 = (ServerWorld)lv2;
                        EnchantmentHelper.applyLocationBasedEffects(lv3, lv6, this, lv5);
                    }
                });
            }
        }
        return map;
    }

    public boolean areItemsDifferent(ItemStack stack, ItemStack stack2) {
        return !ItemStack.areEqual(stack2, stack);
    }

    private void checkHandStackSwap(Map<EquipmentSlot, ItemStack> equipmentChanges) {
        ItemStack lv = equipmentChanges.get(EquipmentSlot.MAINHAND);
        ItemStack lv2 = equipmentChanges.get(EquipmentSlot.OFFHAND);
        if (lv != null && lv2 != null && ItemStack.areEqual(lv, this.getSyncedHandStack(EquipmentSlot.OFFHAND)) && ItemStack.areEqual(lv2, this.getSyncedHandStack(EquipmentSlot.MAINHAND))) {
            ((ServerWorld)this.getWorld()).getChunkManager().sendToOtherNearbyPlayers(this, new EntityStatusS2CPacket(this, EntityStatuses.SWAP_HANDS));
            equipmentChanges.remove(EquipmentSlot.MAINHAND);
            equipmentChanges.remove(EquipmentSlot.OFFHAND);
            this.setSyncedHandStack(EquipmentSlot.MAINHAND, lv.copy());
            this.setSyncedHandStack(EquipmentSlot.OFFHAND, lv2.copy());
        }
    }

    private void sendEquipmentChanges(Map<EquipmentSlot, ItemStack> equipmentChanges) {
        ArrayList<Pair<EquipmentSlot, ItemStack>> list = Lists.newArrayListWithCapacity(equipmentChanges.size());
        equipmentChanges.forEach((slot, stack) -> {
            ItemStack lv = stack.copy();
            list.add(Pair.of(slot, lv));
            switch (slot.getType()) {
                case HAND: {
                    this.setSyncedHandStack((EquipmentSlot)slot, lv);
                    break;
                }
                case HUMANOID_ARMOR: {
                    this.setSyncedArmorStack((EquipmentSlot)slot, lv);
                    break;
                }
                case ANIMAL_ARMOR: {
                    this.syncedBodyArmorStack = lv;
                }
            }
        });
        ((ServerWorld)this.getWorld()).getChunkManager().sendToOtherNearbyPlayers(this, new EntityEquipmentUpdateS2CPacket(this.getId(), list));
    }

    private ItemStack getSyncedArmorStack(EquipmentSlot slot) {
        return this.syncedArmorStacks.get(slot.getEntitySlotId());
    }

    private void setSyncedArmorStack(EquipmentSlot slot, ItemStack armor) {
        this.syncedArmorStacks.set(slot.getEntitySlotId(), armor);
    }

    private ItemStack getSyncedHandStack(EquipmentSlot slot) {
        return this.syncedHandStacks.get(slot.getEntitySlotId());
    }

    private void setSyncedHandStack(EquipmentSlot slot, ItemStack stack) {
        this.syncedHandStacks.set(slot.getEntitySlotId(), stack);
    }

    protected float turnHead(float bodyRotation, float headRotation) {
        boolean bl;
        float h = MathHelper.wrapDegrees(bodyRotation - this.bodyYaw);
        this.bodyYaw += h * 0.3f;
        float i = MathHelper.wrapDegrees(this.getYaw() - this.bodyYaw);
        float j = this.getMaxRelativeHeadRotation();
        if (Math.abs(i) > j) {
            this.bodyYaw += i - (float)MathHelper.sign(i) * j;
        }
        boolean bl2 = bl = i < -90.0f || i >= 90.0f;
        if (bl) {
            headRotation *= -1.0f;
        }
        return headRotation;
    }

    protected float getMaxRelativeHeadRotation() {
        return 50.0f;
    }

    /*
     * Unable to fully structure code
     */
    public void tickMovement() {
        if (this.jumpingCooldown > 0) {
            --this.jumpingCooldown;
        }
        if (this.isLogicalSideForUpdatingMovement()) {
            this.bodyTrackingIncrements = 0;
            this.updateTrackedPosition(this.getX(), this.getY(), this.getZ());
        }
        if (this.bodyTrackingIncrements > 0) {
            this.lerpPosAndRotation(this.bodyTrackingIncrements, this.serverX, this.serverY, this.serverZ, this.serverYaw, this.serverPitch);
            --this.bodyTrackingIncrements;
        } else if (!this.canMoveVoluntarily()) {
            this.setVelocity(this.getVelocity().multiply(0.98));
        }
        if (this.headTrackingIncrements > 0) {
            this.lerpHeadYaw(this.headTrackingIncrements, this.serverHeadYaw);
            --this.headTrackingIncrements;
        }
        lv = this.getVelocity();
        d = lv.x;
        e = lv.y;
        f = lv.z;
        if (Math.abs(lv.x) < 0.003) {
            d = 0.0;
        }
        if (Math.abs(lv.y) < 0.003) {
            e = 0.0;
        }
        if (Math.abs(lv.z) < 0.003) {
            f = 0.0;
        }
        this.setVelocity(d, e, f);
        this.getWorld().getProfiler().push("ai");
        if (this.isImmobile()) {
            this.jumping = false;
            this.sidewaysSpeed = 0.0f;
            this.forwardSpeed = 0.0f;
        } else if (this.canMoveVoluntarily()) {
            this.getWorld().getProfiler().push("newAi");
            this.tickNewAi();
            this.getWorld().getProfiler().pop();
        }
        this.getWorld().getProfiler().pop();
        this.getWorld().getProfiler().push("jump");
        if (this.jumping && this.shouldSwimInFluids()) {
            g = this.isInLava() != false ? this.getFluidHeight(FluidTags.LAVA) : this.getFluidHeight(FluidTags.WATER);
            bl = this.isTouchingWater() != false && g > 0.0;
            h = this.getSwimHeight();
            if (bl && (!this.isOnGround() || g > h)) {
                this.swimUpward(FluidTags.WATER);
            } else if (this.isInLava() && (!this.isOnGround() || g > h)) {
                this.swimUpward(FluidTags.LAVA);
            } else if ((this.isOnGround() || bl && g <= h) && this.jumpingCooldown == 0) {
                this.jump();
                this.jumpingCooldown = 10;
            }
        } else {
            this.jumpingCooldown = 0;
        }
        this.getWorld().getProfiler().pop();
        this.getWorld().getProfiler().push("travel");
        this.sidewaysSpeed *= 0.98f;
        this.forwardSpeed *= 0.98f;
        this.tickFallFlying();
        lv2 = this.getBoundingBox();
        lv3 = new Vec3d(this.sidewaysSpeed, this.upwardSpeed, this.forwardSpeed);
        if (this.hasStatusEffect(StatusEffects.SLOW_FALLING) || this.hasStatusEffect(StatusEffects.LEVITATION)) {
            this.onLanding();
        }
        if (!((var11_11 = this.getControllingPassenger()) instanceof PlayerEntity)) ** GOTO lbl-1000
        lv4 = (PlayerEntity)var11_11;
        if (this.isAlive()) {
            this.travelControlled(lv4, lv3);
        } else lbl-1000:
        // 2 sources

        {
            this.travel(lv3);
        }
        this.getWorld().getProfiler().pop();
        this.getWorld().getProfiler().push("freezing");
        if (!this.getWorld().isClient && !this.isDead()) {
            i = this.getFrozenTicks();
            if (this.inPowderSnow && this.canFreeze()) {
                this.setFrozenTicks(Math.min(this.getMinFreezeDamageTicks(), i + 1));
            } else {
                this.setFrozenTicks(Math.max(0, i - 2));
            }
        }
        this.removePowderSnowSlow();
        this.addPowderSnowSlowIfNeeded();
        if (!this.getWorld().isClient && this.age % 40 == 0 && this.isFrozen() && this.canFreeze()) {
            this.damage(this.getDamageSources().freeze(), 1.0f);
        }
        this.getWorld().getProfiler().pop();
        this.getWorld().getProfiler().push("push");
        if (this.riptideTicks > 0) {
            --this.riptideTicks;
            this.tickRiptide(lv2, this.getBoundingBox());
        }
        this.tickCramming();
        this.getWorld().getProfiler().pop();
        if (!this.getWorld().isClient && this.hurtByWater() && this.isWet()) {
            this.damage(this.getDamageSources().drown(), 1.0f);
        }
    }

    public boolean hurtByWater() {
        return false;
    }

    private void tickFallFlying() {
        boolean bl = this.getFlag(Entity.FALL_FLYING_FLAG_INDEX);
        if (bl && !this.isOnGround() && !this.hasVehicle() && !this.hasStatusEffect(StatusEffects.LEVITATION)) {
            ItemStack lv = this.getEquippedStack(EquipmentSlot.CHEST);
            if (lv.isOf(Items.ELYTRA) && ElytraItem.isUsable(lv)) {
                bl = true;
                int i = this.fallFlyingTicks + 1;
                if (!this.getWorld().isClient && i % 10 == 0) {
                    int j = i / 10;
                    if (j % 2 == 0) {
                        lv.damage(1, this, EquipmentSlot.CHEST);
                    }
                    this.emitGameEvent(GameEvent.ELYTRA_GLIDE);
                }
            } else {
                bl = false;
            }
        } else {
            bl = false;
        }
        if (!this.getWorld().isClient) {
            this.setFlag(Entity.FALL_FLYING_FLAG_INDEX, bl);
        }
    }

    protected void tickNewAi() {
    }

    protected void tickCramming() {
        if (this.getWorld().isClient()) {
            this.getWorld().getEntitiesByType(TypeFilter.instanceOf(PlayerEntity.class), this.getBoundingBox(), EntityPredicates.canBePushedBy(this)).forEach(this::pushAway);
            return;
        }
        List<Entity> list = this.getWorld().getOtherEntities(this, this.getBoundingBox(), EntityPredicates.canBePushedBy(this));
        if (!list.isEmpty()) {
            int i = this.getWorld().getGameRules().getInt(GameRules.MAX_ENTITY_CRAMMING);
            if (i > 0 && list.size() > i - 1 && this.random.nextInt(4) == 0) {
                int j = 0;
                for (Entity lv : list) {
                    if (lv.hasVehicle()) continue;
                    ++j;
                }
                if (j > i - 1) {
                    this.damage(this.getDamageSources().cramming(), 6.0f);
                }
            }
            for (Entity lv2 : list) {
                this.pushAway(lv2);
            }
        }
    }

    protected void tickRiptide(Box a, Box b) {
        Box lv = a.union(b);
        List<Entity> list = this.getWorld().getOtherEntities(this, lv);
        if (!list.isEmpty()) {
            for (Entity lv2 : list) {
                if (!(lv2 instanceof LivingEntity)) continue;
                this.attackLivingEntity((LivingEntity)lv2);
                this.riptideTicks = 0;
                this.setVelocity(this.getVelocity().multiply(-0.2));
                break;
            }
        } else if (this.horizontalCollision) {
            this.riptideTicks = 0;
        }
        if (!this.getWorld().isClient && this.riptideTicks <= 0) {
            this.setLivingFlag(USING_RIPTIDE_FLAG, false);
            this.riptideAttackDamage = 0.0f;
            this.riptideStack = null;
        }
    }

    protected void pushAway(Entity entity) {
        entity.pushAwayFrom(this);
    }

    protected void attackLivingEntity(LivingEntity target) {
    }

    public boolean isUsingRiptide() {
        return (this.dataTracker.get(LIVING_FLAGS) & 4) != 0;
    }

    @Override
    public void stopRiding() {
        Entity lv = this.getVehicle();
        super.stopRiding();
        if (lv != null && lv != this.getVehicle() && !this.getWorld().isClient) {
            this.onDismounted(lv);
        }
    }

    @Override
    public void tickRiding() {
        super.tickRiding();
        this.prevStepBobbingAmount = this.stepBobbingAmount;
        this.stepBobbingAmount = 0.0f;
        this.onLanding();
    }

    @Override
    public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps) {
        this.serverX = x;
        this.serverY = y;
        this.serverZ = z;
        this.serverYaw = yaw;
        this.serverPitch = pitch;
        this.bodyTrackingIncrements = interpolationSteps;
    }

    @Override
    public double getLerpTargetX() {
        return this.bodyTrackingIncrements > 0 ? this.serverX : this.getX();
    }

    @Override
    public double getLerpTargetY() {
        return this.bodyTrackingIncrements > 0 ? this.serverY : this.getY();
    }

    @Override
    public double getLerpTargetZ() {
        return this.bodyTrackingIncrements > 0 ? this.serverZ : this.getZ();
    }

    @Override
    public float getLerpTargetPitch() {
        return this.bodyTrackingIncrements > 0 ? (float)this.serverPitch : this.getPitch();
    }

    @Override
    public float getLerpTargetYaw() {
        return this.bodyTrackingIncrements > 0 ? (float)this.serverYaw : this.getYaw();
    }

    @Override
    public void updateTrackedHeadRotation(float yaw, int interpolationSteps) {
        this.serverHeadYaw = yaw;
        this.headTrackingIncrements = interpolationSteps;
    }

    public void setJumping(boolean jumping) {
        this.jumping = jumping;
    }

    public void triggerItemPickedUpByEntityCriteria(ItemEntity item) {
        Entity lv = item.getOwner();
        if (lv instanceof ServerPlayerEntity) {
            Criteria.THROWN_ITEM_PICKED_UP_BY_ENTITY.trigger((ServerPlayerEntity)lv, item.getStack(), this);
        }
    }

    public void sendPickup(Entity item, int count) {
        if (!item.isRemoved() && !this.getWorld().isClient && (item instanceof ItemEntity || item instanceof PersistentProjectileEntity || item instanceof ExperienceOrbEntity)) {
            ((ServerWorld)this.getWorld()).getChunkManager().sendToOtherNearbyPlayers(item, new ItemPickupAnimationS2CPacket(item.getId(), this.getId(), count));
        }
    }

    public boolean canSee(Entity entity) {
        if (entity.getWorld() != this.getWorld()) {
            return false;
        }
        Vec3d lv = new Vec3d(this.getX(), this.getEyeY(), this.getZ());
        Vec3d lv2 = new Vec3d(entity.getX(), entity.getEyeY(), entity.getZ());
        if (lv2.distanceTo(lv) > 128.0) {
            return false;
        }
        return this.getWorld().raycast(new RaycastContext(lv, lv2, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this)).getType() == HitResult.Type.MISS;
    }

    @Override
    public float getYaw(float tickDelta) {
        if (tickDelta == 1.0f) {
            return this.headYaw;
        }
        return MathHelper.lerp(tickDelta, this.prevHeadYaw, this.headYaw);
    }

    public float getHandSwingProgress(float tickDelta) {
        float g = this.handSwingProgress - this.lastHandSwingProgress;
        if (g < 0.0f) {
            g += 1.0f;
        }
        return this.lastHandSwingProgress + g * tickDelta;
    }

    @Override
    public boolean canHit() {
        return !this.isRemoved();
    }

    @Override
    public boolean isPushable() {
        return this.isAlive() && !this.isSpectator() && !this.isClimbing();
    }

    @Override
    public float getHeadYaw() {
        return this.headYaw;
    }

    @Override
    public void setHeadYaw(float headYaw) {
        this.headYaw = headYaw;
    }

    @Override
    public void setBodyYaw(float bodyYaw) {
        this.bodyYaw = bodyYaw;
    }

    @Override
    public Vec3d positionInPortal(Direction.Axis portalAxis, BlockLocating.Rectangle portalRect) {
        return LivingEntity.positionInPortal(super.positionInPortal(portalAxis, portalRect));
    }

    public static Vec3d positionInPortal(Vec3d pos) {
        return new Vec3d(pos.x, pos.y, 0.0);
    }

    public float getAbsorptionAmount() {
        return this.absorptionAmount;
    }

    public final void setAbsorptionAmount(float absorptionAmount) {
        this.setAbsorptionAmountUnclamped(MathHelper.clamp(absorptionAmount, 0.0f, this.getMaxAbsorption()));
    }

    protected void setAbsorptionAmountUnclamped(float absorptionAmount) {
        this.absorptionAmount = absorptionAmount;
    }

    public void enterCombat() {
    }

    public void endCombat() {
    }

    protected void markEffectsDirty() {
        this.effectsChanged = true;
    }

    public abstract Arm getMainArm();

    public boolean isUsingItem() {
        return (this.dataTracker.get(LIVING_FLAGS) & 1) > 0;
    }

    public Hand getActiveHand() {
        return (this.dataTracker.get(LIVING_FLAGS) & 2) > 0 ? Hand.OFF_HAND : Hand.MAIN_HAND;
    }

    private void tickActiveItemStack() {
        if (this.isUsingItem()) {
            if (ItemStack.areItemsEqual(this.getStackInHand(this.getActiveHand()), this.activeItemStack)) {
                this.activeItemStack = this.getStackInHand(this.getActiveHand());
                this.tickItemStackUsage(this.activeItemStack);
            } else {
                this.clearActiveItem();
            }
        }
    }

    protected void tickItemStackUsage(ItemStack stack) {
        stack.usageTick(this.getWorld(), this, this.getItemUseTimeLeft());
        if (this.shouldSpawnConsumptionEffects()) {
            this.spawnConsumptionEffects(stack, 5);
        }
        if (--this.itemUseTimeLeft == 0 && !this.getWorld().isClient && !stack.isUsedOnRelease()) {
            this.consumeItem();
        }
    }

    private boolean shouldSpawnConsumptionEffects() {
        int j;
        int i = this.activeItemStack.getMaxUseTime(this) - this.getItemUseTimeLeft();
        boolean bl = i > (j = (int)((float)this.activeItemStack.getMaxUseTime(this) * 0.21875f));
        return bl && this.getItemUseTimeLeft() % 4 == 0;
    }

    private void updateLeaningPitch() {
        this.lastLeaningPitch = this.leaningPitch;
        this.leaningPitch = this.isInSwimmingPose() ? Math.min(1.0f, this.leaningPitch + 0.09f) : Math.max(0.0f, this.leaningPitch - 0.09f);
    }

    protected void setLivingFlag(int mask, boolean value) {
        int j = this.dataTracker.get(LIVING_FLAGS).byteValue();
        j = value ? (j |= mask) : (j &= ~mask);
        this.dataTracker.set(LIVING_FLAGS, (byte)j);
    }

    public void setCurrentHand(Hand hand) {
        ItemStack lv = this.getStackInHand(hand);
        if (lv.isEmpty() || this.isUsingItem()) {
            return;
        }
        this.activeItemStack = lv;
        this.itemUseTimeLeft = lv.getMaxUseTime(this);
        if (!this.getWorld().isClient) {
            this.setLivingFlag(USING_ITEM_FLAG, true);
            this.setLivingFlag(OFF_HAND_ACTIVE_FLAG, hand == Hand.OFF_HAND);
            this.emitGameEvent(GameEvent.ITEM_INTERACT_START);
        }
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);
        if (SLEEPING_POSITION.equals(data)) {
            if (this.getWorld().isClient) {
                this.getSleepingPosition().ifPresent(this::setPositionInBed);
            }
        } else if (LIVING_FLAGS.equals(data) && this.getWorld().isClient) {
            if (this.isUsingItem() && this.activeItemStack.isEmpty()) {
                this.activeItemStack = this.getStackInHand(this.getActiveHand());
                if (!this.activeItemStack.isEmpty()) {
                    this.itemUseTimeLeft = this.activeItemStack.getMaxUseTime(this);
                }
            } else if (!this.isUsingItem() && !this.activeItemStack.isEmpty()) {
                this.activeItemStack = ItemStack.EMPTY;
                this.itemUseTimeLeft = 0;
            }
        }
    }

    @Override
    public void lookAt(EntityAnchorArgumentType.EntityAnchor anchorPoint, Vec3d target) {
        super.lookAt(anchorPoint, target);
        this.prevHeadYaw = this.headYaw;
        this.prevBodyYaw = this.bodyYaw = this.headYaw;
    }

    @Override
    public float lerpYaw(float delta) {
        return MathHelper.lerp(delta, this.prevBodyYaw, this.bodyYaw);
    }

    protected void spawnConsumptionEffects(ItemStack stack, int particleCount) {
        if (stack.isEmpty() || !this.isUsingItem()) {
            return;
        }
        if (stack.getUseAction() == UseAction.DRINK) {
            this.playSound(this.getDrinkSound(stack), 0.5f, this.getWorld().random.nextFloat() * 0.1f + 0.9f);
        }
        if (stack.getUseAction() == UseAction.EAT) {
            this.spawnItemParticles(stack, particleCount);
            this.playSound(this.getEatSound(stack), 0.5f + 0.5f * (float)this.random.nextInt(2), (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
        }
    }

    private void spawnItemParticles(ItemStack stack, int count) {
        for (int j = 0; j < count; ++j) {
            Vec3d lv = new Vec3d(((double)this.random.nextFloat() - 0.5) * 0.1, Math.random() * 0.1 + 0.1, 0.0);
            lv = lv.rotateX(-this.getPitch() * ((float)Math.PI / 180));
            lv = lv.rotateY(-this.getYaw() * ((float)Math.PI / 180));
            double d = (double)(-this.random.nextFloat()) * 0.6 - 0.3;
            Vec3d lv2 = new Vec3d(((double)this.random.nextFloat() - 0.5) * 0.3, d, 0.6);
            lv2 = lv2.rotateX(-this.getPitch() * ((float)Math.PI / 180));
            lv2 = lv2.rotateY(-this.getYaw() * ((float)Math.PI / 180));
            lv2 = lv2.add(this.getX(), this.getEyeY(), this.getZ());
            this.getWorld().addParticle(new ItemStackParticleEffect(ParticleTypes.ITEM, stack), lv2.x, lv2.y, lv2.z, lv.x, lv.y + 0.05, lv.z);
        }
    }

    protected void consumeItem() {
        if (this.getWorld().isClient && !this.isUsingItem()) {
            return;
        }
        Hand lv = this.getActiveHand();
        if (!this.activeItemStack.equals(this.getStackInHand(lv))) {
            this.stopUsingItem();
            return;
        }
        if (!this.activeItemStack.isEmpty() && this.isUsingItem()) {
            this.spawnConsumptionEffects(this.activeItemStack, 16);
            ItemStack lv2 = this.activeItemStack.finishUsing(this.getWorld(), this);
            if (lv2 != this.activeItemStack) {
                this.setStackInHand(lv, lv2);
            }
            this.clearActiveItem();
        }
    }

    public ItemStack getActiveItem() {
        return this.activeItemStack;
    }

    public int getItemUseTimeLeft() {
        return this.itemUseTimeLeft;
    }

    public int getItemUseTime() {
        if (this.isUsingItem()) {
            return this.activeItemStack.getMaxUseTime(this) - this.getItemUseTimeLeft();
        }
        return 0;
    }

    public void stopUsingItem() {
        if (!this.activeItemStack.isEmpty()) {
            this.activeItemStack.onStoppedUsing(this.getWorld(), this, this.getItemUseTimeLeft());
            if (this.activeItemStack.isUsedOnRelease()) {
                this.tickActiveItemStack();
            }
        }
        this.clearActiveItem();
    }

    public void clearActiveItem() {
        if (!this.getWorld().isClient) {
            boolean bl = this.isUsingItem();
            this.setLivingFlag(USING_ITEM_FLAG, false);
            if (bl) {
                this.emitGameEvent(GameEvent.ITEM_INTERACT_FINISH);
            }
        }
        this.activeItemStack = ItemStack.EMPTY;
        this.itemUseTimeLeft = 0;
    }

    public boolean isBlocking() {
        if (!this.isUsingItem() || this.activeItemStack.isEmpty()) {
            return false;
        }
        Item lv = this.activeItemStack.getItem();
        if (lv.getUseAction(this.activeItemStack) != UseAction.BLOCK) {
            return false;
        }
        return lv.getMaxUseTime(this.activeItemStack, this) - this.itemUseTimeLeft >= 5;
    }

    public boolean isHoldingOntoLadder() {
        return this.isSneaking();
    }

    public boolean isFallFlying() {
        return this.getFlag(Entity.FALL_FLYING_FLAG_INDEX);
    }

    @Override
    public boolean isInSwimmingPose() {
        return super.isInSwimmingPose() || !this.isFallFlying() && this.isInPose(EntityPose.FALL_FLYING);
    }

    public int getFallFlyingTicks() {
        return this.fallFlyingTicks;
    }

    public boolean teleport(double x, double y, double z, boolean particleEffects) {
        LivingEntity livingEntity;
        double g = this.getX();
        double h = this.getY();
        double i = this.getZ();
        double j = y;
        boolean bl2 = false;
        BlockPos lv = BlockPos.ofFloored(x, j, z);
        World lv2 = this.getWorld();
        if (lv2.isChunkLoaded(lv)) {
            boolean bl3 = false;
            while (!bl3 && lv.getY() > lv2.getBottomY()) {
                BlockPos lv3 = lv.down();
                BlockState lv4 = lv2.getBlockState(lv3);
                if (lv4.blocksMovement()) {
                    bl3 = true;
                    continue;
                }
                j -= 1.0;
                lv = lv3;
            }
            if (bl3) {
                this.requestTeleport(x, j, z);
                if (lv2.isSpaceEmpty(this) && !lv2.containsFluid(this.getBoundingBox())) {
                    bl2 = true;
                }
            }
        }
        if (!bl2) {
            this.requestTeleport(g, h, i);
            return false;
        }
        if (particleEffects) {
            lv2.sendEntityStatus(this, EntityStatuses.ADD_PORTAL_PARTICLES);
        }
        if ((livingEntity = this) instanceof PathAwareEntity) {
            PathAwareEntity lv5 = (PathAwareEntity)livingEntity;
            lv5.getNavigation().stop();
        }
        return true;
    }

    public boolean isAffectedBySplashPotions() {
        return !this.isDead();
    }

    public boolean isMobOrPlayer() {
        return true;
    }

    public void setNearbySongPlaying(BlockPos songPosition, boolean playing) {
    }

    public boolean canEquip(ItemStack stack) {
        return false;
    }

    @Override
    public final EntityDimensions getDimensions(EntityPose pose) {
        return pose == EntityPose.SLEEPING ? SLEEPING_DIMENSIONS : this.getBaseDimensions(pose).scaled(this.getScale());
    }

    protected EntityDimensions getBaseDimensions(EntityPose pose) {
        return this.getType().getDimensions().scaled(this.getScaleFactor());
    }

    public ImmutableList<EntityPose> getPoses() {
        return ImmutableList.of(EntityPose.STANDING);
    }

    public Box getBoundingBox(EntityPose pose) {
        EntityDimensions lv = this.getDimensions(pose);
        return new Box(-lv.width() / 2.0f, 0.0, -lv.width() / 2.0f, lv.width() / 2.0f, lv.height(), lv.width() / 2.0f);
    }

    protected boolean wouldNotSuffocateInPose(EntityPose pose) {
        Box lv = this.getDimensions(pose).getBoxAt(this.getPos());
        return this.getWorld().isBlockSpaceEmpty(this, lv);
    }

    @Override
    public boolean canUsePortals(boolean allowVehicles) {
        return super.canUsePortals(allowVehicles) && !this.isSleeping();
    }

    public Optional<BlockPos> getSleepingPosition() {
        return this.dataTracker.get(SLEEPING_POSITION);
    }

    public void setSleepingPosition(BlockPos pos) {
        this.dataTracker.set(SLEEPING_POSITION, Optional.of(pos));
    }

    public void clearSleepingPosition() {
        this.dataTracker.set(SLEEPING_POSITION, Optional.empty());
    }

    public boolean isSleeping() {
        return this.getSleepingPosition().isPresent();
    }

    public void sleep(BlockPos pos) {
        BlockState lv;
        if (this.hasVehicle()) {
            this.stopRiding();
        }
        if ((lv = this.getWorld().getBlockState(pos)).getBlock() instanceof BedBlock) {
            this.getWorld().setBlockState(pos, (BlockState)lv.with(BedBlock.OCCUPIED, true), Block.NOTIFY_ALL);
        }
        this.setPose(EntityPose.SLEEPING);
        this.setPositionInBed(pos);
        this.setSleepingPosition(pos);
        this.setVelocity(Vec3d.ZERO);
        this.velocityDirty = true;
    }

    private void setPositionInBed(BlockPos pos) {
        this.setPosition((double)pos.getX() + 0.5, (double)pos.getY() + 0.6875, (double)pos.getZ() + 0.5);
    }

    private boolean isSleepingInBed() {
        return this.getSleepingPosition().map(pos -> this.getWorld().getBlockState((BlockPos)pos).getBlock() instanceof BedBlock).orElse(false);
    }

    public void wakeUp() {
        this.getSleepingPosition().filter(this.getWorld()::isChunkLoaded).ifPresent(pos -> {
            BlockState lv = this.getWorld().getBlockState((BlockPos)pos);
            if (lv.getBlock() instanceof BedBlock) {
                Direction lv2 = lv.get(BedBlock.FACING);
                this.getWorld().setBlockState((BlockPos)pos, (BlockState)lv.with(BedBlock.OCCUPIED, false), Block.NOTIFY_ALL);
                Vec3d lv3 = BedBlock.findWakeUpPosition(this.getType(), (CollisionView)this.getWorld(), pos, lv2, this.getYaw()).orElseGet(() -> {
                    BlockPos lv = pos.up();
                    return new Vec3d((double)lv.getX() + 0.5, (double)lv.getY() + 0.1, (double)lv.getZ() + 0.5);
                });
                Vec3d lv4 = Vec3d.ofBottomCenter(pos).subtract(lv3).normalize();
                float f = (float)MathHelper.wrapDegrees(MathHelper.atan2(lv4.z, lv4.x) * 57.2957763671875 - 90.0);
                this.setPosition(lv3.x, lv3.y, lv3.z);
                this.setYaw(f);
                this.setPitch(0.0f);
            }
        });
        Vec3d lv = this.getPos();
        this.setPose(EntityPose.STANDING);
        this.setPosition(lv.x, lv.y, lv.z);
        this.clearSleepingPosition();
    }

    @Nullable
    public Direction getSleepingDirection() {
        BlockPos lv = this.getSleepingPosition().orElse(null);
        return lv != null ? BedBlock.getDirection(this.getWorld(), lv) : null;
    }

    @Override
    public boolean isInsideWall() {
        return !this.isSleeping() && super.isInsideWall();
    }

    public ItemStack getProjectileType(ItemStack stack) {
        return ItemStack.EMPTY;
    }

    public final ItemStack tryEatFood(World world, ItemStack stack) {
        FoodComponent lv = stack.get(DataComponentTypes.FOOD);
        if (lv != null) {
            return this.eatFood(world, stack, lv);
        }
        return stack;
    }

    public ItemStack eatFood(World world, ItemStack stack, FoodComponent foodComponent) {
        world.playSound(null, this.getX(), this.getY(), this.getZ(), this.getEatSound(stack), SoundCategory.NEUTRAL, 1.0f, 1.0f + (world.random.nextFloat() - world.random.nextFloat()) * 0.4f);
        this.applyFoodEffects(foodComponent);
        stack.decrementUnlessCreative(1, this);
        this.emitGameEvent(GameEvent.EAT);
        return stack;
    }

    private void applyFoodEffects(FoodComponent component) {
        if (this.getWorld().isClient()) {
            return;
        }
        List<FoodComponent.StatusEffectEntry> list = component.effects();
        for (FoodComponent.StatusEffectEntry lv : list) {
            if (!(this.random.nextFloat() < lv.probability())) continue;
            this.addStatusEffect(lv.effect());
        }
    }

    private static byte getEquipmentBreakStatus(EquipmentSlot slot) {
        return switch (slot) {
            default -> throw new MatchException(null, null);
            case EquipmentSlot.MAINHAND -> 47;
            case EquipmentSlot.OFFHAND -> 48;
            case EquipmentSlot.HEAD -> 49;
            case EquipmentSlot.CHEST -> 50;
            case EquipmentSlot.FEET -> 52;
            case EquipmentSlot.LEGS -> 51;
            case EquipmentSlot.BODY -> 65;
        };
    }

    public void sendEquipmentBreakStatus(Item item, EquipmentSlot slot) {
        this.getWorld().sendEntityStatus(this, LivingEntity.getEquipmentBreakStatus(slot));
    }

    public static EquipmentSlot getSlotForHand(Hand hand) {
        return hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
    }

    @Override
    public Box getVisibilityBoundingBox() {
        if (this.getEquippedStack(EquipmentSlot.HEAD).isOf(Items.DRAGON_HEAD)) {
            float f = 0.5f;
            return this.getBoundingBox().expand(0.5, 0.5, 0.5);
        }
        return super.getVisibilityBoundingBox();
    }

    public EquipmentSlot getPreferredEquipmentSlot(ItemStack stack) {
        EquipmentSlot lv2;
        Equipment lv = Equipment.fromStack(stack);
        if (lv != null && this.canUseSlot(lv2 = lv.getSlotType())) {
            return lv2;
        }
        return EquipmentSlot.MAINHAND;
    }

    private static StackReference getStackReference(LivingEntity entity, EquipmentSlot slot) {
        if (slot == EquipmentSlot.HEAD || slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
            return StackReference.of(entity, slot);
        }
        return StackReference.of(entity, slot, stack -> stack.isEmpty() || entity.getPreferredEquipmentSlot((ItemStack)stack) == slot);
    }

    @Nullable
    private static EquipmentSlot getEquipmentSlot(int slotId) {
        if (slotId == 100 + EquipmentSlot.HEAD.getEntitySlotId()) {
            return EquipmentSlot.HEAD;
        }
        if (slotId == 100 + EquipmentSlot.CHEST.getEntitySlotId()) {
            return EquipmentSlot.CHEST;
        }
        if (slotId == 100 + EquipmentSlot.LEGS.getEntitySlotId()) {
            return EquipmentSlot.LEGS;
        }
        if (slotId == 100 + EquipmentSlot.FEET.getEntitySlotId()) {
            return EquipmentSlot.FEET;
        }
        if (slotId == 98) {
            return EquipmentSlot.MAINHAND;
        }
        if (slotId == 99) {
            return EquipmentSlot.OFFHAND;
        }
        if (slotId == 105) {
            return EquipmentSlot.BODY;
        }
        return null;
    }

    @Override
    public StackReference getStackReference(int mappedIndex) {
        EquipmentSlot lv = LivingEntity.getEquipmentSlot(mappedIndex);
        if (lv != null) {
            return LivingEntity.getStackReference(this, lv);
        }
        return super.getStackReference(mappedIndex);
    }

    @Override
    public boolean canFreeze() {
        if (this.isSpectator()) {
            return false;
        }
        boolean bl = !this.getEquippedStack(EquipmentSlot.HEAD).isIn(ItemTags.FREEZE_IMMUNE_WEARABLES) && !this.getEquippedStack(EquipmentSlot.CHEST).isIn(ItemTags.FREEZE_IMMUNE_WEARABLES) && !this.getEquippedStack(EquipmentSlot.LEGS).isIn(ItemTags.FREEZE_IMMUNE_WEARABLES) && !this.getEquippedStack(EquipmentSlot.FEET).isIn(ItemTags.FREEZE_IMMUNE_WEARABLES) && !this.getEquippedStack(EquipmentSlot.BODY).isIn(ItemTags.FREEZE_IMMUNE_WEARABLES);
        return bl && super.canFreeze();
    }

    @Override
    public boolean isGlowing() {
        return !this.getWorld().isClient() && this.hasStatusEffect(StatusEffects.GLOWING) || super.isGlowing();
    }

    @Override
    public float getBodyYaw() {
        return this.bodyYaw;
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        double d = packet.getX();
        double e = packet.getY();
        double f = packet.getZ();
        float g = packet.getYaw();
        float h = packet.getPitch();
        this.updateTrackedPosition(d, e, f);
        this.bodyYaw = packet.getHeadYaw();
        this.headYaw = packet.getHeadYaw();
        this.prevBodyYaw = this.bodyYaw;
        this.prevHeadYaw = this.headYaw;
        this.setId(packet.getEntityId());
        this.setUuid(packet.getUuid());
        this.updatePositionAndAngles(d, e, f, g, h);
        this.setVelocity(packet.getVelocityX(), packet.getVelocityY(), packet.getVelocityZ());
    }

    public boolean disablesShield() {
        return this.getWeaponStack().getItem() instanceof AxeItem;
    }

    @Override
    public float getStepHeight() {
        float f = (float)this.getAttributeValue(EntityAttributes.GENERIC_STEP_HEIGHT);
        return this.getControllingPassenger() instanceof PlayerEntity ? Math.max(f, 1.0f) : f;
    }

    @Override
    public Vec3d getPassengerRidingPos(Entity passenger) {
        return this.getPos().add(this.getPassengerAttachmentPos(passenger, this.getDimensions(this.getPose()), this.getScale() * this.getScaleFactor()));
    }

    protected void lerpHeadYaw(int headTrackingIncrements, double serverHeadYaw) {
        this.headYaw = (float)MathHelper.lerpAngleDegrees(1.0 / (double)headTrackingIncrements, (double)this.headYaw, serverHeadYaw);
    }

    @Override
    public void setOnFireForTicks(int ticks) {
        super.setOnFireForTicks(MathHelper.ceil((double)ticks * this.getAttributeValue(EntityAttributes.GENERIC_BURNING_TIME)));
    }

    public boolean isInCreativeMode() {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        ServerWorld lv;
        World world;
        return super.isInvulnerableTo(damageSource) || (world = this.getWorld()) instanceof ServerWorld && EnchantmentHelper.isInvulnerableTo(lv = (ServerWorld)world, this, damageSource);
    }

    public record FallSounds(SoundEvent small, SoundEvent big) {
    }
}

