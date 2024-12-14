/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.passive;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.ServerConfigHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EntityView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class TameableEntity
extends AnimalEntity
implements Tameable {
    public static final int field_52002 = 144;
    private static final int field_52003 = 2;
    private static final int field_52004 = 3;
    private static final int field_52005 = 1;
    protected static final TrackedData<Byte> TAMEABLE_FLAGS = DataTracker.registerData(TameableEntity.class, TrackedDataHandlerRegistry.BYTE);
    protected static final TrackedData<Optional<UUID>> OWNER_UUID = DataTracker.registerData(TameableEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
    private boolean sitting;

    protected TameableEntity(EntityType<? extends TameableEntity> arg, World arg2) {
        super((EntityType<? extends AnimalEntity>)arg, arg2);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(TAMEABLE_FLAGS, (byte)0);
        builder.add(OWNER_UUID, Optional.empty());
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (this.getOwnerUuid() != null) {
            nbt.putUuid("Owner", this.getOwnerUuid());
        }
        nbt.putBoolean("Sitting", this.sitting);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        UUID uUID;
        super.readCustomDataFromNbt(nbt);
        if (nbt.containsUuid("Owner")) {
            uUID = nbt.getUuid("Owner");
        } else {
            String string = nbt.getString("Owner");
            uUID = ServerConfigHandler.getPlayerUuidByName(this.getServer(), string);
        }
        if (uUID != null) {
            try {
                this.setOwnerUuid(uUID);
                this.setTamed(true, false);
            } catch (Throwable throwable) {
                this.setTamed(false, true);
            }
        }
        this.sitting = nbt.getBoolean("Sitting");
        this.setInSittingPose(this.sitting);
    }

    @Override
    public boolean canBeLeashed() {
        return true;
    }

    @Override
    public boolean beforeLeashTick(Entity leashHolder, float distance) {
        if (this.isInSittingPose()) {
            if (distance > 10.0f) {
                this.detachLeash(true, true);
            }
            return false;
        }
        return super.beforeLeashTick(leashHolder, distance);
    }

    protected void showEmoteParticle(boolean positive) {
        SimpleParticleType lv = ParticleTypes.HEART;
        if (!positive) {
            lv = ParticleTypes.SMOKE;
        }
        for (int i = 0; i < 7; ++i) {
            double d = this.random.nextGaussian() * 0.02;
            double e = this.random.nextGaussian() * 0.02;
            double f = this.random.nextGaussian() * 0.02;
            ((World)this.getWorld()).addParticle(lv, this.getParticleX(1.0), this.getRandomBodyY() + 0.5, this.getParticleZ(1.0), d, e, f);
        }
    }

    @Override
    public void handleStatus(byte status) {
        if (status == EntityStatuses.ADD_POSITIVE_PLAYER_REACTION_PARTICLES) {
            this.showEmoteParticle(true);
        } else if (status == EntityStatuses.ADD_NEGATIVE_PLAYER_REACTION_PARTICLES) {
            this.showEmoteParticle(false);
        } else {
            super.handleStatus(status);
        }
    }

    public boolean isTamed() {
        return (this.dataTracker.get(TAMEABLE_FLAGS) & 4) != 0;
    }

    public void setTamed(boolean tamed, boolean updateAttributes) {
        byte b = this.dataTracker.get(TAMEABLE_FLAGS);
        if (tamed) {
            this.dataTracker.set(TAMEABLE_FLAGS, (byte)(b | 4));
        } else {
            this.dataTracker.set(TAMEABLE_FLAGS, (byte)(b & 0xFFFFFFFB));
        }
        if (updateAttributes) {
            this.updateAttributesForTamed();
        }
    }

    protected void updateAttributesForTamed() {
    }

    public boolean isInSittingPose() {
        return (this.dataTracker.get(TAMEABLE_FLAGS) & 1) != 0;
    }

    public void setInSittingPose(boolean inSittingPose) {
        byte b = this.dataTracker.get(TAMEABLE_FLAGS);
        if (inSittingPose) {
            this.dataTracker.set(TAMEABLE_FLAGS, (byte)(b | 1));
        } else {
            this.dataTracker.set(TAMEABLE_FLAGS, (byte)(b & 0xFFFFFFFE));
        }
    }

    @Override
    @Nullable
    public UUID getOwnerUuid() {
        return this.dataTracker.get(OWNER_UUID).orElse(null);
    }

    public void setOwnerUuid(@Nullable UUID uuid) {
        this.dataTracker.set(OWNER_UUID, Optional.ofNullable(uuid));
    }

    public void setOwner(PlayerEntity player) {
        this.setTamed(true, true);
        this.setOwnerUuid(player.getUuid());
        if (player instanceof ServerPlayerEntity) {
            ServerPlayerEntity lv = (ServerPlayerEntity)player;
            Criteria.TAME_ANIMAL.trigger(lv, this);
        }
    }

    @Override
    public boolean canTarget(LivingEntity target) {
        if (this.isOwner(target)) {
            return false;
        }
        return super.canTarget(target);
    }

    public boolean isOwner(LivingEntity entity) {
        return entity == this.getOwner();
    }

    public boolean canAttackWithOwner(LivingEntity target, LivingEntity owner) {
        return true;
    }

    @Override
    public Team getScoreboardTeam() {
        LivingEntity lv;
        if (this.isTamed() && (lv = this.getOwner()) != null) {
            return lv.getScoreboardTeam();
        }
        return super.getScoreboardTeam();
    }

    @Override
    public boolean isTeammate(Entity other) {
        if (this.isTamed()) {
            LivingEntity lv = this.getOwner();
            if (other == lv) {
                return true;
            }
            if (lv != null) {
                return lv.isTeammate(other);
            }
        }
        return super.isTeammate(other);
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        if (!((World)this.getWorld()).isClient && ((World)this.getWorld()).getGameRules().getBoolean(GameRules.SHOW_DEATH_MESSAGES) && this.getOwner() instanceof ServerPlayerEntity) {
            this.getOwner().sendMessage(this.getDamageTracker().getDeathMessage());
        }
        super.onDeath(damageSource);
    }

    public boolean isSitting() {
        return this.sitting;
    }

    public void setSitting(boolean sitting) {
        this.sitting = sitting;
    }

    public void tryTeleportToOwner() {
        LivingEntity lv = this.getOwner();
        if (lv != null) {
            this.tryTeleportNear(lv.getBlockPos());
        }
    }

    public boolean shouldTryTeleportToOwner() {
        LivingEntity lv = this.getOwner();
        return lv != null && this.squaredDistanceTo(this.getOwner()) >= 144.0;
    }

    private void tryTeleportNear(BlockPos pos) {
        for (int i = 0; i < 10; ++i) {
            int j = this.random.nextBetween(-3, 3);
            int k = this.random.nextBetween(-3, 3);
            if (Math.abs(j) < 2 && Math.abs(k) < 2) continue;
            int l = this.random.nextBetween(-1, 1);
            if (!this.tryTeleportTo(pos.getX() + j, pos.getY() + l, pos.getZ() + k)) continue;
            return;
        }
    }

    private boolean tryTeleportTo(int x, int y, int z) {
        if (!this.canTeleportTo(new BlockPos(x, y, z))) {
            return false;
        }
        this.refreshPositionAndAngles((double)x + 0.5, y, (double)z + 0.5, this.getYaw(), this.getPitch());
        this.navigation.stop();
        return true;
    }

    private boolean canTeleportTo(BlockPos pos) {
        PathNodeType lv = LandPathNodeMaker.getLandNodeType(this, pos);
        if (lv != PathNodeType.WALKABLE) {
            return false;
        }
        BlockState lv2 = ((World)this.getWorld()).getBlockState(pos.down());
        if (!this.canTeleportOntoLeaves() && lv2.getBlock() instanceof LeavesBlock) {
            return false;
        }
        BlockPos lv3 = pos.subtract(this.getBlockPos());
        return this.getWorld().isSpaceEmpty(this, this.getBoundingBox().offset(lv3));
    }

    public final boolean cannotFollowOwner() {
        return this.isSitting() || this.hasVehicle() || this.mightBeLeashed() || this.getOwner() != null && this.getOwner().isSpectator();
    }

    protected boolean canTeleportOntoLeaves() {
        return false;
    }

    @Override
    public /* synthetic */ EntityView getWorld() {
        return super.getWorld();
    }

    public class TameableEscapeDangerGoal
    extends EscapeDangerGoal {
        public TameableEscapeDangerGoal(double speed, TagKey<DamageType> dangerousDamageTypes) {
            super((PathAwareEntity)TameableEntity.this, speed, dangerousDamageTypes);
        }

        public TameableEscapeDangerGoal(double speed) {
            super(TameableEntity.this, speed);
        }

        @Override
        public void tick() {
            if (!TameableEntity.this.cannotFollowOwner() && TameableEntity.this.shouldTryTeleportToOwner()) {
                TameableEntity.this.tryTeleportToOwner();
            }
            super.tick();
        }
    }
}

