/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.passive;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CaveVines;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.VariantHolder;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.DiveJumpingGoal;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.EscapeSunlightGoal;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.entity.ai.goal.PounceAtTargetGoal;
import net.minecraft.entity.ai.goal.PowderSnowJumpGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.PolarBearEntity;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.entity.passive.SchoolingFishEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class FoxEntity
extends AnimalEntity
implements VariantHolder<Type> {
    private static final TrackedData<Integer> TYPE = DataTracker.registerData(FoxEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Byte> FOX_FLAGS = DataTracker.registerData(FoxEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final int SITTING_FLAG = 1;
    public static final int CROUCHING_FLAG = 4;
    public static final int ROLLING_HEAD_FLAG = 8;
    public static final int CHASING_FLAG = 16;
    private static final int SLEEPING_FLAG = 32;
    private static final int WALKING_FLAG = 64;
    private static final int AGGRESSIVE_FLAG = 128;
    private static final TrackedData<Optional<UUID>> OWNER = DataTracker.registerData(FoxEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
    private static final TrackedData<Optional<UUID>> OTHER_TRUSTED = DataTracker.registerData(FoxEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
    static final Predicate<ItemEntity> PICKABLE_DROP_FILTER = item -> !item.cannotPickup() && item.isAlive();
    private static final Predicate<Entity> JUST_ATTACKED_SOMETHING_FILTER = entity -> {
        if (entity instanceof LivingEntity) {
            LivingEntity lv = (LivingEntity)entity;
            return lv.getAttacking() != null && lv.getLastAttackTime() < lv.age + 600;
        }
        return false;
    };
    static final Predicate<Entity> CHICKEN_AND_RABBIT_FILTER = entity -> entity instanceof ChickenEntity || entity instanceof RabbitEntity;
    private static final Predicate<Entity> NOTICEABLE_PLAYER_FILTER = entity -> !entity.isSneaky() && EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.test((Entity)entity);
    private static final int EATING_DURATION = 600;
    private static final EntityDimensions BABY_BASE_DIMENSIONS = EntityType.FOX.getDimensions().scaled(0.5f).withEyeHeight(0.2975f);
    private Goal followChickenAndRabbitGoal;
    private Goal followBabyTurtleGoal;
    private Goal followFishGoal;
    private float headRollProgress;
    private float lastHeadRollProgress;
    float extraRollingHeight;
    float lastExtraRollingHeight;
    private int eatingTime;

    public FoxEntity(EntityType<? extends FoxEntity> arg, World arg2) {
        super((EntityType<? extends AnimalEntity>)arg, arg2);
        this.lookControl = new FoxLookControl();
        this.moveControl = new FoxMoveControl();
        this.setPathfindingPenalty(PathNodeType.DANGER_OTHER, 0.0f);
        this.setPathfindingPenalty(PathNodeType.DAMAGE_OTHER, 0.0f);
        this.setCanPickUpLoot(true);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(OWNER, Optional.empty());
        builder.add(OTHER_TRUSTED, Optional.empty());
        builder.add(TYPE, 0);
        builder.add(FOX_FLAGS, (byte)0);
    }

    @Override
    protected void initGoals() {
        this.followChickenAndRabbitGoal = new ActiveTargetGoal<AnimalEntity>(this, AnimalEntity.class, 10, false, false, entity -> entity instanceof ChickenEntity || entity instanceof RabbitEntity);
        this.followBabyTurtleGoal = new ActiveTargetGoal<TurtleEntity>(this, TurtleEntity.class, 10, false, false, TurtleEntity.BABY_TURTLE_ON_LAND_FILTER);
        this.followFishGoal = new ActiveTargetGoal<FishEntity>(this, FishEntity.class, 20, false, false, entity -> entity instanceof SchoolingFishEntity);
        this.goalSelector.add(0, new FoxSwimGoal());
        this.goalSelector.add(0, new PowderSnowJumpGoal(this, this.getWorld()));
        this.goalSelector.add(1, new StopWanderingGoal());
        this.goalSelector.add(2, new EscapeWhenNotAggressiveGoal(2.2));
        this.goalSelector.add(3, new MateGoal(this, 1.0));
        this.goalSelector.add(4, new FleeEntityGoal<PlayerEntity>(this, PlayerEntity.class, 16.0f, 1.6, 1.4, entity -> NOTICEABLE_PLAYER_FILTER.test((Entity)entity) && !this.canTrust(entity.getUuid()) && !this.isAggressive()));
        this.goalSelector.add(4, new FleeEntityGoal<WolfEntity>(this, WolfEntity.class, 8.0f, 1.6, 1.4, entity -> !((WolfEntity)entity).isTamed() && !this.isAggressive()));
        this.goalSelector.add(4, new FleeEntityGoal<PolarBearEntity>(this, PolarBearEntity.class, 8.0f, 1.6, 1.4, entity -> !this.isAggressive()));
        this.goalSelector.add(5, new MoveToHuntGoal());
        this.goalSelector.add(6, new JumpChasingGoal());
        this.goalSelector.add(6, new AvoidDaylightGoal(1.25));
        this.goalSelector.add(7, new AttackGoal((double)1.2f, true));
        this.goalSelector.add(7, new DelayedCalmDownGoal());
        this.goalSelector.add(8, new FollowParentGoal(this, this, 1.25));
        this.goalSelector.add(9, new GoToVillageGoal(32, 200));
        this.goalSelector.add(10, new EatBerriesGoal((double)1.2f, 12, 1));
        this.goalSelector.add(10, new PounceAtTargetGoal(this, 0.4f));
        this.goalSelector.add(11, new WanderAroundFarGoal(this, 1.0));
        this.goalSelector.add(11, new PickupItemGoal());
        this.goalSelector.add(12, new LookAtEntityGoal(this, PlayerEntity.class, 24.0f));
        this.goalSelector.add(13, new SitDownAndLookAroundGoal());
        this.targetSelector.add(3, new DefendFriendGoal(LivingEntity.class, false, false, entity -> JUST_ATTACKED_SOMETHING_FILTER.test((Entity)entity) && !this.canTrust(entity.getUuid())));
    }

    @Override
    public SoundEvent getEatSound(ItemStack stack) {
        return SoundEvents.ENTITY_FOX_EAT;
    }

    @Override
    public void tickMovement() {
        if (!this.getWorld().isClient && this.isAlive() && this.canMoveVoluntarily()) {
            LivingEntity lv3;
            ++this.eatingTime;
            ItemStack lv = this.getEquippedStack(EquipmentSlot.MAINHAND);
            if (this.canEat(lv)) {
                if (this.eatingTime > 600) {
                    ItemStack lv2 = lv.finishUsing(this.getWorld(), this);
                    if (!lv2.isEmpty()) {
                        this.equipStack(EquipmentSlot.MAINHAND, lv2);
                    }
                    this.eatingTime = 0;
                } else if (this.eatingTime > 560 && this.random.nextFloat() < 0.1f) {
                    this.playSound(this.getEatSound(lv), 1.0f, 1.0f);
                    this.getWorld().sendEntityStatus(this, EntityStatuses.CREATE_EATING_PARTICLES);
                }
            }
            if ((lv3 = this.getTarget()) == null || !lv3.isAlive()) {
                this.setCrouching(false);
                this.setRollingHead(false);
            }
        }
        if (this.isSleeping() || this.isImmobile()) {
            this.jumping = false;
            this.sidewaysSpeed = 0.0f;
            this.forwardSpeed = 0.0f;
        }
        super.tickMovement();
        if (this.isAggressive() && this.random.nextFloat() < 0.05f) {
            this.playSound(SoundEvents.ENTITY_FOX_AGGRO, 1.0f, 1.0f);
        }
    }

    @Override
    protected boolean isImmobile() {
        return this.isDead();
    }

    private boolean canEat(ItemStack stack) {
        return stack.contains(DataComponentTypes.FOOD) && this.getTarget() == null && this.isOnGround() && !this.isSleeping();
    }

    @Override
    protected void initEquipment(Random random, LocalDifficulty localDifficulty) {
        if (random.nextFloat() < 0.2f) {
            float f = random.nextFloat();
            ItemStack lv = f < 0.05f ? new ItemStack(Items.EMERALD) : (f < 0.2f ? new ItemStack(Items.EGG) : (f < 0.4f ? (random.nextBoolean() ? new ItemStack(Items.RABBIT_FOOT) : new ItemStack(Items.RABBIT_HIDE)) : (f < 0.6f ? new ItemStack(Items.WHEAT) : (f < 0.8f ? new ItemStack(Items.LEATHER) : new ItemStack(Items.FEATHER)))));
            this.equipStack(EquipmentSlot.MAINHAND, lv);
        }
    }

    @Override
    public void handleStatus(byte status) {
        if (status == EntityStatuses.CREATE_EATING_PARTICLES) {
            ItemStack lv = this.getEquippedStack(EquipmentSlot.MAINHAND);
            if (!lv.isEmpty()) {
                for (int i = 0; i < 8; ++i) {
                    Vec3d lv2 = new Vec3d(((double)this.random.nextFloat() - 0.5) * 0.1, Math.random() * 0.1 + 0.1, 0.0).rotateX(-this.getPitch() * ((float)Math.PI / 180)).rotateY(-this.getYaw() * ((float)Math.PI / 180));
                    this.getWorld().addParticle(new ItemStackParticleEffect(ParticleTypes.ITEM, lv), this.getX() + this.getRotationVector().x / 2.0, this.getY(), this.getZ() + this.getRotationVector().z / 2.0, lv2.x, lv2.y + 0.05, lv2.z);
                }
            }
        } else {
            super.handleStatus(status);
        }
    }

    public static DefaultAttributeContainer.Builder createFoxAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3f).add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0).add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0).add(EntityAttributes.GENERIC_SAFE_FALL_DISTANCE, 5.0);
    }

    @Override
    @Nullable
    public FoxEntity createChild(ServerWorld arg, PassiveEntity arg2) {
        FoxEntity lv = EntityType.FOX.create(arg);
        if (lv != null) {
            lv.setVariant(this.random.nextBoolean() ? this.getVariant() : ((FoxEntity)arg2).getVariant());
        }
        return lv;
    }

    public static boolean canSpawn(EntityType<FoxEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        return world.getBlockState(pos.down()).isIn(BlockTags.FOXES_SPAWNABLE_ON) && FoxEntity.isLightLevelValidForNaturalSpawn(world, pos);
    }

    @Override
    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        RegistryEntry<Biome> lv = world.getBiome(this.getBlockPos());
        Type lv2 = Type.fromBiome(lv);
        boolean bl = false;
        if (entityData instanceof FoxData) {
            FoxData lv3 = (FoxData)entityData;
            lv2 = lv3.type;
            if (lv3.getSpawnedCount() >= 2) {
                bl = true;
            }
        } else {
            entityData = new FoxData(lv2);
        }
        this.setVariant(lv2);
        if (bl) {
            this.setBreedingAge(-24000);
        }
        if (world instanceof ServerWorld) {
            this.addTypeSpecificGoals();
        }
        this.initEquipment(world.getRandom(), difficulty);
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    private void addTypeSpecificGoals() {
        if (this.getVariant() == Type.RED) {
            this.targetSelector.add(4, this.followChickenAndRabbitGoal);
            this.targetSelector.add(4, this.followBabyTurtleGoal);
            this.targetSelector.add(6, this.followFishGoal);
        } else {
            this.targetSelector.add(4, this.followFishGoal);
            this.targetSelector.add(6, this.followChickenAndRabbitGoal);
            this.targetSelector.add(6, this.followBabyTurtleGoal);
        }
    }

    @Override
    protected void eat(PlayerEntity player, Hand hand, ItemStack stack) {
        if (this.isBreedingItem(stack)) {
            this.playSound(this.getEatSound(stack), 1.0f, 1.0f);
        }
        super.eat(player, hand, stack);
    }

    @Override
    public EntityDimensions getBaseDimensions(EntityPose pose) {
        return this.isBaby() ? BABY_BASE_DIMENSIONS : super.getBaseDimensions(pose);
    }

    @Override
    public Type getVariant() {
        return Type.fromId(this.dataTracker.get(TYPE));
    }

    @Override
    public void setVariant(Type arg) {
        this.dataTracker.set(TYPE, arg.getId());
    }

    List<UUID> getTrustedUuids() {
        ArrayList<UUID> list = Lists.newArrayList();
        list.add(this.dataTracker.get(OWNER).orElse(null));
        list.add(this.dataTracker.get(OTHER_TRUSTED).orElse(null));
        return list;
    }

    void addTrustedUuid(@Nullable UUID uuid) {
        if (this.dataTracker.get(OWNER).isPresent()) {
            this.dataTracker.set(OTHER_TRUSTED, Optional.ofNullable(uuid));
        } else {
            this.dataTracker.set(OWNER, Optional.ofNullable(uuid));
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        List<UUID> list = this.getTrustedUuids();
        NbtList lv = new NbtList();
        for (UUID uUID : list) {
            if (uUID == null) continue;
            lv.add(NbtHelper.fromUuid(uUID));
        }
        nbt.put("Trusted", lv);
        nbt.putBoolean("Sleeping", this.isSleeping());
        nbt.putString("Type", this.getVariant().asString());
        nbt.putBoolean("Sitting", this.isSitting());
        nbt.putBoolean("Crouching", this.isInSneakingPose());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        NbtList lv = nbt.getList("Trusted", NbtElement.INT_ARRAY_TYPE);
        for (NbtElement lv2 : lv) {
            this.addTrustedUuid(NbtHelper.toUuid(lv2));
        }
        this.setSleeping(nbt.getBoolean("Sleeping"));
        this.setVariant(Type.byName(nbt.getString("Type")));
        this.setSitting(nbt.getBoolean("Sitting"));
        this.setCrouching(nbt.getBoolean("Crouching"));
        if (this.getWorld() instanceof ServerWorld) {
            this.addTypeSpecificGoals();
        }
    }

    public boolean isSitting() {
        return this.getFoxFlag(SITTING_FLAG);
    }

    public void setSitting(boolean sitting) {
        this.setFoxFlag(SITTING_FLAG, sitting);
    }

    public boolean isWalking() {
        return this.getFoxFlag(WALKING_FLAG);
    }

    void setWalking(boolean walking) {
        this.setFoxFlag(WALKING_FLAG, walking);
    }

    boolean isAggressive() {
        return this.getFoxFlag(AGGRESSIVE_FLAG);
    }

    void setAggressive(boolean aggressive) {
        this.setFoxFlag(AGGRESSIVE_FLAG, aggressive);
    }

    @Override
    public boolean isSleeping() {
        return this.getFoxFlag(SLEEPING_FLAG);
    }

    void setSleeping(boolean sleeping) {
        this.setFoxFlag(SLEEPING_FLAG, sleeping);
    }

    private void setFoxFlag(int mask, boolean value) {
        if (value) {
            this.dataTracker.set(FOX_FLAGS, (byte)(this.dataTracker.get(FOX_FLAGS) | mask));
        } else {
            this.dataTracker.set(FOX_FLAGS, (byte)(this.dataTracker.get(FOX_FLAGS) & ~mask));
        }
    }

    private boolean getFoxFlag(int bitmask) {
        return (this.dataTracker.get(FOX_FLAGS) & bitmask) != 0;
    }

    @Override
    public boolean canEquip(ItemStack stack) {
        EquipmentSlot lv = this.getPreferredEquipmentSlot(stack);
        if (!this.getEquippedStack(lv).isEmpty()) {
            return false;
        }
        return lv == EquipmentSlot.MAINHAND && super.canEquip(stack);
    }

    @Override
    public boolean canPickupItem(ItemStack stack) {
        ItemStack lv = this.getEquippedStack(EquipmentSlot.MAINHAND);
        return lv.isEmpty() || this.eatingTime > 0 && stack.contains(DataComponentTypes.FOOD) && !lv.contains(DataComponentTypes.FOOD);
    }

    private void spit(ItemStack stack) {
        if (stack.isEmpty() || this.getWorld().isClient) {
            return;
        }
        ItemEntity lv = new ItemEntity(this.getWorld(), this.getX() + this.getRotationVector().x, this.getY() + 1.0, this.getZ() + this.getRotationVector().z, stack);
        lv.setPickupDelay(40);
        lv.setThrower(this);
        this.playSound(SoundEvents.ENTITY_FOX_SPIT, 1.0f, 1.0f);
        this.getWorld().spawnEntity(lv);
    }

    private void dropItem(ItemStack stack) {
        ItemEntity lv = new ItemEntity(this.getWorld(), this.getX(), this.getY(), this.getZ(), stack);
        this.getWorld().spawnEntity(lv);
    }

    @Override
    protected void loot(ItemEntity item) {
        ItemStack lv = item.getStack();
        if (this.canPickupItem(lv)) {
            int i = lv.getCount();
            if (i > 1) {
                this.dropItem(lv.split(i - 1));
            }
            this.spit(this.getEquippedStack(EquipmentSlot.MAINHAND));
            this.triggerItemPickedUpByEntityCriteria(item);
            this.equipStack(EquipmentSlot.MAINHAND, lv.split(1));
            this.updateDropChances(EquipmentSlot.MAINHAND);
            this.sendPickup(item, lv.getCount());
            item.discard();
            this.eatingTime = 0;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.canMoveVoluntarily()) {
            boolean bl = this.isTouchingWater();
            if (bl || this.getTarget() != null || this.getWorld().isThundering()) {
                this.stopSleeping();
            }
            if (bl || this.isSleeping()) {
                this.setSitting(false);
            }
            if (this.isWalking() && this.getWorld().random.nextFloat() < 0.2f) {
                BlockPos lv = this.getBlockPos();
                BlockState lv2 = this.getWorld().getBlockState(lv);
                this.getWorld().syncWorldEvent(WorldEvents.BLOCK_BROKEN, lv, Block.getRawIdFromState(lv2));
            }
        }
        this.lastHeadRollProgress = this.headRollProgress;
        this.headRollProgress = this.isRollingHead() ? (this.headRollProgress += (1.0f - this.headRollProgress) * 0.4f) : (this.headRollProgress += (0.0f - this.headRollProgress) * 0.4f);
        this.lastExtraRollingHeight = this.extraRollingHeight;
        if (this.isInSneakingPose()) {
            this.extraRollingHeight += 0.2f;
            if (this.extraRollingHeight > 3.0f) {
                this.extraRollingHeight = 3.0f;
            }
        } else {
            this.extraRollingHeight = 0.0f;
        }
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.isIn(ItemTags.FOX_FOOD);
    }

    @Override
    protected void onPlayerSpawnedChild(PlayerEntity player, MobEntity child) {
        ((FoxEntity)child).addTrustedUuid(player.getUuid());
    }

    public boolean isChasing() {
        return this.getFoxFlag(CHASING_FLAG);
    }

    public void setChasing(boolean chasing) {
        this.setFoxFlag(CHASING_FLAG, chasing);
    }

    public boolean isJumping() {
        return this.jumping;
    }

    public boolean isFullyCrouched() {
        return this.extraRollingHeight == 3.0f;
    }

    public void setCrouching(boolean crouching) {
        this.setFoxFlag(CROUCHING_FLAG, crouching);
    }

    @Override
    public boolean isInSneakingPose() {
        return this.getFoxFlag(CROUCHING_FLAG);
    }

    public void setRollingHead(boolean rollingHead) {
        this.setFoxFlag(ROLLING_HEAD_FLAG, rollingHead);
    }

    public boolean isRollingHead() {
        return this.getFoxFlag(ROLLING_HEAD_FLAG);
    }

    public float getHeadRoll(float tickDelta) {
        return MathHelper.lerp(tickDelta, this.lastHeadRollProgress, this.headRollProgress) * 0.11f * (float)Math.PI;
    }

    public float getBodyRotationHeightOffset(float tickDelta) {
        return MathHelper.lerp(tickDelta, this.lastExtraRollingHeight, this.extraRollingHeight);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (this.isAggressive() && target == null) {
            this.setAggressive(false);
        }
        super.setTarget(target);
    }

    void stopSleeping() {
        this.setSleeping(false);
    }

    void stopActions() {
        this.setRollingHead(false);
        this.setCrouching(false);
        this.setSitting(false);
        this.setSleeping(false);
        this.setAggressive(false);
        this.setWalking(false);
    }

    boolean wantsToPickupItem() {
        return !this.isSleeping() && !this.isSitting() && !this.isWalking();
    }

    @Override
    public void playAmbientSound() {
        SoundEvent lv = this.getAmbientSound();
        if (lv == SoundEvents.ENTITY_FOX_SCREECH) {
            this.playSound(lv, 2.0f, this.getSoundPitch());
        } else {
            super.playAmbientSound();
        }
    }

    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        List<Entity> list;
        if (this.isSleeping()) {
            return SoundEvents.ENTITY_FOX_SLEEP;
        }
        if (!this.getWorld().isDay() && this.random.nextFloat() < 0.1f && (list = this.getWorld().getEntitiesByClass(PlayerEntity.class, this.getBoundingBox().expand(16.0, 16.0, 16.0), EntityPredicates.EXCEPT_SPECTATOR)).isEmpty()) {
            return SoundEvents.ENTITY_FOX_SCREECH;
        }
        return SoundEvents.ENTITY_FOX_AMBIENT;
    }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_FOX_HURT;
    }

    @Override
    @Nullable
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_FOX_DEATH;
    }

    boolean canTrust(UUID uuid) {
        return this.getTrustedUuids().contains(uuid);
    }

    @Override
    protected void drop(ServerWorld world, DamageSource damageSource) {
        ItemStack lv = this.getEquippedStack(EquipmentSlot.MAINHAND);
        if (!lv.isEmpty()) {
            this.dropStack(lv);
            this.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
        super.drop(world, damageSource);
    }

    public static boolean canJumpChase(FoxEntity fox, LivingEntity chasedEntity) {
        double d = chasedEntity.getZ() - fox.getZ();
        double e = chasedEntity.getX() - fox.getX();
        double f = d / e;
        int i = 6;
        for (int j = 0; j < 6; ++j) {
            double g = f == 0.0 ? 0.0 : d * (double)((float)j / 6.0f);
            double h = f == 0.0 ? e * (double)((float)j / 6.0f) : g / f;
            for (int k = 1; k < 4; ++k) {
                if (fox.getWorld().getBlockState(BlockPos.ofFloored(fox.getX() + h, fox.getY() + (double)k, fox.getZ() + g)).isReplaceable()) continue;
                return false;
            }
        }
        return true;
    }

    @Override
    public Vec3d getLeashOffset() {
        return new Vec3d(0.0, 0.55f * this.getStandingEyeHeight(), this.getWidth() * 0.4f);
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

    public class FoxLookControl
    extends LookControl {
        public FoxLookControl() {
            super(FoxEntity.this);
        }

        @Override
        public void tick() {
            if (!FoxEntity.this.isSleeping()) {
                super.tick();
            }
        }

        @Override
        protected boolean shouldStayHorizontal() {
            return !FoxEntity.this.isChasing() && !FoxEntity.this.isInSneakingPose() && !FoxEntity.this.isRollingHead() && !FoxEntity.this.isWalking();
        }
    }

    class FoxMoveControl
    extends MoveControl {
        public FoxMoveControl() {
            super(FoxEntity.this);
        }

        @Override
        public void tick() {
            if (FoxEntity.this.wantsToPickupItem()) {
                super.tick();
            }
        }
    }

    class FoxSwimGoal
    extends SwimGoal {
        public FoxSwimGoal() {
            super(FoxEntity.this);
        }

        @Override
        public void start() {
            super.start();
            FoxEntity.this.stopActions();
        }

        @Override
        public boolean canStart() {
            return FoxEntity.this.isTouchingWater() && FoxEntity.this.getFluidHeight(FluidTags.WATER) > 0.25 || FoxEntity.this.isInLava();
        }
    }

    class StopWanderingGoal
    extends Goal {
        int timer;

        public StopWanderingGoal() {
            this.setControls(EnumSet.of(Goal.Control.LOOK, Goal.Control.JUMP, Goal.Control.MOVE));
        }

        @Override
        public boolean canStart() {
            return FoxEntity.this.isWalking();
        }

        @Override
        public boolean shouldContinue() {
            return this.canStart() && this.timer > 0;
        }

        @Override
        public void start() {
            this.timer = this.getTickCount(40);
        }

        @Override
        public void stop() {
            FoxEntity.this.setWalking(false);
        }

        @Override
        public void tick() {
            --this.timer;
        }
    }

    class EscapeWhenNotAggressiveGoal
    extends EscapeDangerGoal {
        public EscapeWhenNotAggressiveGoal(double speed) {
            super(FoxEntity.this, speed);
        }

        @Override
        public boolean isInDanger() {
            return !FoxEntity.this.isAggressive() && super.isInDanger();
        }
    }

    class MateGoal
    extends AnimalMateGoal {
        public MateGoal(FoxEntity fox, double chance) {
            super(fox, chance);
        }

        @Override
        public void start() {
            ((FoxEntity)this.animal).stopActions();
            ((FoxEntity)this.mate).stopActions();
            super.start();
        }

        @Override
        protected void breed() {
            ServerWorld lv = (ServerWorld)this.world;
            FoxEntity lv2 = (FoxEntity)this.animal.createChild(lv, this.mate);
            if (lv2 == null) {
                return;
            }
            ServerPlayerEntity lv3 = this.animal.getLovingPlayer();
            ServerPlayerEntity lv4 = this.mate.getLovingPlayer();
            ServerPlayerEntity lv5 = lv3;
            if (lv3 != null) {
                lv2.addTrustedUuid(lv3.getUuid());
            } else {
                lv5 = lv4;
            }
            if (lv4 != null && lv3 != lv4) {
                lv2.addTrustedUuid(lv4.getUuid());
            }
            if (lv5 != null) {
                lv5.incrementStat(Stats.ANIMALS_BRED);
                Criteria.BRED_ANIMALS.trigger(lv5, this.animal, this.mate, lv2);
            }
            this.animal.setBreedingAge(6000);
            this.mate.setBreedingAge(6000);
            this.animal.resetLoveTicks();
            this.mate.resetLoveTicks();
            lv2.setBreedingAge(-24000);
            lv2.refreshPositionAndAngles(this.animal.getX(), this.animal.getY(), this.animal.getZ(), 0.0f, 0.0f);
            lv.spawnEntityAndPassengers(lv2);
            this.world.sendEntityStatus(this.animal, EntityStatuses.ADD_BREEDING_PARTICLES);
            if (this.world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) {
                this.world.spawnEntity(new ExperienceOrbEntity(this.world, this.animal.getX(), this.animal.getY(), this.animal.getZ(), this.animal.getRandom().nextInt(7) + 1));
            }
        }
    }

    class MoveToHuntGoal
    extends Goal {
        public MoveToHuntGoal() {
            this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
        }

        @Override
        public boolean canStart() {
            if (FoxEntity.this.isSleeping()) {
                return false;
            }
            LivingEntity lv = FoxEntity.this.getTarget();
            return lv != null && lv.isAlive() && CHICKEN_AND_RABBIT_FILTER.test(lv) && FoxEntity.this.squaredDistanceTo(lv) > 36.0 && !FoxEntity.this.isInSneakingPose() && !FoxEntity.this.isRollingHead() && !FoxEntity.this.jumping;
        }

        @Override
        public void start() {
            FoxEntity.this.setSitting(false);
            FoxEntity.this.setWalking(false);
        }

        @Override
        public void stop() {
            LivingEntity lv = FoxEntity.this.getTarget();
            if (lv != null && FoxEntity.canJumpChase(FoxEntity.this, lv)) {
                FoxEntity.this.setRollingHead(true);
                FoxEntity.this.setCrouching(true);
                FoxEntity.this.getNavigation().stop();
                FoxEntity.this.getLookControl().lookAt(lv, FoxEntity.this.getMaxHeadRotation(), FoxEntity.this.getMaxLookPitchChange());
            } else {
                FoxEntity.this.setRollingHead(false);
                FoxEntity.this.setCrouching(false);
            }
        }

        @Override
        public void tick() {
            LivingEntity lv = FoxEntity.this.getTarget();
            if (lv == null) {
                return;
            }
            FoxEntity.this.getLookControl().lookAt(lv, FoxEntity.this.getMaxHeadRotation(), FoxEntity.this.getMaxLookPitchChange());
            if (FoxEntity.this.squaredDistanceTo(lv) <= 36.0) {
                FoxEntity.this.setRollingHead(true);
                FoxEntity.this.setCrouching(true);
                FoxEntity.this.getNavigation().stop();
            } else {
                FoxEntity.this.getNavigation().startMovingTo(lv, 1.5);
            }
        }
    }

    public class JumpChasingGoal
    extends DiveJumpingGoal {
        @Override
        public boolean canStart() {
            if (!FoxEntity.this.isFullyCrouched()) {
                return false;
            }
            LivingEntity lv = FoxEntity.this.getTarget();
            if (lv == null || !lv.isAlive()) {
                return false;
            }
            if (lv.getMovementDirection() != lv.getHorizontalFacing()) {
                return false;
            }
            boolean bl = FoxEntity.canJumpChase(FoxEntity.this, lv);
            if (!bl) {
                FoxEntity.this.getNavigation().findPathTo(lv, 0);
                FoxEntity.this.setCrouching(false);
                FoxEntity.this.setRollingHead(false);
            }
            return bl;
        }

        @Override
        public boolean shouldContinue() {
            LivingEntity lv = FoxEntity.this.getTarget();
            if (lv == null || !lv.isAlive()) {
                return false;
            }
            double d = FoxEntity.this.getVelocity().y;
            return !(d * d < (double)0.05f && Math.abs(FoxEntity.this.getPitch()) < 15.0f && FoxEntity.this.isOnGround() || FoxEntity.this.isWalking());
        }

        @Override
        public boolean canStop() {
            return false;
        }

        @Override
        public void start() {
            FoxEntity.this.setJumping(true);
            FoxEntity.this.setChasing(true);
            FoxEntity.this.setRollingHead(false);
            LivingEntity lv = FoxEntity.this.getTarget();
            if (lv != null) {
                FoxEntity.this.getLookControl().lookAt(lv, 60.0f, 30.0f);
                Vec3d lv2 = new Vec3d(lv.getX() - FoxEntity.this.getX(), lv.getY() - FoxEntity.this.getY(), lv.getZ() - FoxEntity.this.getZ()).normalize();
                FoxEntity.this.setVelocity(FoxEntity.this.getVelocity().add(lv2.x * 0.8, 0.9, lv2.z * 0.8));
            }
            FoxEntity.this.getNavigation().stop();
        }

        @Override
        public void stop() {
            FoxEntity.this.setCrouching(false);
            FoxEntity.this.extraRollingHeight = 0.0f;
            FoxEntity.this.lastExtraRollingHeight = 0.0f;
            FoxEntity.this.setRollingHead(false);
            FoxEntity.this.setChasing(false);
        }

        @Override
        public void tick() {
            LivingEntity lv = FoxEntity.this.getTarget();
            if (lv != null) {
                FoxEntity.this.getLookControl().lookAt(lv, 60.0f, 30.0f);
            }
            if (!FoxEntity.this.isWalking()) {
                Vec3d lv2 = FoxEntity.this.getVelocity();
                if (lv2.y * lv2.y < (double)0.03f && FoxEntity.this.getPitch() != 0.0f) {
                    FoxEntity.this.setPitch(MathHelper.lerpAngleDegrees(0.2f, FoxEntity.this.getPitch(), 0.0f));
                } else {
                    double d = lv2.horizontalLength();
                    double e = Math.signum(-lv2.y) * Math.acos(d / lv2.length()) * 57.2957763671875;
                    FoxEntity.this.setPitch((float)e);
                }
            }
            if (lv != null && FoxEntity.this.distanceTo(lv) <= 2.0f) {
                FoxEntity.this.tryAttack(lv);
            } else if (FoxEntity.this.getPitch() > 0.0f && FoxEntity.this.isOnGround() && (float)FoxEntity.this.getVelocity().y != 0.0f && FoxEntity.this.getWorld().getBlockState(FoxEntity.this.getBlockPos()).isOf(Blocks.SNOW)) {
                FoxEntity.this.setPitch(60.0f);
                FoxEntity.this.setTarget(null);
                FoxEntity.this.setWalking(true);
            }
        }
    }

    class AvoidDaylightGoal
    extends EscapeSunlightGoal {
        private int timer;

        public AvoidDaylightGoal(double speed) {
            super(FoxEntity.this, speed);
            this.timer = AvoidDaylightGoal.toGoalTicks(100);
        }

        @Override
        public boolean canStart() {
            if (FoxEntity.this.isSleeping() || this.mob.getTarget() != null) {
                return false;
            }
            if (FoxEntity.this.getWorld().isThundering() && FoxEntity.this.getWorld().isSkyVisible(this.mob.getBlockPos())) {
                return this.targetShadedPos();
            }
            if (this.timer > 0) {
                --this.timer;
                return false;
            }
            this.timer = 100;
            BlockPos lv = this.mob.getBlockPos();
            return FoxEntity.this.getWorld().isDay() && FoxEntity.this.getWorld().isSkyVisible(lv) && !((ServerWorld)FoxEntity.this.getWorld()).isNearOccupiedPointOfInterest(lv) && this.targetShadedPos();
        }

        @Override
        public void start() {
            FoxEntity.this.stopActions();
            super.start();
        }
    }

    class AttackGoal
    extends MeleeAttackGoal {
        public AttackGoal(double speed, boolean pauseWhenIdle) {
            super(FoxEntity.this, speed, pauseWhenIdle);
        }

        @Override
        protected void attack(LivingEntity target) {
            if (this.canAttack(target)) {
                this.resetCooldown();
                this.mob.tryAttack(target);
                FoxEntity.this.playSound(SoundEvents.ENTITY_FOX_BITE, 1.0f, 1.0f);
            }
        }

        @Override
        public void start() {
            FoxEntity.this.setRollingHead(false);
            super.start();
        }

        @Override
        public boolean canStart() {
            return !FoxEntity.this.isSitting() && !FoxEntity.this.isSleeping() && !FoxEntity.this.isInSneakingPose() && !FoxEntity.this.isWalking() && super.canStart();
        }
    }

    class DelayedCalmDownGoal
    extends CalmDownGoal {
        private static final int MAX_CALM_DOWN_TIME = DelayedCalmDownGoal.toGoalTicks(140);
        private int timer;

        public DelayedCalmDownGoal() {
            this.timer = FoxEntity.this.random.nextInt(MAX_CALM_DOWN_TIME);
            this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK, Goal.Control.JUMP));
        }

        @Override
        public boolean canStart() {
            if (FoxEntity.this.sidewaysSpeed != 0.0f || FoxEntity.this.upwardSpeed != 0.0f || FoxEntity.this.forwardSpeed != 0.0f) {
                return false;
            }
            return this.canNotCalmDown() || FoxEntity.this.isSleeping();
        }

        @Override
        public boolean shouldContinue() {
            return this.canNotCalmDown();
        }

        private boolean canNotCalmDown() {
            if (this.timer > 0) {
                --this.timer;
                return false;
            }
            return FoxEntity.this.getWorld().isDay() && this.isAtFavoredLocation() && !this.canCalmDown() && !FoxEntity.this.inPowderSnow;
        }

        @Override
        public void stop() {
            this.timer = FoxEntity.this.random.nextInt(MAX_CALM_DOWN_TIME);
            FoxEntity.this.stopActions();
        }

        @Override
        public void start() {
            FoxEntity.this.setSitting(false);
            FoxEntity.this.setCrouching(false);
            FoxEntity.this.setRollingHead(false);
            FoxEntity.this.setJumping(false);
            FoxEntity.this.setSleeping(true);
            FoxEntity.this.getNavigation().stop();
            FoxEntity.this.getMoveControl().moveTo(FoxEntity.this.getX(), FoxEntity.this.getY(), FoxEntity.this.getZ(), 0.0);
        }
    }

    class FollowParentGoal
    extends net.minecraft.entity.ai.goal.FollowParentGoal {
        private final FoxEntity fox;

        public FollowParentGoal(FoxEntity arg, FoxEntity fox, double speed) {
            super(fox, speed);
            this.fox = fox;
        }

        @Override
        public boolean canStart() {
            return !this.fox.isAggressive() && super.canStart();
        }

        @Override
        public boolean shouldContinue() {
            return !this.fox.isAggressive() && super.shouldContinue();
        }

        @Override
        public void start() {
            this.fox.stopActions();
            super.start();
        }
    }

    class GoToVillageGoal
    extends net.minecraft.entity.ai.goal.GoToVillageGoal {
        public GoToVillageGoal(int unused, int searchRange) {
            super(FoxEntity.this, searchRange);
        }

        @Override
        public void start() {
            FoxEntity.this.stopActions();
            super.start();
        }

        @Override
        public boolean canStart() {
            return super.canStart() && this.canGoToVillage();
        }

        @Override
        public boolean shouldContinue() {
            return super.shouldContinue() && this.canGoToVillage();
        }

        private boolean canGoToVillage() {
            return !FoxEntity.this.isSleeping() && !FoxEntity.this.isSitting() && !FoxEntity.this.isAggressive() && FoxEntity.this.getTarget() == null;
        }
    }

    public class EatBerriesGoal
    extends MoveToTargetPosGoal {
        private static final int EATING_TIME = 40;
        protected int timer;

        public EatBerriesGoal(double speed, int range, int maxYDifference) {
            super(FoxEntity.this, speed, range, maxYDifference);
        }

        @Override
        public double getDesiredDistanceToTarget() {
            return 2.0;
        }

        @Override
        public boolean shouldResetPath() {
            return this.tryingTime % 100 == 0;
        }

        @Override
        protected boolean isTargetPos(WorldView world, BlockPos pos) {
            BlockState lv = world.getBlockState(pos);
            return lv.isOf(Blocks.SWEET_BERRY_BUSH) && lv.get(SweetBerryBushBlock.AGE) >= 2 || CaveVines.hasBerries(lv);
        }

        @Override
        public void tick() {
            if (this.hasReached()) {
                if (this.timer >= 40) {
                    this.eatBerries();
                } else {
                    ++this.timer;
                }
            } else if (!this.hasReached() && FoxEntity.this.random.nextFloat() < 0.05f) {
                FoxEntity.this.playSound(SoundEvents.ENTITY_FOX_SNIFF, 1.0f, 1.0f);
            }
            super.tick();
        }

        protected void eatBerries() {
            if (!FoxEntity.this.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
                return;
            }
            BlockState lv = FoxEntity.this.getWorld().getBlockState(this.targetPos);
            if (lv.isOf(Blocks.SWEET_BERRY_BUSH)) {
                this.pickSweetBerries(lv);
            } else if (CaveVines.hasBerries(lv)) {
                this.pickGlowBerries(lv);
            }
        }

        private void pickGlowBerries(BlockState state) {
            CaveVines.pickBerries(FoxEntity.this, state, FoxEntity.this.getWorld(), this.targetPos);
        }

        private void pickSweetBerries(BlockState state) {
            int i = state.get(SweetBerryBushBlock.AGE);
            state.with(SweetBerryBushBlock.AGE, 1);
            int j = 1 + FoxEntity.this.getWorld().random.nextInt(2) + (i == 3 ? 1 : 0);
            ItemStack lv = FoxEntity.this.getEquippedStack(EquipmentSlot.MAINHAND);
            if (lv.isEmpty()) {
                FoxEntity.this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.SWEET_BERRIES));
                --j;
            }
            if (j > 0) {
                Block.dropStack(FoxEntity.this.getWorld(), this.targetPos, new ItemStack(Items.SWEET_BERRIES, j));
            }
            FoxEntity.this.playSound(SoundEvents.BLOCK_SWEET_BERRY_BUSH_PICK_BERRIES, 1.0f, 1.0f);
            FoxEntity.this.getWorld().setBlockState(this.targetPos, (BlockState)state.with(SweetBerryBushBlock.AGE, 1), Block.NOTIFY_LISTENERS);
            FoxEntity.this.getWorld().emitGameEvent(GameEvent.BLOCK_CHANGE, this.targetPos, GameEvent.Emitter.of(FoxEntity.this));
        }

        @Override
        public boolean canStart() {
            return !FoxEntity.this.isSleeping() && super.canStart();
        }

        @Override
        public void start() {
            this.timer = 0;
            FoxEntity.this.setSitting(false);
            super.start();
        }
    }

    class PickupItemGoal
    extends Goal {
        public PickupItemGoal() {
            this.setControls(EnumSet.of(Goal.Control.MOVE));
        }

        @Override
        public boolean canStart() {
            if (!FoxEntity.this.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty()) {
                return false;
            }
            if (FoxEntity.this.getTarget() != null || FoxEntity.this.getAttacker() != null) {
                return false;
            }
            if (!FoxEntity.this.wantsToPickupItem()) {
                return false;
            }
            if (FoxEntity.this.getRandom().nextInt(PickupItemGoal.toGoalTicks(10)) != 0) {
                return false;
            }
            List<ItemEntity> list = FoxEntity.this.getWorld().getEntitiesByClass(ItemEntity.class, FoxEntity.this.getBoundingBox().expand(8.0, 8.0, 8.0), PICKABLE_DROP_FILTER);
            return !list.isEmpty() && FoxEntity.this.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty();
        }

        @Override
        public void tick() {
            List<ItemEntity> list = FoxEntity.this.getWorld().getEntitiesByClass(ItemEntity.class, FoxEntity.this.getBoundingBox().expand(8.0, 8.0, 8.0), PICKABLE_DROP_FILTER);
            ItemStack lv = FoxEntity.this.getEquippedStack(EquipmentSlot.MAINHAND);
            if (lv.isEmpty() && !list.isEmpty()) {
                FoxEntity.this.getNavigation().startMovingTo(list.get(0), 1.2f);
            }
        }

        @Override
        public void start() {
            List<ItemEntity> list = FoxEntity.this.getWorld().getEntitiesByClass(ItemEntity.class, FoxEntity.this.getBoundingBox().expand(8.0, 8.0, 8.0), PICKABLE_DROP_FILTER);
            if (!list.isEmpty()) {
                FoxEntity.this.getNavigation().startMovingTo(list.get(0), 1.2f);
            }
        }
    }

    class LookAtEntityGoal
    extends net.minecraft.entity.ai.goal.LookAtEntityGoal {
        public LookAtEntityGoal(MobEntity fox, Class<? extends LivingEntity> targetType, float range) {
            super(fox, targetType, range);
        }

        @Override
        public boolean canStart() {
            return super.canStart() && !FoxEntity.this.isWalking() && !FoxEntity.this.isRollingHead();
        }

        @Override
        public boolean shouldContinue() {
            return super.shouldContinue() && !FoxEntity.this.isWalking() && !FoxEntity.this.isRollingHead();
        }
    }

    class SitDownAndLookAroundGoal
    extends CalmDownGoal {
        private double lookX;
        private double lookZ;
        private int timer;
        private int counter;

        public SitDownAndLookAroundGoal() {
            this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
        }

        @Override
        public boolean canStart() {
            return FoxEntity.this.getAttacker() == null && FoxEntity.this.getRandom().nextFloat() < 0.02f && !FoxEntity.this.isSleeping() && FoxEntity.this.getTarget() == null && FoxEntity.this.getNavigation().isIdle() && !this.canCalmDown() && !FoxEntity.this.isChasing() && !FoxEntity.this.isInSneakingPose();
        }

        @Override
        public boolean shouldContinue() {
            return this.counter > 0;
        }

        @Override
        public void start() {
            this.chooseNewAngle();
            this.counter = 2 + FoxEntity.this.getRandom().nextInt(3);
            FoxEntity.this.setSitting(true);
            FoxEntity.this.getNavigation().stop();
        }

        @Override
        public void stop() {
            FoxEntity.this.setSitting(false);
        }

        @Override
        public void tick() {
            --this.timer;
            if (this.timer <= 0) {
                --this.counter;
                this.chooseNewAngle();
            }
            FoxEntity.this.getLookControl().lookAt(FoxEntity.this.getX() + this.lookX, FoxEntity.this.getEyeY(), FoxEntity.this.getZ() + this.lookZ, FoxEntity.this.getMaxHeadRotation(), FoxEntity.this.getMaxLookPitchChange());
        }

        private void chooseNewAngle() {
            double d = Math.PI * 2 * FoxEntity.this.getRandom().nextDouble();
            this.lookX = Math.cos(d);
            this.lookZ = Math.sin(d);
            this.timer = this.getTickCount(80 + FoxEntity.this.getRandom().nextInt(20));
        }
    }

    class DefendFriendGoal
    extends ActiveTargetGoal<LivingEntity> {
        @Nullable
        private LivingEntity offender;
        @Nullable
        private LivingEntity friend;
        private int lastAttackedTime;

        public DefendFriendGoal(Class<LivingEntity> targetEntityClass, boolean checkVisibility, @Nullable boolean checkCanNavigate, Predicate<LivingEntity> targetPredicate) {
            super(FoxEntity.this, targetEntityClass, 10, checkVisibility, checkCanNavigate, targetPredicate);
        }

        @Override
        public boolean canStart() {
            if (this.reciprocalChance > 0 && this.mob.getRandom().nextInt(this.reciprocalChance) != 0) {
                return false;
            }
            for (UUID uUID : FoxEntity.this.getTrustedUuids()) {
                LivingEntity lv2;
                Entity lv;
                if (uUID == null || !(FoxEntity.this.getWorld() instanceof ServerWorld) || !((lv = ((ServerWorld)FoxEntity.this.getWorld()).getEntity(uUID)) instanceof LivingEntity)) continue;
                this.friend = lv2 = (LivingEntity)lv;
                this.offender = lv2.getAttacker();
                int i = lv2.getLastAttackedTime();
                return i != this.lastAttackedTime && this.canTrack(this.offender, this.targetPredicate);
            }
            return false;
        }

        @Override
        public void start() {
            this.setTargetEntity(this.offender);
            this.targetEntity = this.offender;
            if (this.friend != null) {
                this.lastAttackedTime = this.friend.getLastAttackedTime();
            }
            FoxEntity.this.playSound(SoundEvents.ENTITY_FOX_AGGRO, 1.0f, 1.0f);
            FoxEntity.this.setAggressive(true);
            FoxEntity.this.stopSleeping();
            super.start();
        }
    }

    public static enum Type implements StringIdentifiable
    {
        RED(0, "red"),
        SNOW(1, "snow");

        public static final StringIdentifiable.EnumCodec<Type> CODEC;
        private static final IntFunction<Type> BY_ID;
        private final int id;
        private final String key;

        private Type(int id, String key) {
            this.id = id;
            this.key = key;
        }

        @Override
        public String asString() {
            return this.key;
        }

        public int getId() {
            return this.id;
        }

        public static Type byName(String name) {
            return CODEC.byId(name, RED);
        }

        public static Type fromId(int id) {
            return BY_ID.apply(id);
        }

        public static Type fromBiome(RegistryEntry<Biome> biome) {
            return biome.isIn(BiomeTags.SPAWNS_SNOW_FOXES) ? SNOW : RED;
        }

        static {
            CODEC = StringIdentifiable.createCodec(Type::values);
            BY_ID = ValueLists.createIdToValueFunction(Type::getId, Type.values(), ValueLists.OutOfBoundsHandling.ZERO);
        }
    }

    public static class FoxData
    extends PassiveEntity.PassiveData {
        public final Type type;

        public FoxData(Type type) {
            super(false);
            this.type = type;
        }
    }

    abstract class CalmDownGoal
    extends Goal {
        private final TargetPredicate WORRIABLE_ENTITY_PREDICATE;

        CalmDownGoal() {
            this.WORRIABLE_ENTITY_PREDICATE = TargetPredicate.createAttackable().setBaseMaxDistance(12.0).ignoreVisibility().setPredicate(new WorriableEntityFilter());
        }

        protected boolean isAtFavoredLocation() {
            BlockPos lv = BlockPos.ofFloored(FoxEntity.this.getX(), FoxEntity.this.getBoundingBox().maxY, FoxEntity.this.getZ());
            return !FoxEntity.this.getWorld().isSkyVisible(lv) && FoxEntity.this.getPathfindingFavor(lv) >= 0.0f;
        }

        protected boolean canCalmDown() {
            return !FoxEntity.this.getWorld().getTargets(LivingEntity.class, this.WORRIABLE_ENTITY_PREDICATE, FoxEntity.this, FoxEntity.this.getBoundingBox().expand(12.0, 6.0, 12.0)).isEmpty();
        }
    }

    public class WorriableEntityFilter
    implements Predicate<LivingEntity> {
        @Override
        public boolean test(LivingEntity arg) {
            if (arg instanceof FoxEntity) {
                return false;
            }
            if (arg instanceof ChickenEntity || arg instanceof RabbitEntity || arg instanceof HostileEntity) {
                return true;
            }
            if (arg instanceof TameableEntity) {
                return !((TameableEntity)arg).isTamed();
            }
            if (arg instanceof PlayerEntity && (arg.isSpectator() || ((PlayerEntity)arg).isCreative())) {
                return false;
            }
            if (FoxEntity.this.canTrust(arg.getUuid())) {
                return false;
            }
            return !arg.isSleeping() && !arg.isSneaky();
        }

        @Override
        public /* synthetic */ boolean test(Object entity) {
            return this.test((LivingEntity)entity);
        }
    }
}

