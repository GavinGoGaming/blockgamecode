/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.passive;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemSteerable;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Saddleable;
import net.minecraft.entity.SaddledComponent;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.FollowParentGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
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
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class PigEntity
extends AnimalEntity
implements ItemSteerable,
Saddleable {
    private static final TrackedData<Boolean> SADDLED = DataTracker.registerData(PigEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> BOOST_TIME = DataTracker.registerData(PigEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private final SaddledComponent saddledComponent;

    public PigEntity(EntityType<? extends PigEntity> arg, World arg2) {
        super((EntityType<? extends AnimalEntity>)arg, arg2);
        this.saddledComponent = new SaddledComponent(this.dataTracker, BOOST_TIME, SADDLED);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new EscapeDangerGoal(this, 1.25));
        this.goalSelector.add(3, new AnimalMateGoal(this, 1.0));
        this.goalSelector.add(4, new TemptGoal(this, 1.2, stack -> stack.isOf(Items.CARROT_ON_A_STICK), false));
        this.goalSelector.add(4, new TemptGoal(this, 1.2, stack -> stack.isIn(ItemTags.PIG_FOOD), false));
        this.goalSelector.add(5, new FollowParentGoal(this, 1.1));
        this.goalSelector.add(6, new WanderAroundFarGoal(this, 1.0));
        this.goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 6.0f));
        this.goalSelector.add(8, new LookAroundGoal(this));
    }

    public static DefaultAttributeContainer.Builder createPigAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25);
    }

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        PlayerEntity lv;
        Entity entity;
        if (this.isSaddled() && (entity = this.getFirstPassenger()) instanceof PlayerEntity && (lv = (PlayerEntity)entity).isHolding(Items.CARROT_ON_A_STICK)) {
            return lv;
        }
        return super.getControllingPassenger();
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if (BOOST_TIME.equals(data) && this.getWorld().isClient) {
            this.saddledComponent.boost();
        }
        super.onTrackedDataSet(data);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(SADDLED, false);
        builder.add(BOOST_TIME, 0);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        this.saddledComponent.writeNbt(nbt);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.saddledComponent.readNbt(nbt);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_PIG_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_PIG_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_PIG_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.ENTITY_PIG_STEP, 0.15f, 1.0f);
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        boolean bl = this.isBreedingItem(player.getStackInHand(hand));
        if (!bl && this.isSaddled() && !this.hasPassengers() && !player.shouldCancelInteraction()) {
            if (!this.getWorld().isClient) {
                player.startRiding(this);
            }
            return ActionResult.success(this.getWorld().isClient);
        }
        ActionResult lv = super.interactMob(player, hand);
        if (!lv.isAccepted()) {
            ItemStack lv2 = player.getStackInHand(hand);
            if (lv2.isOf(Items.SADDLE)) {
                return lv2.useOnEntity(player, this, hand);
            }
            return ActionResult.PASS;
        }
        return lv;
    }

    @Override
    public boolean canBeSaddled() {
        return this.isAlive() && !this.isBaby();
    }

    @Override
    protected void dropInventory() {
        super.dropInventory();
        if (this.isSaddled()) {
            this.dropItem(Items.SADDLE);
        }
    }

    @Override
    public boolean isSaddled() {
        return this.saddledComponent.isSaddled();
    }

    @Override
    public void saddle(ItemStack stack, @Nullable SoundCategory soundCategory) {
        this.saddledComponent.setSaddled(true);
        if (soundCategory != null) {
            this.getWorld().playSoundFromEntity(null, this, SoundEvents.ENTITY_PIG_SADDLE, soundCategory, 0.5f, 1.0f);
        }
    }

    @Override
    public Vec3d updatePassengerForDismount(LivingEntity passenger) {
        Direction lv = this.getMovementDirection();
        if (lv.getAxis() == Direction.Axis.Y) {
            return super.updatePassengerForDismount(passenger);
        }
        int[][] is = Dismounting.getDismountOffsets(lv);
        BlockPos lv2 = this.getBlockPos();
        BlockPos.Mutable lv3 = new BlockPos.Mutable();
        for (EntityPose lv4 : passenger.getPoses()) {
            Box lv5 = passenger.getBoundingBox(lv4);
            for (int[] js : is) {
                lv3.set(lv2.getX() + js[0], lv2.getY(), lv2.getZ() + js[1]);
                double d = this.getWorld().getDismountHeight(lv3);
                if (!Dismounting.canDismountInBlock(d)) continue;
                Vec3d lv6 = Vec3d.ofCenter(lv3, d);
                if (!Dismounting.canPlaceEntityAt(this.getWorld(), passenger, lv5.offset(lv6))) continue;
                passenger.setPose(lv4);
                return lv6;
            }
        }
        return super.updatePassengerForDismount(passenger);
    }

    @Override
    public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
        if (world.getDifficulty() != Difficulty.PEACEFUL) {
            ZombifiedPiglinEntity lv = EntityType.ZOMBIFIED_PIGLIN.create(world);
            if (lv != null) {
                lv.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
                lv.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), this.getPitch());
                lv.setAiDisabled(this.isAiDisabled());
                lv.setBaby(this.isBaby());
                if (this.hasCustomName()) {
                    lv.setCustomName(this.getCustomName());
                    lv.setCustomNameVisible(this.isCustomNameVisible());
                }
                lv.setPersistent();
                world.spawnEntity(lv);
                this.discard();
            } else {
                super.onStruckByLightning(world, lightning);
            }
        } else {
            super.onStruckByLightning(world, lightning);
        }
    }

    @Override
    protected void tickControlled(PlayerEntity controllingPlayer, Vec3d movementInput) {
        super.tickControlled(controllingPlayer, movementInput);
        this.setRotation(controllingPlayer.getYaw(), controllingPlayer.getPitch() * 0.5f);
        this.bodyYaw = this.headYaw = this.getYaw();
        this.prevYaw = this.headYaw;
        this.saddledComponent.tickBoost();
    }

    @Override
    protected Vec3d getControlledMovementInput(PlayerEntity controllingPlayer, Vec3d movementInput) {
        return new Vec3d(0.0, 0.0, 1.0);
    }

    @Override
    protected float getSaddledSpeed(PlayerEntity controllingPlayer) {
        return (float)(this.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) * 0.225 * (double)this.saddledComponent.getMovementSpeedMultiplier());
    }

    @Override
    public boolean consumeOnAStickItem() {
        return this.saddledComponent.boost(this.getRandom());
    }

    @Override
    @Nullable
    public PigEntity createChild(ServerWorld arg, PassiveEntity arg2) {
        return EntityType.PIG.create(arg);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.isIn(ItemTags.PIG_FOOD);
    }

    @Override
    public Vec3d getLeashOffset() {
        return new Vec3d(0.0, 0.6f * this.getStandingEyeHeight(), this.getWidth() * 0.4f);
    }

    @Override
    @Nullable
    public /* synthetic */ PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return this.createChild(world, entity);
    }
}

