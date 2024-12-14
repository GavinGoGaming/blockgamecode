/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.mob;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.provider.EnchantmentProviders;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.UniversalAngerGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.EndermiteEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.Potions;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class EndermanEntity
extends HostileEntity
implements Angerable {
    private static final Identifier ATTACKING_SPEED_MODIFIER_ID = Identifier.ofVanilla("attacking");
    private static final EntityAttributeModifier ATTACKING_SPEED_BOOST = new EntityAttributeModifier(ATTACKING_SPEED_MODIFIER_ID, 0.15f, EntityAttributeModifier.Operation.ADD_VALUE);
    private static final int field_30462 = 400;
    private static final int field_30461 = 600;
    private static final TrackedData<Optional<BlockState>> CARRIED_BLOCK = DataTracker.registerData(EndermanEntity.class, TrackedDataHandlerRegistry.OPTIONAL_BLOCK_STATE);
    private static final TrackedData<Boolean> ANGRY = DataTracker.registerData(EndermanEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> PROVOKED = DataTracker.registerData(EndermanEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private int lastAngrySoundAge = Integer.MIN_VALUE;
    private int ageWhenTargetSet;
    private static final UniformIntProvider ANGER_TIME_RANGE = TimeHelper.betweenSeconds(20, 39);
    private int angerTime;
    @Nullable
    private UUID angryAt;

    public EndermanEntity(EntityType<? extends EndermanEntity> arg, World arg2) {
        super((EntityType<? extends HostileEntity>)arg, arg2);
        this.setPathfindingPenalty(PathNodeType.WATER, -1.0f);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new ChasePlayerGoal(this));
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.add(7, new WanderAroundFarGoal((PathAwareEntity)this, 1.0, 0.0f));
        this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(8, new LookAroundGoal(this));
        this.goalSelector.add(10, new PlaceBlockGoal(this));
        this.goalSelector.add(11, new PickUpBlockGoal(this));
        this.targetSelector.add(1, new TeleportTowardsPlayerGoal(this, this::shouldAngerAt));
        this.targetSelector.add(2, new RevengeGoal(this, new Class[0]));
        this.targetSelector.add(3, new ActiveTargetGoal<EndermiteEntity>((MobEntity)this, EndermiteEntity.class, true, false));
        this.targetSelector.add(4, new UniversalAngerGoal<EndermanEntity>(this, false));
    }

    public static DefaultAttributeContainer.Builder createEndermanAttributes() {
        return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 40.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3f).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 7.0).add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64.0).add(EntityAttributes.GENERIC_STEP_HEIGHT, 1.0);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
        EntityAttributeInstance lv = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (target == null) {
            this.ageWhenTargetSet = 0;
            this.dataTracker.set(ANGRY, false);
            this.dataTracker.set(PROVOKED, false);
            lv.removeModifier(ATTACKING_SPEED_MODIFIER_ID);
        } else {
            this.ageWhenTargetSet = this.age;
            this.dataTracker.set(ANGRY, true);
            if (!lv.hasModifier(ATTACKING_SPEED_MODIFIER_ID)) {
                lv.addTemporaryModifier(ATTACKING_SPEED_BOOST);
            }
        }
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(CARRIED_BLOCK, Optional.empty());
        builder.add(ANGRY, false);
        builder.add(PROVOKED, false);
    }

    @Override
    public void chooseRandomAngerTime() {
        this.setAngerTime(ANGER_TIME_RANGE.get(this.random));
    }

    @Override
    public void setAngerTime(int angerTime) {
        this.angerTime = angerTime;
    }

    @Override
    public int getAngerTime() {
        return this.angerTime;
    }

    @Override
    public void setAngryAt(@Nullable UUID angryAt) {
        this.angryAt = angryAt;
    }

    @Override
    @Nullable
    public UUID getAngryAt() {
        return this.angryAt;
    }

    public void playAngrySound() {
        if (this.age >= this.lastAngrySoundAge + 400) {
            this.lastAngrySoundAge = this.age;
            if (!this.isSilent()) {
                this.getWorld().playSound(this.getX(), this.getEyeY(), this.getZ(), SoundEvents.ENTITY_ENDERMAN_STARE, this.getSoundCategory(), 2.5f, 1.0f, false);
            }
        }
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if (ANGRY.equals(data) && this.isProvoked() && this.getWorld().isClient) {
            this.playAngrySound();
        }
        super.onTrackedDataSet(data);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        BlockState lv = this.getCarriedBlock();
        if (lv != null) {
            nbt.put("carriedBlockState", NbtHelper.fromBlockState(lv));
        }
        this.writeAngerToNbt(nbt);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        BlockState lv = null;
        if (nbt.contains("carriedBlockState", NbtElement.COMPOUND_TYPE) && (lv = NbtHelper.toBlockState(this.getWorld().createCommandRegistryWrapper(RegistryKeys.BLOCK), nbt.getCompound("carriedBlockState"))).isAir()) {
            lv = null;
        }
        this.setCarriedBlock(lv);
        this.readAngerFromNbt(this.getWorld(), nbt);
    }

    boolean isPlayerStaring(PlayerEntity player) {
        ItemStack lv = player.getInventory().armor.get(3);
        if (lv.isOf(Blocks.CARVED_PUMPKIN.asItem())) {
            return false;
        }
        Vec3d lv2 = player.getRotationVec(1.0f).normalize();
        Vec3d lv3 = new Vec3d(this.getX() - player.getX(), this.getEyeY() - player.getEyeY(), this.getZ() - player.getZ());
        double d = lv3.length();
        double e = lv2.dotProduct(lv3 = lv3.normalize());
        if (e > 1.0 - 0.025 / d) {
            return player.canSee(this);
        }
        return false;
    }

    @Override
    public void tickMovement() {
        if (this.getWorld().isClient) {
            for (int i = 0; i < 2; ++i) {
                this.getWorld().addParticle(ParticleTypes.PORTAL, this.getParticleX(0.5), this.getRandomBodyY() - 0.25, this.getParticleZ(0.5), (this.random.nextDouble() - 0.5) * 2.0, -this.random.nextDouble(), (this.random.nextDouble() - 0.5) * 2.0);
            }
        }
        this.jumping = false;
        if (!this.getWorld().isClient) {
            this.tickAngerLogic((ServerWorld)this.getWorld(), true);
        }
        super.tickMovement();
    }

    @Override
    public boolean hurtByWater() {
        return true;
    }

    @Override
    protected void mobTick() {
        float f;
        if (this.getWorld().isDay() && this.age >= this.ageWhenTargetSet + 600 && (f = this.getBrightnessAtEyes()) > 0.5f && this.getWorld().isSkyVisible(this.getBlockPos()) && this.random.nextFloat() * 30.0f < (f - 0.4f) * 2.0f) {
            this.setTarget(null);
            this.teleportRandomly();
        }
        super.mobTick();
    }

    protected boolean teleportRandomly() {
        if (this.getWorld().isClient() || !this.isAlive()) {
            return false;
        }
        double d = this.getX() + (this.random.nextDouble() - 0.5) * 64.0;
        double e = this.getY() + (double)(this.random.nextInt(64) - 32);
        double f = this.getZ() + (this.random.nextDouble() - 0.5) * 64.0;
        return this.teleportTo(d, e, f);
    }

    boolean teleportTo(Entity entity) {
        Vec3d lv = new Vec3d(this.getX() - entity.getX(), this.getBodyY(0.5) - entity.getEyeY(), this.getZ() - entity.getZ());
        lv = lv.normalize();
        double d = 16.0;
        double e = this.getX() + (this.random.nextDouble() - 0.5) * 8.0 - lv.x * 16.0;
        double f = this.getY() + (double)(this.random.nextInt(16) - 8) - lv.y * 16.0;
        double g = this.getZ() + (this.random.nextDouble() - 0.5) * 8.0 - lv.z * 16.0;
        return this.teleportTo(e, f, g);
    }

    private boolean teleportTo(double x, double y, double z) {
        BlockPos.Mutable lv = new BlockPos.Mutable(x, y, z);
        while (lv.getY() > this.getWorld().getBottomY() && !this.getWorld().getBlockState(lv).blocksMovement()) {
            lv.move(Direction.DOWN);
        }
        BlockState lv2 = this.getWorld().getBlockState(lv);
        boolean bl = lv2.blocksMovement();
        boolean bl2 = lv2.getFluidState().isIn(FluidTags.WATER);
        if (!bl || bl2) {
            return false;
        }
        Vec3d lv3 = this.getPos();
        boolean bl3 = this.teleport(x, y, z, true);
        if (bl3) {
            this.getWorld().emitGameEvent(GameEvent.TELEPORT, lv3, GameEvent.Emitter.of(this));
            if (!this.isSilent()) {
                this.getWorld().playSound(null, this.prevX, this.prevY, this.prevZ, SoundEvents.ENTITY_ENDERMAN_TELEPORT, this.getSoundCategory(), 1.0f, 1.0f);
                this.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            }
        }
        return bl3;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isAngry() ? SoundEvents.ENTITY_ENDERMAN_SCREAM : SoundEvents.ENTITY_ENDERMAN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_ENDERMAN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_ENDERMAN_DEATH;
    }

    @Override
    protected void dropEquipment(ServerWorld world, DamageSource source, boolean causedByPlayer) {
        super.dropEquipment(world, source, causedByPlayer);
        BlockState lv = this.getCarriedBlock();
        if (lv != null) {
            ItemStack lv2 = new ItemStack(Items.DIAMOND_AXE);
            EnchantmentHelper.applyEnchantmentProvider(lv2, world.getRegistryManager(), EnchantmentProviders.ENDERMAN_LOOT_DROP, world.getLocalDifficulty(this.getBlockPos()), this.getRandom());
            LootContextParameterSet.Builder lv3 = new LootContextParameterSet.Builder((ServerWorld)this.getWorld()).add(LootContextParameters.ORIGIN, this.getPos()).add(LootContextParameters.TOOL, lv2).addOptional(LootContextParameters.THIS_ENTITY, this);
            List<ItemStack> list = lv.getDroppedStacks(lv3);
            for (ItemStack lv4 : list) {
                this.dropStack(lv4);
            }
        }
    }

    public void setCarriedBlock(@Nullable BlockState state) {
        this.dataTracker.set(CARRIED_BLOCK, Optional.ofNullable(state));
    }

    @Nullable
    public BlockState getCarriedBlock() {
        return this.dataTracker.get(CARRIED_BLOCK).orElse(null);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        boolean bl = source.getSource() instanceof PotionEntity;
        if (source.isIn(DamageTypeTags.IS_PROJECTILE) || bl) {
            boolean bl2 = bl && this.damageFromPotion(source, (PotionEntity)source.getSource(), amount);
            for (int i = 0; i < 64; ++i) {
                if (!this.teleportRandomly()) continue;
                return true;
            }
            return bl2;
        }
        boolean bl2 = super.damage(source, amount);
        if (!this.getWorld().isClient() && !(source.getAttacker() instanceof LivingEntity) && this.random.nextInt(10) != 0) {
            this.teleportRandomly();
        }
        return bl2;
    }

    private boolean damageFromPotion(DamageSource source, PotionEntity potion, float amount) {
        ItemStack lv = potion.getStack();
        PotionContentsComponent lv2 = lv.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
        if (lv2.matches(Potions.WATER)) {
            return super.damage(source, amount);
        }
        return false;
    }

    public boolean isAngry() {
        return this.dataTracker.get(ANGRY);
    }

    public boolean isProvoked() {
        return this.dataTracker.get(PROVOKED);
    }

    public void setProvoked() {
        this.dataTracker.set(PROVOKED, true);
    }

    @Override
    public boolean cannotDespawn() {
        return super.cannotDespawn() || this.getCarriedBlock() != null;
    }

    static class ChasePlayerGoal
    extends Goal {
        private final EndermanEntity enderman;
        @Nullable
        private LivingEntity target;

        public ChasePlayerGoal(EndermanEntity enderman) {
            this.enderman = enderman;
            this.setControls(EnumSet.of(Goal.Control.JUMP, Goal.Control.MOVE));
        }

        @Override
        public boolean canStart() {
            this.target = this.enderman.getTarget();
            if (!(this.target instanceof PlayerEntity)) {
                return false;
            }
            double d = this.target.squaredDistanceTo(this.enderman);
            if (d > 256.0) {
                return false;
            }
            return this.enderman.isPlayerStaring((PlayerEntity)this.target);
        }

        @Override
        public void start() {
            this.enderman.getNavigation().stop();
        }

        @Override
        public void tick() {
            this.enderman.getLookControl().lookAt(this.target.getX(), this.target.getEyeY(), this.target.getZ());
        }
    }

    static class PlaceBlockGoal
    extends Goal {
        private final EndermanEntity enderman;

        public PlaceBlockGoal(EndermanEntity enderman) {
            this.enderman = enderman;
        }

        @Override
        public boolean canStart() {
            if (this.enderman.getCarriedBlock() == null) {
                return false;
            }
            if (!this.enderman.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
                return false;
            }
            return this.enderman.getRandom().nextInt(PlaceBlockGoal.toGoalTicks(2000)) == 0;
        }

        @Override
        public void tick() {
            Random lv = this.enderman.getRandom();
            World lv2 = this.enderman.getWorld();
            int i = MathHelper.floor(this.enderman.getX() - 1.0 + lv.nextDouble() * 2.0);
            int j = MathHelper.floor(this.enderman.getY() + lv.nextDouble() * 2.0);
            int k = MathHelper.floor(this.enderman.getZ() - 1.0 + lv.nextDouble() * 2.0);
            BlockPos lv3 = new BlockPos(i, j, k);
            BlockState lv4 = lv2.getBlockState(lv3);
            BlockPos lv5 = lv3.down();
            BlockState lv6 = lv2.getBlockState(lv5);
            BlockState lv7 = this.enderman.getCarriedBlock();
            if (lv7 == null) {
                return;
            }
            if (this.canPlaceOn(lv2, lv3, lv7 = Block.postProcessState(lv7, this.enderman.getWorld(), lv3), lv4, lv6, lv5)) {
                lv2.setBlockState(lv3, lv7, Block.NOTIFY_ALL);
                lv2.emitGameEvent(GameEvent.BLOCK_PLACE, lv3, GameEvent.Emitter.of(this.enderman, lv7));
                this.enderman.setCarriedBlock(null);
            }
        }

        private boolean canPlaceOn(World world, BlockPos posAbove, BlockState carriedState, BlockState stateAbove, BlockState state, BlockPos pos) {
            return stateAbove.isAir() && !state.isAir() && !state.isOf(Blocks.BEDROCK) && state.isFullCube(world, pos) && carriedState.canPlaceAt(world, posAbove) && world.getOtherEntities(this.enderman, Box.from(Vec3d.of(posAbove))).isEmpty();
        }
    }

    static class PickUpBlockGoal
    extends Goal {
        private final EndermanEntity enderman;

        public PickUpBlockGoal(EndermanEntity enderman) {
            this.enderman = enderman;
        }

        @Override
        public boolean canStart() {
            if (this.enderman.getCarriedBlock() != null) {
                return false;
            }
            if (!this.enderman.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
                return false;
            }
            return this.enderman.getRandom().nextInt(PickUpBlockGoal.toGoalTicks(20)) == 0;
        }

        @Override
        public void tick() {
            Random lv = this.enderman.getRandom();
            World lv2 = this.enderman.getWorld();
            int i = MathHelper.floor(this.enderman.getX() - 2.0 + lv.nextDouble() * 4.0);
            int j = MathHelper.floor(this.enderman.getY() + lv.nextDouble() * 3.0);
            int k = MathHelper.floor(this.enderman.getZ() - 2.0 + lv.nextDouble() * 4.0);
            BlockPos lv3 = new BlockPos(i, j, k);
            BlockState lv4 = lv2.getBlockState(lv3);
            Vec3d lv5 = new Vec3d((double)this.enderman.getBlockX() + 0.5, (double)j + 0.5, (double)this.enderman.getBlockZ() + 0.5);
            Vec3d lv6 = new Vec3d((double)i + 0.5, (double)j + 0.5, (double)k + 0.5);
            BlockHitResult lv7 = lv2.raycast(new RaycastContext(lv5, lv6, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, this.enderman));
            boolean bl = lv7.getBlockPos().equals(lv3);
            if (lv4.isIn(BlockTags.ENDERMAN_HOLDABLE) && bl) {
                lv2.removeBlock(lv3, false);
                lv2.emitGameEvent(GameEvent.BLOCK_DESTROY, lv3, GameEvent.Emitter.of(this.enderman, lv4));
                this.enderman.setCarriedBlock(lv4.getBlock().getDefaultState());
            }
        }
    }

    static class TeleportTowardsPlayerGoal
    extends ActiveTargetGoal<PlayerEntity> {
        private final EndermanEntity enderman;
        @Nullable
        private PlayerEntity targetPlayer;
        private int lookAtPlayerWarmup;
        private int ticksSinceUnseenTeleport;
        private final TargetPredicate staringPlayerPredicate;
        private final TargetPredicate validTargetPredicate = TargetPredicate.createAttackable().ignoreVisibility();
        private final Predicate<LivingEntity> angerPredicate;

        public TeleportTowardsPlayerGoal(EndermanEntity enderman, @Nullable Predicate<LivingEntity> targetPredicate) {
            super(enderman, PlayerEntity.class, 10, false, false, targetPredicate);
            this.enderman = enderman;
            this.angerPredicate = playerEntity -> (enderman.isPlayerStaring((PlayerEntity)playerEntity) || enderman.shouldAngerAt((LivingEntity)playerEntity)) && !enderman.hasPassengerDeep((Entity)playerEntity);
            this.staringPlayerPredicate = TargetPredicate.createAttackable().setBaseMaxDistance(this.getFollowRange()).setPredicate(this.angerPredicate);
        }

        @Override
        public boolean canStart() {
            this.targetPlayer = this.enderman.getWorld().getClosestPlayer(this.staringPlayerPredicate, this.enderman);
            return this.targetPlayer != null;
        }

        @Override
        public void start() {
            this.lookAtPlayerWarmup = this.getTickCount(5);
            this.ticksSinceUnseenTeleport = 0;
            this.enderman.setProvoked();
        }

        @Override
        public void stop() {
            this.targetPlayer = null;
            super.stop();
        }

        @Override
        public boolean shouldContinue() {
            if (this.targetPlayer != null) {
                if (!this.angerPredicate.test(this.targetPlayer)) {
                    return false;
                }
                this.enderman.lookAtEntity(this.targetPlayer, 10.0f, 10.0f);
                return true;
            }
            if (this.targetEntity != null) {
                if (this.enderman.hasPassengerDeep(this.targetEntity)) {
                    return false;
                }
                if (this.validTargetPredicate.test(this.enderman, this.targetEntity)) {
                    return true;
                }
            }
            return super.shouldContinue();
        }

        @Override
        public void tick() {
            if (this.enderman.getTarget() == null) {
                super.setTargetEntity(null);
            }
            if (this.targetPlayer != null) {
                if (--this.lookAtPlayerWarmup <= 0) {
                    this.targetEntity = this.targetPlayer;
                    this.targetPlayer = null;
                    super.start();
                }
            } else {
                if (this.targetEntity != null && !this.enderman.hasVehicle()) {
                    if (this.enderman.isPlayerStaring((PlayerEntity)this.targetEntity)) {
                        if (this.targetEntity.squaredDistanceTo(this.enderman) < 16.0) {
                            this.enderman.teleportRandomly();
                        }
                        this.ticksSinceUnseenTeleport = 0;
                    } else if (this.targetEntity.squaredDistanceTo(this.enderman) > 256.0 && this.ticksSinceUnseenTeleport++ >= this.getTickCount(30) && this.enderman.teleportTo(this.targetEntity)) {
                        this.ticksSinceUnseenTeleport = 0;
                    }
                }
                super.tick();
            }
        }
    }
}

