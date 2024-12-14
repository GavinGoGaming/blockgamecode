/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.raid;

import com.google.common.collect.Lists;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.MoveToRaidCenterGoal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.IllagerEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PatrolEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.raid.Raid;
import net.minecraft.village.raid.RaidManager;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestTypes;
import org.jetbrains.annotations.Nullable;

public abstract class RaiderEntity
extends PatrolEntity {
    protected static final TrackedData<Boolean> CELEBRATING = DataTracker.registerData(RaiderEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    static final Predicate<ItemEntity> OBTAINABLE_OMINOUS_BANNER_PREDICATE = itemEntity -> !itemEntity.cannotPickup() && itemEntity.isAlive() && ItemStack.areEqual(itemEntity.getStack(), Raid.getOminousBanner(itemEntity.getRegistryManager().getWrapperOrThrow(RegistryKeys.BANNER_PATTERN)));
    @Nullable
    protected Raid raid;
    private int wave;
    private boolean ableToJoinRaid;
    private int outOfRaidCounter;

    protected RaiderEntity(EntityType<? extends RaiderEntity> arg, World arg2) {
        super((EntityType<? extends PatrolEntity>)arg, arg2);
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(1, new PickupBannerAsLeaderGoal(this, this));
        this.goalSelector.add(3, new MoveToRaidCenterGoal<RaiderEntity>(this));
        this.goalSelector.add(4, new AttackHomeGoal(this, 1.05f, 1));
        this.goalSelector.add(5, new CelebrateGoal(this));
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(CELEBRATING, false);
    }

    public abstract void addBonusForWave(ServerWorld var1, int var2, boolean var3);

    public boolean canJoinRaid() {
        return this.ableToJoinRaid;
    }

    public void setAbleToJoinRaid(boolean ableToJoinRaid) {
        this.ableToJoinRaid = ableToJoinRaid;
    }

    @Override
    public void tickMovement() {
        if (this.getWorld() instanceof ServerWorld && this.isAlive()) {
            Raid lv = this.getRaid();
            if (this.canJoinRaid()) {
                if (lv == null) {
                    Raid lv2;
                    if (this.getWorld().getTime() % 20L == 0L && (lv2 = ((ServerWorld)this.getWorld()).getRaidAt(this.getBlockPos())) != null && RaidManager.isValidRaiderFor(this, lv2)) {
                        lv2.addRaider(lv2.getGroupsSpawned(), this, null, true);
                    }
                } else {
                    LivingEntity lv3 = this.getTarget();
                    if (lv3 != null && (lv3.getType() == EntityType.PLAYER || lv3.getType() == EntityType.IRON_GOLEM)) {
                        this.despawnCounter = 0;
                    }
                }
            }
        }
        super.tickMovement();
    }

    @Override
    protected void updateDespawnCounter() {
        this.despawnCounter += 2;
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        if (this.getWorld() instanceof ServerWorld) {
            Entity lv = damageSource.getAttacker();
            Raid lv2 = this.getRaid();
            if (lv2 != null) {
                if (this.isPatrolLeader()) {
                    lv2.removeLeader(this.getWave());
                }
                if (lv != null && lv.getType() == EntityType.PLAYER) {
                    lv2.addHero(lv);
                }
                lv2.removeFromWave(this, false);
            }
        }
        super.onDeath(damageSource);
    }

    @Override
    public boolean hasNoRaid() {
        return !this.hasActiveRaid();
    }

    public void setRaid(@Nullable Raid raid) {
        this.raid = raid;
    }

    @Nullable
    public Raid getRaid() {
        return this.raid;
    }

    public boolean isCaptain() {
        ItemStack lv = this.getEquippedStack(EquipmentSlot.HEAD);
        boolean bl = !lv.isEmpty() && ItemStack.areEqual(lv, Raid.getOminousBanner(this.getRegistryManager().getWrapperOrThrow(RegistryKeys.BANNER_PATTERN)));
        boolean bl2 = this.isPatrolLeader();
        return bl && bl2;
    }

    public boolean hasRaid() {
        World world = this.getWorld();
        if (!(world instanceof ServerWorld)) {
            return false;
        }
        ServerWorld lv = (ServerWorld)world;
        return this.getRaid() != null || lv.getRaidAt(this.getBlockPos()) != null;
    }

    public boolean hasActiveRaid() {
        return this.getRaid() != null && this.getRaid().isActive();
    }

    public void setWave(int wave) {
        this.wave = wave;
    }

    public int getWave() {
        return this.wave;
    }

    public boolean isCelebrating() {
        return this.dataTracker.get(CELEBRATING);
    }

    public void setCelebrating(boolean celebrating) {
        this.dataTracker.set(CELEBRATING, celebrating);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("Wave", this.wave);
        nbt.putBoolean("CanJoinRaid", this.ableToJoinRaid);
        if (this.raid != null) {
            nbt.putInt("RaidId", this.raid.getRaidId());
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.wave = nbt.getInt("Wave");
        this.ableToJoinRaid = nbt.getBoolean("CanJoinRaid");
        if (nbt.contains("RaidId", NbtElement.INT_TYPE)) {
            if (this.getWorld() instanceof ServerWorld) {
                this.raid = ((ServerWorld)this.getWorld()).getRaidManager().getRaid(nbt.getInt("RaidId"));
            }
            if (this.raid != null) {
                this.raid.addToWave(this.wave, this, false);
                if (this.isPatrolLeader()) {
                    this.raid.setWaveCaptain(this.wave, this);
                }
            }
        }
    }

    @Override
    protected void loot(ItemEntity item) {
        boolean bl;
        ItemStack lv = item.getStack();
        boolean bl2 = bl = this.hasActiveRaid() && this.getRaid().getCaptain(this.getWave()) != null;
        if (this.hasActiveRaid() && !bl && ItemStack.areEqual(lv, Raid.getOminousBanner(this.getRegistryManager().getWrapperOrThrow(RegistryKeys.BANNER_PATTERN)))) {
            EquipmentSlot lv2 = EquipmentSlot.HEAD;
            ItemStack lv3 = this.getEquippedStack(lv2);
            double d = this.getDropChance(lv2);
            if (!lv3.isEmpty() && (double)Math.max(this.random.nextFloat() - 0.1f, 0.0f) < d) {
                this.dropStack(lv3);
            }
            this.triggerItemPickedUpByEntityCriteria(item);
            this.equipStack(lv2, lv);
            this.sendPickup(item, lv.getCount());
            item.discard();
            this.getRaid().setWaveCaptain(this.getWave(), this);
            this.setPatrolLeader(true);
        } else {
            super.loot(item);
        }
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        if (this.getRaid() == null) {
            return super.canImmediatelyDespawn(distanceSquared);
        }
        return false;
    }

    @Override
    public boolean cannotDespawn() {
        return super.cannotDespawn() || this.getRaid() != null;
    }

    public int getOutOfRaidCounter() {
        return this.outOfRaidCounter;
    }

    public void setOutOfRaidCounter(int outOfRaidCounter) {
        this.outOfRaidCounter = outOfRaidCounter;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.hasActiveRaid()) {
            this.getRaid().updateBar();
        }
        return super.damage(source, amount);
    }

    @Override
    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        this.setAbleToJoinRaid(this.getType() != EntityType.WITCH || spawnReason != SpawnReason.NATURAL);
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    public abstract SoundEvent getCelebratingSound();

    public class PickupBannerAsLeaderGoal<T extends RaiderEntity>
    extends Goal {
        private final T actor;

        /*
         * WARNING - Possible parameter corruption
         */
        public PickupBannerAsLeaderGoal(T actor) {
            this.actor = actor;
            this.setControls(EnumSet.of(Goal.Control.MOVE));
        }

        @Override
        public boolean canStart() {
            List<ItemEntity> list;
            Raid lv = ((RaiderEntity)this.actor).getRaid();
            if (!((RaiderEntity)this.actor).hasActiveRaid() || ((RaiderEntity)this.actor).getRaid().isFinished() || !((PatrolEntity)this.actor).canLead() || ItemStack.areEqual(((MobEntity)this.actor).getEquippedStack(EquipmentSlot.HEAD), Raid.getOminousBanner(((Entity)this.actor).getRegistryManager().getWrapperOrThrow(RegistryKeys.BANNER_PATTERN)))) {
                return false;
            }
            RaiderEntity lv2 = lv.getCaptain(((RaiderEntity)this.actor).getWave());
            if (!(lv2 != null && lv2.isAlive() || (list = ((Entity)this.actor).getWorld().getEntitiesByClass(ItemEntity.class, ((Entity)this.actor).getBoundingBox().expand(16.0, 8.0, 16.0), OBTAINABLE_OMINOUS_BANNER_PREDICATE)).isEmpty())) {
                return ((MobEntity)this.actor).getNavigation().startMovingTo(list.get(0), 1.15f);
            }
            return false;
        }

        @Override
        public void tick() {
            List<ItemEntity> list;
            if (((MobEntity)this.actor).getNavigation().getTargetPos().isWithinDistance(((Entity)this.actor).getPos(), 1.414) && !(list = ((Entity)this.actor).getWorld().getEntitiesByClass(ItemEntity.class, ((Entity)this.actor).getBoundingBox().expand(4.0, 4.0, 4.0), OBTAINABLE_OMINOUS_BANNER_PREDICATE)).isEmpty()) {
                ((RaiderEntity)this.actor).loot(list.get(0));
            }
        }
    }

    static class AttackHomeGoal
    extends Goal {
        private final RaiderEntity raider;
        private final double speed;
        private BlockPos home;
        private final List<BlockPos> lastHomes = Lists.newArrayList();
        private final int distance;
        private boolean finished;

        public AttackHomeGoal(RaiderEntity raider, double speed, int distance) {
            this.raider = raider;
            this.speed = speed;
            this.distance = distance;
            this.setControls(EnumSet.of(Goal.Control.MOVE));
        }

        @Override
        public boolean canStart() {
            this.purgeMemory();
            return this.isRaiding() && this.tryFindHome() && this.raider.getTarget() == null;
        }

        private boolean isRaiding() {
            return this.raider.hasActiveRaid() && !this.raider.getRaid().isFinished();
        }

        private boolean tryFindHome() {
            ServerWorld lv = (ServerWorld)this.raider.getWorld();
            BlockPos lv2 = this.raider.getBlockPos();
            Optional<BlockPos> optional = lv.getPointOfInterestStorage().getPosition(arg -> arg.matchesKey(PointOfInterestTypes.HOME), this::canLootHome, PointOfInterestStorage.OccupationStatus.ANY, lv2, 48, this.raider.random);
            if (optional.isEmpty()) {
                return false;
            }
            this.home = optional.get().toImmutable();
            return true;
        }

        @Override
        public boolean shouldContinue() {
            if (this.raider.getNavigation().isIdle()) {
                return false;
            }
            return this.raider.getTarget() == null && !this.home.isWithinDistance(this.raider.getPos(), (double)(this.raider.getWidth() + (float)this.distance)) && !this.finished;
        }

        @Override
        public void stop() {
            if (this.home.isWithinDistance(this.raider.getPos(), (double)this.distance)) {
                this.lastHomes.add(this.home);
            }
        }

        @Override
        public void start() {
            super.start();
            this.raider.setDespawnCounter(0);
            this.raider.getNavigation().startMovingTo(this.home.getX(), this.home.getY(), this.home.getZ(), this.speed);
            this.finished = false;
        }

        @Override
        public void tick() {
            if (this.raider.getNavigation().isIdle()) {
                Vec3d lv = Vec3d.ofBottomCenter(this.home);
                Vec3d lv2 = NoPenaltyTargeting.findTo(this.raider, 16, 7, lv, 0.3141592741012573);
                if (lv2 == null) {
                    lv2 = NoPenaltyTargeting.findTo(this.raider, 8, 7, lv, 1.5707963705062866);
                }
                if (lv2 == null) {
                    this.finished = true;
                    return;
                }
                this.raider.getNavigation().startMovingTo(lv2.x, lv2.y, lv2.z, this.speed);
            }
        }

        private boolean canLootHome(BlockPos pos) {
            for (BlockPos lv : this.lastHomes) {
                if (!Objects.equals(pos, lv)) continue;
                return false;
            }
            return true;
        }

        private void purgeMemory() {
            if (this.lastHomes.size() > 2) {
                this.lastHomes.remove(0);
            }
        }
    }

    public class CelebrateGoal
    extends Goal {
        private final RaiderEntity raider;

        CelebrateGoal(RaiderEntity raider) {
            this.raider = raider;
            this.setControls(EnumSet.of(Goal.Control.MOVE));
        }

        @Override
        public boolean canStart() {
            Raid lv = this.raider.getRaid();
            return this.raider.isAlive() && this.raider.getTarget() == null && lv != null && lv.hasLost();
        }

        @Override
        public void start() {
            this.raider.setCelebrating(true);
            super.start();
        }

        @Override
        public void stop() {
            this.raider.setCelebrating(false);
            super.stop();
        }

        @Override
        public void tick() {
            if (!this.raider.isSilent() && this.raider.random.nextInt(this.getTickCount(100)) == 0) {
                RaiderEntity.this.playSound(RaiderEntity.this.getCelebratingSound());
            }
            if (!this.raider.hasVehicle() && this.raider.random.nextInt(this.getTickCount(50)) == 0) {
                this.raider.getJumpControl().setActive();
            }
            super.tick();
        }
    }

    protected class PatrolApproachGoal
    extends Goal {
        private final RaiderEntity raider;
        private final float squaredDistance;
        public final TargetPredicate closeRaiderPredicate = TargetPredicate.createNonAttackable().setBaseMaxDistance(8.0).ignoreVisibility().ignoreDistanceScalingFactor();

        public PatrolApproachGoal(RaiderEntity raider, IllagerEntity illager, float distance) {
            this.raider = illager;
            this.squaredDistance = distance * distance;
            this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
        }

        @Override
        public boolean canStart() {
            LivingEntity lv = this.raider.getAttacker();
            return this.raider.getRaid() == null && this.raider.isRaidCenterSet() && this.raider.getTarget() != null && !this.raider.isAttacking() && (lv == null || lv.getType() != EntityType.PLAYER);
        }

        @Override
        public void start() {
            super.start();
            this.raider.getNavigation().stop();
            List<RaiderEntity> list = this.raider.getWorld().getTargets(RaiderEntity.class, this.closeRaiderPredicate, this.raider, this.raider.getBoundingBox().expand(8.0, 8.0, 8.0));
            for (RaiderEntity lv : list) {
                lv.setTarget(this.raider.getTarget());
            }
        }

        @Override
        public void stop() {
            super.stop();
            LivingEntity lv = this.raider.getTarget();
            if (lv != null) {
                List<RaiderEntity> list = this.raider.getWorld().getTargets(RaiderEntity.class, this.closeRaiderPredicate, this.raider, this.raider.getBoundingBox().expand(8.0, 8.0, 8.0));
                for (RaiderEntity lv2 : list) {
                    lv2.setTarget(lv);
                    lv2.setAttacking(true);
                }
                this.raider.setAttacking(true);
            }
        }

        @Override
        public boolean shouldRunEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity lv = this.raider.getTarget();
            if (lv == null) {
                return;
            }
            if (this.raider.squaredDistanceTo(lv) > (double)this.squaredDistance) {
                this.raider.getLookControl().lookAt(lv, 30.0f, 30.0f);
                if (this.raider.random.nextInt(50) == 0) {
                    this.raider.playAmbientSound();
                }
            } else {
                this.raider.setAttacking(true);
            }
            super.tick();
        }
    }
}

