/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.passive;

import com.mojang.serialization.Codec;
import java.util.function.IntFunction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DyedCarpetBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAttachmentType;
import net.minecraft.entity.EntityAttachments;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.VariantHolder;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.FollowParentGoal;
import net.minecraft.entity.ai.goal.FormCaravanGoal;
import net.minecraft.entity.ai.goal.HorseBondWithPlayerGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.ProjectileAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.LlamaSpitEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.DyeColor;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;
import net.minecraft.util.function.ValueLists;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class LlamaEntity
extends AbstractDonkeyEntity
implements VariantHolder<Variant>,
RangedAttackMob {
    private static final int MAX_STRENGTH = 5;
    private static final TrackedData<Integer> STRENGTH = DataTracker.registerData(LlamaEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> VARIANT = DataTracker.registerData(LlamaEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final EntityDimensions BABY_BASE_DIMENSIONS = EntityType.LLAMA.getDimensions().withAttachments(EntityAttachments.builder().add(EntityAttachmentType.PASSENGER, 0.0f, EntityType.LLAMA.getHeight() - 0.8125f, -0.3f)).scaled(0.5f);
    boolean spit;
    @Nullable
    private LlamaEntity following;
    @Nullable
    private LlamaEntity follower;

    public LlamaEntity(EntityType<? extends LlamaEntity> arg, World arg2) {
        super((EntityType<? extends AbstractDonkeyEntity>)arg, arg2);
    }

    public boolean isTrader() {
        return false;
    }

    private void setStrength(int strength) {
        this.dataTracker.set(STRENGTH, Math.max(1, Math.min(5, strength)));
    }

    private void initializeStrength(Random random) {
        int i = random.nextFloat() < 0.04f ? 5 : 3;
        this.setStrength(1 + random.nextInt(i));
    }

    public int getStrength() {
        return this.dataTracker.get(STRENGTH);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("Variant", this.getVariant().id);
        nbt.putInt("Strength", this.getStrength());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        this.setStrength(nbt.getInt("Strength"));
        super.readCustomDataFromNbt(nbt);
        this.setVariant(Variant.byId(nbt.getInt("Variant")));
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new HorseBondWithPlayerGoal(this, 1.2));
        this.goalSelector.add(2, new FormCaravanGoal(this, 2.1f));
        this.goalSelector.add(3, new ProjectileAttackGoal(this, 1.25, 40, 20.0f));
        this.goalSelector.add(3, new EscapeDangerGoal(this, 1.2));
        this.goalSelector.add(4, new AnimalMateGoal(this, 1.0));
        this.goalSelector.add(5, new TemptGoal(this, 1.25, stack -> stack.isIn(ItemTags.LLAMA_TEMPT_ITEMS), false));
        this.goalSelector.add(6, new FollowParentGoal(this, 1.0));
        this.goalSelector.add(7, new WanderAroundFarGoal(this, 0.7));
        this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 6.0f));
        this.goalSelector.add(9, new LookAroundGoal(this));
        this.targetSelector.add(1, new SpitRevengeGoal(this));
        this.targetSelector.add(2, new ChaseWolvesGoal(this));
    }

    public static DefaultAttributeContainer.Builder createLlamaAttributes() {
        return LlamaEntity.createAbstractDonkeyAttributes().add(EntityAttributes.GENERIC_FOLLOW_RANGE, 40.0);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(STRENGTH, 0);
        builder.add(VARIANT, 0);
    }

    @Override
    public Variant getVariant() {
        return Variant.byId(this.dataTracker.get(VARIANT));
    }

    @Override
    public void setVariant(Variant arg) {
        this.dataTracker.set(VARIANT, arg.id);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.isIn(ItemTags.LLAMA_FOOD);
    }

    @Override
    protected boolean receiveFood(PlayerEntity player, ItemStack item) {
        SoundEvent lv;
        int i = 0;
        int j = 0;
        float f = 0.0f;
        boolean bl = false;
        if (item.isOf(Items.WHEAT)) {
            i = 10;
            j = 3;
            f = 2.0f;
        } else if (item.isOf(Blocks.HAY_BLOCK.asItem())) {
            i = 90;
            j = 6;
            f = 10.0f;
            if (this.isTame() && this.getBreedingAge() == 0 && this.canEat()) {
                bl = true;
                this.lovePlayer(player);
            }
        }
        if (this.getHealth() < this.getMaxHealth() && f > 0.0f) {
            this.heal(f);
            bl = true;
        }
        if (this.isBaby() && i > 0) {
            ((World)this.getWorld()).addParticle(ParticleTypes.HAPPY_VILLAGER, this.getParticleX(1.0), this.getRandomBodyY() + 0.5, this.getParticleZ(1.0), 0.0, 0.0, 0.0);
            if (!((World)this.getWorld()).isClient) {
                this.growUp(i);
            }
            bl = true;
        }
        if (j > 0 && (bl || !this.isTame()) && this.getTemper() < this.getMaxTemper()) {
            bl = true;
            if (!((World)this.getWorld()).isClient) {
                this.addTemper(j);
            }
        }
        if (bl && !this.isSilent() && (lv = this.getEatSound()) != null) {
            ((World)this.getWorld()).playSound(null, this.getX(), this.getY(), this.getZ(), this.getEatSound(), this.getSoundCategory(), 1.0f, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.2f);
        }
        return bl;
    }

    @Override
    public boolean isImmobile() {
        return this.isDead() || this.isEatingGrass();
    }

    @Override
    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        Variant lv2;
        Random lv = world.getRandom();
        this.initializeStrength(lv);
        if (entityData instanceof LlamaData) {
            lv2 = ((LlamaData)entityData).variant;
        } else {
            lv2 = Util.getRandom(Variant.values(), lv);
            entityData = new LlamaData(lv2);
        }
        this.setVariant(lv2);
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    @Override
    protected boolean shouldAmbientStand() {
        return false;
    }

    @Override
    protected SoundEvent getAngrySound() {
        return SoundEvents.ENTITY_LLAMA_ANGRY;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_LLAMA_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_LLAMA_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_LLAMA_DEATH;
    }

    @Override
    @Nullable
    protected SoundEvent getEatSound() {
        return SoundEvents.ENTITY_LLAMA_EAT;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.ENTITY_LLAMA_STEP, 0.15f, 1.0f);
    }

    @Override
    protected void playAddChestSound() {
        this.playSound(SoundEvents.ENTITY_LLAMA_CHEST, 1.0f, (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
    }

    @Override
    public int getInventoryColumns() {
        return this.hasChest() ? this.getStrength() : 0;
    }

    @Override
    public boolean canUseSlot(EquipmentSlot slot) {
        return true;
    }

    @Override
    public boolean isHorseArmor(ItemStack stack) {
        return stack.isIn(ItemTags.WOOL_CARPETS);
    }

    @Override
    public boolean canBeSaddled() {
        return false;
    }

    @Nullable
    private static DyeColor getColorFromCarpet(ItemStack color) {
        Block lv = Block.getBlockFromItem(color.getItem());
        if (lv instanceof DyedCarpetBlock) {
            return ((DyedCarpetBlock)lv).getDyeColor();
        }
        return null;
    }

    @Nullable
    public DyeColor getCarpetColor() {
        return LlamaEntity.getColorFromCarpet(this.getEquippedStack(EquipmentSlot.BODY));
    }

    @Override
    public int getMaxTemper() {
        return 30;
    }

    @Override
    public boolean canBreedWith(AnimalEntity other) {
        return other != this && other instanceof LlamaEntity && this.canBreed() && ((LlamaEntity)other).canBreed();
    }

    @Override
    @Nullable
    public LlamaEntity createChild(ServerWorld arg, PassiveEntity arg2) {
        LlamaEntity lv = this.createChild();
        if (lv != null) {
            this.setChildAttributes(arg2, lv);
            LlamaEntity lv2 = (LlamaEntity)arg2;
            int i = this.random.nextInt(Math.max(this.getStrength(), lv2.getStrength())) + 1;
            if (this.random.nextFloat() < 0.03f) {
                ++i;
            }
            lv.setStrength(i);
            lv.setVariant(this.random.nextBoolean() ? this.getVariant() : lv2.getVariant());
        }
        return lv;
    }

    @Nullable
    protected LlamaEntity createChild() {
        return EntityType.LLAMA.create((World)this.getWorld());
    }

    private void spitAt(LivingEntity target) {
        LlamaSpitEntity lv = new LlamaSpitEntity((World)this.getWorld(), this);
        double d = target.getX() - this.getX();
        double e = target.getBodyY(0.3333333333333333) - lv.getY();
        double f = target.getZ() - this.getZ();
        double g = Math.sqrt(d * d + f * f) * (double)0.2f;
        lv.setVelocity(d, e + g, f, 1.5f, 10.0f);
        if (!this.isSilent()) {
            ((World)this.getWorld()).playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_LLAMA_SPIT, this.getSoundCategory(), 1.0f, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.2f);
        }
        this.getWorld().spawnEntity(lv);
        this.spit = true;
    }

    void setSpit(boolean spit) {
        this.spit = spit;
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        int i = this.computeFallDamage(fallDistance, damageMultiplier);
        if (i <= 0) {
            return false;
        }
        if (fallDistance >= 6.0f) {
            this.damage(damageSource, i);
            if (this.hasPassengers()) {
                for (Entity lv : this.getPassengersDeep()) {
                    lv.damage(damageSource, i);
                }
            }
        }
        this.playBlockFallSound();
        return true;
    }

    public void stopFollowing() {
        if (this.following != null) {
            this.following.follower = null;
        }
        this.following = null;
    }

    public void follow(LlamaEntity llama) {
        this.following = llama;
        this.following.follower = this;
    }

    public boolean hasFollower() {
        return this.follower != null;
    }

    public boolean isFollowing() {
        return this.following != null;
    }

    @Nullable
    public LlamaEntity getFollowing() {
        return this.following;
    }

    @Override
    protected double getFollowLeashSpeed() {
        return 2.0;
    }

    @Override
    protected void walkToParent() {
        if (!this.isFollowing() && this.isBaby()) {
            super.walkToParent();
        }
    }

    @Override
    public boolean eatsGrass() {
        return false;
    }

    @Override
    public void shootAt(LivingEntity target, float pullProgress) {
        this.spitAt(target);
    }

    @Override
    public Vec3d getLeashOffset() {
        return new Vec3d(0.0, 0.75 * (double)this.getStandingEyeHeight(), (double)this.getWidth() * 0.5);
    }

    @Override
    public EntityDimensions getBaseDimensions(EntityPose pose) {
        return this.isBaby() ? BABY_BASE_DIMENSIONS : super.getBaseDimensions(pose);
    }

    @Override
    protected Vec3d getPassengerAttachmentPos(Entity passenger, EntityDimensions dimensions, float scaleFactor) {
        return LlamaEntity.getPassengerAttachmentPos(this, passenger, dimensions.attachments());
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

    public static enum Variant implements StringIdentifiable
    {
        CREAMY(0, "creamy"),
        WHITE(1, "white"),
        BROWN(2, "brown"),
        GRAY(3, "gray");

        public static final Codec<Variant> CODEC;
        private static final IntFunction<Variant> BY_ID;
        final int id;
        private final String name;

        private Variant(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getIndex() {
            return this.id;
        }

        public static Variant byId(int id) {
            return BY_ID.apply(id);
        }

        @Override
        public String asString() {
            return this.name;
        }

        static {
            CODEC = StringIdentifiable.createCodec(Variant::values);
            BY_ID = ValueLists.createIdToValueFunction(Variant::getIndex, Variant.values(), ValueLists.OutOfBoundsHandling.CLAMP);
        }
    }

    static class SpitRevengeGoal
    extends RevengeGoal {
        public SpitRevengeGoal(LlamaEntity llama) {
            super(llama, new Class[0]);
        }

        @Override
        public boolean shouldContinue() {
            MobEntity mobEntity = this.mob;
            if (mobEntity instanceof LlamaEntity) {
                LlamaEntity lv = (LlamaEntity)mobEntity;
                if (lv.spit) {
                    lv.setSpit(false);
                    return false;
                }
            }
            return super.shouldContinue();
        }
    }

    static class ChaseWolvesGoal
    extends ActiveTargetGoal<WolfEntity> {
        public ChaseWolvesGoal(LlamaEntity llama) {
            super(llama, WolfEntity.class, 16, false, true, wolf -> !((WolfEntity)wolf).isTamed());
        }

        @Override
        protected double getFollowRange() {
            return super.getFollowRange() * 0.25;
        }
    }

    static class LlamaData
    extends PassiveEntity.PassiveData {
        public final Variant variant;

        LlamaData(Variant variant) {
            super(true);
            this.variant = variant;
        }
    }
}

